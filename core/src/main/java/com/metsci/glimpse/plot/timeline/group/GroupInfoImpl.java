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
package com.metsci.glimpse.plot.timeline.group;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import com.metsci.glimpse.event.mouse.GlimpseMouseAllListener;
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
    protected boolean collapsible;

    public GroupInfoImpl( CollapsibleTimePlot2D _plot, PlotInfo group, Collection<? extends PlotInfo> subplots )
    {
        super( group );

        this.plot = _plot;
        this.labelPainter = new GroupLabelPainter( "" );
        this.info.getLayout( ).addPainter( this.labelPainter );
        this.info.setSize( 22 );
        this.collapsible = true;
        this.expanded = true;

        this.children = new LinkedHashSet<PlotInfo>( );
        this.children.addAll( subplots );

        for ( PlotInfo info : subplots )
        {
            info.setParent( this );
        }

        GlimpseLayout layout = this.info.getLayout( );
        layout.setEventConsumer( false );
        layout.setEventGenerator( true );
        layout.addGlimpseMouseAllListener( new GlimpseMouseAllListener( )
        {
            protected boolean click = false;

            @Override
            public void mousePressed( GlimpseMouseEvent event )
            {
                click = true;
            }

            @Override
            public void mouseEntered( GlimpseMouseEvent event )
            {
                click = false;
            }

            @Override
            public void mouseExited( GlimpseMouseEvent event )
            {
                click = false;
            }

            @Override
            public void mouseReleased( GlimpseMouseEvent event )
            {
                if ( click && collapsible )
                {
                    int x = event.getScreenPixelsX( );

                    // collapse/expand via clicks on arrow button only
                    //if ( x < labelPainter.getArrowSize( ) + labelPainter.getArrowSpacing( ) * 2 )

                    // collapse/expand via clicks on button or label
                    if ( x < plot.getLabelSize( ) )
                    {
                        setExpanded( !isExpanded( ) );
                        event.setHandled( true );
                        plot.validate( );
                    }

                    click = false;
                }
            }

            @Override
            public void mouseMoved( GlimpseMouseEvent e )
            {
                click = false;
            }

            @Override
            public void mouseWheelMoved( GlimpseMouseEvent e )
            {
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
        return super.isExpanded( ) ? this.expanded : false;
    }

    @Override
    public void setExpanded( boolean expanded )
    {
        this.expanded = expanded;
        this.labelPainter.setExpanded( expanded );
        if ( this.plot.isAutoValidate( ) ) this.plot.validateLayout( );
    }

    @Override
    public void setCollapsible( boolean collapsible )
    {
        this.collapsible = collapsible;
    }

    @Override
    public boolean isCollapsible( )
    {
        return this.collapsible;
    }

    @Override
    public void addChildPlot( PlotInfo childPlot )
    {
        childPlot.setParent( this );
        this.children.add( childPlot );
        if ( this.plot.isAutoValidate( ) ) this.plot.validateLayout( );
    }

    @Override
    public void removeChildPlot( PlotInfo childPlot )
    {
        childPlot.setParent( null );
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