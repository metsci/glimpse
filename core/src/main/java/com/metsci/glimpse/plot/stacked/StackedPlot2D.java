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
package com.metsci.glimpse.plot.stacked;

import static com.metsci.glimpse.plot.stacked.StackedPlot2D.Orientation.HORIZONTAL;
import static com.metsci.glimpse.plot.stacked.StackedPlot2D.Orientation.VERTICAL;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.layout.GlimpseAxisLayout1D;
import com.metsci.glimpse.layout.GlimpseAxisLayout2D;
import com.metsci.glimpse.layout.GlimpseAxisLayoutX;
import com.metsci.glimpse.layout.GlimpseAxisLayoutY;
import com.metsci.glimpse.layout.GlimpseLayout;
import com.metsci.glimpse.layout.GlimpseLayoutManagerMig;
import com.metsci.glimpse.painter.decoration.BackgroundPainter;
import com.metsci.glimpse.support.settings.DefaultLookAndFeel;
import com.metsci.glimpse.support.settings.LookAndFeel;

/**
 * A plot which allows multiple plots or GlimpseLayout areas to be easily arranged in a vertical
 * or horizontal stack.
 *
 * @author ulman
 */
public class StackedPlot2D extends GlimpseLayout
{
    // by default, StackedPlot2D automatically calls validate() when changes
    // like the addition of new sub plots are performed
    // however, this is not always desirable, such as when many changes are
    // being made in rapid succession
    protected boolean autoValidate = true;

    protected int outerBorder = 10;
    protected int plotSpacing = 0;

    protected Axis1D commonAxis;

    protected GlimpseLayoutManagerMig layout;
    protected Map<Object, PlotInfo> stackedPlots;

    protected BackgroundPainter backgroundPainter;

    protected Orientation orient = VERTICAL;

    protected LookAndFeel laf;

    // layout covering all plots (but not necessarily labels)
    protected GlimpseAxisLayout1D overlayLayout;
    protected GlimpseAxisLayout1D underlayLayout;
    // overlay overing the whole plot
    protected GlimpseLayout fullOverlayLayout;

    public StackedPlot2D( Orientation orientation, Axis1D commonAxis )
    {
        this.orient = orientation;
        this.initializePlot( commonAxis );
    }

    public StackedPlot2D( Orientation orientation )
    {
        this( orientation, null );
    }

    public StackedPlot2D( Axis1D commonAxis )
    {
        this( VERTICAL, commonAxis );
    }

    public StackedPlot2D( )
    {
        this( VERTICAL, null );
    }

    //////////////////////////////////////
    //     Initialization Methods       //
    //////////////////////////////////////

    protected void initializePlot( Axis1D commonAxis )
    {
        initializeAxes( commonAxis );
        initializeArrays( );
        initializeLayout( );
        initializeOverlayLayout( );
        initializePainters( );
        initializeLookAndFeel( );
        updatePainterLayout( );
    }

    protected void initializeAxes( Axis1D commonAxis )
    {
        if ( commonAxis == null )
        {
            this.commonAxis = createCommonAxis( );
        }
        else
        {
            this.commonAxis = commonAxis;
        }
    }

    protected Axis1D createCommonAxis( )
    {
        return new Axis1D( );
    }

    protected void initializeArrays( )
    {
        this.stackedPlots = new LinkedHashMap<Object, PlotInfo>( );
    }

    protected void initializeLayout( )
    {
        this.layout = new GlimpseLayoutManagerMig( );
        this.setLayoutManager( layout );
    }

    protected void initializePainters( )
    {
        this.backgroundPainter = new BackgroundPainter( true );
        this.addPainter( this.backgroundPainter, Integer.MIN_VALUE );
    }

    protected void initializeLookAndFeel( )
    {
        setLookAndFeel( new DefaultLookAndFeel( ) );
    }

    protected void initializeOverlayLayout( )
    {
        if ( getOrientation( ) == VERTICAL )
        {
            this.overlayLayout = new GlimpseAxisLayoutX( this, "Overlay", commonAxis );
            this.underlayLayout = new GlimpseAxisLayoutX( this, "Underlay", commonAxis );
        }
        else
        {
            this.overlayLayout = new GlimpseAxisLayoutY( this, "Overlay", commonAxis );
            this.underlayLayout = new GlimpseAxisLayoutY( this, "Underlay", commonAxis );
        }

        this.fullOverlayLayout = new GlimpseLayout( this, "Full Overlay" );
        this.fullOverlayLayout.setEventGenerator( true );
        this.fullOverlayLayout.setEventConsumer( false );

        this.overlayLayout.setEventGenerator( true );
        this.overlayLayout.setEventConsumer( false );

        this.underlayLayout.setEventGenerator( true );
        this.underlayLayout.setEventConsumer( false );

        // nothing should be placed in front of the overlayLayout
        this.setZOrder( this.fullOverlayLayout, Integer.MAX_VALUE );
        this.setZOrder( this.overlayLayout, Integer.MAX_VALUE );
        this.setZOrder( this.underlayLayout, Integer.MIN_VALUE );
    }

    protected void updatePainterLayout( )
    {
        this.lock.lock( );
        try
        {
            setRowColumnConstraints( );
            setLayoutConstraints( );

            List<PlotInfo> list = getSortedPlots( stackedPlots.values( ) );
            for ( int index = 0; index < list.size( ); index++ )
            {
                PlotInfo info = list.get( index );
                String data = info.getLayoutData( );

                if ( data == null )
                {
                    info.updateLayout( index );
                }
                else
                {
                    info.getBaseLayout( ).setLayoutData( data );
                }
            }

            this.updateOverlayLayout( list );

            this.invalidateLayout( );
        }
        finally
        {
            lock.unlock( );
        }
    }

    protected void updateOverlayLayout( List<PlotInfo> list )
    {
        // position the overlay in absolute coordinates based on the position of the plots
        // which are given miglayout ids: i0, i1, etc...
        if ( this.overlayLayout != null )
        {
            int offsetX = getOverlayLayoutOffsetX( ) + getBorderSize( );
            int offsetX2 = getOverlayLayoutOffsetX2( ) + getBorderSize( );
            int offsetY = getOverlayLayoutOffsetY( ) + getBorderSize( );
            int offsetY2 = getOverlayLayoutOffsetY2( ) + getBorderSize( );

            String layout = String.format( "pos container.x+(%d) container.y+(%d) container.x2-(%d) container.y2-(%d)", offsetX, offsetY, offsetX2, offsetY2 );
            this.overlayLayout.setLayoutData( layout );
            this.underlayLayout.setLayoutData( layout );
        }

        if ( this.fullOverlayLayout != null )
        {
            this.fullOverlayLayout.setLayoutData( String.format( "pos container.x+%1$d container.y+%1$d container.x2-%1$d container.y2-%1$d", getBorderSize( ) ) );
        }
    }

    public int getOverlayLayoutOffsetX( )
    {
        return 0;
    }

    public int getOverlayLayoutOffsetX2( )
    {
        return 0;
    }

    public int getOverlayLayoutOffsetY( )
    {
        return 0;
    }

    public int getOverlayLayoutOffsetY2( )
    {
        return 0;
    }

    protected void setRowColumnConstraints( )
    {
        setRowColumnConstraints( 0, 0 );
    }

    protected void setRowColumnConstraints( int maxLevel, int indentSize )
    {
        StringBuilder b = new StringBuilder( );
        for ( int i = 0; i < maxLevel; i++ )
        {
            b.append( String.format( "[%d]", indentSize ) );
        }
        b.append( "[grow,fill]" );

        if ( getOrientation( ) == VERTICAL )
        {
            layout.setColumnConstraints( b.toString( ) );
        }
        else if ( getOrientation( ) == HORIZONTAL )
        {
            layout.setRowConstraints( b.toString( ) );
        }
    }

    protected void setLayoutConstraints( )
    {
        layout.setLayoutConstraints( String.format( "bottomtotop, gapx 0, gapy 0, insets %d %d %d %d", outerBorder, outerBorder, outerBorder, outerBorder ) );
    }

    //////////////////////////////////////
    //   Getter / Setter Methods        //
    //////////////////////////////////////

    @Override
    public GlimpseLayoutManagerMig getLayoutManager( )
    {
        return this.layout;
    }

    public boolean isAutoValidate( )
    {
        return this.autoValidate;
    }

    public void setAutoValidate( boolean autoValidate )
    {
        this.autoValidate = autoValidate;
    }

    public int getPlotSpacing( )
    {
        return this.plotSpacing;
    }

    public Orientation getOrientation( )
    {
        return orient;
    }

    public Axis1D getCommonAxis( )
    {
        return commonAxis;
    }

    public PlotInfo getPlot( Object id )
    {
        return this.stackedPlots.get( id );
    }

    public Collection<PlotInfo> getAllPlots( )
    {
        this.lock.lock( );
        try
        {
            return Collections.unmodifiableCollection( this.stackedPlots.values( ) );
        }
        finally
        {
            this.lock.unlock( );
        }
    }

    public List<PlotInfo> getSortedPlots( )
    {
        this.lock.lock( );
        try
        {
            return getSortedPlots( this.stackedPlots.values( ) );
        }
        finally
        {
            this.lock.unlock( );
        }
    }

    public void setPlotSpacing( int size )
    {
        this.plotSpacing = size;

        for ( PlotInfo info : stackedPlots.values( ) )
        {
            info.setPlotSpacing( size );
        }

        if ( isAutoValidate( ) ) this.validate( );
    }

    public float[] getBackgroundColor( )
    {
        return this.backgroundPainter.getColor( );
    }

    public void setBackgroundColor( float[] color )
    {
        this.backgroundPainter.setColor( color );
    }

    public void setBorderSize( int size )
    {
        this.outerBorder = size;
        if ( isAutoValidate( ) ) this.validate( );
    }

    public int getBorderSize( )
    {
        return this.outerBorder;
    }

    public void validate( )
    {
        this.validateLayout( );
        this.validateAxes( );
    }

    public void validateAxes( )
    {
        this.lock.lock( );
        try
        {
            commonAxis.validate( );
            for ( PlotInfo info : stackedPlots.values( ) )
            {
                Axis2D axis = info.getLayout( ).getAxis( );
                if ( axis != null )
                {
                    axis.getAxisX( ).validate( );
                    axis.getAxisY( ).validate( );
                }
            }
        }
        finally
        {
            this.lock.unlock( );
        }
    }

    public void validateLayout( )
    {
        updatePainterLayout( );
    }

    /**
     * @deprecated {@link #removePlot(Object)}
     * @param id
     */
    public void deletePlot( Object id )
    {
        removePlot( id );
    }

    public void removePlot( Object id )
    {
        this.lock.lock( );
        try
        {
            PlotInfo info = stackedPlots.remove( id );
            if ( info == null ) return;

            info.removePlot( );

            if ( isAutoValidate( ) ) validate( );
        }
        finally
        {
            this.lock.unlock( );
        }
    }

    public void addPlot( PlotInfo info )
    {
        if ( info.getStackedPlot( ) != this )
        {
            throw new IllegalArgumentException( "Only PlotInfo created by this StackedPlot2D may be added." );
        }

        this.lock.lock( );
        try
        {
            stackedPlots.put( info.getId( ), info );
            addLayout( info.getBaseLayout( ) );

            if ( isAutoValidate( ) ) validate( );
        }
        finally
        {
            this.lock.unlock( );
        }
    }

    public PlotInfo createPlot( )
    {
        return createPlot( UUID.randomUUID( ) );
    }

    public PlotInfo createPlot( Object id )
    {
        return createPlot( id, new Axis1D( ) );
    }

    public PlotInfo createPlot( Object id, Axis1D axis )
    {
        this.lock.lock( );
        try
        {
            PlotInfo info = createPlot0( id, axis );
            stackedPlots.put( id, info );

            if ( isAutoValidate( ) ) validate( );

            return info;
        }
        finally
        {
            this.lock.unlock( );
        }
    }

    public Axis1D getCommonAxis( Axis2D axis )
    {
        return orient == HORIZONTAL ? axis.getAxisY( ) : axis.getAxisX( );
    }

    public Axis1D getOrthogonalAxis( Axis2D axis )
    {
        return orient == HORIZONTAL ? axis.getAxisX( ) : axis.getAxisY( );
    }

    @Override
    public String toString( )
    {
        return StackedPlot2D.class.getSimpleName( );
    }

    //////////////////////////////////////
    //              Internals           //
    //////////////////////////////////////

    protected List<PlotInfo> getSortedPlots( Collection<PlotInfo> unsorted )
    {
        List<PlotInfo> sortedList = new ArrayList<PlotInfo>( );

        sortedList.addAll( unsorted );

        // this sort is guaranteed to be stable
        // LinkedHashMap ensures that the unsorted array will
        // be in the order that plots were added
        // this means that plots with the same order constant
        // will be displayed in the order they were added
        Collections.sort( sortedList, PlotInfoImpl.getComparator( ) );

        return sortedList;
    }

    // must be called while holding lock
    protected PlotInfo createPlot0( Object id, Axis1D axis )
    {
        if ( id == null )
        {
            throw new IllegalArgumentException( "Plot ID cannot be null." );
        }
        else if ( stackedPlots.containsKey( id ) )
        {
            throw new IllegalArgumentException( "Plot ID: " + id + " already exists." );
        }

        int order = 0;
        int size = -1;

        GlimpseAxisLayout2D layout = new GlimpseAxisLayout2D( null, id.toString( ), createAxis2D( axis ) );
        layout.setLookAndFeel( laf );

        addLayout( layout );
        PlotInfo info = new PlotInfoImpl( this, id, order, size, plotSpacing, layout );
        return info;
    }

    protected Axis2D createAxis2D( Axis1D axis )
    {
        Axis1D commonChildAxis = commonAxis.clone( );
        Axis2D axis2D = orient == HORIZONTAL ? new Axis2D( axis, commonChildAxis ) : new Axis2D( commonChildAxis, axis );

        return axis2D;
    }

    @Override
    public void setLookAndFeel( LookAndFeel laf )
    {
        super.setLookAndFeel( laf );

        this.laf = laf;
    }

    //////////////////////////////////////
    //           Inner Classes          //
    //////////////////////////////////////

    public enum Orientation
    {
        HORIZONTAL, VERTICAL;
    }
}
