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
package com.metsci.glimpse.layout;

import static java.lang.Math.max;

import net.miginfocom.layout.ComponentWrapper;

/**
 * A GlimpseLayout that shifts its layout-children up or down based on a vertical-
 * offset field, which can be set by calling the {@link #setVerticalOffset(int)}
 * method.
 *
 * For an example of controlling the vertical-offset using a Swing scrollbar, see
 * {@link com.metsci.glimpse.examples.layout.VerticallyScrollableLayoutExample}.
 *
 * In typical usage, when this layout's container is taller than minContentHeight,
 * the child-layouts will not be scrollable -- this layout will size its child-
 * layouts to fit the container.
 *
 * However, when this layout's container is shorter than minContentHeight, it will
 * set child-layout heights to minContentHeight -- and verticalOffset then affects
 * what portion of the content fits inside the container's bounds.
 *
 * @author hogye
 */
public class GlimpseVerticallyScrollableLayout extends GlimpseLayout
{

    protected int minContentHeight;
    protected int verticalOffset;


    public GlimpseVerticallyScrollableLayout( int minContentHeight )
    {
        this.minContentHeight = minContentHeight;
        this.verticalOffset = 0;

        setLayoutManager( new GlimpseLayoutManager( )
        {
            public void layout( GlimpseLayoutDelegate parent )
            {
                int contentWidth = parent.getWidth( );

                int minContentHeight = GlimpseVerticallyScrollableLayout.this.minContentHeight;
                int contentHeight = max( minContentHeight, parent.getHeight( ) + verticalOffset );
                int top = parent.getHeight( ) + verticalOffset;
                int bottom = top - contentHeight;

                for ( ComponentWrapper child : parent.getComponents( ) )
                {
                    child.setBounds( 0, bottom, contentWidth, contentHeight );
                }
            }
        } );
    }

    public int getVerticalOffset( )
    {
        return verticalOffset;
    }

    public void setVerticalOffset( int verticalOffset )
    {
        if ( verticalOffset != this.verticalOffset )
        {
            this.verticalOffset = verticalOffset;
            invalidateLayout( );
        }
    }

    public int getMinContentHeight( )
    {
        return minContentHeight;
    }

    public void setMinContentHeight( int minContentHeight )
    {
        if ( minContentHeight != this.minContentHeight )
        {
            this.minContentHeight = minContentHeight;
            invalidateLayout( );
        }
    }

}
