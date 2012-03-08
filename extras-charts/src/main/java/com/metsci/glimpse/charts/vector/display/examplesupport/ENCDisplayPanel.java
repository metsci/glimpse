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
package com.metsci.glimpse.charts.vector.display.examplesupport;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.xml.bind.JAXBException;

import org.xml.sax.SAXException;

import com.metsci.glimpse.axis.UpdateMode;
import com.metsci.glimpse.canvas.SwingGlimpseCanvas;
import com.metsci.glimpse.charts.vector.display.BasicSkin;
import com.metsci.glimpse.charts.vector.display.BasicSkinHelper;
import com.metsci.glimpse.charts.vector.display.ColorPalette;
import com.metsci.glimpse.charts.vector.display.ContentHandler;
import com.metsci.glimpse.charts.vector.display.DummySkin;
import com.metsci.glimpse.charts.vector.display.Skin;
import com.metsci.glimpse.charts.vector.iteration.GeoFilterableRecordList;
import com.metsci.glimpse.charts.vector.painter.EncChartPainter;
import com.metsci.glimpse.charts.vector.painter.EncPainterUtils;
import com.metsci.glimpse.charts.vector.parser.objects.GeoObject;
import com.metsci.glimpse.charts.vector.parser.objects.GeoShape;
import com.metsci.glimpse.layout.GlimpseLayout;
import com.metsci.glimpse.painter.decoration.CopyrightPainter;
import com.metsci.glimpse.painter.info.MeasurementPainter;
import com.metsci.glimpse.plot.MapPlot2D;
import com.metsci.glimpse.util.geo.LatLonGeo;
import com.metsci.glimpse.util.geo.projection.GeoProjection;
import com.metsci.glimpse.util.units.Length;
import com.metsci.glimpse.util.vector.Vector2d;

import java.util.ArrayList;

/**
 * Panel to display the GeoObject.  Provides controls to change the foreground 
 * and background skin and several geo filter.
 * 
 * @author Cunningham
 *
 */
public class ENCDisplayPanel<V extends GeoObject>
{
    private static Logger logger = Logger.getLogger( ENCDisplayPanel.class.toString( ) );

    private SwingGlimpseCanvas canvas;
    private GeoProjection projection;
    private GeoFilterableRecordList<V> encList;
    private BasicSkinHelper<V> skinHelper;
    private String skinResource;

    private MapPlot2D geoplot;

    private EncChartPainter<V> backgroundEncPainter;
    private EncChartPainter<V> foregroundEncPainter;

    private V encOnDisplay;
    private List<Skin<V>> selectedForegroundSkins;
    private List<Skin<V>> selectedBackgroundSkins;

    private DelegateSkin<V> normalSkin;
    private DelegateSkin<V> grayscaleSkin;
    private DelegateSkin<V> redHighlightSkin;

    private boolean autoRezoom = false;

    public ENCDisplayPanel( SwingGlimpseCanvas canvas,
                            GeoProjection projection,
                            GeoFilterableRecordList<V> sourceENCList,
                            String skinResource,
                            BasicSkinHelper<V> skinHelper) throws Exception
    {
        this.canvas = canvas;
        this.projection = projection;
        this.encList = sourceENCList;
        this.skinHelper = skinHelper;
        this.skinResource = skinResource;
        this.loadSkins( );

        GlimpseLayout plot = buildGeoPlot( projection );
        this.canvas.addLayout( plot );

        this.backgroundEncPainter.addENCObjects( sourceENCList.iterator( ) );

        this.canvas.setPreferredSize( new Dimension( 500, 500 ) );
    }

    public void displayForegroundPainter( boolean isVisible )
    {
        foregroundEncPainter.setVisible( isVisible );
    }

    public void setAutoRezoom( boolean auto_rezoom )
    {
        autoRezoom = auto_rezoom;
    }

    private void loadSkins( ) throws JAXBException, IOException, SAXException
    {
        logger.fine("loading skin resource " + skinResource);
        normalSkin = new DelegateSkin<V>( new BasicSkin<V>( skinHelper, skinResource ) );
        grayscaleSkin = new DelegateSkin<V>( skinHelper.createDefaultHardCodedSkin( ColorPalette.createGrayscaleColorPalette( ) ) );
        redHighlightSkin = new DelegateSkin<V>( new DummySkin<V>( Color.red ) );
    }

    public void reloadSkins( ) throws Exception
    {
        normalSkin.setSkin( new BasicSkin<V>( skinHelper, skinResource ) );
        grayscaleSkin.setSkin( skinHelper.createDefaultHardCodedSkin( ColorPalette.createGrayscaleColorPalette( ) ) );
        redHighlightSkin.setSkin( new DummySkin<V>( Color.red ) );

        backgroundEncPainter.changeSkins( encList.iterator( ), selectedBackgroundSkins );
        if ( encOnDisplay == null )
            foregroundEncPainter.changeSkins( Collections.<V> emptyList( ).iterator( ), selectedForegroundSkins );
        else
            foregroundEncPainter.changeSkins( Collections.<V> singletonList( encOnDisplay ).iterator( ), selectedForegroundSkins );
    }

    public void newENCSource( GeoFilterableRecordList<V> encList ) throws Exception
    {
        this.encList = encList;

        backgroundEncPainter.clearAll( );
        backgroundEncPainter.addENCObjects( encList.iterator( ) );

        foregroundEncPainter.clearAll( );
        encOnDisplay = null;
    }

    public void populateListenBackgroundCombo( JComboBox backgroundCombo )
    {
        backgroundCombo.addItem( new SelectableComboItem<List<Skin<V>>>( "Color Skin", sugar( normalSkin ) ) );
        backgroundCombo.addItem( new SelectableComboItem<List<Skin<V>>>( "Grayscale Skin", sugar( grayscaleSkin ) ) );
        backgroundCombo.setSelectedIndex( 0 );
        @SuppressWarnings("unchecked")
        List<Skin<V>> tmpSelectedBackgroundSkins  = ( ( SelectableComboItem<List<Skin<V>>> ) backgroundCombo.getSelectedItem( ) ).getItem( );
        selectedBackgroundSkins = tmpSelectedBackgroundSkins;
        backgroundCombo.addActionListener( new ActionListener( )
        {
            @Override
            public void actionPerformed( ActionEvent e )
            {
                JComboBox combo = ( JComboBox ) e.getSource( );
                @SuppressWarnings("unchecked")
                SelectableComboItem<List<Skin<V>>> selectedItem = ( SelectableComboItem<List<Skin<V>>> ) combo.getSelectedItem( );
                selectedBackgroundSkins = selectedItem.getItem( );
                try
                {
                    backgroundEncPainter.changeSkins( encList.iterator( ), selectedBackgroundSkins );
                }
                catch ( Exception ex )
                {
                    logger.log( Level.SEVERE, null, ex );
                    JOptionPane.showMessageDialog( canvas, ex.getMessage( ) );
                }
            }
        } );
    }

    public void populateListenForegroundCombo( JComboBox foregroundCombo )
    {
        foregroundCombo.addItem( new SelectableComboItem<List<Skin<V>>>( "Red Highlight Skin", sugar( redHighlightSkin ) ) );
        foregroundCombo.addItem( new SelectableComboItem<List<Skin<V>>>( "Color Skin", sugar( normalSkin ) ) );
        foregroundCombo.addItem( new SelectableComboItem<List<Skin<V>>>( "Color 1st, Red 2nd", sugar( normalSkin, redHighlightSkin ) ) );
        foregroundCombo.setSelectedIndex( 0 );

        @SuppressWarnings("unchecked")
        List<Skin<V>> tmpSelectedForegroundSkins  = ( ( SelectableComboItem<List<Skin<V>>> ) foregroundCombo.getSelectedItem( ) ).getItem( );
        selectedForegroundSkins = tmpSelectedForegroundSkins;
        foregroundCombo.addActionListener( new ActionListener( )
        {
            @Override
            public void actionPerformed( ActionEvent e )
            {
                JComboBox foregroundCombo = ( JComboBox ) e.getSource( );
                @SuppressWarnings("unchecked")
                SelectableComboItem<List<Skin<V>>> selectedItem = ( SelectableComboItem<List<Skin<V>>> ) foregroundCombo.getSelectedItem( );
                selectedForegroundSkins = selectedItem.getItem( );
                try
                {
                    foregroundEncPainter.changeSkins( Collections.<V> singleton( encOnDisplay ).iterator( ), selectedForegroundSkins );
                }
                catch ( Exception ex )
                {
                    logger.log( Level.SEVERE, null, ex );
                    JOptionPane.showMessageDialog( canvas, ex.getMessage( ) );
                }
            }
        } );
    }

    public void positionAxisOnMapCenter( ) throws IOException
    {
        // Several enc object types tend to surround the whole map.  These types have
        // been assigned into one of two sets - more reliable and reliable but not
        // quite as reliable.
        //
        // Search the enc list for an object of the most reliable surround-map type. If
        // found, position axis around it.  If none found, search the enc list for an
        // object of the second most reliable type.  Of the objects of this type, choose
        // the one with the most vertices.
        //
        // If no object contain types in either of those two sets, simply pick the first
        // object that has more than 1 vertex. Otherwise, do nothing.
        int size = encList.size( );
        if ( size == 0 ) return;
        V bestBoundaryObject = null;
        V secondBestBoundaryObject = null;
        int bestNumVertexes = -1;
        for ( int i = ( size - 1 ); i >= 0; i-- )
        {
            V enc = encList.get( i );

            if ( skinHelper.isBestBoundaryType( enc ) )
            {
                bestBoundaryObject = enc;
                //positionAxisAroundObject( enc );
                break;
            }

            if ( skinHelper.isSecondBestBoundaryType( enc ) )
            {
                if ( !enc.getGeoShapes( ).isEmpty( ) )
                {
                    int numVertexes = enc.getFirstGeoShape( ).getNumCoordinates( );
                    if ( numVertexes > bestNumVertexes )
                    {
                        secondBestBoundaryObject = enc;
                        bestNumVertexes = numVertexes;
                    }
                }
            }
        }

        if ( bestBoundaryObject != null )
            positionAxisAroundObject( bestBoundaryObject );
        else if ( secondBestBoundaryObject != null )
            positionAxisAroundObject( secondBestBoundaryObject );
        else
        {
            for ( int i = ( size - 1 ); i >= 0; i-- )
            {
                V enc = encList.get( i );
                int applyCount = positionAxisAroundObject( enc );
                if ( applyCount > 1 ) break;
            }
        }
    }

    public int positionAxisAroundObject( V object )
    {
        BoundingBox box = new BoundingBox( );
        Collection<? extends GeoShape> shapeList = object.getGeoShapes();

        int applyCount = 0;
        for ( GeoShape shape : shapeList )
        {
            int numCoords = shape.getNumCoordinates( );
            for ( int i = 0; i < numCoords; i++ )
            {
                double currLon = shape.getVertex( 0, i );
                double currLat = shape.getVertex( 1, i );
                if ( !EncPainterUtils.validateLongitude( currLat ) || !EncPainterUtils.validateLatitude( currLat ) ) continue;
                Vector2d xy = projection.project( LatLonGeo.fromDeg( currLat, currLon ) );
                box.applyPoint( xy.getX( ), xy.getY( ) );
                applyCount++;
            }
        }

        if ( applyCount == 0 ) return applyCount;

        double xDist = box.getMaxX( ) - box.getMinX( );
        if ( xDist == 0 ) xDist = Length.fromNauticalMiles( 60 );
        double minX = box.getMinX( ) - ( xDist * .1 );
        double maxX = box.getMaxX( ) + ( xDist * .1 );
        double yDist = box.getMaxY( ) - box.getMinY( );
        if ( yDist == 0 ) yDist = Length.fromNauticalMiles( 60 );
        double minY = box.getMinY( ) - ( yDist * .1 );
        double maxY = box.getMaxY( ) + ( yDist * .1 );

        //System.out.println("minX: " + minX + "; maxX: " + maxX + "; minY: " + minY + "; maxY: " + maxY);

        geoplot.getAxisX( ).setMin( minX );
        geoplot.getAxisY( ).setMin( minY );
        geoplot.getAxisX( ).setMax( maxX );
        geoplot.getAxisY( ).setMax( maxY );

        geoplot.validate( );
        //System.out.println("after validate x: " + geoplot.getAxisX() + "; y: " + geoplot.getAxisY());
        return applyCount;
    }

    public void drawObject( V enc )
    {
        if ( encOnDisplay != null ) foregroundEncPainter.clearAll( );

        encOnDisplay = enc;
        foregroundEncPainter.addENCObject( encOnDisplay );
        if ( autoRezoom )
        {
            positionAxisAroundObject( encOnDisplay );
        }
    }

    public JPanel getPanel( )
    {
        return canvas;
    }

    public void setZoomAdjustedDisplay( boolean zoomAdjust )
    {
        backgroundEncPainter.setZoomAdjustedDisplay( zoomAdjust );
    }

    private GlimpseLayout buildGeoPlot( GeoProjection project ) throws Exception
    {
        geoplot = new MapPlot2D( project );

        backgroundEncPainter = new EncChartPainter<V>( project, Collections.<Skin<V>>singletonList(normalSkin) );
        geoplot.addPainter( backgroundEncPainter );

        foregroundEncPainter = new EncChartPainter<V>( project, Collections.<Skin<V>>singletonList(redHighlightSkin) );
        foregroundEncPainter.setZoomAdjustedDisplay( false );
        geoplot.addPainter( foregroundEncPainter );

        geoplot.addPainter( new CopyrightPainter( ) );

        // add a painter which displays angles and distances between points (activated by right clicking)
        geoplot.addPainter( new MeasurementPainter( "m" ) );

        // lock the plot to a 1 to 1 aspect ratio
        geoplot.lockAspectRatioXY( 1.0f );
        geoplot.setUpdateModeXY( UpdateMode.CenterScale );

        // set the plot title
        geoplot.setTitle( skinHelper.geoName() + " Object viewer" );

        // don't show horizontal/vertical bars marking the cursor position
        geoplot.getCrosshairPainter( ).setVisible( false );

        return geoplot;
    }

    private Skin<V>[] sugar( Skin<V>... skins )
    {
        return skins;
    };

    private List<Skin<V>> sugar( Skin<V> skin )
    {
        return Collections.<Skin<V>>singletonList( skin );
    };

    private List<Skin<V>> sugar( Skin<V> skin, Skin<V> skin2 )
    {
        List<Skin<V>> skinList = new ArrayList<Skin<V>>( );
        skinList.add(skin);
        skinList.add(skin2);
        return skinList;
    };

    public UpdateListener<SelectedShapeChange<V>> getSelectedShapeListener( )
    {
        return new UpdateListener<SelectedShapeChange<V>>( )
        {
            @Override
            public void updateOccurred( SelectedShapeChange<V> newShape )
            {
                ENCDisplayPanel.this.drawObject( newShape.getGeoObject( ) );
            }
        };
    }

    private static class DelegateSkin<V extends GeoObject>  implements Skin<V>
    {
        private Skin<V> innerSkin;

        public DelegateSkin( Skin<V> innerSkin )
        {
            this.innerSkin = innerSkin;
        }

        public void setSkin( Skin<V> innerSkin )
        {
            this.innerSkin = innerSkin;
        }

        @Override
        public List<ContentHandler<V>> getHandlersForGeoObject( V encObject )
        {
            return innerSkin.getHandlersForGeoObject( encObject );
        }

        @Override
        public void reset( )
        {
            innerSkin.reset( );
        }

    }
}
