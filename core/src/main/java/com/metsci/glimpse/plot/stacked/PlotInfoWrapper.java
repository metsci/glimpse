package com.metsci.glimpse.plot.stacked;

import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.context.GlimpseTargetStack;
import com.metsci.glimpse.layout.GlimpseAxisLayout2D;
import com.metsci.glimpse.layout.GlimpseLayout;
import com.metsci.glimpse.support.settings.LookAndFeel;
import com.metsci.glimpse.plot.stacked.PlotInfo;


public class PlotInfoWrapper implements PlotInfo
{
    public PlotInfo info;
    
    public PlotInfoWrapper( PlotInfo info )
    {
        this.info = info;
    }
    
    @Override
    public String getLayoutData( )
    {
        return info.getLayoutData( );
    }
    
    @Override
    public void setLayoutData( String layoutData )
    {
        info.setLayoutData( layoutData );
    }

    @Override
    public StackedPlot2D getStackedPlot( )
    {
        return info.getStackedPlot( );
    }

    @Override
    public Object getId( )
    {
        return info.getId( );
    }

    @Override
    public int getOrder( )
    {
        return info.getOrder( );
    }

    @Override
    public int getSize( )
    {
        return info.getSize( );
    }
    
    @Override
    public void setOrder( int order )
    {
        info.setOrder( order );
    }

    @Override
    public void setSize( int size )
    {
        info.setSize( size );
    }
    
    @Override
    public boolean isGrow( )
    {
        return info.isGrow( );
    }
    
    @Override
    public void setGrow( boolean grow )
    {
        info.setGrow( grow );
    }
    

    @Override
    public void setPlotSpacing( int spacing )
    {
        info.setPlotSpacing( spacing );
    }

    @Override
    public int getPlotSpacing( )
    {
        return info.getPlotSpacing( );
    }

    @Override
    public GlimpseAxisLayout2D getLayout( )
    {
        return info.getLayout( );
    }
    
    @Override
    public GlimpseLayout getBaseLayout( )
    {
        return info.getBaseLayout( );
    }

    @Override
    public Axis1D getCommonAxis( GlimpseTargetStack stack )
    {
        return info.getCommonAxis( stack );
    }

    @Override
    public Axis1D getOrthogonalAxis( GlimpseTargetStack stack )
    {
        return info.getOrthogonalAxis( stack );
    }

    @Override
    public Axis1D getCommonAxis( )
    {
        return info.getCommonAxis( );
    }

    @Override
    public Axis1D getOrthogonalAxis( )
    {
        return info.getOrthogonalAxis( );
    }

    @Override
    public void addLayout( GlimpseAxisLayout2D childLayout )
    {
        info.addLayout( childLayout );
    }

    @Override
    public void setLookAndFeel( LookAndFeel laf )
    {
        info.setLookAndFeel( laf );
    }

    @Override
    public void updateLayout( int index )
    {
        info.updateLayout( index );
    }

    @Override
    public void setVisible( boolean visible )
    {
        info.setVisible( visible );
    }

    @Override
    public boolean isVisible( )
    {
        return info.isVisible( );
    }

    @Override
    public void deletePlot( )
    {
        info.deletePlot( );
    }

    @Override
    public void setIndentLevel( int level )
    {
        info.setIndentLevel( level );
    }

    @Override
    public int getIndentLevel( )
    {
        return info.getIndentLevel( );
    }
}