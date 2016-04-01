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
package com.metsci.glimpse.axis.tagged;

import java.util.List;

import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.axis.listener.mouse.AxisMouseListener1D;
import com.metsci.glimpse.event.mouse.GlimpseMouseEvent;
import com.metsci.glimpse.event.mouse.ModifierKey;
import com.metsci.glimpse.event.mouse.MouseButton;

/**
 * A mouse listener which allows adjustment of the {@link Tag} values of
 * a {@link TaggedAxis1D} via mouse gestures.
 *
 * @author ulman
 */
public class TaggedAxisMouseListener1D extends AxisMouseListener1D
{
    public static final int MAX_PIXEL_DISTANCE = 20;

    protected double tagAnchor;

    protected int maxDistance;
    protected Tag selectedTag;
    protected boolean dragTogether;

    protected boolean enableDragTogether = true;

    public TaggedAxisMouseListener1D( int maxDistance )
    {
        this.maxDistance = maxDistance;
    }

    public TaggedAxisMouseListener1D( )
    {
        this( MAX_PIXEL_DISTANCE );
    }

    public void setDragTagsTogether( boolean enabled )
    {
        this.enableDragTogether = enabled;
    }

    @Override
    public void mouseMoved( GlimpseMouseEvent e, Axis1D axis, boolean horizontal )
    {
        TaggedAxis1D taggedAxis = ( TaggedAxis1D ) axis;

        if ( selectedTag == null )
        {
            super.mouseMoved( e, taggedAxis, horizontal );
        }
        else if ( e.isButtonDown( MouseButton.Button1 ) )
        {
            anchor( taggedAxis, horizontal, e.getX( ), e.getY( ) );

            int mousePosPixels = getDim( horizontal, e.getX( ), taggedAxis.getSizePixels( ) - e.getY( ) );
            int panPixels = getDim( horizontal, anchorPixelsX, anchorPixelsY ) - mousePosPixels;
            double panValue = panPixels / taggedAxis.getPixelsPerValue( );
            double newTagValue = tagAnchor - panValue;

            if ( e.isKeyDown( ModifierKey.Shift ) || dragTogether )
            {
                double currentTagValue = selectedTag.getValue( );
                double deltaTagValue = newTagValue - currentTagValue;

                for ( Tag tag : taggedAxis.getSortedTags( ) )
                {
                    tag.setValue( tag.getValue( ) + deltaTagValue );
                }
            }
            else
            {
                this.selectedTag.setValue( newTagValue );
            }

            taggedAxis.validateTags( );
        }
    }

    @Override
    public void mousePressed( GlimpseMouseEvent e, Axis1D axis, boolean horizontal )
    {
        super.mousePressed( e, axis, horizontal );
        this.updateTagSelection( e, axis, horizontal );
    }

    protected void updateTagSelection( GlimpseMouseEvent e, Axis1D axis, boolean horizontal )
    {
        TaggedAxis1D taggedAxis = ( TaggedAxis1D ) axis;

        List<Tag> tags = taggedAxis.getSortedTags( );
        int pos = getDim( horizontal, e.getX( ), taggedAxis.getSizePixels( ) - e.getY( ) );
        this.selectedTag = getSelectedTag( taggedAxis, tags, pos, maxDistance );

        if ( this.selectedTag != null )
        {
            this.tagAnchor = selectedTag.getValue( );
        }
    }

    @Override
    public void mouseReleased( GlimpseMouseEvent e, Axis1D axis, boolean horizontal )
    {
        super.mouseReleased( e, axis, horizontal );

        this.selectedTag = null;
    }

    protected Tag getSelectedTag( TaggedAxis1D taggedAxis, List<Tag> tags, int mousePos, int maxPixelDist )
    {
        double maxDistance = maxPixelDist / taggedAxis.getPixelsPerValue( );
        double mouseValue = taggedAxis.screenPixelToValue( mousePos );

        dragTogether = false;
        Tag selectedTag = null;
        double minDistance = Double.POSITIVE_INFINITY;

        for ( Tag tag : tags )
        {
            double distance = Math.abs( mouseValue - tag.getValue( ) );

            if ( distance < minDistance && distance < maxDistance )
            {
                minDistance = distance;
                selectedTag = tag;
            }
        }

        if ( enableDragTogether && selectedTag == null && tags.size( ) > 1 && mouseValue > tags.get( 0 ).getValue( ) && mouseValue < tags.get( tags.size( ) - 1 ).getValue( ) )
        {
            selectedTag = tags.get( 0 );
            dragTogether = true;
        }

        return selectedTag;
    }
}
