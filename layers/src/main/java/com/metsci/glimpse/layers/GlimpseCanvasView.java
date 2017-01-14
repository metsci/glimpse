package com.metsci.glimpse.layers;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;

import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.support.swing.NewtSwingEDTGlimpseCanvas;

public abstract class GlimpseCanvasView extends View
{

    public final NewtSwingEDTGlimpseCanvas canvas;

    protected final List<Layer> layers;

    protected boolean isContextValid;
    protected boolean areTraitsValid;


    public GlimpseCanvasView( )
    {
        this.canvas = new NewtSwingEDTGlimpseCanvas( );

        this.layers = new ArrayList<>( );

        this.isContextValid = false;
        this.areTraitsValid = false;

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
        // re-create them all when the context is available again. That's what this GLEventListener
        // is doing.
        //
        this.canvas.getGLDrawable( ).addGLEventListener( new GLEventListener( )
        {
            @Override
            public void init( GLAutoDrawable glDrawable )
            {
                GlimpseCanvasView thisView = GlimpseCanvasView.this;
                thisView.contextReady( thisView.canvas.getGlimpseContext( ) );
            }

            @Override
            public void dispose( GLAutoDrawable glDrawable )
            {
                GlimpseCanvasView thisView = GlimpseCanvasView.this;
                thisView.contextDying( thisView.canvas.getGlimpseContext( ) );
            }

            @Override
            public void reshape( GLAutoDrawable glDrawable, int x, int y, int width, int height )
            { }

            @Override
            public void display( GLAutoDrawable glDrawable )
            { }
        } );
    }

    protected void contextReady( GlimpseContext context )
    {
        if ( this.isContextValid )
        {
            // This should never happen -- if it does, it means we've misunderstood the GLContext lifecycle
            throw new RuntimeException( "Context is already valid" );
        }

        this.isContextValid = true;

        this.doContextReady( this.canvas.getGlimpseContext( ) );

        if ( this.areTraitsValid )
        {
            this.init( );
        }

        for ( Layer layer : this.layers )
        {
            // Call super.addLayer() to install the facet, without re-adding the layer to
            // this.layers
            super.addLayer( layer );
        }
    }

    @Override
    protected void init( )
    {
        this.areTraitsValid = true;

        if ( this.isContextValid )
        {
            super.init( );
        }
    }

    protected void contextDying( GlimpseContext context )
    {
        if ( !this.isContextValid )
        {
            // This should never happen -- if it does, it means we've misunderstood the GLContext lifecycle
            throw new RuntimeException( "Context is already not valid" );
        }

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

        this.isContextValid = false;
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

        if ( this.isContextValid )
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
        return this.canvas;
    }

    @Override
    public GLAutoDrawable getGLDrawable( )
    {
        return this.canvas.getGLDrawable( );
    }

    @Override
    protected void dispose( )
    {
        super.dispose( );
        this.canvas.dispose( );
    }

}
