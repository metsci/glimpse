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

import java.util.List;

import com.metsci.glimpse.plot.StackedPlot2D.LayoutDataUpdater;
import com.metsci.glimpse.plot.StackedPlot2D.PlotInfo;
import com.metsci.glimpse.plot.timeline.StackedTimePlot2D;

public class TimeLayoutDataUpdater implements LayoutDataUpdater
{
    protected TimePlotInfo info;

    public TimeLayoutDataUpdater( TimePlotInfo info )
    {
        this.info = info;
    }

    @Override
    public int getSizePixels( List<PlotInfo> list, int index )
    {
        return info.getSize( );
    }

    @Override
    public void updateLayoutData( List<PlotInfo> list, int index, int size )
    {
        StackedTimePlot2D parent = info.getStackedTimePlot( );

        //XXX hack, overload negative size to mean "grow to fill available space"
        boolean grow = size < 0;

        int plotSpacing = parent.getPlotSpacing( );

        if ( parent.isTimeAxisHorizontal( ) )
        {
            int topSpace = index == 0 || index >= list.size( ) - 1 ? 0 : plotSpacing;
            int bottomSpace = index >= list.size( ) - 2 ? 0 : plotSpacing;
            int labelSize = parent.isShowLabels( ) ? parent.getLabelSize( ) : 0;

            if ( grow )
            {
                String format = "cell %d %d 1 1, push, grow, id i%2$d, gap 0 0 %3$d %4$d";
                String layout = String.format( format, 1, index, topSpace, bottomSpace );
                info.getLayout( ).setLayoutData( layout );

                format = "cell %d %d 1 1, pushy, growy, width %d!, gap 0 0 %4$d %5$d";
                layout = String.format( format, 0, index, labelSize, topSpace, bottomSpace );
                info.getLabelLayout( ).setLayoutData( layout );
                info.getLabelLayout( ).setVisible( parent.isShowLabels( ) );
            }
            else
            {
                String format = "cell %d %d 1 1, pushx, growx, height %d!, id i%2$d, gap 0 0 %4$d %5$d";
                String layout = String.format( format, 1, index, size, topSpace, bottomSpace );
                info.getLayout( ).setLayoutData( layout );

                format = "cell %d %d 1 1, width %d!, height %d!, gap 0 0 %5$d %6$d";
                layout = String.format( format, 0, index, labelSize, size, topSpace, bottomSpace );
                info.getLabelLayout( ).setLayoutData( layout );
                info.getLabelLayout( ).setVisible( parent.isShowLabels( ) );
            }
        }
        else
        {
            int topSpace = index <= 1 ? 0 : plotSpacing;
            int bottomSpace = index == 0 || index >= list.size( ) - 1 ? 0 : plotSpacing;
            int labelSize = parent.isShowLabels( ) ? parent.getLabelSize( ) : 0;

            if ( grow )
            {
                String format = "cell %d %d 1 1, push, grow, id i%1$d, gap %3$d %4$d 0 0";
                String layout = String.format( format, index, 1, topSpace, bottomSpace );
                info.getLayout( ).setLayoutData( layout );

                format = "cell %d %d 1 1, pushx, growx, height %d!, gap %4$d %5$d 0 0";
                layout = String.format( format, index, 0, labelSize, topSpace, bottomSpace );
                info.getLabelLayout( ).setLayoutData( layout );
                info.getLabelLayout( ).setVisible( parent.isShowLabels( ) );
            }
            else
            {
                String format = "cell %d %d 1 1, pushy, growy, width %d!, id i%1$d, gap %4$d %5$d 0 0";
                String layout = String.format( format, index, 1, size, topSpace, bottomSpace );
                info.getLayout( ).setLayoutData( layout );

                format = "cell %d %d 1 1, height %d!, width %d!, gap %5$d %6$d 0 0";
                layout = String.format( format, index, 0, labelSize, size, topSpace, bottomSpace );
                info.getLabelLayout( ).setLayoutData( layout );
                info.getLabelLayout( ).setVisible( parent.isShowLabels( ) );
            }
        }
    }
}