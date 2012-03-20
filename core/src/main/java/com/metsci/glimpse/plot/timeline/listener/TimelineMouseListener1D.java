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
package com.metsci.glimpse.plot.timeline.listener;

import java.util.List;

import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.axis.tagged.Tag;
import com.metsci.glimpse.axis.tagged.TaggedAxis1D;
import com.metsci.glimpse.axis.tagged.TaggedAxisMouseListener1D;
import com.metsci.glimpse.event.mouse.GlimpseMouseEvent;
import com.metsci.glimpse.event.mouse.ModifierKey;
import com.metsci.glimpse.event.mouse.MouseButton;
import com.metsci.glimpse.plot.StackedPlot2D.Orientation;
import com.metsci.glimpse.plot.timeline.StackedTimePlot2D;

public class TimelineMouseListener1D extends TaggedAxisMouseListener1D
{
    protected boolean dragTogether = false;
    protected boolean onlyMoveCurrent = false;
    protected StackedTimePlot2D plot;
    protected boolean timeIsX;

    public TimelineMouseListener1D( StackedTimePlot2D plot )
    {
        super( 25 );

        this.timeIsX = plot.getOrientation( ) == Orientation.VERTICAL;
        this.plot = plot;
    }

    public Tag getSelectedTag( )
    {
        return selectedTag;
    }

    @Override
    public void mousePressed( GlimpseMouseEvent e, Axis1D axis, boolean horizontal )
    {
        super.mousePressed( e, axis, horizontal );

        // right clicks toggle selection locking
        if ( this.allowSelectionLock && e.isButtonDown( MouseButton.Button3 ) )
        {
            if ( plot.isCurrentTimeLocked( ) || plot.isSelectionLocked( ) )
            {
                plot.setCurrentTimeLocked( false );
                plot.setSelectionLocked( false );
            }
            else
            {
                plot.setSelectionLocked( !plot.isSelectionLocked( ) );
            }
        }
    }

    @Override
    protected Tag getSelectedTag( TaggedAxis1D taggedAxis, List<Tag> tags, int mousePos, int maxPixelDist )
    {
        Tag minTag = taggedAxis.getTag( StackedTimePlot2D.MIN_TIME );
        Tag maxTag = taggedAxis.getTag( StackedTimePlot2D.MAX_TIME );
        Tag currentTag = taggedAxis.getTag( StackedTimePlot2D.CURRENT_TIME );

        dragTogether = false;

        double maxDistance = maxPixelDist / taggedAxis.getPixelsPerValue( );
        double mouseValue = taggedAxis.screenPixelToValue( mousePos );

        boolean closeToMin = Math.abs( mouseValue - minTag.getValue( ) ) < maxDistance;
        boolean closeToMax = Math.abs( mouseValue - maxTag.getValue( ) ) < maxDistance;

        Tag selectedTag = null;

        if ( closeToMin )
        {
            selectedTag = minTag;
        }
        else if ( closeToMax )
        {
            selectedTag = maxTag;
        }
        else if ( Math.abs( mouseValue - currentTag.getValue( ) ) < maxDistance )
        {
            selectedTag = currentTag;
        }

        return selectedTag;
    }

    @Override
    public void mouseMoved( GlimpseMouseEvent e, Axis1D axis, boolean horizontal )
    {
        TaggedAxis1D taggedAxis = ( TaggedAxis1D ) axis;

        Tag minTag = taggedAxis.getTag( StackedTimePlot2D.MIN_TIME );
        Tag maxTag = taggedAxis.getTag( StackedTimePlot2D.MAX_TIME );
        Tag currentTag = taggedAxis.getTag( StackedTimePlot2D.CURRENT_TIME );

        if ( e.isKeyDown( ModifierKey.Ctrl ) && e.isButtonDown( MouseButton.Button1 ) && selectedTag != null )
        {
            anchor( axis, horizontal, e.getX( ), e.getY( ) );

            int mousePosPixels = getDim( horizontal, e.getX( ), taggedAxis.getSizePixels( ) - e.getY( ) );
            int panPixels = getDim( horizontal, anchorPixelsX, anchorPixelsY ) - mousePosPixels;
            double panValue = panPixels / taggedAxis.getPixelsPerValue( );
            double newTagValue = tagAnchor - panValue;

            this.selectedTag.setValue( newTagValue );
        }
        else if ( e.isButtonDown( MouseButton.Button1 ) && !plot.isCurrentTimeLocked( ) )
        {
            pan( axis, horizontal, e.getX( ), e.getY( ) );
        }
        else if ( !plot.isSelectionLocked( ) && !plot.isCurrentTimeLocked( ) )
        {
            int mousePosPixels = getDim( horizontal, e.getX( ), taggedAxis.getSizePixels( ) - e.getY( ) );
            double mousePosValue = taggedAxis.screenPixelToValue( mousePosPixels );

            double minDiff = minTag.getValue( ) - currentTag.getValue( );
            double maxDiff = maxTag.getValue( ) - currentTag.getValue( );

            minTag.setValue( mousePosValue + minDiff );
            maxTag.setValue( mousePosValue + maxDiff );
            currentTag.setValue( mousePosValue );
        }

        taggedAxis.validateTags( );
        taggedAxis.validate( );

        notifyTagsUpdated( taggedAxis );
    }

    @Override
    public void mouseWheelMoved( GlimpseMouseEvent e )
    {
        TaggedAxis1D taggedAxis = getTaggedAxis1D( e );

        if ( taggedAxis == null ) return;

        if ( e.isKeyDown( ModifierKey.Ctrl ) || e.isKeyDown( ModifierKey.Meta ) )
        {
            handleCtrlMouseWheel( e );
        }
        else
        {
            if ( timeIsX )
            {
                this.mouseWheelMoved( e, taggedAxis, true );
            }
            else
            {
                this.mouseWheelMoved( e, taggedAxis, false );
            }
        }

        taggedAxis.validateTags( );
        taggedAxis.validate( );

        notifyTagsUpdated( taggedAxis );
    }

    public void handleCtrlMouseWheel( GlimpseMouseEvent e )
    {
        TaggedAxis1D taggedAxis = getTaggedAxis1D( e );

        if ( taggedAxis == null ) return;

        Tag minTag = taggedAxis.getTag( StackedTimePlot2D.MIN_TIME );
        Tag maxTag = taggedAxis.getTag( StackedTimePlot2D.MAX_TIME );
        Tag currentTag = taggedAxis.getTag( StackedTimePlot2D.CURRENT_TIME );

        double minValue = minTag.getValue( );
        double maxValue = maxTag.getValue( );
        double selectionSize = maxValue - minValue;

        int zoomIncrements = e.getWheelIncrement( );

        double zoomPercentDbl = 1.0f;
        for ( int i = 0; i < Math.abs( zoomIncrements ); i++ )
        {
            zoomPercentDbl *= 1.0 + zoomConstant;
        }
        zoomPercentDbl = zoomIncrements > 0 ? 1.0 / zoomPercentDbl : zoomPercentDbl;
        double newSelectionSize = selectionSize * zoomPercentDbl;

        minTag.setValue( maxValue - newSelectionSize );
        maxTag.setValue( maxValue );
        currentTag.setValue( maxValue );

        taggedAxis.validateTags( );
        taggedAxis.validate( );

        notifyTagsUpdated( minTag.getValue( ), maxTag.getValue( ), currentTag.getValue( ) );
    }

    protected TaggedAxis1D getTaggedAxis1D( GlimpseMouseEvent e )
    {
        return ( TaggedAxis1D ) e.getAxis1D( );
    }

    protected void moveAllTags( TaggedAxis1D taggedAxis, double deltaTagValue )
    {
        for ( Tag tag : taggedAxis.getSortedTags( ) )
        {
            tag.setValue( tag.getValue( ) + deltaTagValue );
        }
    }

    protected void notifyTagsUpdated( TaggedAxis1D timeAxis )
    {
        List<Tag> tags = timeAxis.getSortedTags( );
        notifyTagsUpdated( tags.get( 0 ).getValue( ), tags.get( 2 ).getValue( ), tags.get( 1 ).getValue( ) );
    }

    protected void notifyTagsUpdated( double startValue, double endValue, double selectedValue )
    {
    }
}
