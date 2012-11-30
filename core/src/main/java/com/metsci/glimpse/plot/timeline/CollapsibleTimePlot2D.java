package com.metsci.glimpse.plot.timeline;

import static javax.media.opengl.GL.GL_MODELVIEW;
import static javax.media.opengl.GL.GL_PROJECTION;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.media.opengl.GL;

import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.context.GlimpseTargetStack;
import com.metsci.glimpse.layout.GlimpseAxisLayout2D;
import com.metsci.glimpse.painter.base.GlimpsePainterImpl;
import com.metsci.glimpse.painter.info.SimpleTextPainter;
import com.metsci.glimpse.painter.info.SimpleTextPainter.HorizontalPosition;
import com.metsci.glimpse.painter.info.SimpleTextPainter.VerticalPosition;
import com.metsci.glimpse.plot.StackedPlot2D;
import com.metsci.glimpse.plot.timeline.data.Epoch;
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
    public GroupInfo createGroup( String id, PlotInfo... subplots )
    {
        LinkedList<PlotInfo> list = new LinkedList<PlotInfo>( );
        for ( int i = 0; i < subplots.length; i++ )
            list.add( subplots[i] );

        return createGroup( id, list );
    }

    public GroupInfo createGroup( String id, Collection<PlotInfo> subplots )
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
        Set<PlotInfo> ungroupedPlots = new HashSet<PlotInfo>( );
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

    @Override
    protected void setPlotInfoLayout( int i, int size, PlotInfo info )
    {
        super.setPlotInfoLayout( i, size, info );

        if ( info instanceof GroupInfo )
        {
            int topSpace = i == 0 || i >= size - 1 ? 0 : plotSpacing;
            int bottomSpace = i >= size - 2 ? 0 : plotSpacing;

            if ( info.getSize( ) < 0 ) // slight hack, overload negative size to mean "grow to fill available space"
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
    }

    public static class GroupLabelPainter extends GlimpsePainterImpl
    {
        protected final int buttonSize = 20;
        protected final int padding = 5;

        protected float[] lineColor = GlimpseColor.getBlack( );

        protected SimpleTextPainter textDelegate;

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

        public void addChildPlot( PlotInfo plot );

        public void removeChildPlot( PlotInfo plot );

        public Collection<PlotInfo> getChildPlots( );
    }

    public class GroupInfoImpl implements GroupInfo
    {
        protected Set<PlotInfo> subplots;
        protected PlotInfo group;

        protected GroupLabelPainter label;

        public GroupInfoImpl( PlotInfo group, Collection<PlotInfo> subplots )
        {
            this.group = group;
            this.subplots = new HashSet<PlotInfo>( );
            this.subplots.addAll( subplots );

            this.label = new GroupLabelPainter( group.getId( ) );
            this.group.getLayout( ).addPainter( this.label );

            this.group.setSize( 22 );
        }

        @Override
        public void addChildPlot( PlotInfo plot )
        {
            subplots.add( plot );
            childParentMap.put( plot, this );
            getStackedPlot( ).invalidateLayout( );
        }

        @Override
        public void removeChildPlot( PlotInfo plot )
        {
            subplots.remove( plot );
            childParentMap.remove( plot );
            getStackedPlot( ).invalidateLayout( );
        }

        @Override
        public Collection<PlotInfo> getChildPlots( )
        {
            return Collections.unmodifiableCollection( this.subplots );
        }

        @Override
        public void setLabelText( String text )
        {
            this.label.setText( text );
        }

        @Override
        public StackedPlot2D getStackedPlot( )
        {
            return group.getStackedPlot( );
        }

        @Override
        public String getId( )
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
    }
}
