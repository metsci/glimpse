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
import java.util.List;
import java.util.Set;

import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.context.GlimpseTargetStack;
import com.metsci.glimpse.event.mouse.GlimpseMouseAdapter;
import com.metsci.glimpse.event.mouse.GlimpseMouseEvent;
import com.metsci.glimpse.layout.GlimpseAxisLayout2D;
import com.metsci.glimpse.layout.GlimpseLayout;
import com.metsci.glimpse.painter.info.SimpleTextPainter;
import com.metsci.glimpse.plot.StackedPlot2D;
import com.metsci.glimpse.plot.StackedPlot2D.LayoutDataUpdater;
import com.metsci.glimpse.plot.StackedPlot2D.Orientation;
import com.metsci.glimpse.plot.StackedPlot2D.PlotInfo;
import com.metsci.glimpse.plot.timeline.CollapsibleTimePlot2D;
import com.metsci.glimpse.support.settings.LookAndFeel;

public class GroupInfoImpl implements GroupInfo
{
    protected CollapsibleTimePlot2D plot;

    protected Set<PlotInfo> children;
    protected PlotInfo group;

    protected GroupLabelPainter labelPainter;
    protected String label;

    protected boolean expanded;

    public GroupInfoImpl( CollapsibleTimePlot2D plot, PlotInfo group, Collection<? extends PlotInfo> subplots )
    {
        this.plot = plot;
        this.group = group;
        this.labelPainter = new GroupLabelPainter( "" );
        this.group.getLayout( ).addPainter( this.labelPainter );
        this.group.setSize( 22 );
        this.expanded = true;

        this.children = new LinkedHashSet<PlotInfo>( );
        this.children.addAll( subplots );

        GlimpseLayout layout = this.group.getLayout( );
        layout.setEventConsumer( false );
        layout.setEventGenerator( true );
        layout.addGlimpseMouseListener( new GlimpseMouseAdapter( )
        {
            @Override
            public void mousePressed( GlimpseMouseEvent event )
            {
                int x = event.getScreenPixelsX( );

                if ( x < labelPainter.getArrowSize( ) + labelPainter.getArrowSpacing( ) * 2 )
                {
                    setExpanded( !expanded );
                    event.setHandled( true );
                }
            }
        } );

        this.setLayoutDataUpdater( new LayoutDataUpdater( )
        {
            @Override
            public int getSizePixels( List<PlotInfo> list, int index )
            {
                return getSize( );
            }

            @Override
            public void updateLayoutData( List<PlotInfo> list, int index, int size )
            {
                Orientation orientation = getStackedPlot( ).getOrientation( );
                int plotSpacing = getStackedPlot( ).getPlotSpacing( );

                String layoutData = null;

                if ( orientation == Orientation.HORIZONTAL )
                {
                    int gapTop = index == 0 ? 0 : plotSpacing;
                    int gapBottom = index == list.size( ) - 1 ? 0 : plotSpacing;

                    String format = "cell %d %d, spany, width %d!, gap 0 0 %d %d, id i%1$d";
                    layoutData = String.format( format, index, 0, size, gapTop, gapBottom );
                }
                else if ( orientation == Orientation.VERTICAL )
                {
                    int gapLeft = index == 0 ? 0 : plotSpacing;
                    int gapRight = index == list.size( ) - 1 ? 0 : plotSpacing;

                    String format = "cell %d %d, spanx, growx, height %d!, gap %d %d 0 0, id i%2$d";
                    layoutData = String.format( format, 0, index, size, gapLeft, gapRight );
                }

                getLayout( ).setLayoutData( layoutData );
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
        this.plot.validateLayout( );
    }

    @Override
    public void addChildPlot( PlotInfo childPlot )
    {
        this.children.add( childPlot );
        this.plot.validateLayout( );
    }

    @Override
    public void removeChildPlot( PlotInfo childPlot )
    {
        this.children.remove( childPlot );
        this.plot.validateLayout( );
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
    public StackedPlot2D getStackedPlot( )
    {
        return group.getStackedPlot( );
    }

    @Override
    public Object getId( )
    {
        return group.getId( );
    }

    @Override
    public int getOrder( )
    {
        return group.getOrder( );
    }

    @Override
    public int getSize( )
    {
        return group.getSize( );
    }

    @Override
    public void setOrder( int order )
    {
        group.setOrder( order );
    }

    @Override
    public void setSize( int size )
    {
        group.setSize( size );
    }

    @Override
    public GlimpseAxisLayout2D getLayout( )
    {
        return group.getLayout( );
    }

    @Override
    public Axis1D getCommonAxis( GlimpseTargetStack stack )
    {
        return group.getCommonAxis( stack );
    }

    @Override
    public Axis1D getOrthogonalAxis( GlimpseTargetStack stack )
    {
        return group.getOrthogonalAxis( stack );
    }

    @Override
    public Axis1D getCommonAxis( )
    {
        return group.getCommonAxis( );
    }

    @Override
    public Axis1D getOrthogonalAxis( )
    {
        return group.getOrthogonalAxis( );
    }

    @Override
    public void addLayout( GlimpseAxisLayout2D childLayout )
    {
        group.addLayout( childLayout );
    }

    @Override
    public void setLookAndFeel( LookAndFeel laf )
    {
        group.setLookAndFeel( laf );
        for ( PlotInfo plot : children )
        {
            plot.setLookAndFeel( laf );
        }
    }

    @Override
    public void setLayoutDataUpdater( LayoutDataUpdater updater )
    {
        group.setLayoutDataUpdater( updater );
    }

    @Override
    public LayoutDataUpdater getLayoutDataUpdater( )
    {
        return group.getLayoutDataUpdater( );
    }
    
    
    @Override
    public int hashCode( )
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( ( group == null ) ? 0 : group.hashCode( ) );
        return result;
    }

    @Override
    public boolean equals( Object obj )
    {
        if ( this == obj ) return true;
        if ( obj == null ) return false;
        if ( getClass( ) != obj.getClass( ) ) return false;
        GroupInfoImpl other = ( GroupInfoImpl ) obj;
        if ( group == null )
        {
            if ( other.group != null ) return false;
        }
        else if ( !group.equals( other.group ) ) return false;
        return true;
    }
}