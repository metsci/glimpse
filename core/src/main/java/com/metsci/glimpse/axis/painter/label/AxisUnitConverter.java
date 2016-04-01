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

/**
 * All linked AxisHandlers must use common units for linking to work correctly. However, by setting
 * an AxisUnitConverter on an AxisPainter, the AxisPainter can display tick marks and labels in
 * different units than the AxisHandler.</p>
 *
 * By default, AxisPainters have an identity AxisUnitConverter.
 *
 * @author ulman
 *
 */
public interface AxisUnitConverter
{
    /**
    * Converts from AxisPainter display units to AxisHandler units. Axes which wish to display non-system units
    * must override this method to provide a new mapping.
    *
    * @param value a value in system units.
    * @return the corresponding value in axis units.
    */
    public double fromAxisUnits( double value );

    /**
    * Converts from AxisHandler units to AxisPainter display units. Axes which wish to display non-system units
    * must override this method to provide a new mapping.
    *
    * @param value a value in system units.
    * @return the corresponding value in axis units.
    */
    public double toAxisUnits( double value );
}
