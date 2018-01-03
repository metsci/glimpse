package com.metsci.glimpse.topo;

import static com.metsci.glimpse.axis.UpdateMode.CenterScale;
import static com.metsci.glimpse.util.GeneralUtils.floats;
import static com.metsci.glimpse.util.math.MathConstants.HALF_PI;
import static java.lang.Math.PI;

import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.axis.WrappedAxis1D;
import com.metsci.glimpse.painter.base.GlimpsePainter;
import com.metsci.glimpse.painter.decoration.BackgroundPainter;
import com.metsci.glimpse.painter.decoration.BorderPainter;
import com.metsci.glimpse.painter.group.WrappedPainter;
import com.metsci.glimpse.plot.MultiAxisPlot2D;
import com.metsci.glimpse.topo.io.TopoDataset;
import com.metsci.glimpse.topo.proj.EquirectNormalCylindricalProjection;
import com.metsci.glimpse.topo.proj.MercatorNormalCylindricalProjection;
import com.metsci.glimpse.topo.proj.NormalCylindricalProjection;

public class TopoQuickUtils
{

    public static MultiAxisPlot2D quickTopoPlot( TopoDataset topoDataset, NormalCylindricalProjection proj, GlimpsePainter... painters )
    {
        MultiAxisPlot2D plot = new MultiAxisPlot2D( )
        {
            @Override
            protected void initializeCenterAxis( )
            {
                this.centerAxisX = new WrappedAxis1D( proj.lonToX( -PI ), proj.lonToX( +PI ) );
                this.centerAxisY = new Axis1D( );
            }
        };

        plot.setShowTitle( false );
        plot.setBorderSize( 5 );

        Axis2D axis = plot.getCenterAxis( );
        axis.lockAspectRatioXY( 1.0 );
        axis.getAxisX( ).setUpdateMode( CenterScale );
        axis.getAxisY( ).setUpdateMode( CenterScale );
        axis.set( proj.lonToX( -PI ), proj.lonToX( +PI ), proj.latToY( -HALF_PI ), proj.latToY( +HALF_PI ) );
        axis.validate( );

        BackgroundPainter backgroundPainter = new BackgroundPainter( );
        backgroundPainter.setColor( floats( 0.7f, 0.7f, 0.7f, 1 ) );

        GlimpsePainter topoPainter = createTopoPainter( topoDataset, proj );

        WrappedPainter wrappedPainter = new WrappedPainter( true );
        wrappedPainter.addPainter( backgroundPainter );
        wrappedPainter.addPainter( topoPainter );
        for ( GlimpsePainter painter : painters )
        {
            wrappedPainter.addPainter( painter );
        }

        plot.getLayoutCenter( ).addPainter( wrappedPainter );
        plot.getLayoutCenter( ).addPainter( new BorderPainter( ) );

        return plot;
    }

    public static GlimpsePainter createTopoPainter( TopoDataset topoDataset, NormalCylindricalProjection proj )
    {
        // FIXME: Awkward
        if ( proj instanceof EquirectNormalCylindricalProjection )
        {
            return new EquirectTopoPainter( topoDataset, ( EquirectNormalCylindricalProjection ) proj );
        }
        else if ( proj instanceof MercatorNormalCylindricalProjection )
        {
            return new MercatorTopoPainter( topoDataset, ( MercatorNormalCylindricalProjection ) proj );
        }
        else
        {
            throw new RuntimeException( "Unrecognized projection subclass: " + proj.getClass( ).getName( ) );
        }
    }

}
