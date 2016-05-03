/*
 * Copyright (c) 2016, Metron, Inc.
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
package com.metsci.glimpse.examples.dnc;

import static com.metsci.glimpse.dnc.DncProjections.dncTangentPlane;
import static com.metsci.glimpse.dnc.facc.FaccIo.readFaccAttrs;
import static com.metsci.glimpse.dnc.facc.FaccIo.readFaccFeatures;
import static com.metsci.glimpse.dnc.geosym.DncGeosymThemes.DNC_THEME_STANDARD;
import static com.metsci.glimpse.dnc.util.DncMiscUtils.startThread;
import static com.metsci.glimpse.dnc.util.DncMiscUtils.takeNewValue;
import static com.metsci.glimpse.docking.DockingFrameTitlers.createDefaultFrameTitler;
import static com.metsci.glimpse.docking.DockingGroup.DockingFrameCloseOperation.DISPOSE_ALL_FRAMES;
import static com.metsci.glimpse.docking.DockingThemes.tinyLafDockingTheme;
import static com.metsci.glimpse.docking.DockingUtils.loadDockingArrangement;
import static com.metsci.glimpse.docking.DockingUtils.requireIcon;
import static com.metsci.glimpse.docking.DockingUtils.saveDockingArrangement;
import static com.metsci.glimpse.examples.dnc.DncExampleUtils.initTinyLaf;
import static com.metsci.glimpse.platformFixes.PlatformFixes.fixPlatformQuirks;
import static com.metsci.glimpse.util.logging.LoggerUtils.initializeLogging;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.media.opengl.GLAnimatorControl;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import org.jdesktop.swingx.JXTreeTable;

import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.dnc.DncChunks.DncChunkKey;
import com.metsci.glimpse.dnc.DncFeature;
import com.metsci.glimpse.dnc.DncLineFeature;
import com.metsci.glimpse.dnc.DncPainter;
import com.metsci.glimpse.dnc.DncPainterSettings;
import com.metsci.glimpse.dnc.DncPainterSettingsImpl;
import com.metsci.glimpse.dnc.DncPointFeature;
import com.metsci.glimpse.dnc.DncQuery;
import com.metsci.glimpse.dnc.convert.Flat2Query.QueryCache;
import com.metsci.glimpse.dnc.convert.Flat2Query.QueryCacheConfig;
import com.metsci.glimpse.dnc.convert.Flat2Render.RenderCache;
import com.metsci.glimpse.dnc.convert.Flat2Render.RenderCacheConfig;
import com.metsci.glimpse.dnc.facc.FaccAttr;
import com.metsci.glimpse.dnc.facc.FaccFeature;
import com.metsci.glimpse.dnc.util.DncMiscUtils.ThrowingRunnable;
import com.metsci.glimpse.dnc.util.SingletonEvictingBlockingQueue;
import com.metsci.glimpse.docking.DockingGroup;
import com.metsci.glimpse.docking.DockingGroup.DockingGroupAdapter;
import com.metsci.glimpse.docking.DockingThemes.DockingTheme;
import com.metsci.glimpse.docking.TileFactories.TileFactory;
import com.metsci.glimpse.docking.TileFactories.TileFactoryStandard;
import com.metsci.glimpse.docking.View;
import com.metsci.glimpse.docking.xml.GroupArrangement;
import com.metsci.glimpse.painter.decoration.BorderPainter;
import com.metsci.glimpse.painter.decoration.CrosshairPainter;
import com.metsci.glimpse.painter.info.FpsPainter;
import com.metsci.glimpse.plot.Plot2D;
import com.metsci.glimpse.support.settings.SwingLookAndFeel;
import com.metsci.glimpse.support.swing.NewtSwingEDTGlimpseCanvas;
import com.metsci.glimpse.support.swing.SwingEDTAnimator;

import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;

public class DncQueryExample
{

    public static void main( String[] args ) throws IOException
    {
        initializeLogging( "dnc-examples/logging.properties" );
        fixPlatformQuirks( );
        initTinyLaf( );
        DockingTheme dockingTheme = tinyLafDockingTheme( );



        // Render config
        //

        RenderCacheConfig renderConfig = new RenderCacheConfig( );
        renderConfig.flatParentDir = new File( "/home/mike/metron/data/dnc/DNC_FLAT" );
        renderConfig.renderParentDir = new File( "/home/mike/metron/data/dnc/DNC_RENDER" );

        //renderConfig.proj = dncPlateCarree;
        //renderConfig.proj = dncTangentPlane( 32.7150, -117.1625 ); // San Deigo
        renderConfig.proj = dncTangentPlane( 40.6892, -74.0444 ); // New York

        RenderCache renderCache = new RenderCache( renderConfig, 4 );



        // Query config
        //

        QueryCacheConfig queryConfig = new QueryCacheConfig( );
        queryConfig.flatParentDir = renderConfig.flatParentDir;
        queryConfig.queryParentDir = new File( "/home/mike/metron/data/dnc/DNC_QUERY" );
        queryConfig.proj = renderConfig.proj;

        QueryCache queryCache = new QueryCache( queryConfig, 4 );



        // Create plot
        //

        Plot2D plot = new Plot2D( "" );
        plot.lockAspectRatioXY( 1 );
        plot.setShowMinorTicksX( true );
        plot.setShowMinorTicksY( true );
        plot.setAxisSizeZ( 0 );
        plot.setTitleHeight( 0 );

        DncPainterSettings dncPainterSettings = new DncPainterSettingsImpl( renderConfig.proj );
        DncPainter dncPainter = new DncPainter( renderCache, dncPainterSettings, DNC_THEME_STANDARD );
        dncPainter.activateCoverages( "lim", "nav", "cul", "iwy", "obs", "hyd", "por", "ecr", "lcr", "env", "rel", "coa" );
        dncPainter.addAxis( plot.getAxis( ) );

        plot.getLayoutCenter( ).addPainter( dncPainter );
        plot.getLayoutCenter( ).addPainter( new CrosshairPainter( ) );
        plot.getLayoutCenter( ).addPainter( new FpsPainter( ) );
        plot.getLayoutCenter( ).addPainter( new BorderPainter( ) );



        // Create attrs table
        //

        Map<String,FaccFeature> faccFeatures = readFaccFeatures( );
        Map<String,FaccAttr> faccAttrs = readFaccAttrs( );
        DncQueryExampleTreeTableModel attrsTableModel = new DncQueryExampleTreeTableModel( faccFeatures, faccAttrs );
        JXTreeTable attrsTable = new JXTreeTable( attrsTableModel );
        attrsTable.getTableHeader( ).setReorderingAllowed( false );
        JScrollPane attrsScroller = new JScrollPane( attrsTable );



        // Query
        //

        Predicate<DncFeature> allowHighlight = ( feature ) ->
        {
            if ( feature instanceof DncPointFeature )
            {
                return true;
            }
            else if ( feature instanceof DncLineFeature )
            {
                String coverage = feature.chunkKey.coverage.coverageName;
                return ( coverage.equalsIgnoreCase( "hyd" ) || coverage.equalsIgnoreCase( "ecr" ) || coverage.equalsIgnoreCase( "iwy" ) );
            }
            else
            {
                return false;
            }
        };

        BlockingQueue<DncQuery> queries = new SingletonEvictingBlockingQueue<>( );

        plot.addAxisListener( ( axis ) ->
        {
            Collection<DncChunkKey> chunkKeys = dncPainter.activeChunkKeys( );

            Axis1D xAxis = axis.getAxisX( );
            float xMin = ( float ) ( xAxis.getSelectionCenter( ) - 0.5*xAxis.getSelectionSize( ) );
            float xMax = ( float ) ( xAxis.getSelectionCenter( ) + 0.5*xAxis.getSelectionSize( ) );

            Axis1D yAxis = axis.getAxisY( );
            float yMin = ( float ) ( yAxis.getSelectionCenter( ) - 0.5*yAxis.getSelectionSize( ) );
            float yMax = ( float ) ( yAxis.getSelectionCenter( ) + 0.5*yAxis.getSelectionSize( ) );

            queries.add( new DncQuery( chunkKeys, xMin, xMax, yMin, yMax ) );
        } );

        startThread( "DncQuery", true, new ThrowingRunnable( )
        {
            DncQuery oldQuery = null;
            public void runThrows( ) throws Exception
            {
                while ( true )
                {
                    DncQuery query = takeNewValue( queries, oldQuery );

                    attrsTableModel.retainChunks( new HashSet<>( query.chunkKeys ) );

                    queryCache.runQuery( query, dncPainter.chunkPriorityFunc, ( chunkKey, features ) ->
                    {
                        List<DncFeature> highlighted = features.stream( ).filter( allowHighlight ).collect( Collectors.toList( ) );

                        attrsTableModel.setChunkFeatures( chunkKey, highlighted );

                        IntCollection featureNums = new IntOpenHashSet( );
                        highlighted.stream( ).mapToInt( f -> f.featureNum ).forEach( featureNums::add );
                        dncPainter.highlightFeatures( chunkKey, featureNums );
                    } );

                    oldQuery = query;
                }
            }
        } );



        // Show
        //

        SwingUtilities.invokeLater( ( ) ->
        {
            NewtSwingEDTGlimpseCanvas geoCanvas = new NewtSwingEDTGlimpseCanvas( );
            geoCanvas.addLayout( plot );
            geoCanvas.setLookAndFeel( new SwingLookAndFeel( ) );

            GLAnimatorControl animator = new SwingEDTAnimator( 30 );
            animator.add( geoCanvas.getGLDrawable( ) );
            animator.start( );

            View[] views =
            {
                new View( "geoView",   geoCanvas,     "Geo",      false, null, requireIcon( "icons/fugue/map.png"        ) ),
                new View( "attrsView", attrsScroller, "Features", false, null, requireIcon( "icons/eclipse/class_hi.gif" ) ),
            };

            String appName = "dnc-query-example";
            DockingGroup dockingGroup = new DockingGroup( dockingTheme, DISPOSE_ALL_FRAMES );
            dockingGroup.addListener( createDefaultFrameTitler( "DNC Query Example" ) );

            TileFactory tileFactory = new TileFactoryStandard( dockingGroup );

            GroupArrangement groupArr = loadDockingArrangement( appName, DncQueryExample.class.getClassLoader( ).getResource( "dnc-examples/docking-defaults.xml" ) );
            dockingGroup.restoreArrangement( groupArr, tileFactory, views );
            dockingGroup.addListener( new DockingGroupAdapter( )
            {
                public void disposingAllFrames( DockingGroup group )
                {
                    saveDockingArrangement( appName, dockingGroup.captureArrangement( ) );
                    attrsTableModel.dispose( );
                    animator.stop( );
                }
            } );
        } );
    }

}
