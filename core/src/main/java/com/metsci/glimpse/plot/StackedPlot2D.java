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
package com.metsci.glimpse.plot;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.context.GlimpseTargetStack;
import com.metsci.glimpse.layout.GlimpseAxisLayout2D;
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
    protected int outerBorder = 10;
    protected int plotSpacing = 0;

    protected Axis1D commonAxis;

    protected GlimpseLayoutManagerMig layout;
    protected Map<String, PlotInfo> stackedPlots;

    protected BackgroundPainter backgroundPainter;

    protected Orientation orientation = Orientation.VERTICAL;

    protected LookAndFeel laf;
    
    public StackedPlot2D( Orientation orientation )
    {
        this.orientation = orientation;

        this.initializePlot( );
    }

    public StackedPlot2D( )
    {
        this( Orientation.VERTICAL );
    }

    
    //////////////////////////////////////
    //     Initialization Methods       //
    //////////////////////////////////////

    
    protected void initializePlot( )
    {
        initializeAxes( );
        initializeArrays( );
        initializeLayout( );
        initializePainters( );
        initializeLookAndFeel( );
        updatePainterLayout( );
    }

    protected void initializeAxes( )
    {
        this.commonAxis = createCommonAxis( );
    }

    protected Axis1D createCommonAxis( )
    {
        return new Axis1D( );
    }

    protected void initializeArrays( )
    {
        this.stackedPlots = new LinkedHashMap<String, PlotInfo>( );
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

    protected void updatePainterLayout( )
    {
        this.lock.lock( );
        try
        {
            this.layout.setLayoutConstraints( String.format( "bottomtotop, gapx 0, gapy 0, insets %d %d %d %d", outerBorder, outerBorder, outerBorder, outerBorder ) );

            List<PlotInfo> axisList = getSortedAxes( stackedPlots.values( ) );
            for ( int i = 0; i < axisList.size( ); i++ )
            {
                PlotInfo info = axisList.get( i );

                if ( info.getSize( ) < 0 ) // slight hack, overload negative size to mean "grow to fill available space"
                {
                    String format = "cell %d %d 1 1, push, grow";
                    String layout = orientation == Orientation.HORIZONTAL ? String.format( format, i, 0 ) : String.format( format, 0, i );
                    info.getLayout( ).setLayoutData( layout );
                }
                else
                {
                    if ( orientation == Orientation.HORIZONTAL )
                    {
                        String format = "cell %d %d 1 1, pushy, growy, width %d!, gap 0 0 %d %d";
                        String layout = String.format( format, i, 0, info.getSize( ), i == 0 ? 0 :plotSpacing, i == axisList.size( )-1 ? 0 : plotSpacing );
                        info.getLayout( ).setLayoutData( layout );
                    }
                    else if ( orientation == Orientation.VERTICAL )
                    {
                        String format = "cell %d %d 1 1, pushx, growx, height %d!, gap %d %d 0 0";
                        String layout = String.format( format, 0, i, info.getSize( ), i == 0 ? 0 :plotSpacing, i == axisList.size( )-1 ? 0 : plotSpacing );
                        info.getLayout( ).setLayoutData( layout );
                    }
                }
            }

            this.invalidateLayout( );
        }
        finally
        {
            lock.unlock( );
        }
    }

    
    //////////////////////////////////////
    //          Getter Methods          //
    //////////////////////////////////////

    
    public Orientation getOrientation( )
    {
        return orientation;
    }

    public Axis1D getCommonAxis( )
    {
        return commonAxis;
    }

    public PlotInfo getPlot( String name )
    {
        return this.stackedPlots.get( name );
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
    

    //////////////////////////////////////
    //      Customization Methods       //
    //////////////////////////////////////

    
    public void setPlotSpacing( int size )
    {
        this.plotSpacing = size;
        this.validate( );
    }
    
    public void setBackgroundColor( float[] color )
    {
        this.backgroundPainter.setColor( color );
    }

    public void setBorderSize( int size )
    {
        this.outerBorder = size;
        this.validate( );
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
                info.getLayout( ).getAxis( ).getAxisX( ).validate( );
                info.getLayout( ).getAxis( ).getAxisY( ).validate( );
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

    public void deletePlot( String name )
    {
        this.lock.lock( );
        try
        {
            PlotInfo info = stackedPlots.get( name );

            if ( info == null ) return;

            this.removeLayout( info.getLayout( ) );
            stackedPlots.remove( name );

            validate( );
        }
        finally
        {
            this.lock.unlock( );
        }
    }

    public PlotInfo createPlot( String name )
    {
        return createPlot( name, new Axis1D( ) );
    }

    public PlotInfo createPlot( String name, Axis1D axis )
    {
        this.lock.lock( );
        try
        {
            PlotInfo info = createPlot0( name, axis );
            stackedPlots.put( name, info );
            validate( );
            return info;
        }
        finally
        {
            this.lock.unlock( );
        }
    }
    
    @Override
    public String toString( )
    {
        return StackedPlot2D.class.getSimpleName( );
    }
    
    
    //////////////////////////////////////
    //              Internals           //
    //////////////////////////////////////
    

    protected List<PlotInfo> getSortedAxes( Collection<PlotInfo> unsorted )
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

    protected Axis1D getCommonAxis( Axis2D axis )
    {
        return orientation == Orientation.HORIZONTAL ? axis.getAxisY( ) : axis.getAxisX( );
    }

    protected Axis1D getOrthogonalAxis( Axis2D axis )
    {
        return orientation == Orientation.HORIZONTAL ? axis.getAxisX( ) : axis.getAxisY( );
    }
    
    // must be called while holding lock
    protected PlotInfo createPlot0( String name, Axis1D axis )
    {
        if ( name == null )
        {
            throw new IllegalArgumentException( "Plot ID cannot be null." );
        }
        else if ( stackedPlots.containsKey( name ) )
        {
            throw new IllegalArgumentException( "Plot ID: " + name + " already exists." );
        }

        int order = 0;
        int size = -1;

        Axis1D commonChildAxis = commonAxis.clone( );
        Axis2D axis2D = orientation == Orientation.HORIZONTAL ? new Axis2D( axis, commonChildAxis ) : new Axis2D( commonChildAxis, axis );

        GlimpseAxisLayout2D layout = new GlimpseAxisLayout2D( null, name, axis2D );
        layout.setLookAndFeel( laf );
        
        addLayout( layout );
        PlotInfo info = new PlotInfoImpl( this, name, order, size, layout );
        return info;
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

    public static interface PlotInfo
    {
        /**
         * Gets a reference to the parent StackedPlot2D which this PlotInfo
         * belongs to.
         *
         * @return the parent StackedPlot2D
         */
        public StackedPlot2D getStackedPlot( );
        
        /**
         * Gets the unique identifier assigned to this plot. This identifier can
         * be used to retrieve this plot handle from the StackedPlot2D.
         *
         * @return the plot unique identifier
         */
        public String getId( );

        /**
         * @return the ordering value for this plot
         */
        public int getOrder( );

        /**
         * @return the pixel size of this plot
         */
        public int getSize( );

        /**
         * Sets the ordering of this plot relative to the other plots in the
         * StackedPlot2D. The particular value does not matter, only the
         * values relative to other plots. All plots start with order 0.
         * Plots with the same order value are arranged in the order they
         * were added to the StackedPlot2D.
         *
         * @param order the ordering value for this plot
         */
        public void setOrder( int order );

        /**
         * Sets the size in pixels for this plot. If {@code size < 0}, then
         * the plot will attempt to fill all available space, sharing space
         * evenly with other plots with negative size.<p>
         *
         * For a VERTICAL oriented plot, {@code setSize( )} adjusts the plot
         * height, for a HORIZONTAL oriented plot, the width is adjusted.
         *
         * @param size the size of the plot in pixels.
         */
        public void setSize( int size );

        /**
         * Returns the {@code GlimpseLayout} for this plot. This can be used
         * to add subplots of painters to the plotting area.
         */
        public GlimpseAxisLayout2D getLayout( );

        /**
         * Returns the common axis associated with the given GlimpseTargetStack.
         * Users generally should simply call {@link #getCommonAxis()}.
         */
        public Axis1D getCommonAxis( GlimpseTargetStack stack );

        /**
         * Returns the data axis associated with the given GlimpseTargetStack.
         * Users generally should simply call {@link #getOrthogonalAxis()}.
         */
        public Axis1D getOrthogonalAxis( GlimpseTargetStack stack );

        /**
         * Returns the common axis shared by all the plots in a StackedPlot2D.
         *
         * @return the shared axis for this plot
         */
        public Axis1D getCommonAxis( );

        /**
         * Returns the data axis associated with this plot. The data axes for
         * each GlimpseLayout in a StackedPlot2D are unlinked by default, but
         * they can be linked if desired using {@link com.metsci.glimpse.axis.Axis1D#setParent( Axis1D )}.
         *
         * @return the data axis for this plot
         */
        public Axis1D getOrthogonalAxis( );

        /**
         * Adds the childLayout to the part of the StackedPlot2D represented
         * by this LayoutInfo. Also links the common axis of the child to the
         * common axis of the parent layout.
         *
         * @param childLayout
         */
        public void addLayout( GlimpseAxisLayout2D childLayout );
        
        public void setLookAndFeel( LookAndFeel laf );
    }

    public static class PlotInfoImpl implements PlotInfo
    {
        protected String id;
        protected int order;
        protected int size;
        protected GlimpseAxisLayout2D layout;
        protected StackedPlot2D parent;

        public PlotInfoImpl( StackedPlot2D parent, String id, int order, int size, GlimpseAxisLayout2D layout )
        {
            this.parent = parent;
            this.id = id;
            this.order = order;
            this.size = size;
            this.layout = layout;
        }

        @Override
        public StackedPlot2D getStackedPlot( )
        {
            return parent;
        }

        @Override
        public String getId( )
        {
            return id;
        }

        @Override
        public int getOrder( )
        {
            return order;
        }

        @Override
        public int getSize( )
        {
            return size;
        }

        @Override
        public void setOrder( int order )
        {
            this.order = order;
            this.parent.validate( );
        }

        @Override
        public void setSize( int size )
        {
            this.size = size;
            this.parent.validate( );
        }

        @Override
        public GlimpseAxisLayout2D getLayout( )
        {
            return this.layout;
        }

        @Override
        public Axis1D getCommonAxis( GlimpseTargetStack stack )
        {
            return parent.getCommonAxis( layout.getAxis( stack ) );
        }

        @Override
        public Axis1D getOrthogonalAxis( GlimpseTargetStack stack )
        {
            return parent.getOrthogonalAxis( layout.getAxis( stack ) );
        }

        @Override
        public Axis1D getCommonAxis( )
        {
            return parent.getCommonAxis( layout.getAxis( ) );
        }

        @Override
        public Axis1D getOrthogonalAxis( )
        {
            return parent.getOrthogonalAxis( layout.getAxis( ) );
        }

        @Override
        public void addLayout( GlimpseAxisLayout2D childLayout )
        {
            if ( childLayout.getAxis( ) != null )
            {
                Axis1D childCommonAxis = this.parent.getCommonAxis( childLayout.getAxis( ) );
                Axis1D parentCommonAxis = this.parent.getCommonAxis( this.layout.getAxis( ) );
                childCommonAxis.setParent( parentCommonAxis );
            }

            this.layout.addLayout( childLayout );
        }
        
        @Override
        public void setLookAndFeel( LookAndFeel laf )
        {
            this.layout.setLookAndFeel( laf );
        }

        @Override
        public int hashCode( )
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + ( ( id == null ) ? 0 : id.hashCode( ) );
            return result;
        }

        @Override
        public boolean equals( Object obj )
        {
            if ( this == obj ) return true;
            if ( obj == null ) return false;
            if ( getClass( ) != obj.getClass( ) ) return false;
            PlotInfoImpl other = ( PlotInfoImpl ) obj;
            if ( id == null )
            {
                if ( other.id != null ) return false;
            }
            else if ( !id.equals( other.id ) ) return false;
            return true;
        }
        
        public static Comparator<PlotInfo> getComparator( )
        {
            return new Comparator<PlotInfo>( )
            {
                @Override
                public int compare( PlotInfo axis0, PlotInfo axis1 )
                {
                    if ( axis0.getOrder( ) < axis1.getOrder( ) )
                    {
                        return -1;
                    }
                    else if ( axis0.getOrder( ) > axis1.getOrder( ) )
                    {
                        return 1;
                    }
                    else
                    {
                        return 0;
                    }
                }
            };
        }
    }
}
