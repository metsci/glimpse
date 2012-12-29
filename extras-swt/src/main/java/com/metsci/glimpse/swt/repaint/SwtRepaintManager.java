package com.metsci.glimpse.swt.repaint;

import static com.metsci.glimpse.util.logging.LoggerUtils.logWarning;

import org.eclipse.swt.widgets.Display;

import com.metsci.glimpse.canvas.GlimpseCanvas;
import com.metsci.glimpse.support.repaint.RepaintManager;

public class SwtRepaintManager extends RepaintManager
{
    public static SwtRepaintManager newRepaintManager( GlimpseCanvas canvas )
    {
        SwtRepaintManager manager = new SwtRepaintManager( );
        manager.addGlimpseCanvas( canvas );
        manager.start( );
        return manager;
    }

    public void asyncExec( Runnable runnable )
    {
        Display.getDefault( ).asyncExec( runnable );
    }

    public void syncExec( Runnable runnable )
    {
        Display.getDefault( ).syncExec( runnable );
    }

    public boolean checkThread( )
    {
        return Thread.currentThread( ).equals( Display.getDefault( ).getSyncThread( ) );
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

                Display.getDefault( ).syncExec( new Runnable( )
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