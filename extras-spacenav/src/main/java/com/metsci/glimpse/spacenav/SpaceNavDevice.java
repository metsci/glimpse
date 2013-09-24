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

import java.util.logging.Logger;

import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;

/**
 * Represents the basic functionality for recognizing the device and getting its
 * state. It requires a little cleanup.
 */
public class SpaceNavDevice
{
    public static final int NUM_BUTTONS = 2;

    private static final Logger logger = Logger.getLogger( SpaceNavDevice.class.getName( ) );
    
    private int xAxisIdx, yAxisIdx, zAxisIdx, rxAxisIdx, ryAxisIdx, rzAxisIdx;
    private int buttonsIdx[];

    private Controller controller;
    private Component[] components;
    private boolean terminated = false;

    private static SpaceNavDevice instance;
    private static boolean instanceInUse = false;

    private SpaceNavDevice( Controller controller )
    {
        this.controller = controller;
        findCompIndices( controller );
    }

    public synchronized static SpaceNavDevice getDevice( ) throws SpaceNavException
    {
        JInputDriver.init( );

        if ( instance == null )
        {
            ControllerEnvironment ce = ControllerEnvironment.getDefaultEnvironment( );
            Controller[] cs = ce.getControllers( );

            Controller c = findSpaceNavigator( cs );
            if ( c != null )
            {
                // not quite right, needs to run "findCompIndices" fist to be
                // sure
                instance = new SpaceNavDevice( c );
                instanceInUse = false;
            }
            else
            {
                throw new SpaceNavException( "Space Navigator not found." );
            }
        }
        else if ( instanceInUse )
        {
            throw new SpaceNavException( "Device already in use." );
        }

        instanceInUse = true;
        return instance;
    }

    public synchronized static void releaseDevice( SpaceNavDevice device )
    {
        if ( device == instance )
        {
            instance.terminate( );
            instanceInUse = false;
            instance = null;
        }
    }

    public synchronized SpaceNavState poll( ) throws SpaceNavException
    {
        if ( terminated ) throw new SpaceNavException( "Device instance terminated." );

        long systemTimeMillis = System.currentTimeMillis( );
        controller.poll( );

        float xTranslation = components[xAxisIdx].getPollData( );
        float yTranslation = components[yAxisIdx].getPollData( );
        float zTranslation = components[zAxisIdx].getPollData( );

        float xRotation = components[rxAxisIdx].getPollData( );
        float yRotation = components[ryAxisIdx].getPollData( );
        float zRotation = components[rzAxisIdx].getPollData( );

        return new SpaceNavState( systemTimeMillis, xTranslation, yTranslation, zTranslation, xRotation, yRotation, zRotation );
    }

    private synchronized void terminate( )
    {
        terminated = true;
    }

    private static Controller findSpaceNavigator( Controller[] cs )
    {
        for ( int i = 0; i < cs.length; i++ )
        {
            logger.info( "Controller: " + cs[i] );

            if ( cs[i].getType( ) == Controller.Type.STICK || cs[i].getName( ).equals( "3Dconnexion SpaceNavigator" ) ) return cs[i];
        }

        return null;
    }

    private void findCompIndices( Controller controller )
    {
        components = controller.getComponents( );
        if ( components.length == 0 )
        {
            logger.info( "No Components found" );
            System.exit( 0 );
        }
        else
            logger.info( "Num. Components: " + components.length );

        // get the indices for the axes of the analog sticks: (x,y) and (z,rz)
        xAxisIdx = findCompIndex( components, Component.Identifier.Axis.X, "x" );
        yAxisIdx = findCompIndex( components, Component.Identifier.Axis.Y, "y" );
        zAxisIdx = findCompIndex( components, Component.Identifier.Axis.Z, "z" );

        rxAxisIdx = findCompIndex( components, Component.Identifier.Axis.RY, "rx" );
        ryAxisIdx = findCompIndex( components, Component.Identifier.Axis.RX, "ry" );
        rzAxisIdx = findCompIndex( components, Component.Identifier.Axis.RZ, "rz" );

        if ( rxAxisIdx == -1 ) rxAxisIdx = 5;
        if ( ryAxisIdx == -1 ) ryAxisIdx = 6;
        if ( rzAxisIdx == -1 ) rzAxisIdx = 7;

        findButtons( components );
    }

    private int findCompIndex( Component[] comps, Component.Identifier id, String nm )
    {
        Component c;
        for ( int i = 0; i < comps.length; i++ )
        {
            c = comps[i];
            if ( ( c.getIdentifier( ) == id ) )
            {
                logger.info( "Found " + c.getName( ) + "; index: " + i );
                return i;
            }
        }

        logger.info( "No " + nm + " component found" );
        return -1;
    }

    /**
     * Search through comps[] for NUM_BUTTONS buttons, storing their indices in
     * buttonsIdx[]. Ignore excessive buttons. If there aren't enough buttons,
     * then fill the empty spots in buttonsIdx[] with -1's.
     */
    private void findButtons( Component[] comps )
    {
        buttonsIdx = new int[NUM_BUTTONS];
        int numButtons = 0;
        Component c;

        for ( int i = 0; i < comps.length; i++ )
        {
            c = comps[i];
            if ( isButton( c ) )
            { // deal with a button
                if ( numButtons == NUM_BUTTONS ) // already enough buttons
                    logger.info( "Found an extra button; index: " + i + ". Ignoring it" );
                else
                {
                    buttonsIdx[numButtons] = i; // store button index
                    logger.info( "Found " + c.getName( ) + "; index: " + i );
                    numButtons++;
                }
            }
        }

        // fill empty spots in buttonsIdx[] with -1's
        if ( numButtons < NUM_BUTTONS )
        {
            logger.info( "Too few buttons (" + numButtons + "); expecting " + NUM_BUTTONS );
            while ( numButtons < NUM_BUTTONS )
            {
                buttonsIdx[numButtons] = -1;
                numButtons++;
            }
        }
    } // end of findButtons()

    /**
     * Return true if the component is a digital/absolute button, and its
     * identifier name ends with "Button" (i.e. the identifier class is
     * Component.Identifier.Button).
     */
    private boolean isButton( Component c )
    {
        if ( !c.isAnalog( ) && !c.isRelative( ) )
        { // digital and absolute
            String className = c.getIdentifier( ).getClass( ).getName( );
            if ( className.endsWith( "Button" ) ) return true;
        }
        return false;
    }

    /**
     * Return all the buttons in a single array. Each button value is a boolean.
     */
    protected boolean[] getButtons( )
    {
        boolean[] buttons = new boolean[NUM_BUTTONS];
        float value;
        for ( int i = 0; i < NUM_BUTTONS; i++ )
        {
            value = components[buttonsIdx[i]].getPollData( );
            buttons[i] = ( ( value == 0.0f ) ? false : true );
        }
        return buttons;
    }

    /**
     * Return the button value (a boolean) for button number 'pos'. pos is in
     * the range 1-NUM_BUTTONS to match the game pad button labels.
     */
    protected boolean isButtonPressed( int pos )
    {
        if ( ( pos > NUM_BUTTONS ) )
        {
            logger.info( "Button position out of range (1-" + NUM_BUTTONS + "): " + pos );
            return false;
        }

        // no button found at that position
        if ( buttonsIdx[pos - 1] == -1 ) return false;

        // array range is 0-NUM_BUTTONS-1
        float value = components[buttonsIdx[pos - 1]].getPollData( );

        return ( ( value == 0.0f ) ? false : true );
    }
}
