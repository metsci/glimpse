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
package com.metsci.glimpse.painter.treemap;

import java.awt.geom.Rectangle2D;

/**
 * Lays out the children in a simplistic manner. It just divides the bounding
 * rectangle into n slices (direction depends on the level).
 *
 * @author borkholder
 */
public class SliceAndDiceLayout implements TreeMapLayout
{
    @Override
    public Rectangle2D[] layout( Rectangle2D boundary, double[] sizes, int level )
    {
        boolean horizontal = level % 2 == 0;

        double area = boundary.getWidth( ) * boundary.getHeight( );
        double totalSize = 0;
        for ( int i = 0; i < sizes.length; i++ )
        {
            totalSize += sizes[i];
        }

        Rectangle2D[] rects = new Rectangle2D[sizes.length];

        double xOffset = boundary.getMinX( );
        double yOffset = boundary.getMinY( );
        for ( int i = 0; i < sizes.length; i++ )
        {
            double scaledSize = sizes[i] / totalSize * area;
            double height = boundary.getHeight( );
            double width = boundary.getWidth( );

            if ( horizontal )
            {
                width = scaledSize / boundary.getHeight( );
            }
            else
            {
                height = scaledSize / boundary.getWidth( );
            }

            rects[i] = new Rectangle2D.Double( xOffset, yOffset, width, height );

            if ( horizontal )
            {
                xOffset += width;
            }
            else
            {
                yOffset += height;
            }
        }

        return rects;
    }
}
