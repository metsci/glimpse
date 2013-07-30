package com.metsci.glimpse.canvas;

import static com.metsci.glimpse.util.logging.LoggerUtils.*;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.List;
import java.util.logging.Logger;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLContext;
import javax.media.opengl.GLDrawableFactory;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLOffscreenAutoDrawable;
import javax.media.opengl.GLProfile;
import javax.swing.JPanel;

import com.jogamp.newt.awt.NewtCanvasAWT;
import com.jogamp.newt.event.MouseAdapter;
import com.jogamp.newt.event.awt.AWTMouseAdapter;
import com.jogamp.newt.opengl.GLWindow;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.context.GlimpseContextImpl;
import com.metsci.glimpse.context.GlimpseTarget;
import com.metsci.glimpse.context.GlimpseTargetStack;
import com.metsci.glimpse.event.mouse.swing.MouseWrapperSwing;
import com.metsci.glimpse.layout.GlimpseLayout;
import com.metsci.glimpse.support.repaint.RepaintManager;
import com.metsci.glimpse.support.settings.LookAndFeel;

// Jogamp / NEWT Links:
//      https://github.com/sgothel/jogl/blob/master/src/test/com/jogamp/opengl/test/junit/jogl/acore/TestSharedContextVBOES2NEWT.java
//      http://forum.jogamp.org/Advantages-of-using-NEWT-vs-GLCanvas-td3703674.html
//      http://jogamp.org/jogl/doc/NEWT-Overview.html
//      http://jogamp.org/git/?p=jogl.git;a=blob;f=src/test/com/jogamp/opengl/test/junit/newt/parenting/TestParenting01cAWT.java;hb=HEAD
//      http://jogamp.org/deployment/jogamp-next/javadoc/jogl/javadoc/com/jogamp/newt/event/awt/AWTAdapter.html
public class SwingGlimpseCanvasNew extends JPanel implements GlimpseCanvas
{
    private static final Logger logger = Logger.getLogger( SwingGlimpseCanvasNew.class.getName( ) );

    protected GLProfile glProfile;
    protected GLCapabilities glCapabilities;
    protected GLWindow glWindow;
    protected NewtCanvasAWT glCanvas;
    protected GLOffscreenAutoDrawable glDrawable;

    protected boolean isEventConsumer = true;
    protected boolean isEventGenerator = true;
    protected boolean isDisposed = false;

    protected LayoutManager layoutManager;
    protected MouseWrapperSwing mouseHelper;

    public SwingGlimpseCanvasNew( String profile, GLContext context )
    {
        this.glProfile = GLProfile.get( profile );
        this.glCapabilities = new GLCapabilities( glProfile );
        this.glDrawable = GLDrawableFactory.getFactory( glProfile ).createOffscreenAutoDrawable( null, glCapabilities, null, 1, 1, context );
        this.glWindow = GLWindow.create( glCapabilities );
        this.glWindow.setSharedContext( glDrawable.getContext( ) );
        this.glCanvas = new NewtCanvasAWT( glWindow );
        
        this.glWindow.addGLEventListener( createGLEventListener( ) );
        
        this.mouseHelper = new MouseWrapperSwing( this );
        this.addMouseListener( this.mouseHelper );
        this.addMouseMotionListener( this.mouseHelper );
        this.addMouseWheelListener( this.mouseHelper );

        this.layoutManager = new LayoutManager( );

        this.setLayout( new BorderLayout( ) );
        this.add( this.glCanvas, BorderLayout.CENTER );

        // workaround to enable the panel to shrink
        this.setMinimumSize( new Dimension( 0, 0 ) );

        this.isDisposed = false;
    }

    public SwingGlimpseCanvasNew( )
    {
        this( GLProfile.GL2ES2, null );
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
                for ( GlimpseLayout layout : layoutManager.getLayoutList( ) )
                {
                    layout.layoutTo( getGlimpseContext( ) );
                }
            }

            @Override
            public void dispose( GLAutoDrawable drawable )
            {
                // TODO Auto-generated method stub -- ttran17

            }
        };
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

    @Override
    // the glCanvas covers the entire underlying JPanel, so event listeners should be attached to the glCanvas, not this
    public void addMouseListener( MouseListener listener )
    {
//        com.jogamp.newt.event.MouseListener newtListener = new com.jogamp.newt.event.MouseListener( )
//        {
//            @Override
//            public void mouseClicked( com.jogamp.newt.event.MouseEvent event )
//            {
//            }
//
//            @Override
//            public void mouseDragged( com.jogamp.newt.event.MouseEvent event )
//            {
//            }
//
//            @Override
//            public void mouseEntered( com.jogamp.newt.event.MouseEvent event )
//            {
//            }
//
//            @Override
//            public void mouseExited( com.jogamp.newt.event.MouseEvent event )
//            {
//            }
//
//            @Override
//            public void mouseMoved( com.jogamp.newt.event.MouseEvent event )
//            {
//            }
//
//            @Override
//            public void mousePressed( com.jogamp.newt.event.MouseEvent event )
//            {
//                System.out.println( "NEWT Pressed" );
//            }
//
//            @Override
//            public void mouseReleased( com.jogamp.newt.event.MouseEvent event )
//            {
//            }
//
//            @Override
//            public void mouseWheelMoved( com.jogamp.newt.event.MouseEvent event )
//            {
//            }
//        };
//        
//        AWTMouseAdapter awtListener = new AWTMouseAdapter( newtListener );
//        {
//            @Override
//            public void mouseClicked( MouseEvent event )
//            {
//            }
//
//            @Override
//            public void mouseDragged( MouseEvent event )
//            {
//            }
//
//            @Override
//            public void mouseEntered( MouseEvent event )
//            {
//            }
//
//            @Override
//            public void mouseExited( MouseEvent event )
//            {
//            }
//
//            @Override
//            public void mouseMoved( MouseEvent event )
//            {
//            }
//
//            @Override
//            public void mousePressed( MouseEvent event )
//            {
//                System.out.println( "AWT Pressed" );
//            }
//
//            @Override
//            public void mouseReleased( MouseEvent event )
//            {
//            }
//
//            @Override
//            public void mouseWheelMoved( MouseWheelEvent event )
//            {
//            }
//        };
//        
//        awtListener.addTo( glCanvas );
//        
//        this.glWindow.addMouseListener( newtListener );
    
        this.glCanvas.addMouseListener( listener );
    }

    @Override
    // the glCanvas covers the entire underlying JPanel, so event listeners should be attached to the glCanvas, not this
    public void addMouseMotionListener( MouseMotionListener listener )
    {
        this.glCanvas.addMouseMotionListener( listener );
    }

    @Override
    // the glCanvas covers the entire underlying JPanel, so event listeners should be attached to the glCanvas, not this
    public void addMouseWheelListener( MouseWheelListener listener )
    {
        this.glCanvas.addMouseWheelListener( listener );
    }

    @Override
    // the glCanvas covers the entire underlying JPanel, so event listeners should be attached to the glCanvas, not this
    public void removeMouseListener( MouseListener listener )
    {
        this.glCanvas.removeMouseListener( listener );
    }

    @Override
    // the glCanvas covers the entire underlying JPanel, so event listeners should be attached to the glCanvas, not this
    public void removeMouseMotionListener( MouseMotionListener listener )
    {
        this.glCanvas.removeMouseMotionListener( listener );
    }

    @Override
    // the glCanvas covers the entire underlying JPanel, so event listeners should be attached to the glCanvas, not this
    public void removeMouseWheelListener( MouseWheelListener listener )
    {
        this.glCanvas.removeMouseWheelListener( listener );
    }

    @Override
    // the glCanvas covers the entire underlying JPanel, so event listeners should be attached to the glCanvas, not this
    public void addKeyListener( KeyListener listener )
    {
        this.glCanvas.addKeyListener( listener );
    }

    @Override
    // the glCanvas covers the entire underlying JPanel, so event listeners should be attached to the glCanvas, not this
    public void removeKeyListener( KeyListener listener )
    {
        this.glCanvas.removeKeyListener( listener );
    }

    public Dimension getDimension( )
    {
        return this.glCanvas.getSize( );
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
        return SwingGlimpseCanvasNew.class.getSimpleName( );
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
    public void dispose( RepaintManager manager )
    {
        Runnable dispose = new Runnable( )
        {
            @Override
            public void run( )
            {
                GLContext glContext = getGLContext( );
                GlimpseContext context = new GlimpseContextImpl( glContext );
                glContext.makeCurrent( );
                try
                {
                    for ( GlimpseLayout layout : layoutManager.getLayoutList( ) )
                    {
                        layout.dispose( context );
                    }
                }
                finally
                {
                    glContext.release( );
                }

                isDisposed = true;
            }
        };

        if ( manager != null )
        {
            manager.asyncExec( dispose );
        }
        else
        {
            dispose.run( );
        }
    }
}
