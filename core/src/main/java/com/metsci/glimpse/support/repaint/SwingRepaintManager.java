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
