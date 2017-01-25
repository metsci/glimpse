package com.metsci.glimpse.layers;

import static com.metsci.glimpse.layers.misc.UiUtils.requireSwingThread;
import static com.metsci.glimpse.support.DisposableUtils.onGLDispose;
import static com.metsci.glimpse.support.DisposableUtils.onGLInit;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.media.opengl.GLAnimatorControl;
import javax.media.opengl.GLProfile;
import javax.swing.JPanel;

import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.support.swing.NewtSwingEDTGlimpseCanvas;

public abstract class GlimpseCanvasView extends View
{

    public static interface GlimpseRunnable
    {
        boolean run( GlimpseContext context );
    }


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

    protected void setUpCanvas( GLProfile glProfile )
    {
        if ( this.canvas == null )
        {
            this.canvas = new NewtSwingEDTGlimpseCanvas( glProfile );

            // Once canvas is ready, do view-specific setup and install facets
            onGLInit( this.canvas, ( drawable ) ->
            {
                this.doContextReady( this.canvas.getGlimpseContext( ) );

                this.isCanvasReady = true;
                if ( this.areTraitsSet )
                {
                    super.init( );
                }

                for ( Layer layer : this.layers )
                {
                    // Call super.addLayer() to install the facet, without re-adding the layer to this.layers
                    super.addLayer( layer );
                }
            } );

            // Before canvas gets destroyed, uninstall facets and do view-specific tear-down
            onGLDispose( this.canvas, ( drawable ) ->
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

                this.isCanvasReady = false;
            } );

            this.canvasParent.add( this.canvas );

            this.animator.start( );
            this.animator.add( this.canvas.getGLDrawable( ) );
        }
    }

    protected void tearDownCanvas( )
    {
        if ( this.canvas != null )
        {
            this.animator.remove( this.canvas.getGLDrawable( ) );

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
    public void setGLAnimator( GLAnimatorControl animator )
    {
        this.animator = animator;
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
