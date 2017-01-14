package com.metsci.glimpse.layers;

import java.awt.Component;

import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;

import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.support.swing.NewtSwingEDTGlimpseCanvas;

public abstract class GlimpseCanvasView extends View
{

    public final NewtSwingEDTGlimpseCanvas canvas;


    public GlimpseCanvasView( )
    {
        this.canvas = new NewtSwingEDTGlimpseCanvas( );

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

                thisView.onContextReady( thisView.canvas.getGlimpseContext( ) );

                thisView.init( );

                for ( Layer layer : asdf )
                {
                    thisView.addLayer( layer );
                }
            }

            @Override
            public void dispose( GLAutoDrawable glDrawable )
            {
                GlimpseCanvasView thisView = GlimpseCanvasView.this;

                for ( Layer layer : asdf )
                {
                    // The GLContext is being disposed, so something must be happening to the
                    // view as a whole: either the view is being closed (in which case the value
                    // of isReinstall doesn't matter), or it is being re-parented (in which case
                    // we want isReinstall to be true).
                    thisView.removeLayer( layer, true );
                }

                thisView.onContextDying( thisView.canvas.getGlimpseContext( ) );
            }

            @Override
            public void reshape( GLAutoDrawable glDrawable, int x, int y, int width, int height )
            { }

            @Override
            public void display( GLAutoDrawable glDrawable )
            { }
        } );
    }

    protected abstract void onContextReady( GlimpseContext context );

    protected abstract void onContextDying( GlimpseContext context );

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
