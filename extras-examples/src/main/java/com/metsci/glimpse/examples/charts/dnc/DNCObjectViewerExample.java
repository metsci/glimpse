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
package com.metsci.glimpse.examples.charts.dnc;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import com.metsci.glimpse.canvas.SwingGlimpseCanvas;
import com.metsci.glimpse.charts.vector.MercatorProjection;
import com.metsci.glimpse.charts.vector.display.DNCBasicSkinHelper;
import com.metsci.glimpse.charts.vector.display.examplesupport.AttributePanel;
import com.metsci.glimpse.charts.vector.display.examplesupport.ENCDisplayPanel;
import com.metsci.glimpse.charts.vector.display.examplesupport.ObjectSelectPanel;
import com.metsci.glimpse.charts.vector.display.examplesupport.SelectableComboItem;
import com.metsci.glimpse.charts.vector.display.examplesupport.SelectedShapeChange;
import com.metsci.glimpse.charts.vector.display.examplesupport.UpdateListener;
import com.metsci.glimpse.charts.vector.display.examplesupport.UpdatePublisher;
import com.metsci.glimpse.charts.vector.iteration.DNCObjectLoader;
import com.metsci.glimpse.charts.vector.iteration.GeoAttributeExistanceFilter;
import com.metsci.glimpse.charts.vector.iteration.GeoFilter;
import com.metsci.glimpse.charts.vector.iteration.GeoFilterableRecordList;
import com.metsci.glimpse.charts.vector.iteration.GeoRecordListForStream;
import com.metsci.glimpse.charts.vector.parser.objects.DNCAttributeType;
import com.metsci.glimpse.charts.vector.parser.objects.DNCObject;
import com.metsci.glimpse.gl.Jogular;
import com.metsci.glimpse.support.repaint.RepaintManager;
import com.metsci.glimpse.util.geo.projection.GeoProjection;

/**
 * Glimpse has preliminary support for displaying Digital Navigation Chart data
 * available from NGA
 * <p>
 *
 * To use your own data set:
 *
 * 1) Select a chart. One possible source:
 * http://dnc.nga.mil/NGAPortal/DNC.portal
 *
 * 2) Run com.metsci.glimpse.charts.vector.parser.DNCHarvest tool. For now,
 * you'll have to edit the main method of the class to specify data dir and
 * libary coverage type (Harbor, Coastal, Approach, General)
 *
 * 3) The DNCHarvest utility makes use of the native program ogrinfo to convert
 * DNC files into Metron's internal DNC format. It also assumes that the
 * executable is located at /usr/bin/ogrinfo. If not running linux, or if the
 * executable is in a different location, the field s57parserLocation of class
 * com.metsci.glimpse.charts.vector.parser.OGRInfo must be modified. **THIS
 * NEEDS TO BE FIXED***
 *
 * 4) More information about ogrinfo can be found at:
 * http://www.gdal.org/ogrinfo.html
 *
 * 5) The [DATA_DIR] parameter should point to the directory containing the
 * DNC_ROOT directory
 *
 * 6) The DNCHarvest will print to the specified output directory
 *
 * @author cunningham
 * @author ulman
 */

@SuppressWarnings( "serial" )
public class DNCObjectViewerExample extends JPanel
{
    private static Logger logger = Logger.getLogger( DNCObjectViewerExample.class.toString( ) );

    private UpdatePublisher<SelectedShapeChange<DNCObject>> selectedShapesPublisher;
    private ObjectSelectPanel<DNCObject> objectSelectPanel;
    private AttributePanel<DNCObject> attributePanel;
    private ENCDisplayPanel<DNCObject> glimpsePanel;
    private JButton priorObject;
    private JButton nextObject;
    private JLabel mapName;
    private JComboBox filterCombo;
    private JCheckBox zoomAdjustCheckbox;

    private int objectIndex;
    private GeoFilterableRecordList<DNCObject> sourceENCList;
    private DNCObject currentENCObject;

    private boolean iAmPublishing = false;

    public DNCObjectViewerExample( SwingGlimpseCanvas canvas, GeoProjection GeoProjection, String encResource ) throws Exception
    {
        GeoFilterableRecordList<DNCObject> encSource = new GeoRecordListForStream<DNCObject>( new DNCObjectLoader( ), encResource );
        commonConstructor( canvas, GeoProjection, encSource );

        setMapName( encResource );
    }

    public DNCObjectViewerExample( SwingGlimpseCanvas canvas, GeoProjection GeoProjection, GeoFilterableRecordList<DNCObject> sourceENCList ) throws Exception
    {
        commonConstructor( canvas, GeoProjection, sourceENCList );

        setMapName( null );
    }

    private void commonConstructor( SwingGlimpseCanvas canvas, GeoProjection projection, final GeoFilterableRecordList<DNCObject> sourceENCList ) throws Exception
    {
        selectedShapesPublisher = new UpdatePublisher<SelectedShapeChange<DNCObject>>( );
        this.sourceENCList = sourceENCList;
        objectIndex = -1;

        setLayout( new BorderLayout( ) );
        setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5 ) );

        JSplitPane outerSplitPane = new JSplitPane( JSplitPane.VERTICAL_SPLIT );
        add( outerSplitPane, BorderLayout.CENTER );

        //----------- inner split pane
        JSplitPane innerSplitPane = new JSplitPane( JSplitPane.VERTICAL_SPLIT );
        innerSplitPane.setDividerLocation( .5 );
        innerSplitPane.setResizeWeight( .5 );
        outerSplitPane.setTopComponent( innerSplitPane );

        objectSelectPanel = new ObjectSelectPanel<DNCObject>( selectedShapesPublisher );
        objectSelectPanel.setList( sourceENCList );
        innerSplitPane.setTopComponent( objectSelectPanel );

        attributePanel = new AttributePanel<DNCObject>( );
        innerSplitPane.setBottomComponent( attributePanel );
        //----------- inner split pane end

        JPanel glimpsePanelAndSkinNavControls = new JPanel( new BorderLayout( ) );
        outerSplitPane.setBottomComponent( glimpsePanelAndSkinNavControls );
        outerSplitPane.setDividerLocation( 450 );

        glimpsePanel = new ENCDisplayPanel<DNCObject>( canvas, projection, sourceENCList, "data/dncskin.xml", new DNCBasicSkinHelper( ) );
        glimpsePanel.getPanel( ).setMinimumSize( new Dimension( 0, 200 ) );
        glimpsePanelAndSkinNavControls.add( glimpsePanel.getPanel( ), BorderLayout.CENTER );

        JPanel northPanel = createSkinSelectionPanel( );
        glimpsePanelAndSkinNavControls.add( northPanel, BorderLayout.NORTH );

        JPanel southPanel = createRecordNavigationPanel( glimpsePanel );
        glimpsePanelAndSkinNavControls.add( southPanel, BorderLayout.SOUTH );

        selectedShapesPublisher.addUpdateListener( attributePanel.getSelectedShapeListener( ) );
        selectedShapesPublisher.addUpdateListener( glimpsePanel.getSelectedShapeListener( ) );
        selectedShapesPublisher.addUpdateListener( objectSelectPanel.getSelectedShapeListener( ) );
        selectedShapesPublisher.addUpdateListener( new MyUpdateListener( ) );

        assert ( sourceENCList.size( ) > 0 );
        nextRecord( );
        glimpsePanel.positionAxisOnMapCenter( );
    }

    private void nextRecord( ) throws Exception
    {
        ++objectIndex;
        handleRecordChange( true );
    }

    private void priorRecord( ) throws Exception
    {
        --objectIndex;
        handleRecordChange( true );
    }

    private void skipToRecord( int index ) throws Exception
    {
        objectIndex = index;
        handleRecordChange( false );
    }

    private void handleRecordChange( boolean publish ) throws Exception
    {
        currentENCObject = sourceENCList.get( objectIndex );

        priorObject.setEnabled( objectIndex >= 0 );
        nextObject.setEnabled( objectIndex < sourceENCList.size( ) );
        if ( publish )
        {
            iAmPublishing = true;
            selectedShapesPublisher.notifyUpdateOccurred( new SelectedShapeChange<DNCObject>( objectIndex, currentENCObject ) );
            iAmPublishing = false;
        }
    }

    private JPanel createSkinSelectionPanel( )
    {
        JPanel northPanel = new JPanel( );
        BoxLayout northPanelLayout = new BoxLayout( northPanel, BoxLayout.X_AXIS );
        northPanel.setLayout( northPanelLayout );

        northPanel.add( new JLabel( "Background: " ) );
        final JComboBox backgroundCombo = new JComboBox( );
        backgroundCombo.setLightWeightPopupEnabled( false );
        glimpsePanel.populateListenBackgroundCombo( backgroundCombo );

        northPanel.add( backgroundCombo );

        northPanel.add( Box.createHorizontalStrut( 5 ) );

        northPanel.add( new JLabel( "Selected Item: " ) );
        final JComboBox foregroundCombo = new JComboBox( );
        foregroundCombo.setLightWeightPopupEnabled( false );
        glimpsePanel.populateListenForegroundCombo( foregroundCombo );
        northPanel.add( foregroundCombo );

        northPanel.add( Box.createHorizontalStrut( 5 ) );

        final JCheckBox redOverlayCheckbox = new JCheckBox( "Selected Visibility" );
        redOverlayCheckbox.setSelected( true );
        redOverlayCheckbox.addActionListener( new ActionListener( )
        {
            @Override
            public void actionPerformed( ActionEvent e )
            {
                glimpsePanel.displayForegroundPainter( redOverlayCheckbox.isSelected( ) );
            }
        } );
        northPanel.add( redOverlayCheckbox );
        return northPanel;
    }

    private JPanel createRecordNavigationPanel( final ENCDisplayPanel<DNCObject> glimpsePanel )
    {
        final JPanel southPanel = new JPanel( );
        BoxLayout southPanelLayout = new BoxLayout( southPanel, BoxLayout.X_AXIS );
        southPanel.setLayout( southPanelLayout );

        mapName = new JLabel( );
        southPanel.add( mapName );

        southPanel.add( Box.createHorizontalGlue( ) );

        JButton openENCButton = new JButton( "New Map" );
        openENCButton.addActionListener( new ActionListener( )
        {
            public void actionPerformed( ActionEvent e )
            {
                JFileChooser fileChooser = new JFileChooser( System.getProperty( "user.dir" ) );
                int returnVal = fileChooser.showOpenDialog( southPanel );
                if ( returnVal == JFileChooser.APPROVE_OPTION )
                {
                    File file = fileChooser.getSelectedFile( );
                    try
                    {
                        sourceENCList = new GeoRecordListForStream<DNCObject>( new DNCObjectLoader( ), file.getPath( ) );
                        objectSelectPanel.setList( sourceENCList );
                        glimpsePanel.newENCSource( sourceENCList );
                        filterCombo.setSelectedIndex( 0 );

                        objectIndex = -1;
                        nextRecord( );
                        zoomAdjustCheckbox.setSelected( true );
                        glimpsePanel.setZoomAdjustedDisplay( true );
                        glimpsePanel.positionAxisOnMapCenter( );
                        setMapName( file.getName( ) );
                    }
                    catch ( Exception ee )
                    {
                        logger.log( Level.SEVERE, "", ee );
                        JOptionPane.showMessageDialog( southPanel, "Error: " + ee.getMessage( ) );
                    }
                }
            }
        } );
        southPanel.add( openENCButton );

        southPanel.add( Box.createHorizontalStrut( 5 ) );

        JButton refreshButton = new JButton( "Reload Skins" );
        refreshButton.addActionListener( new ActionListener( )
        {
            public void actionPerformed( ActionEvent e )
            {
                try
                {
                    glimpsePanel.reloadSkins( );
                }
                catch ( Exception ex )
                {
                    logger.log( Level.SEVERE, null, ex );
                    JOptionPane.showMessageDialog( DNCObjectViewerExample.this, "Error: " + ex.getMessage( ) );
                }
            }
        } );
        southPanel.add( refreshButton );

        southPanel.add( Box.createHorizontalStrut( 5 ) );

        Vector<SelectableComboItem<GeoFilter<DNCObject>>> filterList = new Vector<SelectableComboItem<GeoFilter<DNCObject>>>( )
        {
            {
                add( new SelectableComboItem<GeoFilter<DNCObject>>( "No Filter", null ) );
                add( new SelectableComboItem<GeoFilter<DNCObject>>( "Has 'nam' (name) attrib", new GeoAttributeExistanceFilter<DNCObject>( DNCAttributeType.NAM ) ) );
            }
        };

        filterCombo = new JComboBox( filterList );
        filterCombo.setLightWeightPopupEnabled( false );

        filterCombo.addActionListener( new ActionListener( )
        {
            public void actionPerformed( ActionEvent e )
            {
                @SuppressWarnings( "unchecked" )
                SelectableComboItem<GeoFilter<DNCObject>> comboItem = ( SelectableComboItem<GeoFilter<DNCObject>> ) filterCombo.getSelectedItem( );
                GeoFilter<DNCObject> filter = comboItem.getItem( );
                if ( filter == null )
                {
                    sourceENCList.clearAllFilters( );
                }
                else
                {
                    sourceENCList.applyFilter( filter, true );
                }

                try
                {
                    objectSelectPanel.setList( sourceENCList );
                    glimpsePanel.newENCSource( sourceENCList );
                }
                catch ( Exception ex )
                {
                    logger.log( Level.SEVERE, null, ex );
                    JOptionPane.showMessageDialog( southPanel, "Error " + ex.getMessage( ) );
                }
            }
        } );

        southPanel.add( filterCombo );

        southPanel.add( Box.createHorizontalStrut( 5 ) );

        zoomAdjustCheckbox = new JCheckBox( "Zoom adjust" );
        zoomAdjustCheckbox.setSelected( true );

        zoomAdjustCheckbox.addActionListener( new ActionListener( )
        {
            @Override
            public void actionPerformed( ActionEvent e )
            {
                glimpsePanel.setZoomAdjustedDisplay( zoomAdjustCheckbox.isSelected( ) );
            }
        } );

        southPanel.add( zoomAdjustCheckbox );

        southPanel.add( Box.createHorizontalStrut( 5 ) );

        priorObject = new JButton( "<--" );

        priorObject.addActionListener( new ActionListener( )
        {
            public void actionPerformed( ActionEvent e )
            {
                try
                {
                    priorRecord( );
                }
                catch ( Exception ex )
                {
                    logger.log( Level.SEVERE, null, ex );
                }
            }
        } );

        southPanel.add( priorObject );

        southPanel.add( Box.createHorizontalStrut( 5 ) );

        nextObject = new JButton( "-->" );
        nextObject.addActionListener( new ActionListener( )
        {
            public void actionPerformed( ActionEvent e )
            {
                try
                {
                    nextRecord( );
                }
                catch ( Exception ex )
                {
                    logger.log( Level.SEVERE, null, ex );
                }
            }
        } );

        southPanel.add( nextObject );

        return southPanel;
    }

    private void setMapName( String encName )
    {
        if ( encName != null )
        {
            int lastIndex = encName.lastIndexOf( '/' );
            if ( lastIndex >= 0 && ( lastIndex != encName.length( ) - 1 ) )
            {
                encName = encName.substring( Math.min( encName.length( ) - 1, lastIndex + 1 ) );
            }

            if ( encName.toLowerCase( ).endsWith( "_bin.txt" ) )
            {
                encName = encName.substring( 0, encName.length( ) - "_bin.txt".length( ) );
            }
            encName = "Map: " + encName;
        }
        else
            encName = "";

        mapName.setText( encName );
    }

    private class MyUpdateListener implements UpdateListener<SelectedShapeChange<DNCObject>>
    {
        public void updateOccurred( SelectedShapeChange<DNCObject> ssc )
        {
            if ( iAmPublishing ) return;

            try
            {
                skipToRecord( ssc.getIndexInFile( ) );
            }
            catch ( Exception ex )
            {
                logger.log( Level.WARNING, null, ex );
            }
        }
    }

    private static JFrame containInJFrame( JPanel mainPanel, String title, Dimension size, ImageIcon icon )
    {
        JFrame frame = new JFrame( );
        if ( title != null ) frame.setTitle( title );
        if ( icon != null )
        {
            frame.setIconImage( icon.getImage( ) );
        }
        frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        if ( size != null ) frame.setPreferredSize( size );
        frame.add( mainPanel );
        frame.pack( );
        return frame;
    }

    public static void main( String[] args )
    {
        try
        {
            Jogular.initJogl( );

            List<String> roots = new ArrayList<String>();
            String dirPath = "/path/to/dnc_dir/";

            if ( args.length > 0 )
            {
                roots.addAll(Arrays.asList(args));
            }
            else
            {
                roots.add("dnc15-harbor");
                //roots.add("dnc15-coastal");
            }

            SwingGlimpseCanvas panel = new SwingGlimpseCanvas( true );
            RepaintManager.newRepaintManager( panel );

            Iterator<String> rootIt = roots.iterator();
            String dncResource =  dirPath + rootIt.next() + ".dnc";
            GeoRecordListForStream<DNCObject> dncSource = new GeoRecordListForStream<DNCObject>( new DNCObjectLoader( ), dncResource );
            while ( rootIt.hasNext() ) {
                dncResource =  dirPath + rootIt.next() + ".dnc";
                dncSource.add( new DNCObjectLoader(), dncResource );
            }

            DNCObjectViewerExample driver = new DNCObjectViewerExample( panel, new MercatorProjection( ), dncSource);

            JFrame frame = containInJFrame( driver, "Shape viewer", new Dimension( 800, 1100 ), null );
            frame.setVisible( true );
        }
        catch ( Throwable t )
        {
            t.printStackTrace( );
        }
    }
}
