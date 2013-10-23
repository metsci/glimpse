package com.metsci.glimpse.swt.canvas;

import static com.metsci.glimpse.util.logging.LoggerUtils.logInfo;
import static com.metsci.glimpse.util.logging.LoggerUtils.logWarning;

import java.awt.Dimension;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLContext;
import javax.media.opengl.GLDrawableFactory;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLOffscreenAutoDrawable;
import javax.media.opengl.GLProfile;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.newt.swt.NewtCanvasSWT;
import com.metsci.glimpse.canvas.LayoutManager;
import com.metsci.glimpse.canvas.NewtGlimpseCanvas;
import com.metsci.glimpse.canvas.NewtSwingGlimpseCanvas;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.context.GlimpseContextImpl;
import com.metsci.glimpse.context.GlimpseTarget;
import com.metsci.glimpse.context.GlimpseTargetStack;
import com.metsci.glimpse.event.mouse.newt.MouseWrapperNewt;
import com.metsci.glimpse.gl.GLRunnable;
import com.metsci.glimpse.layout.GlimpseLayout;
import com.metsci.glimpse.support.settings.LookAndFeel;

public class NewtSwtGlimpseCanvas extends Composite implements NewtGlimpseCanvas
{
    private static final Logger logger = Logger.getLogger( NewtSwtGlimpseCanvas.class.getName( ) );

    protected GLProfile glProfile;
    protected GLCapabilities glCapabilities;
    protected GLWindow glWindow;
    protected NewtCanvasSWT glCanvas;
    protected GLOffscreenAutoDrawable glDrawable;
    protected GLContext glContext;

    protected boolean isEventConsumer = true;
    protected boolean isEventGenerator = true;
    protected boolean isDisposed = false;

    protected LayoutManager layoutManager;
    protected MouseWrapperNewt mouseHelper;

    protected List<GLRunnable> disposeListeners;

    protected Dimension dimension = new Dimension( 0, 0 );

    public NewtSwtGlimpseCanvas( Composite parent, String profile, GLContext context, int options )
    {
        super( parent, options );

        this.glContext = context;
        this.glProfile = GLProfile.get( profile );
        this.glCapabilities = new GLCapabilities( glProfile );
        this.glDrawable = GLDrawableFactory.getFactory( glProfile ).createOffscreenAutoDrawable( null, glCapabilities, null, 1, 1, context );
        this.glWindow = GLWindow.create( glCapabilities );
        this.glWindow.setSharedContext( glDrawable.getContext( ) );

        FillLayout layout = new FillLayout( );
        this.setLayout( layout );

        this.glCanvas = new NewtCanvasSWT( this, SWT.NONE, glWindow );

        this.glWindow.addGLEventListener( createGLEventListener( ) );

        this.layoutManager = new LayoutManager( );

        this.mouseHelper = new MouseWrapperNewt( this );
        this.glWindow.addMouseListener( this.mouseHelper );

        this.isDisposed = false;

        this.disposeListeners = new CopyOnWriteArrayList<GLRunnable>( );
    }

    public void addDisposeListener( final Shell shell, final GLAutoDrawable sharedContextSource )
    {
        // Removing the canvas from the frame may prevent X11 errors (see http://tinyurl.com/m4rnuvf)
        // This listener must be added before adding the SwingGlimpseCanvas to the frame because
        // NEWTGLCanvas adds its own WindowListener and this WindowListener must receive the WindowEvent first.
        shell.addDisposeListener( new DisposeListener( )
        {
            @Override
            public void widgetDisposed( DisposeEvent e )
            {
                // dispose of resources associated with the canvas
                dispose( );

                // destroy the source of the shared glContext
                sharedContextSource.destroy( );
            }
        } );
    }

    private GLEventListener createGLEventListener( )
    {
        return new GLEventListener( )
        {
            @Override
            public void init( GLAutoDrawable drawable )
            {
                try
                {
                    GL gl = drawable.getGL( );
                    gl.setSwapInterval( 0 );
                }
                catch ( Exception e )
                {
                    // without this, repaint rate is tied to screen refresh rate on some systems
                    // this doesn't work on some machines (Mac OSX in particular)
                    // but it's not a big deal if it fails
                    logWarning( logger, "Trouble in init.", e );
                }
            }

            @Override
            public void display( GLAutoDrawable drawable )
            {
                for ( GlimpseLayout layout : layoutManager.getLayoutList( ) )
                {
                    layout.paintTo( getGlimpseContext( ) );
                }
            }

            @Override
            public void reshape( GLAutoDrawable drawable, int x, int y, int width, int height )
            {
                dimension = new Dimension( width, height );

                for ( GlimpseLayout layout : layoutManager.getLayoutList( ) )
                {
                    layout.layoutTo( getGlimpseContext( ) );
                }
            }

            @Override
            public void dispose( GLAutoDrawable drawable )
            {
                logInfo( logger, "Disposing SwingGlimpseCanvas..." );

                for ( GlimpseLayout layout : layoutManager.getLayoutList( ) )
                {
                    layout.dispose( getGlimpseContext( ) );
                }

                for ( GLRunnable runnable : disposeListeners )
                {
                    runnable.run( drawable.getContext( ) );
                }
            }
        };
    }
    
    public NewtCanvasSWT getCanvas( )
    {
    	return glCanvas;
    }

    @Override
    public GLAutoDrawable getGLDrawable( )
    {
        return glDrawable;
    }

    @Override
    public GLWindow getGLWindow( )
    {
        return glWindow;
    }

    @Override
    public GlimpseContext getGlimpseContext( )
    {
        return new GlimpseContextImpl( this );
    }

    @Override
    public void setLookAndFeel( LookAndFeel laf )
    {
        for ( GlimpseTarget target : this.layoutManager.getLayoutList( ) )
        {
            target.setLookAndFeel( laf );
        }
    }

    @Override
    public void addLayout( GlimpseLayout layout )
    {
        this.layoutManager.addLayout( layout );
    }

    @Override
    public void addLayout( GlimpseLayout layout, int zOrder )
    {
        this.layoutManager.addLayout( layout, zOrder );
    }

    @Override
    public void setZOrder( GlimpseLayout layout, int zOrder )
    {
        this.layoutManager.setZOrder( layout, zOrder );
    }

    @Override
    public void removeLayout( GlimpseLayout layout )
    {
        this.layoutManager.removeLayout( layout );
    }

    @Override
    public void removeAllLayouts( )
    {
        this.layoutManager.removeAllLayouts( );
    }

    @SuppressWarnings( { "unchecked", "rawtypes" } )
    @Override
    public List<GlimpseTarget> getTargetChildren( )
    {
        // layoutManager returns an unmodifiable list, thus this cast is typesafe
        // (there is no way for the recipient of the List<GlimpseTarget> view to
        // add GlimpseTargets which are not GlimpseLayouts to the list)
        return ( List ) this.layoutManager.getLayoutList( );
    }

    public Dimension getDimension( )
    {
        return dimension;
    }

    @Override
    public GlimpseBounds getTargetBounds( GlimpseTargetStack stack )
    {
        return new GlimpseBounds( getDimension( ) );
    }

    @Override
    public GlimpseBounds getTargetBounds( )
    {
        return getTargetBounds( null );
    }

    @Override
    public void paint( )
    {
        this.glWindow.display( );
    }

    @Override
    public GLContext getGLContext( )
    {
        return this.glWindow.getContext( );
    }

    @Override
    public String toString( )
    {
        return NewtSwingGlimpseCanvas.class.getSimpleName( );
    }

    @Override
    public boolean isEventConsumer( )
    {
        return this.isEventConsumer;
    }

    @Override
    public void setEventConsumer( boolean consume )
    {
        this.isEventConsumer = consume;
    }

    @Override
    public boolean isEventGenerator( )
    {
        return this.isEventGenerator;
    }

    @Override
    public void setEventGenerator( boolean generate )
    {
        this.isEventGenerator = generate;
    }

    @Override
    public boolean isDisposed( )
    {
        return this.isDisposed;
    }

    @Override
    public void dispose( )
    {
        if ( this.glWindow != null ) this.glWindow.destroy( );
        if ( this.glContext != null ) this.glContext.destroy( );
        this.isDisposed = true;
    }

    @Override
    public void addDisposeListener( GLRunnable runnable )
    {
        this.disposeListeners.add( runnable );
    }
}