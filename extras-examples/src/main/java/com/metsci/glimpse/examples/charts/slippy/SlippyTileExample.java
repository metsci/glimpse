/*
 * Copyright (c) 2019, Metron, Inc.
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
package com.metsci.glimpse.examples.charts.slippy;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JMenuBar;
import javax.swing.JRadioButton;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import com.metsci.glimpse.axis.listener.mouse.AxisMouseListener;
import com.metsci.glimpse.axis.painter.label.AxisUnitConverters;
import com.metsci.glimpse.charts.slippy.SlippyAxisListener2D;
import com.metsci.glimpse.charts.slippy.SlippyAxisMouseListener2D;
import com.metsci.glimpse.charts.slippy.SlippyMapPainter;
import com.metsci.glimpse.charts.slippy.SlippyPainterFactory;
import com.metsci.glimpse.examples.Example;
import com.metsci.glimpse.layout.GlimpseLayout;
import com.metsci.glimpse.layout.GlimpseLayoutProvider;
import com.metsci.glimpse.painter.geo.ScalePainter;
import com.metsci.glimpse.plot.MultiAxisPlot2D;
import com.metsci.glimpse.util.geo.LatLonGeo;
import com.metsci.glimpse.util.geo.projection.GeoProjection;
import com.metsci.glimpse.util.geo.projection.TangentPlane;
import com.metsci.glimpse.util.units.Length;

public class SlippyTileExample implements GlimpseLayoutProvider
{

    public static void main( String[] args ) throws Exception
    {
        try
        {
            UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName( ) );
        }
        catch ( UnsupportedLookAndFeelException e )
        {
        }

        SlippyTileExample slippy = new SlippyTileExample( );
        Example example = Example.showWithSwing( slippy );

        SwingUtilities.invokeLater( ( ) -> {
            example.getFrame( ).setJMenuBar( slippy.mapToolBar );
            example.getFrame( ).validate( );
        } );
    }

    private JMenuBar mapToolBar;

    @Override
    public GlimpseLayout getLayout( ) throws Exception
    {
        final GeoProjection geoProj = new TangentPlane( LatLonGeo.fromDeg( 38.958374, -77.358548 ) );

        // create a plot with a custom mouse listener that restricts axis mouse wheel zooming
        // to discrete steps where the imagery tiles will appear 1 screen pixel per image pixel
        // this makes text in the images look sharper
        final MultiAxisPlot2D mapPlot = new MultiAxisPlot2D( )
        {
            @Override
            protected AxisMouseListener createAxisMouseListenerXY( )
            {
                return new SlippyAxisMouseListener2D( geoProj );
            }
        };

        // add an axis listener which initializes the axis bounds to a zoom level where
        // the imagery tiles will appear 1 screen pixel per image pixel (similar in purpose
        // to SlippyAxisMouseListener2D above, except this fires only once when the plot is
        // initialized as opposed to every time the user scrolls the mouse wheel)
        mapPlot.getCenterAxis( ).addAxisListener( new SlippyAxisListener2D( geoProj ) );

        // set the bounds of the initial view
        double rad = Length.fromKilometers( 1 );
        mapPlot.getCenterAxis( ).lockAspectRatioXY( 1 );
        mapPlot.getCenterAxis( ).set( -rad, rad, -rad, rad );

        final SlippyMapPainter mapPainter = SlippyPainterFactory.getOpenStreetMaps( geoProj );
        mapPlot.addPainter( mapPainter );
        mapPainter.setVisible( false );

        final SlippyMapPainter cartoLightPainter = SlippyPainterFactory.getCartoMap( geoProj, true, true );
        mapPlot.addPainter( cartoLightPainter );

        final SlippyMapPainter cartoDarkPainter = SlippyPainterFactory.getCartoMap( geoProj, false, false );
        mapPlot.addPainter( cartoDarkPainter );
        cartoDarkPainter.setVisible( false );

        ScalePainter scalePainter = new ScalePainter( );
        scalePainter.setUnitConverter( AxisUnitConverters.suShownAsMeters );
        scalePainter.setUnitLabel( "m" );
        mapPlot.addPainter( scalePainter );

        this.mapToolBar = new JMenuBar( );
        ButtonGroup group = new ButtonGroup( );
        final JRadioButton mapCheckBox = new JRadioButton( "OpenStreetMap Layer", mapPainter.isVisible( ) );
        final JRadioButton cartoLightCheckBox = new JRadioButton( "CartoDBLight (Labels)", cartoLightPainter.isVisible( ) );
        final JRadioButton cartoDarkcheckBox = new JRadioButton( "CartoDBDark (No Labels)", cartoDarkPainter.isVisible( ) );

        group.add( mapCheckBox );
//        group.add( satcheckBox );
        group.add( cartoLightCheckBox );
        group.add( cartoDarkcheckBox );

        ActionListener l = new ActionListener( )
        {
            @Override
            public void actionPerformed( ActionEvent e )
            {
                mapPainter.setVisible( mapCheckBox.isSelected( ) );
                cartoLightPainter.setVisible( cartoLightCheckBox.isSelected( ) );
                cartoDarkPainter.setVisible( cartoDarkcheckBox.isSelected( ) );
            }
        };

        mapToolBar.add( mapCheckBox );
        mapToolBar.add( cartoLightCheckBox );
        mapToolBar.add( cartoDarkcheckBox );

        mapCheckBox.addActionListener( l );
        cartoLightCheckBox.addActionListener( l );
        cartoDarkcheckBox.addActionListener( l );

        return mapPlot;
    }

}
