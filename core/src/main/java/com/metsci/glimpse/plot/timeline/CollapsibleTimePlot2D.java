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
package com.metsci.glimpse.plot.timeline;

import static javax.media.opengl.GL.GL_MODELVIEW;
import static javax.media.opengl.GL.GL_PROJECTION;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.media.opengl.GL;

import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.context.GlimpseTargetStack;
import com.metsci.glimpse.event.mouse.GlimpseMouseAdapter;
import com.metsci.glimpse.event.mouse.GlimpseMouseEvent;
import com.metsci.glimpse.layout.GlimpseAxisLayout2D;
import com.metsci.glimpse.layout.GlimpseLayout;
import com.metsci.glimpse.painter.base.GlimpsePainterImpl;
import com.metsci.glimpse.painter.info.SimpleTextPainter;
import com.metsci.glimpse.painter.info.SimpleTextPainter.HorizontalPosition;
import com.metsci.glimpse.painter.info.SimpleTextPainter.VerticalPosition;
import com.metsci.glimpse.plot.StackedPlot2D;
import com.metsci.glimpse.plot.timeline.data.Epoch;
import com.metsci.glimpse.plot.timeline.layout.TimePlotInfo;
import com.metsci.glimpse.support.color.GlimpseColor;
import com.metsci.glimpse.support.font.FontUtils;
import com.metsci.glimpse.support.settings.AbstractLookAndFeel;
import com.metsci.glimpse.support.settings.LookAndFeel;

public class CollapsibleTimePlot2D extends StackedTimePlot2D
{
    protected Map<PlotInfo, GroupInfo> childParentMap;

    public CollapsibleTimePlot2D( )
    {
        super( Orientation.VERTICAL );

        this.childParentMap = new HashMap<PlotInfo, GroupInfo>( );
    }

    public CollapsibleTimePlot2D( Epoch epoch )
    {
        super( Orientation.VERTICAL, epoch );

        this.childParentMap = new HashMap<PlotInfo, GroupInfo>( );
    }

    /**
     * Create a collapsible/expandable group of plots.
     */
    public GroupInfo createGroup( PlotInfo... subplots )
    {
        return createGroup( UUID.randomUUID( ), subplots );
    }
    
    public GroupInfo createGroup( Object id, PlotInfo... subplots )
    {
        LinkedList<PlotInfo> list = new LinkedList<PlotInfo>( );
        for ( int i = 0; i < subplots.length; i++ )
            list.add( subplots[i] );

        return createGroup( id, list );
    }

    public GroupInfo createGroup( Object id, Collection<? extends PlotInfo> subplots )
    {
        this.lock.lock( );
        try
        {
            PlotInfo plotInfo = createPlot0( id, new Axis1D( ) );
            GroupInfo group = new GroupInfoImpl( plotInfo, subplots );
            stackedPlots.put( id, group );
            for ( PlotInfo sub : subplots )
            {
                childParentMap.put( sub, group );
            }
            validate( );
            return group;
        }
        finally
        {
            this.lock.unlock( );
        }
    }

    public Collection<GroupInfo> getAllGroups( )
    {
        this.lock.lock( );
        try
        {
            List<GroupInfo> list = new LinkedList<GroupInfo>( );

            for ( PlotInfo plot : getAllPlots( ) )
            {
                if ( plot instanceof GroupInfo ) list.add( ( GroupInfo ) plot );
            }

            return list;
        }
        finally
        {
            this.lock.unlock( );
        }
    }

    @Override
    protected List<PlotInfo> getSortedAxes( Collection<PlotInfo> unsorted )
    {
        // remove children of groups from list of all plots
        List<PlotInfo> ungroupedPlots = new ArrayList<PlotInfo>( );
        ungroupedPlots.addAll( unsorted );

        for ( GroupInfo group : getAllGroups( ) )
        {
            ungroupedPlots.removeAll( group.getChildPlots( ) );
        }

        List<PlotInfo> sortedPlots = new ArrayList<PlotInfo>( );
        sortedPlots.addAll( ungroupedPlots );
        Collections.sort( sortedPlots, PlotInfoImpl.getComparator( ) );

        List<PlotInfo> sortedPlotsCopy = new ArrayList<PlotInfo>( );
        sortedPlotsCopy.addAll( sortedPlots );

        int totalChildren = 0;
        for ( int i = 0; i < sortedPlotsCopy.size( ); i++ )
        {
            PlotInfo plot = sortedPlotsCopy.get( i );

            if ( plot instanceof GroupInfo )
            {
                GroupInfo group = ( GroupInfo ) plot;
                List<PlotInfo> childPlots = new ArrayList<PlotInfo>( );
                childPlots.addAll( group.getChildPlots( ) );
                Collections.sort( childPlots, PlotInfoImpl.getComparator( ) );
                sortedPlots.addAll( i + totalChildren + 1, childPlots );
                totalChildren += childPlots.size( );
            }
        }

        return sortedPlots;
    }

    //XXX hack, overload negative size to mean "grow to fill available space"
    // count the number of plots who are configured to grow in this way
    @Override
    protected int growingPlotCount( List<PlotInfo> list )
    {
        int count = 0;
        for ( PlotInfo info : list )
        {
            // the children of non-expanded groups don't count
            if ( childParentMap != null )
            {
                GroupInfo group = childParentMap.get( info );
                if ( group != null && !group.isExpanded( ) )
                {
                    continue;
                }
            }

            if ( info.getSize( ) < 0 )
            {
                count++;
            }
        }

        return count;
    }

    @Override
    protected void setPlotInfoLayout( PlotInfo info, int i, int size, int growingPlotCount )
    {
        //XXX hack, overload negative size to mean "grow to fill available space"
        boolean grow = info.getSize( ) < 0 || ( growingPlotCount == 0 && info.getId( ).equals( TIMELINE ) );

        if ( isTimeAxisHorizontal( ) )
        {
            boolean show = true;

            // hide the children of non-expanded groups
            if ( childParentMap != null )
            {
                GroupInfo group = childParentMap.get( info );
                if ( group != null && !group.isExpanded( ) )
                {
                    show = false;
                }
            }

            int topSpace = i == 0 || i >= size - 1 ? 0 : plotSpacing;
            int bottomSpace = i >= size - 2 ? 0 : plotSpacing;

            if ( !show )
            {
                String format = "cell %d %d 1 1, id i%2$d, height 0!";
                String layout = String.format( format, 1, i );
                info.getLayout( ).setLayoutData( layout );
                info.getLayout( ).setVisible( false );

                if ( info instanceof TimePlotInfo )
                {
                    TimePlotInfo timeInfo = ( TimePlotInfo ) info;

                    format = "cell %d %d 1 1, height 0!";
                    layout = String.format( format, 0, i );
                    timeInfo.getLabelLayout( ).setLayoutData( layout );
                    timeInfo.getLabelLayout( ).setVisible( false );
                }
            }
            else
            {
                if ( info instanceof GroupInfo )
                {
                    if ( grow )
                    {
                        String format = "cell %d %d 2 1, grow, id i%2$d, gap 0 0 %3$d %4$d";
                        String layout = String.format( format, 0, i, topSpace, bottomSpace );
                        info.getLayout( ).setLayoutData( layout );
                    }
                    else
                    {
                        String format = "cell %d %d 2 1, growx, height %d!, id i%2$d, gap 0 0 %4$d %5$d";
                        String layout = String.format( format, 0, i, info.getSize( ), topSpace, bottomSpace );
                        info.getLayout( ).setLayoutData( layout );
                    }
                }
                else if ( grow )
                {
                    String format = "cell %d %d 1 1, push, grow, id i%2$d, gap 0 0 %3$d %4$d";
                    String layout = String.format( format, 1, i, topSpace, bottomSpace );
                    info.getLayout( ).setLayoutData( layout );
                    info.getLayout( ).setVisible( true );

                    if ( info instanceof TimePlotInfo )
                    {
                        TimePlotInfo timeInfo = ( TimePlotInfo ) info;

                        format = "cell %d %d 1 1, pushy, growy, width %d!, gap 0 0 %4$d %5$d";
                        layout = String.format( format, 0, i, showLabelLayout ? labelLayoutSize : 0, topSpace, bottomSpace );
                        timeInfo.getLabelLayout( ).setLayoutData( layout );
                        timeInfo.getLabelLayout( ).setVisible( showLabelLayout );
                    }
                }
                else
                {
                    String format = "cell %d %d 1 1, pushx, growx, height %d!, id i%2$d, gap 0 0 %4$d %5$d";
                    String layout = String.format( format, 1, i, info.getSize( ), topSpace, bottomSpace );
                    info.getLayout( ).setLayoutData( layout );
                    info.getLayout( ).setVisible( true );

                    if ( info instanceof TimePlotInfo )
                    {
                        TimePlotInfo timeInfo = ( TimePlotInfo ) info;

                        format = "cell %d %d 1 1, width %d!, height %d!, gap 0 0 %5$d %6$d";
                        layout = String.format( format, 0, i, showLabelLayout ? labelLayoutSize : 0, info.getSize( ), topSpace, bottomSpace );
                        timeInfo.getLabelLayout( ).setLayoutData( layout );
                        timeInfo.getLabelLayout( ).setVisible( showLabelLayout );
                    }
                }
            }
        }
        else
        {
            throw new UnsupportedOperationException( "CollapsibleTimePlot2D must have Vertical Layout" );
        }
    }

    public static class GroupLabelPainter extends GlimpsePainterImpl
    {
        public static final int buttonSize = 8;
        public static final int padding = 5;

        protected float[] lineColor = GlimpseColor.getBlack( );

        protected SimpleTextPainter textDelegate;

        protected boolean isExpanded = true;

        public GroupLabelPainter( String name )
        {
            this.textDelegate = new SimpleTextPainter( );
            this.textDelegate.setHorizontalPosition( HorizontalPosition.Left );
            this.textDelegate.setVerticalPosition( VerticalPosition.Top );
            this.textDelegate.setHorizontalLabels( true );
            this.textDelegate.setHorizontalPadding( buttonSize + padding * 2 );
            this.textDelegate.setVerticalPadding( 0 );
            this.textDelegate.setText( name );
            this.textDelegate.setFont( FontUtils.getDefaultPlain( 14 ), true );
        }

        public void setExpanded( boolean isExpanded )
        {
            this.isExpanded = isExpanded;
        }

        public void setText( String text )
        {
            this.textDelegate.setText( text );
        }

        @Override
        protected void paintTo( GlimpseContext context, GlimpseBounds bounds )
        {
            this.textDelegate.paintTo( context );

            int width = bounds.getWidth( );
            int height = bounds.getHeight( );

            GL gl = context.getGL( );

            gl.glMatrixMode( GL_PROJECTION );
            gl.glLoadIdentity( );
            gl.glOrtho( 0, bounds.getWidth( ), 0, bounds.getHeight( ), -1, 1 );

            gl.glMatrixMode( GL_MODELVIEW );
            gl.glLoadIdentity( );

            // Paint Line
            Rectangle2D textBounds = this.textDelegate.getTextBounds( );
            float startY = ( float ) height / 2.0f;
            float startX = ( float ) ( padding + this.textDelegate.getHorizontalPadding( ) + textBounds.getWidth( ) + ( textBounds.getMinX( ) ) - 1 );

            gl.glLineWidth( 1.0f );
            GlimpseColor.glColor( gl, lineColor );

            gl.glBegin( GL.GL_LINES );
            try
            {
                gl.glVertex2f( startX, startY );
                gl.glVertex2f( width, startY );
            }
            finally
            {
                gl.glEnd( );
            }

            float halfSize = buttonSize / 2.0f;
            float centerX = halfSize + padding;
            float centerY = height / 2.0f;

            // Paint Expand/Collapse Button
            gl.glBegin( GL.GL_POLYGON );
            try
            {
                if ( isExpanded )
                {
                    gl.glVertex2f( centerX - halfSize, centerY + halfSize );
                    gl.glVertex2f( centerX + halfSize, centerY + halfSize );
                    gl.glVertex2f( centerX, centerY - halfSize );
                }
                else
                {
                    gl.glVertex2f( centerX - halfSize, centerY - halfSize );
                    gl.glVertex2f( centerX - halfSize, centerY + halfSize );
                    gl.glVertex2f( centerX + halfSize, centerY );
                }
            }
            finally
            {
                gl.glEnd( );
            }
        }

        @Override
        public void setLookAndFeel( LookAndFeel laf )
        {
            super.setLookAndFeel( laf );

            if ( laf != null )
            {
                this.textDelegate.setLookAndFeel( laf );
                this.lineColor = laf.getColor( AbstractLookAndFeel.BORDER_COLOR );
            }
        }
    }

    public static interface GroupInfo extends PlotInfo
    {
        public void setLabelText( String text );
        public String getLabelText( );

        public void addChildPlot( PlotInfo plot );
        public void removeChildPlot( PlotInfo plot );
        public Collection<PlotInfo> getChildPlots( );

        public void setExpanded( boolean expanded );
        public boolean isExpanded( );
    }

    public class GroupInfoImpl implements GroupInfo
    {
        protected Set<PlotInfo> subplots;
        protected PlotInfo group;

        protected GroupLabelPainter labelPainter;
        protected String label;

        protected boolean expanded;

        public GroupInfoImpl( PlotInfo group, Collection<? extends PlotInfo> subplots )
        {
            this.group = group;
            this.subplots = new LinkedHashSet<PlotInfo>( );
            this.subplots.addAll( subplots );
            for ( PlotInfo plot : subplots )
            {
                childParentMap.put( plot, this );
            }

            this.labelPainter = new GroupLabelPainter( "" );
            this.group.getLayout( ).addPainter( this.labelPainter );

            this.group.setSize( 22 );

            this.expanded = true;

            GlimpseLayout layout = this.group.getLayout( );
            layout.setEventConsumer( false );
            layout.setEventGenerator( true );
            layout.addGlimpseMouseListener( new GlimpseMouseAdapter( )
            {
                @Override
                public void mousePressed( GlimpseMouseEvent event )
                {
                    int x = event.getScreenPixelsX( );

                    if ( x < labelLayoutSize )
                    {
                        setExpanded( !expanded );
                        event.setHandled( true );
                    }
                }
            } );
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
            validateLayout( );
        }

        @Override
        public void addChildPlot( PlotInfo plot )
        {
            subplots.add( plot );
            childParentMap.put( plot, this );
            validateLayout( );
        }

        @Override
        public void removeChildPlot( PlotInfo plot )
        {
            subplots.remove( plot );
            childParentMap.remove( plot );
            validateLayout( );
        }

        @Override
        public Collection<PlotInfo> getChildPlots( )
        {
            return Collections.unmodifiableCollection( this.subplots );
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
            for ( PlotInfo plot : subplots )
            {
                plot.setLookAndFeel( laf );
            }
        }
    }
}