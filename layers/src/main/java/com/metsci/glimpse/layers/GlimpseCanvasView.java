/*
 * Copyright (c) 2019 Metron, Inc.
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
package com.metsci.glimpse.layers;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Objects.equal;
import static com.metsci.glimpse.gl.util.GLCapabilityUtils.getGLRendererString;
import static com.metsci.glimpse.gl.util.GLCapabilityUtils.getGLVersionString;
import static com.metsci.glimpse.layers.misc.UiUtils.ensureAnimating;
import static com.metsci.glimpse.layers.misc.UiUtils.requireSwingThread;
import static com.metsci.glimpse.support.DisposableUtils.onGLDispose;
import static com.metsci.glimpse.support.DisposableUtils.onGLInit;
import static com.metsci.glimpse.util.logging.LoggerUtils.getLogger;
import static com.metsci.glimpse.util.logging.LoggerUtils.logInfo;
import static javax.media.opengl.GLContext.CONTEXT_NOT_CURRENT;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.media.opengl.GLAnimatorControl;
import javax.media.opengl.GLContext;
import javax.media.opengl.GLProfile;
import javax.swing.JPanel;

import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.support.swing.NewtSwingEDTGlimpseCanvas;

public abstract class GlimpseCanvasView extends View
{
    private static final Logger logger = getLogger( GlimpseCanvasView.class );

    /**
     * Specifies the approach to use when moving a GL canvas from one parent component to another.
     * This is exercised during changes to the docking arrangement (both programmatic and interactive).
     * Recognized values are:
     * <ul>
     * <li>FAST: rely on NEWT reparenting -- smooth when it works, but breaks badly on some platforms
     * <li>ROBUST: avoid NEWT reparenting -- reliable across platforms, but can be slow and clunky
     * <li>AUTO: choose the best method, potentially based on the details of the current platform
     * </ul>
     * Defaults to AUTO if no value is specified, or if an unrecognized value is specified.
     */
    public static final String glReparentingMethod = System.getProperty( "layers.glReparentingMethod" );


    protected GLAnimatorControl animator;

    protected boolean areTraitsSet;
    protected boolean isCanvasReady;

    public final JPanel canvasParent;
    protected NewtSwingEDTGlimpseCanvas canvas;

    /**
     * Stores facets states during a reparent -- populated from old facets in the old canvas's
     * onGLDispose(), then applied to new facets (and cleared) in the new canvas's onGLInit().
     */
    protected final Map<Layer,FacetState> facetStatesBeforeReparent;


    public GlimpseCanvasView( GLProfile glProfile, Collection<? extends ViewOption> options )
    {
        super( options );

        this.animator = null;

        this.areTraitsSet = false;
        this.isCanvasReady = false;

        this.canvas = null;

        this.facetStatesBeforeReparent = new HashMap<>( );

        // TODO: Consider platform details when method is AUTO
        if ( equal( glReparentingMethod, "FAST" ) )
        {
            // NEWT's reparenting works fine on some platforms, and is smoother than the method below
            this.canvasParent = new JPanel( new BorderLayout( ) );
            this.setUpCanvas( glProfile );
        }
        else
        {
            // NEWT's reparenting seems to have problematic race conditions -- so remove all GL stuff
            // before the view gets reparented, and re-create the GL stuff after reparenting is done
            this.canvasParent = new JPanel( new BorderLayout( ) )
            {
                @Override
                public void addNotify( )
                {
                    super.addNotify( );
                    setUpCanvas( glProfile );
                }

                @Override
                public void removeNotify( )
                {
                    tearDownCanvas( );
                    super.removeNotify( );
                }
            };
        }
    }

    protected void setUpCanvas( GLProfile glProfile )
    {
        if ( this.canvas == null )
        {
            SharedContextViewOption opt = null;
            for ( ViewOption o : this.viewOptions )
            {
                if ( o instanceof SharedContextViewOption )
                    opt = ( SharedContextViewOption ) o;
            }

            if ( opt == null )
                this.canvas = new NewtSwingEDTGlimpseCanvas( glProfile );
            else
                this.canvas = new NewtSwingEDTGlimpseCanvas( opt.context );

            // Once canvas is ready, do view-specific setup and install facets
            onGLInit( this.canvas, ( drawable ) ->
            {
                logInfo( logger, "GL canvas init: title = \"%s\", GL_VERSION = \"%s\", GL_RENDERER = \"%s\", reparenting = %s", this.title.v( ), getGLVersionString( drawable.getGL( ) ), getGLRendererString( drawable.getGL( ) ), firstNonNull( glReparentingMethod, "default" ) );

                this.doContextReady( this.canvas.getGlimpseContext( ) );
                this.isCanvasReady = true;

                if ( this.areTraitsSet )
                {
                    super.init( );
                }

                for ( Layer layer : this._layers.v( ) )
                {
                    FacetState state = facetStatesBeforeReparent.get( layer );
                    this.installLayer( layer, state );
                }

                facetStatesBeforeReparent.clear( );
            } );

            // Before canvas gets destroyed, uninstall facets and do view-specific tear-down
            onGLDispose( this.canvas, ( drawable ) ->
            {
                logInfo( logger, "GL canvas dispose: title = \"%s\"", this.title.v( ) );

                for ( Layer layer : this._layers.v( ) )
                {
                    // The GLContext is being disposed, so something must be happening to the view as a whole:
                    // either the view is being closed (in which case the value of isReinstall doesn't matter),
                    // or it is being re-parented (in which case we want isReinstall to be true)
                    boolean isReinstall = true;

                    FacetState state = this.uninstallLayer( layer, isReinstall );
                    facetStatesBeforeReparent.put( layer, state );
                }

                this.doContextDying( this.canvas.getGlimpseContext( ) );
                this.isCanvasReady = false;
            } );

            this.canvasParent.add( this.canvas );

            if ( this.animator != null )
            {
                ensureAnimating( this.animator );
                this.animator.add( this.canvas.getGLDrawable( ) );
            }
        }
    }

    protected void tearDownCanvas( )
    {
        if ( this.canvas != null )
        {
            this.animator.remove( this.canvas.getGLDrawable( ) );
            this.canvas.destroy( );
            this.canvasParent.remove( this.canvas );
            this.canvas = null;
        }
    }

    /**
     * Create layouts and painters, and add them to the canvas.
     */
    protected abstract void doContextReady( GlimpseContext context );

    /**
     * Remove layouts and painters from the canvas, and dispose of them.
     */
    protected abstract void doContextDying( GlimpseContext context );

    @Override
    public void setGLAnimator( GLAnimatorControl animator )
    {
        this.animator = animator;

        if ( this.canvas != null )
        {
            ensureAnimating( this.animator );
            this.animator.add( this.canvas.getGLDrawable( ) );
        }
    }

    @Override
    protected void init( )
    {
        this.areTraitsSet = true;
        if ( this.isCanvasReady )
        {
            super.init( );
        }
    }

    @Override
    protected void installLayer( Layer layer, FacetState state )
    {
        if ( this.isCanvasReady )
        {
            super.installLayer( layer, state );
        }
    }

    @Override
    public Component getComponent( )
    {
        return this.canvasParent;
    }

    public void glimpseInvoke( GlimpseRunnable runnable )
    {
        requireSwingThread( );
        glimpseRun( this.canvas.getGlimpseContext( ), runnable );
    }

    // TODO: This should go somewhere more general, once all its issues have been worked out
    protected static void glimpseRun( GlimpseContext context, GlimpseRunnable runnable )
    {
        requireSwingThread( );

        // TODO: Not sure what happens if context is not fully realized yet ... needs to be investigated, but will take some effort

        GLContext glimpse = context.getGLContext( );
        GLContext current = GLContext.getCurrent( );

        if ( current == glimpse )
        {
            runnable.run( context );
        }
        else if ( current == null )
        {
            if ( glimpse.makeCurrent( ) == CONTEXT_NOT_CURRENT )
            {
                throw new RuntimeException( "Failed to make GLContext current in glimpseInvoke()" );
            }
            else
            {
                try
                {
                    runnable.run( context );
                }
                finally
                {
                    glimpse.release( );
                }
            }
        }
        else
        {
            try
            {
                if ( glimpse.makeCurrent( ) == CONTEXT_NOT_CURRENT )
                {
                    throw new RuntimeException( "Failed to make GLContext current in glimpseInvoke()" );
                }
                else
                {
                    runnable.run( context );
                }
            }
            finally
            {
                if ( current.makeCurrent( ) == CONTEXT_NOT_CURRENT )
                {
                    throw new RuntimeException( "Failed to restore original GLContext after glimpseInvoke()" );
                }
            }
        }
    }

    public BufferedImage toBufferedImage( )
    {
        requireSwingThread( );

        // TODO: Should this be using glimpseInvoke()?  Or doing something completely different?
        return canvas.toBufferedImage( );
    }

    @Override
    protected void dispose( )
    {
        if ( this.canvas != null && this.animator != null )
        {
            this.animator.remove( this.canvas.getGLDrawable( ) );
        }

        super.dispose( );

        if ( this.canvas != null )
        {
            this.tearDownCanvas( );
        }
    }

}
