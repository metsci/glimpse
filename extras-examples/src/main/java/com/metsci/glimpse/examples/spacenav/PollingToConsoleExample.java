/*
 * Copyright (c) 2016, Metron, Inc.
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
package com.metsci.glimpse.examples.spacenav;

import java.util.Timer;
import java.util.TimerTask;

import com.metsci.glimpse.spacenav.JInputDriver;
import com.metsci.glimpse.spacenav.SpaceNavDevice;
import com.metsci.glimpse.spacenav.SpaceNavException;
import com.metsci.glimpse.spacenav.SpaceNavState;

/**
 * XXX: Needs cleanup.
 *
 * @author osborn
 */
public class PollingToConsoleExample
{
    public static void main( String[] args ) throws SpaceNavException
    {
        // Like with Jogular: load native libs for event capture
        JInputDriver.init( );

        // Initialize a polling object that can retrieve device state
        final SpaceNavDevice device = SpaceNavDevice.getDevice( );

        // Create a small task to poll and print the state of the device
        TimerTask task = new TimerTask( )
        {
            @Override
            public void run( )
            {
                SpaceNavState state = null;
                try
                {
                    state = device.poll( );
                }
                catch ( SpaceNavException e )
                {
                    e.printStackTrace( );
                    return;
                }

                float tx = state.xTranslation;
                float ty = state.yTranslation;
                float tz = state.zTranslation;

                float rx = state.xRotation;
                float ry = state.yRotation;
                float rz = state.zRotation;

                // Interestingly, the range of the values depends on the
                // polling freq. below
                String stateString = String.format( "xyz-rotation: %5d %5d %5d        xyz-translation: %5d %5d %5d", ( int ) rx, ( int ) ry, ( int ) rz, ( int ) tx, ( int ) ty, ( int ) tz );

                System.out.println( stateString );
            }
        };

        // Setup a recurring event
        Timer timer = new Timer( );
        timer.scheduleAtFixedRate( task, 0, 40 );
    }
}
