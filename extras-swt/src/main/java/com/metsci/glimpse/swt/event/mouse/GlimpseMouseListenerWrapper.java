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
package com.metsci.glimpse.swt.event.mouse;

import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseTrackListener;

import com.metsci.glimpse.event.mouse.GlimpseMouseListener;

import static com.metsci.glimpse.swt.event.mouse.GlimpseMouseWrapper.*;

public class GlimpseMouseListenerWrapper implements MouseListener, MouseTrackListener
{
    protected GlimpseMouseListener listener;

    public GlimpseMouseListenerWrapper( GlimpseMouseListener listener )
    {
        this.listener = listener;
    }

    @Override
    public void mouseDoubleClick( MouseEvent event )
    {
        // not handled by GlimpseMouseListener
    }

    @Override
    public void mouseDown( MouseEvent event )
    {
        listener.mousePressed( fromMouseEvent( event ) );
    }

    @Override
    public void mouseUp( MouseEvent event )
    {
        listener.mouseReleased( fromMouseEvent( event ) );
    }

    @Override
    public void mouseEnter( MouseEvent event )
    {
        listener.mouseEntered( fromMouseEvent( event ) );
    }

    @Override
    public void mouseExit( MouseEvent event )
    {
        listener.mouseExited( fromMouseEvent( event ) );
    }

    @Override
    public void mouseHover( MouseEvent event )
    {
        // not handled by GlimpseMouseListener
    }
}
