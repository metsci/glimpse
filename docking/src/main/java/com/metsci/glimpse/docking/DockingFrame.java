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
package com.metsci.glimpse.docking;

import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;

import javax.swing.JFrame;

@SuppressWarnings("serial")
public class DockingFrame extends JFrame
{

    public final MultiSplitPane docker;

    protected Rectangle normalBounds;


    public DockingFrame( MultiSplitPane docker )
    {
        this.docker = docker;
        setContentPane( docker );

        this.normalBounds = getBounds( );
        addWindowStateListener( new WindowStateListener( )
        {
            @Override
            public void windowStateChanged( WindowEvent ev ) { updateNormalBounds( ); }
        } );
        addComponentListener( new ComponentAdapter( )
        {
            @Override
            public void componentMoved( ComponentEvent ev ) { updateNormalBounds( ); }
            @Override
            public void componentResized( ComponentEvent ev ) { updateNormalBounds( ); }
        } );
    }

    protected void updateNormalBounds( )
    {
        if ( getExtendedState( ) == NORMAL )
        {
            this.normalBounds = getBounds( );
        }
    }

    public void setNormalBounds( int x, int y, int width, int height )
    {
        this.normalBounds = new Rectangle( x, y, width, height );
    }

    public Rectangle getNormalBounds( )
    {
        return new Rectangle( normalBounds );
    }

}
