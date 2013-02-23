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
package com.metsci.glimpse.plot.timeline.group;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import com.metsci.glimpse.event.mouse.GlimpseMouseAdapter;
import com.metsci.glimpse.event.mouse.GlimpseMouseEvent;
import com.metsci.glimpse.layout.GlimpseLayout;
import com.metsci.glimpse.painter.info.SimpleTextPainter;
import com.metsci.glimpse.plot.stacked.PlotInfo;
import com.metsci.glimpse.plot.stacked.PlotInfoWrapper;
import com.metsci.glimpse.plot.timeline.CollapsibleTimePlot2D;

public class GroupInfoImpl extends PlotInfoWrapper implements GroupInfo
{
    protected CollapsibleTimePlot2D plot;

    protected Set<PlotInfo> children;

    protected GroupLabelPainter labelPainter;
    protected String label;

    protected boolean expanded;

    public GroupInfoImpl( CollapsibleTimePlot2D _plot, PlotInfo group, Collection<? extends PlotInfo> subplots )
    {
        super( group );

        this.plot = _plot;
        this.labelPainter = new GroupLabelPainter( "" );
        this.info.getLayout( ).addPainter( this.labelPainter );
        this.info.setSize( 22 );
        this.expanded = true;

        this.children = new LinkedHashSet<PlotInfo>( );
        this.children.addAll( subplots );

        GlimpseLayout layout = this.info.getLayout( );
        layout.setEventConsumer( false );
        layout.setEventGenerator( true );
        layout.addGlimpseMouseListener( new GlimpseMouseAdapter( )
        {
            @Override
            public void mousePressed( GlimpseMouseEvent event )
            {
                int x = event.getScreenPixelsX( );

                // collapse/expand via clicks on arrow button only
                //if ( x < labelPainter.getArrowSize( ) + labelPainter.getArrowSpacing( ) * 2 )
                
                // collapse/expand via clicks on button or label
                if ( x < plot.getLabelSize( ) )
                {
                    setExpanded( !expanded );
                    event.setHandled( true );
                }
            }
        } );
    }

    public SimpleTextPainter getTextPainter( )
    {
        return this.labelPainter.getTextPainter( );
    }

    public void setShowArrow( boolean show )
    {
        this.labelPainter.setShowArrow( show );
    }

    public boolean isShowArrow( )
    {
        return this.labelPainter.isShowArrow( );
    }

    public void setShowDivider( boolean show )
    {
        this.labelPainter.setShowDivider( show );
    }

    public boolean isShowDivider( )
    {
        return this.labelPainter.isShowDivider( );
    }

    public void setDividerColor( float[] color )
    {
        this.labelPainter.setDividerColor( color );
    }

    public float[] getDividerColor( )
    {
        return this.labelPainter.getDividerColor( );
    }

    @Override
    public boolean isExpanded( )
    {
        return this.expanded;
    }

    @Override
    public void setExpanded( boolean expanded )
    {
        this.expanded = expanded;
        this.labelPainter.setExpanded( expanded );
        this.setVisible0( this, expanded );
        this.plot.validateLayout( );
    }
    
    protected void setVisible0( GroupInfo parent, boolean visible )
    {
        for ( PlotInfo child : parent.getChildPlots( ) )
        {
            child.setVisible( visible );
            
            if ( child instanceof GroupInfo )
            {
                GroupInfo childGroup = (GroupInfo) child;
                if ( childGroup.isExpanded( ) )
                {
                    setVisible0( childGroup, visible );
                }
            }
        }
    }

    @Override
    public void addChildPlot( PlotInfo childPlot )
    {
        childPlot.setVisible( this.expanded );
        this.children.add( childPlot );
        if ( this.plot.isAutoValidate( ) ) this.plot.validateLayout( );
    }

    @Override
    public void removeChildPlot( PlotInfo childPlot )
    {
        this.children.remove( childPlot );
        if ( this.plot.isAutoValidate( ) ) this.plot.validateLayout( );
    }

    @Override
    public Collection<PlotInfo> getChildPlots( )
    {
        return Collections.unmodifiableCollection( this.children );
    }

    @Override
    public void setLabelText( String label )
    {
        this.label = label;
        this.labelPainter.setText( label );
    }

    @Override
    public String getLabelText( )
    {
        return this.label;
    }

    @Override
    public int hashCode( )
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( ( info == null ) ? 0 : info.hashCode( ) );
        return result;
    }

    @Override
    public boolean equals( Object obj )
    {
        if ( this == obj ) return true;
        if ( obj == null ) return false;
        if ( getClass( ) != obj.getClass( ) ) return false;
        GroupInfoImpl other = ( GroupInfoImpl ) obj;
        if ( info == null )
        {
            if ( other.info != null ) return false;
        }
        else if ( !info.equals( other.info ) ) return false;
        return true;
    }
}