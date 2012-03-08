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
package com.metsci.glimpse.canvas;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCanvas;
import javax.media.opengl.GLContext;
import javax.media.opengl.GLDrawableFactory;
import javax.media.opengl.GLEventListener;
import javax.swing.JPanel;

import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.context.GlimpseContextImpl;
import com.metsci.glimpse.context.GlimpseTarget;
import com.metsci.glimpse.context.GlimpseTargetStack;
import com.metsci.glimpse.event.mouse.swing.MouseWrapperSwing;
import com.metsci.glimpse.layout.GlimpseLayout;
import com.metsci.glimpse.support.settings.LookAndFeel;

/**
 * A JPanel onto which Glimpse rendering can take place. This class represents
 * the interface between Swing and OpenGL/Glimpse.
 *
 * @author ulman
 */
public class SwingGlimpseCanvas extends JPanel implements GlimpseCanvas
{
    private static final long serialVersionUID = -5279064113986688397L;

    private GLCanvas glCanvas;
    private GLAutoDrawable tempDrawable;

    protected boolean isDisposed;

    protected List<GlimpseTarget> unmodifiableList;
    protected List<GlimpseLayout> layoutList;

    protected MouseWrapperSwing mouseHelper;
    protected boolean isEventConsumer = true;
    protected boolean isEventGenerator = true;

    public SwingGlimpseCanvas( )
    {
        this( true );
    }

    public SwingGlimpseCanvas( GLContext _context )
    {
        this( true, _context );
    }

    public SwingGlimpseCanvas( boolean setNoEraseBackgroundProperty )
    {
        this( setNoEraseBackgroundProperty, null );
    }

    @SuppressWarnings( { "unchecked", "rawtypes" } )
    public SwingGlimpseCanvas( boolean setNoEraseBackgroundProperty, GLContext _context )
    {
        if ( setNoEraseBackgroundProperty )
        {
            System.setProperty( "sun.awt.noerasebackground", "true" );
        }

        if ( _context == null )
        {
            this.glCanvas = new GLCanvas( );
        }
        else
        {
            this.glCanvas = new GLCanvas( null, null, _context, null );
        }

        this.mouseHelper = new MouseWrapperSwing( this );
        this.addMouseListener( this.mouseHelper );
        this.addMouseMotionListener( this.mouseHelper );
        this.addMouseWheelListener( this.mouseHelper );

        this.layoutList = new ArrayList<GlimpseLayout>( );

        // this is typesafe because unmodifiableList is unmodifiable, so it's not
        // possible to corrupt layoutList with non GlimpseLayouts
        this.unmodifiableList = ( List ) Collections.unmodifiableList( this.layoutList );

        this.setLayout( new BorderLayout( ) );
        this.add( this.glCanvas, BorderLayout.CENTER );

        // workaround to enable the panel to shrink
        this.setMinimumSize( new Dimension( 0, 0 ) );

        this.isDisposed = false;

        this.addGLEventListener( this.glCanvas );
    }

    @Override
    public GlimpseContext getGlimpseContext( )
    {
        return new GlimpseContextImpl( this );
    }

    @Override
    public void setLookAndFeel( LookAndFeel laf )
    {
        for ( GlimpseLayout layout : layoutList )
        {
            layout.setLookAndFeel( laf );
        }
    }

    @Override
    public void addLayout( GlimpseLayout layout )
    {
        this.layoutList.add( layout );
    }

    @Override
    public void removeLayout( GlimpseLayout layout )
    {
        this.layoutList.remove( layout );
    }

    @Override
    public List<GlimpseTarget> getTargetChildren( )
    {
        return this.unmodifiableList;
    }

    @Override
    // the glCanvas covers the entire underlying JPanel, so event listeners should be attached to the glCanvas, not this
    public void addMouseListener( MouseListener listener )
    {
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
        this.glCanvas.display( );
    }

    @Override
    public GLContext getGLContext( )
    {
        return this.glCanvas.getContext( );
    }

    @Override
    public void dispose( )
    {
        //TODO how should dispose( ) react? should GlimpseCanvas have a dispose( ) method?
        //     should this be the responsibility of the GlimpseLayout containing the painters?
        //     or of the painter itself since it might be used elsewhere?
        this.isDisposed = true;
    }

    @Override
    public boolean isDisposed( )
    {
        return this.isDisposed;
    }

    @Override
    public String toString( )
    {
        return SwingGlimpseCanvas.class.getSimpleName( );
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

    /**
     * This implementation of removeNotify is to get around an inconvenience
     * where this canvas is removed from one component hierarchy and added to
     * another.  Technically, this will destroy the context.  But since Glimpse
     * makes significant use of buffers and textures, this data needs to come
     * along.  Therefore, we force shared contexts to preserve the information
     * as we transition to another canvas.  This helps for docking frameworks
     * and moving the canvas seamlessly.
     * <p>
     * We could override the removeNotify method of the enclosed GLCanvas, but
     * then we'd have to take care of removing it from drawing into the screen.
     * We could create a new GLCanvas directly shared with the old, instead of
     * a pbuffer, but you have to draw into a new context at least once to share
     * all the information, and you can't draw into a GLCanvas until it's
     * physically displayed.
     * </p>
     */
    @Override
    public void removeNotify( )
    {
        // transfer all contexts to a holding drawable
        if ( tempDrawable == null )
        {
            tempDrawable = GLDrawableFactory.getFactory( ).createGLPbuffer( glCanvas.getChosenGLCapabilities( ), null, 10, 10, glCanvas.getContext( ) );
        }

        attachAllGLListeners( tempDrawable );
        tempDrawable.display( );

        // remove the canvas (will destroy the context)
        boolean autoSwap = glCanvas.getAutoSwapBufferMode( );
        remove( this.glCanvas );
        super.removeNotify( );

        // initialize the new canvas, share the temp context
        this.glCanvas = new GLCanvas( tempDrawable.getChosenGLCapabilities( ), null, tempDrawable.getContext( ), null );
        this.glCanvas.setAutoSwapBufferMode( autoSwap );
        attachAllGLListeners( glCanvas );
        add( this.glCanvas, BorderLayout.CENTER );
    }

    /**
     * Attaches all the glimpse-related listeners to the new drawable.  This
     * should also transfer over the AWT listeners, but it doesn't.
     */
    private void attachAllGLListeners( GLAutoDrawable drawable )
    {
        this.addMouseListener( this.mouseHelper );
        this.addMouseMotionListener( this.mouseHelper );
        this.addMouseWheelListener( this.mouseHelper );
        this.addGLEventListener( drawable );
    }

    private void addGLEventListener( GLAutoDrawable drawable )
    {
        drawable.addGLEventListener( new GLEventListener( )
        {
            @Override
            public void init( GLAutoDrawable drawable )
            {
                // do nothing
            }

            @Override
            public void display( GLAutoDrawable drawable )
            {
                for ( GlimpseLayout layout : layoutList )
                {
                    layout.paintTo( getGlimpseContext( ) );
                }
            }

            @Override
            public void reshape( GLAutoDrawable drawable, int x, int y, int width, int height )
            {
                for ( GlimpseLayout layout : layoutList )
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
}
