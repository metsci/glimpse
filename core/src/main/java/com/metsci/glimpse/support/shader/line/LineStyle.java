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
package com.metsci.glimpse.support.shader.line;

import static com.metsci.glimpse.support.shader.line.LineJoinType.*;

import java.util.Arrays;

import com.metsci.glimpse.support.color.GlimpseColor;

public class LineStyle
{

    /**
     * The thickness of the ideal bounds of the line. Feathering will encroach into
     * these bounds, by half of {@link #feather_PX}.
     */
    public float thickness_PX = 1.0f;

    /**
     * The thickness of the feather region, across which alpha fades to transparent.
     * Half the feather thickness (the more opaque half) lies inside the ideal bounds
     * of the line, and half (the more transparent half) lies outside.
     * <p>
     * For feathering to work, {@link javax.media.opengl.GL#GL_BLEND} must be enabled.
     * <p>
     * Line rendering is likely to be faster with feather set to zero.
     */
    public float feather_PX = 0.9f;

    /**
     * How to join connected line segments. Defaults to NONE, which gives appearance
     * and performance similar to familiar GL line drawing. Other join types may look
     * nicer, but are more computationally expensive to render.
     * <p>
     * Line rendering is likely to be faster with a join-type of NONE.
     */
    public LineJoinType joinType = JOIN_NONE;

    /**
     * To keep miters from growing out of control for very sharp angles, miter joins
     * are only used when:
     * <p>
     * {@code miterLength <= miterLimit * lineThickness}
     * <p>
     * where {@code miterLength} is the distance from the outer tip of the miter to
     * its inner corner.
     * <p>
     * Otherwise, a bevel join is used instead.
     * <p>
     * Has no effect unless {@link #joinType} is {@link LineJoinType#JOIN_MITER}.
     */
    public float miterLimit = 4;

    /**
     * The color used for the most opaque parts of the line. Due to feathering and/or
     * stippling, some parts of the line may have their alpha values scaled down, so
     * that they become more transparent.
     */
    public float[] rgba = GlimpseColor.getBlack( );

    /**
     * Line rendering is likely to be faster with stippling disabled.
     */
    public boolean stippleEnable = false;

    /**
     * The number of pixels, along the length of the line, covered by one bit
     * of {@link #stipplePattern}.
     */
    public float stippleScale = 1.0f;

    /**
     * Least significant bit is drawn at the start of the line. Despite the "int"
     * type, only the bottom 16 bits are used. 1 = opaque, 0 = transparent.
     */
    public int stipplePattern = 0b0101010101010101;

    public LineStyle( )
    {

    }

    public LineStyle( LineStyle source )
    {
        this.thickness_PX = source.thickness_PX;
        this.feather_PX = source.feather_PX;
        this.joinType = source.joinType;
        this.miterLimit = source.miterLimit;
        this.rgba = Arrays.copyOf( source.rgba, source.rgba.length );

        this.stippleEnable = source.stippleEnable;
        this.stippleScale = source.stippleScale;
        this.stipplePattern = source.stipplePattern;
    }

}
