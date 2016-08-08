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
package com.metsci.glimpse.dnc.geosym;

public class DncGeosymLineAreaStyle
{
    
    public final String symbolId;
    public final String symbolType;
    
    public final float lineWidth;
    public final float[] lineRgba;
    public final boolean hasLineStipple;
    public final int lineStippleFactor;
    public final short lineStipplePattern;
    
    public final float[] fillRgba;
    
    
    public DncGeosymLineAreaStyle(String symbolId, String symbolType, float lineWidth, float[] lineRgba, boolean hasLineStipple, int lineStippleFactor, short lineStipplePattern, float[] fillRgba)
    {
        this.symbolId = symbolId;
        this.symbolType = symbolType;
        this.lineWidth = lineWidth;
        this.lineRgba = lineRgba;
        this.hasLineStipple = hasLineStipple;
        this.lineStippleFactor = lineStippleFactor;
        this.lineStipplePattern = lineStipplePattern;
        this.fillRgba = fillRgba;
    }
    
}
