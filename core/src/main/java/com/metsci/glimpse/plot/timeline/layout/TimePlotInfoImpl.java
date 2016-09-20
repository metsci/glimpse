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
package com.metsci.glimpse.plot.timeline.layout;

import java.awt.Font;

import com.metsci.glimpse.axis.painter.NumericXYAxisPainter;
import com.metsci.glimpse.axis.tagged.TaggedAxis1D;
import com.metsci.glimpse.context.GlimpseTargetStack;
import com.metsci.glimpse.event.mouse.GlimpseMouseAllListener;
import com.metsci.glimpse.event.mouse.GlimpseMouseEvent;
import com.metsci.glimpse.layout.GlimpseAxisLayout2D;
import com.metsci.glimpse.layout.GlimpseLayout;
import com.metsci.glimpse.painter.base.GlimpsePainter;
import com.metsci.glimpse.painter.decoration.BackgroundPainter;
import com.metsci.glimpse.painter.decoration.BorderPainter;
import com.metsci.glimpse.painter.decoration.GridPainter;
import com.metsci.glimpse.painter.group.DelegatePainter;
import com.metsci.glimpse.painter.info.SimpleTextPainter;
import com.metsci.glimpse.painter.info.TooltipPainter;
import com.metsci.glimpse.plot.stacked.PlotInfo;
import com.metsci.glimpse.plot.stacked.PlotInfoWrapper;
import com.metsci.glimpse.plot.timeline.CollapsibleTimePlot2D;
import com.metsci.glimpse.plot.timeline.StackedTimePlot2D;
import com.metsci.glimpse.plot.timeline.listener.DataAxisMouseListener1D;
import com.metsci.glimpse.support.settings.LookAndFeel;
import com.metsci.glimpse.util.units.time.TimeStamp;
import com.metsci.glimpse.util.units.time.format.TimeStampFormat;
import com.metsci.glimpse.util.units.time.format.TimeStampFormatStandard;

/**
 * @see TimePlotInfo
 * @author ulman
 */
public class TimePlotInfoImpl extends PlotInfoWrapper implements TimePlotInfo
{
    protected GridPainter gridPainter;
    protected NumericXYAxisPainter axisPainter;
    protected SimpleTextPainter labelPainter;
    protected BorderPainter borderPainter;
    protected BorderPainter labelBorderPainter;
    protected BackgroundPainter backgroundPainter;
    protected DelegatePainter dataPainter;

    protected StackedTimePlot2D parent;

    protected DataAxisMouseListener1D listener;

    protected GlimpseAxisLayout2D plotLayout;
    protected GlimpseLayout labelLayout;

    protected TimeToolTipHandler timeToolTipHandler;

    //@formatter:off

    public TimePlotInfoImpl( final StackedTimePlot2D parent,
                         final PlotInfo child,
                         final GlimpseAxisLayout2D plotLayout,
                         final GlimpseLayout labelLayout,
                         final DataAxisMouseListener1D listener,
                         final GridPainter gridPainter,
                         final NumericXYAxisPainter axisPainter,
                         final SimpleTextPainter labelPainter,
                         final BorderPainter borderPainter,
                         final BorderPainter labelBorderPainter,
                         final BackgroundPainter backgroundPainter,
                         final DelegatePainter dataPainter )
    {
        super( child );

        this.parent = parent;
        this.plotLayout = plotLayout;
        this.labelLayout = labelLayout;
        this.listener = listener;
        this.gridPainter = gridPainter;
        this.axisPainter = axisPainter;
        this.labelPainter = labelPainter;
        this.borderPainter = borderPainter;
        this.labelBorderPainter = labelBorderPainter;
        this.backgroundPainter = backgroundPainter;
        this.dataPainter = dataPainter;

        this.timeToolTipHandler = new TimeToolTipHandler( )
        {
            TimeStampFormat format = new TimeStampFormatStandard( "%2H:%2m:%3S", "UTC" );

            @Override
            public void setToolTip( GlimpseMouseEvent e, TooltipPainter toolTipPainter )
            {
                double value = parent.isTimeAxisHorizontal( ) ? axisPainter.getLabelHandlerY( ).getAxisUnitConverter( ).toAxisUnits( e.getAxisCoordinatesY( ) )
                                                              : axisPainter.getLabelHandlerX( ).getAxisUnitConverter( ).toAxisUnits( e.getAxisCoordinatesX( ) );

                double time = parent.isTimeAxisHorizontal( ) ? e.getAxisCoordinatesX( ) : e.getAxisCoordinatesY( );
                TimeStamp timestamp = parent.getEpoch( ).toTimeStamp( time );

                toolTipPainter.setIcon( null );
                toolTipPainter.setText( String.format( "%s\nTime: %s\nData: %.3f", labelPainter.getText( ), timestamp.toString( format ), value ) );
            }
        };

        this.plotLayout.addGlimpseMouseAllListener( new GlimpseMouseAllListener( )
        {
            @Override
            public void mouseMoved( GlimpseMouseEvent e )
            {
                if ( timeToolTipHandler != null ) timeToolTipHandler.setToolTip( e, parent.getTooltipPainter( ) );
            }

            @Override
            public void mouseExited( GlimpseMouseEvent event )
            {
                if ( timeToolTipHandler != null )
                {
                    TooltipPainter toolTipPainter = parent.getTooltipPainter( );
                    toolTipPainter.setIcon( null );
                    toolTipPainter.setText( null );
                }
            }

            @Override
            public void mouseEntered( GlimpseMouseEvent event )
            {
            }

            @Override
            public void mousePressed( GlimpseMouseEvent event )
            {
            }

            @Override
            public void mouseReleased( GlimpseMouseEvent event )
            {
            }

            @Override
            public void mouseWheelMoved( GlimpseMouseEvent e )
            {
            }
        });
    }
    //@formatter:on

    @Override
    public void setTimeToolTipHandler( TimeToolTipHandler toolTipHandler )
    {
        this.timeToolTipHandler = toolTipHandler;
    }

    @Override
    public DataAxisMouseListener1D getDataAxisMouseListener( )
    {
        return listener;
    }

    @Override
    public void setDefaultEventBorderColor( float[] rgba )
    {
        this.borderPainter.setColor( rgba );
    }

    @Override
    public void setBorderWidth( float width )
    {
        this.borderPainter.setLineWidth( width );
    }

    @Override
    public void setLabelBorderColor( float[] rgba )
    {
        this.labelBorderPainter.setColor( rgba );
    }

    @Override
    public void setLabelBorderWidth( float width )
    {
        this.labelBorderPainter.setLineWidth( width );
    }

    @Override
    public void setLabelText( String text )
    {
        this.labelPainter.setText( text );
    }

    @Override
    public void setLabelColor( float[] rgba )
    {
        this.labelPainter.setColor( rgba );
    }

    @Override
    public void setAxisColor( float[] rgba )
    {
        this.axisPainter.setLineColor( rgba );
        this.axisPainter.setTextColor( rgba );
    }

    @Override
    public void setAxisFont( Font font )
    {
        this.axisPainter.setFont( font );
    }

    @Override
    public void setBackgroundColor( float[] rgba )
    {
        this.backgroundPainter.setColor( rgba );
    }

    @Override
    public GlimpseAxisLayout2D getLayout( )
    {
        return plotLayout;
    }

    @Override
    public void addLayout( GlimpseAxisLayout2D childLayout )
    {
        plotLayout.addLayout( childLayout );
    }

    @Override
    public GlimpseAxisLayout2D getBaseLayout( )
    {
        return info.getBaseLayout( );
    }

    @Override
    public GlimpseLayout getLabelLayout( )
    {
        return labelLayout;
    }

    @Override
    public BackgroundPainter getBackgroundPainter( )
    {
        return this.backgroundPainter;
    }

    @Override
    public GridPainter getGridPainter( )
    {
        return gridPainter;
    }

    @Override
    public NumericXYAxisPainter getAxisPainter( )
    {
        return axisPainter;
    }

    @Override
    public SimpleTextPainter getLabelPainter( )
    {
        return labelPainter;
    }

    @Override
    public BorderPainter getBorderPainter( )
    {
        return borderPainter;
    }

    @Override
    public BorderPainter getLabelBorderPainter( )
    {
        return labelBorderPainter;
    }

    @Override
    public StackedTimePlot2D getStackedTimePlot( )
    {
        return parent;
    }

    @Override
    public DelegatePainter getDataPainter( )
    {
        return dataPainter;
    }

    @Override
    public void addPainter( GlimpsePainter painter )
    {
        this.dataPainter.addPainter( painter );
    }

    @Override
    public void removePainter( GlimpsePainter painter )
    {
        this.dataPainter.removePainter( painter );
    }

    @Override
    public TaggedAxis1D getCommonAxis( GlimpseTargetStack stack )
    {
        return ( TaggedAxis1D ) parent.getCommonAxis( getLayout( ).getAxis( stack ) );
    }

    @Override
    public TaggedAxis1D getCommonAxis( )
    {
        return ( TaggedAxis1D ) super.getCommonAxis( );
    }

    @Override
    public void setLookAndFeel( LookAndFeel laf )
    {
        super.setLookAndFeel( laf );
        labelLayout.setLookAndFeel( laf );
    }

    @Override
    public void updateLayout( int index )
    {
        super.updateLayout( index );

        StackedTimePlot2D parent = getStackedTimePlot( );

        int labelSize;

        if ( parent instanceof CollapsibleTimePlot2D )
        {
            CollapsibleTimePlot2D collapsible = ( ( CollapsibleTimePlot2D ) parent );
            labelSize = ( collapsible.getMaxLevel( ) - getIndentLevel( ) ) * collapsible.getIndentSize( );
        }
        else
        {
            labelSize = parent.isShowLabels( ) ? parent.getLabelSize( ) : 0;
        }

        if ( parent.isTimeAxisHorizontal( ) )
        {
            plotLayout.setLayoutData( "cell 1 0 1 1, push, grow" );
            labelLayout.setLayoutData( String.format( "cell 0 0, pushy, growy, width %d!", labelSize ) );
            labelLayout.setVisible( parent.isShowLabels( ) );
        }
        else
        {
            plotLayout.setLayoutData( "cell 0 1 1 1, push, grow" );
            labelLayout.setLayoutData( String.format( "cell 0 0, pushx, growx, height %d!", labelSize ) );
            labelLayout.setVisible( parent.isShowLabels( ) );
        }
    }
}
