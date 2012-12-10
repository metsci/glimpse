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
package com.metsci.glimpse.plot.timeline.listener;

import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.axis.listener.mouse.AxisMouseListener1D;
import com.metsci.glimpse.axis.listener.mouse.AxisMouseListener2D;
import com.metsci.glimpse.event.mouse.GlimpseMouseEvent;
import com.metsci.glimpse.event.mouse.ModifierKey;
import com.metsci.glimpse.layout.GlimpseAxisLayout2D;
import com.metsci.glimpse.plot.StackedPlot2D.PlotInfo;
import com.metsci.glimpse.plot.timeline.StackedTimePlot2D;

public class TimelineMouseListener2D extends AxisMouseListener2D
{
    protected static final int ORTHOGONAL_AXIS_SIZE = 28;

    protected StackedTimePlot2D plot;
    protected AxisMouseListener1D delegateListener;
    protected PlotInfo info;

    protected boolean axisSelected;
    protected boolean timeIsX;
    
    protected int orthogonalAxisSize = ORTHOGONAL_AXIS_SIZE;

    public TimelineMouseListener2D( StackedTimePlot2D plot, PlotInfo info, AxisMouseListener1D timeMouseListener )
    {
        this.info = info;
        this.plot = plot;
        this.timeIsX = plot.isTimeAxisHorizontal( );
        this.delegateListener = timeMouseListener;

        // we handle these ourselves
        if ( this.timeIsX )
        {
            setAllowSelectionZoomX( false );
        }
        else
        {
            setAllowSelectionZoomY( false );
        }

        setAllowSelectionLock( false );
    }
    
    public TimelineMouseListener2D( StackedTimePlot2D plot, PlotInfo info )
    {
        this( plot, info, new TimelineMouseListener1D( plot ) );
    }

    public void setOrthogonalAxisSize( int size )
    {
        this.orthogonalAxisSize = size;
    }
    
    @Override
    public void mouseMoved( GlimpseMouseEvent e )
    {
        GlimpseAxisLayout2D layout = getAxisLayout( e );
        if ( layout == null ) return;

        Axis2D axis = layout.getAxis( e.getTargetStack( ) );

        Axis1D axisX = axis.getAxisX( );
        Axis1D axisY = axis.getAxisY( );

        if ( e.isAnyButtonDown( ) )
        {
            if ( this.axisSelected )
            {
                if ( this.timeIsX )
                {
                    this.mouseMoved( e, axisY, false );
                }
                else
                {
                    this.mouseMoved( e, axisX, true );
                }
            }
            else
            {
                if ( this.timeIsX )
                {
                    this.delegateListener.mouseMoved( e, axisX, true );
                }
                else
                {
                    this.delegateListener.mouseMoved( e, axisY, false );
                }
            }
        }
        else
        {
            if ( this.timeIsX )
            {
                this.mouseMoved( e, axisY, false );
                this.delegateListener.mouseMoved( e, axisX, true );
            }
            else
            {
                this.mouseMoved( e, axisX, true );
                this.delegateListener.mouseMoved( e, axisY, false );
            }
        }

        this.applyAndUpdate( axisX, axisY );
    }

    @Override
    public void mousePressed( GlimpseMouseEvent e )
    {
        GlimpseAxisLayout2D layout = getAxisLayout( e );
        if ( layout == null ) return;

        plot.setSelectedPlot( info );

        Axis2D axis = layout.getAxis( e.getTargetStack( ) );

        Axis1D axisX = axis.getAxisX( );
        Axis1D axisY = axis.getAxisY( );

        if ( timeIsX && ( e.getX( ) < orthogonalAxisSize || e.isKeyDown( ModifierKey.Shift ) ) )
        {
            this.axisSelected = true;
            this.mousePressed( e, axisY, false );
        }
        else if ( !timeIsX && ( e.getY( ) < orthogonalAxisSize || e.isKeyDown( ModifierKey.Shift ) ) )
        {
            this.axisSelected = true;
            this.mousePressed( e, axisX, true );
        }
        else
        {
            this.axisSelected = false;

            if ( timeIsX )
            {
                this.delegateListener.mousePressed( e, axisX, true );
            }
            else
            {
                this.delegateListener.mousePressed( e, axisY, false );
            }
        }

        this.applyAndUpdate( axisX, axisY );
    }

    @Override
    public void mouseReleased( GlimpseMouseEvent e )
    {
        GlimpseAxisLayout2D layout = getAxisLayout( e );
        if ( layout == null ) return;

        Axis2D axis = layout.getAxis( e.getTargetStack( ) );

        Axis1D axisX = axis.getAxisX( );
        Axis1D axisY = axis.getAxisY( );

        if ( timeIsX )
        {
            this.mouseReleased( e, axisY, false );
            this.delegateListener.mouseReleased( e, axisX, true );
        }
        else
        {
            this.mouseReleased( e, axisX, true );
            this.delegateListener.mouseReleased( e, axisY, false );
        }

        this.applyAndUpdate( axisX, axisY );
    }

    @Override
    public void mouseWheelMoved( GlimpseMouseEvent e )
    {
        GlimpseAxisLayout2D layout = getAxisLayout( e );
        if ( layout == null ) return;

        Axis2D axis = layout.getAxis( e.getTargetStack( ) );

        Axis1D axisX = axis.getAxisX( );
        Axis1D axisY = axis.getAxisY( );

        if ( timeIsX && ( e.getX( ) < orthogonalAxisSize || e.isKeyDown( ModifierKey.Shift ) ) )
        {
            this.mouseWheelMoved( e, axisY, false );
        }
        else if ( !timeIsX && ( e.getY( ) < orthogonalAxisSize || e.isKeyDown( ModifierKey.Shift ) ) )
        {
            this.mouseWheelMoved( e, axisX, true );
        }
        else
        {
            this.delegateListener.mouseWheelMoved( e );
        }

        this.applyAndUpdate( axisX, axisY );
    }

    public void setAllowSelectionLockX( boolean b )
    {
        if ( this.timeIsX )
        {
            this.delegateListener.setAllowSelectionLock( b );
        }
        else
        {
            this.allowSelectionLock = b;
        }
    }

    public void setAllowSelectionZoomX( boolean b )
    {
        if ( this.timeIsX )
        {
            this.delegateListener.setAllowSelectionZoom( b );
        }
        else
        {
            this.allowSelectionZoom = b;
        }
    }

    public void setAllowZoomX( boolean b )
    {
        if ( this.timeIsX )
        {
            this.delegateListener.setAllowZoom( b );
        }
        else
        {
            this.allowZoom = b;
        }
    }

    public void setAllowPanX( boolean b )
    {
        if ( this.timeIsX )
        {
            this.delegateListener.setAllowPan( b );
        }
        else
        {
            this.allowPan = b;
        }
    }

    public void setAllowSelectionLockY( boolean b )
    {
        if ( !this.timeIsX )
        {
            this.delegateListener.setAllowSelectionLock( b );
        }
        else
        {
            this.allowSelectionLock = b;
        }
    }

    public void setAllowSelectionZoomY( boolean b )
    {
        if ( !this.timeIsX )
        {
            this.delegateListener.setAllowSelectionZoom( b );
        }
        else
        {
            this.allowSelectionZoom = b;
        }
    }

    public void setAllowZoomY( boolean b )
    {
        if ( !this.timeIsX )
        {
            this.delegateListener.setAllowZoom( b );
        }
        else
        {
            this.allowZoom = b;
        }
    }

    public void setAllowPanY( boolean b )
    {
        if ( !this.timeIsX )
        {
            this.delegateListener.setAllowPan( b );
        }
        else
        {
            this.allowPan = b;
        }
    }

    @Override
    public void setAllowSelectionLock( boolean b )
    {
        this.delegateListener.setAllowSelectionLock( b );
        this.allowSelectionLock = b;
    }

    @Override
    public void setAllowSelectionZoom( boolean b )
    {
        this.delegateListener.setAllowSelectionZoom( b );
        this.allowSelectionZoom = b;
    }

    @Override
    public void setAllowZoom( boolean b )
    {
        this.delegateListener.setAllowZoom( b );
        this.allowZoom = b;
    }

    @Override
    public void setAllowPan( boolean b )
    {
        this.delegateListener.setAllowPan( b );
        this.allowPan = b;
    }

    /**
     * TimelineMouseListener2D delegates its timeline update behavior to
     * the provided AxisMouseListener1D.
     */
    public void setTimelineMouseListener1D( AxisMouseListener1D mouseListener )
    {
        this.delegateListener = mouseListener;
    }

    public AxisMouseListener1D getTimeMouseListener1D( )
    {
        return this.delegateListener;
    }
}
