/*
 * Copyright (c) 2012, Metron, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of Metron, Inc. nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL METRON, INC. BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.metsci.glimpse.swt.canvas;

import static com.metsci.glimpse.util.logging.LoggerUtils.logWarning;

import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.List;
import java.util.logging.Logger;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCanvas;
import javax.media.opengl.GLContext;
import javax.media.opengl.GLEventListener;

import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import com.metsci.glimpse.canvas.GlimpseCanvas;
import com.metsci.glimpse.canvas.LayoutManager;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.context.GlimpseContextImpl;
import com.metsci.glimpse.context.GlimpseTarget;
import com.metsci.glimpse.context.GlimpseTargetStack;
import com.metsci.glimpse.layout.GlimpseLayout;
import com.metsci.glimpse.support.repaint.RepaintManager;
import com.metsci.glimpse.support.settings.LookAndFeel;
import com.metsci.glimpse.swt.event.mouse.MouseWrapperSWTBridge;

public class SwtBridgeGlimpseCanvas extends Composite implements GlimpseCanvas
{
    private static final Logger logger = Logger.getLogger( SwtBridgeGlimpseCanvas.class.getName( ) );

    protected java.awt.Frame glFrame;
    protected GLCanvas glCanvas;

    protected Composite parent;

    protected LayoutManager layoutManager;

    protected MouseWrapperSWTBridge mouseHelper;
    protected boolean isEventConsumer = true;
    protected boolean isEventGenerator = true;
    protected boolean isDisposed = false;
    
    public SwtBridgeGlimpseCanvas( Composite parent )
    {
        this( parent, null );
    }

    public SwtBridgeGlimpseCanvas( Composite parent, GLContext context )
    {
        this( parent, context, SWT.EMBEDDED );
    }

    public SwtBridgeGlimpseCanvas( Composite parent, GLContext context, int style )
    {
        super( parent, style | SWT.EMBEDDED );

        try
        {
            // alleviates some of the flicker that occurs during resizing (affects Windows only)
            System.setProperty( "sun.awt.noerasebackground", "true" );
        }
        catch ( SecurityException e )
        {
        }

        this.parent = parent;

        if ( context == null )
        {
            this.glCanvas = new GLCanvas( );
        }
        else
        {
            this.glCanvas = new GLCanvas( null, null, context, null );
        }

        this.layoutManager = new LayoutManager( );

        this.mouseHelper = new MouseWrapperSWTBridge( this );
        this.glCanvas.addMouseListener( this.mouseHelper );
        this.glCanvas.addMouseMotionListener( this.mouseHelper );
        this.glCanvas.addMouseWheelListener( this.mouseHelper );

        this.addFocusListener( );

        // use an AWT to SWT wrapper to embed the AWT GLCanvas into the SWT
        // application
        // this works much more cleanly than using
        // org.eclipse.swt.opengl.GLCanvas
        // which has bugs, particularly related to context sharing
        this.glFrame = SWT_AWT.new_Frame( this );
        this.glFrame.add( this.glCanvas );

        this.addGLEventListener( this.glCanvas );
    }

    public GLCanvas getGLCanvas( )
    {
        return this.glCanvas;
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
    public String toString( )
    {
        return SwtGlimpseCanvas.class.getSimpleName( );
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
    public GlimpseBounds getTargetBounds( GlimpseTargetStack stack )
    {
        Dimension dimension = getDimension( );

        if ( dimension != null )
        {
            return new GlimpseBounds( getDimension( ) );
        }
        else
        {
            return null;
        }
    }

    public Dimension getDimension( )
    {
        return this.glCanvas.getSize( );
    }

    @Override
    public GlimpseBounds getTargetBounds( )
    {
        return getTargetBounds( null );
    }

    @Override
    public GLContext getGLContext( )
    {
        return this.glCanvas.getContext( );
    }

    @Override
    public void paint( )
    {
        glCanvas.display( );
    }

    // In linux, the component the mouse pointer is over receives mouse wheel
    // events
    // In windows, the component with focus receives mouse wheel events
    // These listeners emulate linux-like mouse wheel event dispatch for
    // important components
    // This causes the application to work in slightly un-windows-like ways
    // some of the time, but the effect is minor.
    protected void addFocusListener( )
    {
        glCanvas.addMouseListener( new MouseAdapter( )
        {
            public void requestFocus( )
            {
                final Display display = Display.getDefault( );
                
                // we want the glCanvas to have AWT focus and this Composite to have SWT focus
                display.asyncExec( new Runnable( )
                {
                    public void run( )
                    {
                        if ( !display.isDisposed( ) && !isDisposed( ) && !parent.isDisposed( ) )
                        {
                            forceFocus( );
                        }
                    }
                } );

                glCanvas.requestFocus( );
            }

            public void mouseClicked( MouseEvent e )
            {
                requestFocus( );
            }

            public void mousePressed( MouseEvent e )
            {
                requestFocus( );
            }

            public void mouseReleased( MouseEvent e )
            {
                requestFocus( );
            }

            public void mouseEntered( MouseEvent e )
            {
                requestFocus( );
            }

            public void mouseExited( MouseEvent e )
            {
            }

            public void mouseWheelMoved( MouseWheelEvent e )
            {
                requestFocus( );
            }

            public void mouseDragged( MouseEvent e )
            {
                requestFocus( );
            }

            public void mouseMoved( MouseEvent e )
            {
                requestFocus( );
            }
        } );
    }

    private void addGLEventListener( GLAutoDrawable drawable )
    {
        drawable.addGLEventListener( new GLEventListener( )
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
            public void displayChanged( GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged )
            {
                // do nothing
            }
        } );
    }
    
    @Override
    public boolean isDisposed( )
    {
        return isDisposed;
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
            }
        };
        
        if ( manager != null )
        {
            manager.syncExec( dispose );   
        }
        else
        {
            dispose.run( );
        }
        
        dispose( );
    
        isDisposed = true;
    }
}
