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
package com.metsci.glimpse.plot.timeline.layout;

import java.awt.Font;

import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.axis.painter.NumericXYAxisPainter;
import com.metsci.glimpse.axis.tagged.TaggedAxis1D;
import com.metsci.glimpse.context.GlimpseTargetStack;
import com.metsci.glimpse.layout.GlimpseAxisLayout2D;
import com.metsci.glimpse.painter.base.GlimpsePainter;
import com.metsci.glimpse.painter.decoration.BorderPainter;
import com.metsci.glimpse.painter.decoration.GridPainter;
import com.metsci.glimpse.painter.group.DelegatePainter;
import com.metsci.glimpse.painter.info.SimpleTextPainter;
import com.metsci.glimpse.plot.StackedPlot2D;
import com.metsci.glimpse.plot.StackedPlot2D.PlotInfo;
import com.metsci.glimpse.plot.timeline.StackedTimePlot2D;

/**
 *
 * @author ulman
 */
public class TimePlotInfo implements PlotInfo
{
    protected GridPainter gridPainter;
    protected NumericXYAxisPainter axisPainter;
    protected SimpleTextPainter labelPainter;
    protected BorderPainter borderPainter;

    protected DelegatePainter dataPainter;
    
    protected StackedTimePlot2D parent;
    protected PlotInfo child;

    //@formatter:off
    public TimePlotInfo( StackedTimePlot2D parent, PlotInfo child,
                         GridPainter gridPainter, NumericXYAxisPainter axisPainter,
                         SimpleTextPainter labelPainter, BorderPainter borderPainter,
                         DelegatePainter dataPainter )
    {
        this.parent = parent;
        this.child = child;
        this.gridPainter = gridPainter;
        this.axisPainter = axisPainter;
        this.labelPainter = labelPainter;
        this.borderPainter = borderPainter;
        this.dataPainter = dataPainter;
    }
    //@formatter:on

    public void setBorderColor( float[] rgba )
    {
        this.borderPainter.setColor( rgba );
    }

    public void setBorderWidth( float width )
    {
        this.borderPainter.setLineWidth( width );
    }

    public void setLabelText( String text )
    {
        this.labelPainter.setText( text );
    }

    public void setLabelColor( float[] rgba )
    {
        this.labelPainter.setColor( rgba );
    }

    public void setAxisColor( float[] rgba )
    {
        this.axisPainter.setLineColor( rgba );
        this.axisPainter.setTextColor( rgba );
    }

    public void setAxisFont( Font font )
    {
        this.axisPainter.setFont( font );
    }

    public GridPainter getGridPainter( )
    {
        return gridPainter;
    }

    public NumericXYAxisPainter getAxisPainter( )
    {
        return axisPainter;
    }

    public SimpleTextPainter getLabelPainter( )
    {
        return labelPainter;
    }

    public BorderPainter getBorderPainter( )
    {
        return borderPainter;
    }

    public StackedTimePlot2D getStackedTimePlot( )
    {
        return parent;
    }
   
    public DelegatePainter getDataPainter( )
    {
        return dataPainter;
    }
    
    public void addPainter( GlimpsePainter painter )
    {
        this.dataPainter.addPainter( painter );
    }
    
    public void removePainter( GlimpsePainter painter )
    {
        this.dataPainter.removePainter( painter );
    }

    @Override
    public StackedPlot2D getStackedPlot( )
    {
        return child.getStackedPlot( );
    }

    @Override
    public String getId( )
    {
        return child.getId( );
    }

    @Override
    public int getOrder( )
    {
        return child.getOrder( );
    }

    @Override
    public int getSize( )
    {
        return child.getSize( );
    }

    @Override
    public void setOrder( int order )
    {
        child.setOrder( order );
    }

    @Override
    public void setSize( int size )
    {
        child.setSize( size );
    }

    @Override
    public GlimpseAxisLayout2D getLayout( )
    {
        return child.getLayout( );
    }

    @Override
    public Axis1D getOrthogonalAxis( GlimpseTargetStack stack )
    {
        return child.getOrthogonalAxis( stack );
    }

    @Override
    public Axis1D getOrthogonalAxis( )
    {
        return child.getOrthogonalAxis( );
    }

    @Override
    public void addLayout( GlimpseAxisLayout2D childLayout )
    {
        child.addLayout( childLayout );
    }

    @Override
    public TaggedAxis1D getCommonAxis( GlimpseTargetStack stack )
    {
        return (TaggedAxis1D) child.getCommonAxis( stack );
    }

    @Override
    public TaggedAxis1D getCommonAxis( )
    {
        return (TaggedAxis1D) child.getCommonAxis( );
    }
}
