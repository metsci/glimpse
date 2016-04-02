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
package com.metsci.glimpse.axis.painter.label;

import com.metsci.glimpse.axis.Axis1D;

/**
 * Responsible for positioning axis tick marks and their associated labels.</p>
 *
 * @see com.metsci.glimpse.axis.painter.label.GridAxisLabelHandler
 * @author ulman
 */
public interface AxisLabelHandler
{
    /**
     * @return an array containing positions of tick marks in axis coordinates (transformed by any AxisUnitConverter).
     */
    public double[] getTickPositions( Axis1D axis );

    /**
     * @param tickPositions the output from getTickPositions( ).
     * @return an array of string labels for the tick marks provided by getTickPositions( ).
     */
    public String[] getTickLabels( Axis1D axis, double[] tickPositions );

    /**
     * Provides the positions of minor tick marks. How these are rendered differently from regular tick marks
     * can vary, but they will usually be smaller and will not have labels. The positions of the major ticks
     * are provided as input, since the minor tick positions are usually based on these. However, implementations
     * of AxisLabelHandler are free to ignore this argument.
     *
     * @return an array containing positions of minor tick marks in axis coordinates (transformed by any AxisUnitConverter).
     */
    public double[] getMinorTickPositions( double[] tickPositions );

    /**
     * @return a text label describing the axis (the type of data displayed, the units, etc...)
     */
    public String getAxisLabel( Axis1D axis );

    /**
     * @param label provides a hint regarding what label information to display
     */
    public void setAxisLabel( String label );

    /**
     * @return the current AxisUnitConverter (used to determine values returned by getTickPositions() and getTickLabels()
     */
    public AxisUnitConverter getAxisUnitConverter( );

    /**
     * @param converter sets a new AxisUnitConverter
     */
    public void setAxisUnitConverter( AxisUnitConverter converter );
}
