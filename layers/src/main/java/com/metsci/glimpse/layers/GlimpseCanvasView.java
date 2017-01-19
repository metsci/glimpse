package com.metsci.glimpse.layers;

import static com.metsci.glimpse.layers.misc.UiUtils.requireSwingThread;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.media.opengl.GLAnimatorControl;
import javax.media.opengl.GLContext;
import javax.media.opengl.GLProfile;
import javax.swing.JPanel;

import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.support.swing.NewtSwingEDTGlimpseCanvas;

public abstract class GlimpseCanvasView extends View
{

    protected final List<Layer> layers;
    protected GLAnimatorControl animator;

    protected boolean areTraitsSet;
    protected boolean isCanvasReady;

    public final JPanel canvasParent;
    protected NewtSwingEDTGlimpseCanvas canvas;


    public GlimpseCanvasView( GLProfile glProfile )
    {
        this.layers = new ArrayList<>( );
        this.animator = null;

        this.areTraitsSet = false;
        this.isCanvasReady = false;

        this.canvas = null;

        // Some platforms (especially OSX), a canvas can have its GLContext destroyed and replaced
        // in the course of regular usage -- e.g. when a view is moved to a new docking location.
        // This can cause problems for GlimpsePainters, which generally create their GL resources
        // inside a particular context, and then assume the resource handles will always be valid.
        //
        // One way to deal with this is for all canvases to share a master GLContext -- even when
        // a canvas's context gets destroyed, all its resources survive inside the master context.
        // Unfortunately, some platforms (e.g. Intel gfx on Linux) do not support context sharing.
        //
        // The alternative is to dispose all the painters when the context starts to die, and then
        // re-create them all when the context is available again. This can be accomplished with a
        // GLEventListener.
        //
        // Additionally, NEWT's native-window reparenting seems to have race conditions that cause
        // serious problems. The only obvious workaround is to tear down the canvas manually before
        // reparenting starts, and then set up a new canvas manually after reparenting finishes.
        //
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

    @Override
    public void setGLAnimator( GLAnimatorControl animator )
    {
        this.animator = animator;
    }

    protected void setUpCanvas( GLProfile glProfile )
    {
        if ( this.canvas == null )
        {
            this.canvas = new NewtSwingEDTGlimpseCanvas( glProfile );
            this.canvasParent.add( this.canvas );

            this.animator.start( );
            this.animator.add( this.canvas.getGLDrawable( ) );

            this.canvas.getGLDrawable( ).invoke( false, ( glDrawable ) ->
            {
                this.doContextReady( this.canvas.getGlimpseContext( ) );

                this.isCanvasReady = true;
                if ( this.areTraitsSet )
                {
                    this.init( );
                }

                for ( Layer layer : this.layers )
                {
                    // Call super.addLayer() to install the facet, without re-adding the layer to this.layers
                    super.addLayer( layer );
                }

                return false;
            } );
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

    protected void tearDownCanvas( )
    {
        if ( this.canvas != null )
        {
            this.animator.remove( this.canvas.getGLDrawable( ) );

            // WIP: Might be cleaner to do this in GLEventListener.dispose()
            if ( this.isCanvasReady )
            {
                GLContext context = this.canvas.getGLContext( );
                context.makeCurrent( );
                try
                {
                    for ( Layer layer : this.layers )
                    {
                        // The GLContext is being disposed, so something must be happening to the
                        // view as a whole: either the view is being closed (in which case the value
                        // of isReinstall doesn't matter), or it is being re-parented (in which case
                        // we want isReinstall to be true)
                        boolean isReinstall = true;

                        // Call super.removeLayer() to uninstall the facet, while leaving the layer
                        // in this.layers
                        super.removeLayer( layer, isReinstall );
                    }

                    this.doContextDying( this.canvas.getGlimpseContext( ) );
                }
                finally
                {
                    context.release( );
                }

                this.isCanvasReady = false;
            }

            this.canvas.getCanvas( ).setNEWTChild( null );
            this.canvasParent.remove( this.canvas );
            this.canvas.destroy( );
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
    protected void addLayer( Layer layer )
    {
        this.layers.add( layer );

        if ( this.isCanvasReady )
        {
            super.addLayer( layer );
        }
    }

    @Override
    protected void removeLayer( Layer layer, boolean isReinstall )
    {
        this.layers.remove( layer );
        super.removeLayer( layer, isReinstall );
    }

    @Override
    public Component getComponent( )
    {
        return this.canvasParent;
    }

    public static interface GlimpseRunnable
    {
        boolean run( GlimpseContext context );
    }

    public void glimpseInvoke( GlimpseRunnable runnable )
    {
        requireSwingThread( );

        boolean succeeded = this.canvas.getGLDrawable( ).invoke( true, ( glDrawable ) ->
        {
            return runnable.run( this.canvas.getGlimpseContext( ) );
        } );

        if ( !succeeded )
        {
            throw new RuntimeException( "glimpseInvoke() failed" );
        }
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
