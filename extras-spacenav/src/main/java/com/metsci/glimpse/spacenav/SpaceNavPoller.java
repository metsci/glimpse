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
package com.metsci.glimpse.spacenav;

import static com.metsci.glimpse.util.logging.LoggerUtils.*;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

public class SpaceNavPoller
{
    public static final long POLL_INTERVAL = 20; // msec

    protected static final Logger logger = Logger.getLogger( SpaceNavPoller.class.getName( ) );

    private List<SpaceNavListener> listeners;

    private Timer timer;
    private TimerTask task;
    private SpaceNavDevice device;
    private boolean terminated = false;

    private SpaceNavPoller( SpaceNavDevice device, long period )
    {
        this.device = device;
        this.listeners = new CopyOnWriteArrayList<SpaceNavListener>( );

        task = new TimerTask( )
        {
            @Override
            public void run( )
            {
                if ( !terminated )
                {
                    try
                    {
                        SpaceNavState state = SpaceNavPoller.this.device.poll( );

                        try
                        {
                            for ( SpaceNavListener listener : listeners )
                            {
                                listener.update( state );
                            }
                        }
                        catch ( Exception e )
                        {
                            // add logger statement
                            terminate( );
                            logWarning( logger, "SpaceNavPoller encountered error.", e );
                        }
                    }
                    catch ( SpaceNavException e )
                    {
                        // add logger statement
                        terminate( );
                        logWarning( logger, "SpaceNavPoller encountered error.", e );
                    }
                }
            }
        };

        timer = new Timer( );
        timer.scheduleAtFixedRate( task, 0, period );
    }

    public static SpaceNavPoller create( ) throws SpaceNavException
    {
        return new SpaceNavPoller( SpaceNavDevice.getDevice( ), POLL_INTERVAL );
    }

    public void terminate( )
    {
        terminated = true;

        listeners.clear( );
        listeners = null;

        task.cancel( );
        task = null;

        timer.cancel( );
        timer = null;

        SpaceNavDevice.releaseDevice( device );
    }

    public void addListener( SpaceNavListener listener )
    {
        listeners.add( listener );
    }

    public void clearListeners( )
    {
        listeners.clear( );
    }
}
