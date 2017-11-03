package com.metsci.glimpse.support.popup;

import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.plot.MultiAxisPlot2D;

public class MultiAxisPlot2DWithPopup extends MultiAxisPlot2D
{

    @Override
    protected GlimpseAxisLayout2DWithPopup createCenterLayout( String name, Axis2D axis )
    {
        return new GlimpseAxisLayout2DWithPopup( this, name, axis );
    }

    @Override
    public GlimpseAxisLayout2DWithPopup getLayoutCenter( )
    {
        return ( GlimpseAxisLayout2DWithPopup ) super.getLayoutCenter( );
    }

}
