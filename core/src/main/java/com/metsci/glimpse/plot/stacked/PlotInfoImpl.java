package com.metsci.glimpse.plot.stacked;

import static com.metsci.glimpse.plot.stacked.StackedPlot2D.Orientation.HORIZONTAL;
import static com.metsci.glimpse.plot.stacked.StackedPlot2D.Orientation.VERTICAL;

import java.util.Comparator;

import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.context.GlimpseTargetStack;
import com.metsci.glimpse.layout.GlimpseAxisLayout2D;
import com.metsci.glimpse.plot.stacked.StackedPlot2D.Orientation;
import com.metsci.glimpse.support.settings.LookAndFeel;

public class PlotInfoImpl implements PlotInfo
{
    protected boolean visible;
    protected boolean grow;
    protected Object id;
    protected int order;
    protected int size;
    protected int spacing;
    protected GlimpseAxisLayout2D layout;
    protected StackedPlot2D parent;

    public PlotInfoImpl( StackedPlot2D parent, Object id, int order, int size, int spacing, GlimpseAxisLayout2D layout )
    {
        this.parent = parent;
        this.id = id;
        this.order = order;
        this.size = size;
        this.spacing = spacing;
        this.layout = layout;
        this.grow = size < 0;
        this.visible = true;
    }

    @Override
    public StackedPlot2D getStackedPlot( )
    {
        return parent;
    }

    @Override
    public Object getId( )
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
    public void setGrow( boolean grow )
    {
        this.grow = grow;
    }

    @Override
    public boolean isGrow( )
    {
        return grow && isVisible( );
    }

    @Override
    public void setOrder( int order )
    {
        this.order = order;
        
        if ( this.parent.isAutoValidate( ) ) this.parent.validate( );
    }

    @Override
    public void setSize( int size )
    {
        this.size = size;
        this.grow = size < 0;
        
        if ( this.parent.isAutoValidate( ) )  this.parent.validate( );
    }

    @Override
    public void setPlotSpacing( int spacing )
    {
        this.spacing = spacing;
    }

    @Override
    public int getPlotSpacing( )
    {
        return this.spacing;
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
    public void setVisible( boolean visible )
    {
        this.visible = visible;
        this.layout.setVisible( visible );
    }

    @Override
    public boolean isVisible( )
    {
        return visible;
    }
    
    @Override
    public void deletePlot( )
    {
        if ( this.parent != null )
        {
            StackedPlot2D oldParent = this.parent;
            this.parent = null;
            
            oldParent.removeLayout( layout );
            oldParent.deletePlot( id );
        }
    }

    @Override
    public void updateLayout( int index )
    {
        Orientation orient = getStackedPlot( ).getOrientation( );

        int plotCount = getStackedPlot( ).getAllPlots( ).size( );
        
        int plotSpacing = getPlotSpacing( );
        int plotSize = getSize( );
        
        if ( !isVisible( ) )
        {
            plotSpacing = 0;
            plotSize = 0;
        }
        
        // no spacing for the last plot (there's no plot beyond it and spacing between
        // it and the edge of the stacked plot is controlled by setBorderSize(int)
        if ( index == plotCount - 1 && orient == HORIZONTAL ) plotSpacing = 0;
        else if ( index == 0 && orient == VERTICAL ) plotSpacing = 0;

        String layoutData = null;
        if ( orient == VERTICAL )
        {
            if ( isGrow( ) )
            {
                String format = "cell %d %d, push, grow, gaptop %d, id i%2$d";
                layoutData = String.format( format, 0, index, plotSpacing );
            }
            else
            {
                String format = "cell %d %d, growx, pushx, height %d!, gaptop %d, id i%2$d";
                layoutData = String.format( format, 0, index, plotSize, plotSpacing );
            }
        }
        else if ( orient == HORIZONTAL )
        {
            if ( isGrow( ) )
            {
                String format = "cell %d %d, push, grow, gapright %d, id i%1$d";
                layoutData = String.format( format, index, 0, plotSpacing );
            }
            else
            {
                String format = "cell %d %d, growy, pushy, width %d!, gapright %d, id i%1$d";
                layoutData = String.format( format, index, 0, plotSize, plotSpacing );
            }
        }

        getLayout( ).setLayoutData( layoutData );
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