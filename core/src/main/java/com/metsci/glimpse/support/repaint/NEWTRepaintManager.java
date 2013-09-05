package com.metsci.glimpse.support.repaint;

import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLRunnable;

import com.metsci.glimpse.canvas.NewtGlimpseCanvas;

/**
 * A repaint manager for NEWT. Currently experimental: syncExec may not actually block
 * like other RepaintManager implementations.
 * 
 * SwingRepaintManager or RepaintManager will also work. However NEWT decouples
 * GL repainting from the EDT and SwingRepaintManager places the repaint loop on the EDT.
 * 
 * @author ulman
 */
public class NEWTRepaintManager extends RepaintManager
{
    public static NEWTRepaintManager newRepaintManager( NewtGlimpseCanvas canvas )
    {
        NEWTRepaintManager manager = new NEWTRepaintManager( canvas.getGLDrawable( ) );
        manager.addGlimpseCanvas( canvas );
        manager.start( );
        return manager;
    }

    protected GLAutoDrawable drawable;

    public NEWTRepaintManager( GLAutoDrawable drawable )
    {
        this.drawable = drawable;
    }

    public void asyncExec( final Runnable runnable )
    {
        exec( runnable, false );
    }

    public void syncExec( Runnable runnable )
    {
        exec( runnable, true );
    }

    protected void exec( final Runnable runnable, boolean sync )
    {
        // true argument causes invoke to wait until repaint
        // https://projectsforge.org/projects/bundles/browser/trunk/jogl-2.0-rc2/jogl/src/main/java/javax/media/opengl/GLAutoDrawable.java?rev=2
        drawable.invoke( sync, new GLRunnable( )
        {
            // true return value indicates framebuffer remains intact
            // http://jogamp.org/deployment/jogamp-next/javadoc/jogl/javadoc/javax/media/opengl/GLRunnable.html
            @Override
            public boolean run( GLAutoDrawable drawable )
            {
                runnable.run( );
                return true;
            }
        } );
    }
}
