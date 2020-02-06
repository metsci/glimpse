/*
 * Copyright (c) 2019, Metron, Inc.
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
package com.metsci.glimpse.core.plot.timeline.listener;

import static java.awt.Cursor.E_RESIZE_CURSOR;
import static java.awt.Cursor.S_RESIZE_CURSOR;
import static java.awt.Cursor.getDefaultCursor;
import static java.awt.Cursor.getPredefinedCursor;
import static java.lang.Math.abs;

import java.awt.Component;
import java.awt.Cursor;
import java.util.List;

import javax.swing.JButton;

import com.metsci.glimpse.core.canvas.NewtSwingGlimpseCanvas;
import com.metsci.glimpse.core.context.GlimpseTarget;
import com.metsci.glimpse.core.event.mouse.GlimpseMouseAllAdapter;
import com.metsci.glimpse.core.event.mouse.GlimpseMouseEvent;
import com.metsci.glimpse.core.event.mouse.MouseButton;
import com.metsci.glimpse.core.plot.timeline.StackedTimePlot2D;
import com.metsci.glimpse.core.support.swing.NewtSwingEDTGlimpseCanvas;

/**
 * Allows the user to draw and resize the label area on a {@link StackedTimePlot2D}.
 *
 * <p>
 * Attach the handler to the full overlayout layout:
 * <pre>
 * plot.getFullOverlayLayout( ).addGlimpseMouseAllListener( new LabelResizeMouseHandler( plot ) );
 * </pre>
 * </p>
 *
 * @author borkholder
 */
public class LabelResizeMouseHandler extends GlimpseMouseAllAdapter
{
    protected final StackedTimePlot2D plot;

    protected boolean isDragging;
    protected Component component;
    protected Cursor resizeCursor;
    protected Cursor defaultCursor;

    public LabelResizeMouseHandler( StackedTimePlot2D plot )
    {
        this.plot = plot;
        component = new JButton( );
        isDragging = false;
        resizeCursor = plot.isTimeAxisHorizontal( ) ? getPredefinedCursor( E_RESIZE_CURSOR ) : getPredefinedCursor( S_RESIZE_CURSOR );
        defaultCursor = getDefaultCursor( );
    }

    @Override
    public void mouseEntered( GlimpseMouseEvent e )
    {
        List<GlimpseTarget> targetList = e.getTargetStack( ).getTargetList( );
        GlimpseTarget bottom = targetList.get( targetList.size( ) - 1 );
        if ( bottom instanceof NewtSwingGlimpseCanvas )
        {
            component = ( ( NewtSwingGlimpseCanvas ) bottom ).getCanvas( );
        }
        else if ( bottom instanceof NewtSwingEDTGlimpseCanvas )
        {
            component = ( ( NewtSwingEDTGlimpseCanvas ) bottom ).getCanvas( );
        }
    }

    @Override
    public void mousePressed( GlimpseMouseEvent e )
    {
        if ( isInDragArea( e ) && e.isButtonDown( MouseButton.Button1 ) )
        {
            isDragging = true;
            e.setHandled( true );
        }
        else
        {
            isDragging = false;
        }
    }

    @Override
    public void mouseReleased( GlimpseMouseEvent event )
    {
        isDragging = false;
    }

    boolean isInDragArea( GlimpseMouseEvent e )
    {
        if ( plot.isTimeAxisHorizontal( ) )
        {
            return abs( e.getX( ) - plot.getLabelSize( ) ) < 10;
        }
        else
        {
            return abs( e.getY( ) - plot.getLabelSize( ) ) < 10;
        }
    }

    @Override
    public void mouseMoved( GlimpseMouseEvent e )
    {
        if ( isInDragArea( e ) )
        {
            component.setCursor( resizeCursor );
        }
        else
        {
            component.setCursor( defaultCursor );
        }

        if ( isDragging )
        {
            if ( plot.isTimeAxisHorizontal( ) )
            {
                plot.setLabelSize( e.getX( ) );
            }
            else
            {
                plot.setLabelSize( e.getY( ) );
            }
            e.setHandled( true );
        }
    }
}