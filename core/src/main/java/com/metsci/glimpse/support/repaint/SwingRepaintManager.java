package com.metsci.glimpse.support.repaint;

import static com.metsci.glimpse.util.logging.LoggerUtils.logWarning;

import java.lang.reflect.InvocationTargetException;

import javax.swing.SwingUtilities;

import com.metsci.glimpse.canvas.GlimpseCanvas;

public class SwingRepaintManager extends RepaintManager
{
    public static SwingRepaintManager newRepaintManager( GlimpseCanvas canvas )
    {
        SwingRepaintManager manager = new SwingRepaintManager( );
        manager.addGlimpseCanvas( canvas );
        manager.start( );
        return manager;
    }
    
    public void asyncExec( Runnable runnable )
    {
        SwingUtilities.invokeLater( runnable );
    }

    public void syncExec( Runnable runnable )
    {
        try
        {
            SwingUtilities.invokeAndWait( runnable );
        }
        catch ( InterruptedException e )
        {
            logWarning( logger, "Trouble in RepaintManager", e );
        }
        catch ( InvocationTargetException e )
        {
            logWarning( logger, "Trouble in RepaintManager", e );
        }
    }

    public boolean checkThread( )
    {
        return SwingUtilities.isEventDispatchThread( );
    }

    public Runnable newRepaintRunnable( )
    {
        return new RepaintRunnable( );
    }

    public class RepaintRunnable implements Runnable
    {
        @Override
        public void run( )
        {
            try
            {
                lock.lock( );
                try
                {
                    while ( paused )
                    {
                        pause.await( );
                    }
                }
                finally
                {
                    lock.unlock( );
                }

                SwingUtilities.invokeAndWait( new Runnable( )
                {
                    public void run( )
                    {
                        for ( GlimpseCanvas canvas : canvasList )
                        {
                            canvas.paint( );
                        }
                    }
                } );
            }
            catch ( Exception e )
            {
                logWarning( logger, "Problem Repainting...", e );
            }
        }
    }
}
