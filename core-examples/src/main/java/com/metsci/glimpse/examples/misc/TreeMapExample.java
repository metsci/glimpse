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
package com.metsci.glimpse.examples.misc;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.ZipInputStream;

import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.axis.UpdateMode;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.examples.Example;
import com.metsci.glimpse.layout.GlimpseLayout;
import com.metsci.glimpse.layout.GlimpseLayoutProvider;
import com.metsci.glimpse.painter.info.CursorTextPainter;
import com.metsci.glimpse.painter.info.FpsPainter;
import com.metsci.glimpse.painter.treemap.NestedTreeMap;
import com.metsci.glimpse.painter.treemap.SimpleTreeMapPainter;
import com.metsci.glimpse.painter.treemap.SquarifiedLayout;
import com.metsci.glimpse.plot.Plot2D;
import com.metsci.glimpse.support.colormap.ColorGradient;
import com.metsci.glimpse.support.colormap.ColorGradients;
import com.metsci.glimpse.util.io.StreamOpener;

/**
 * @author borkholder
 * @see com.metsci.glimpse.painter.treemap.SimpleTreeMapPainter
 */
public class TreeMapExample implements GlimpseLayoutProvider
{
    public static void main( String[] args ) throws Exception
    {
        Example.showWithSwing( new TreeMapExample( ) );
    }

    @Override
    public GlimpseLayout getLayout( ) throws Exception
    {
        Plot2D plot = new Plot2D( "treemap" );
        plot.setAxisSizeX( 0 );
        plot.setAxisSizeY( 0 );
        plot.setAxisSizeZ( 0 );
        plot.setTitle( "TreeMap of States" );

        final NestedTreeMap tree = createLargeGeoTree( );

        SimpleTreeMapPainter painter = new SimpleTreeMapPainter( )
        {
            ColorGradient scale = ColorGradients.jet;

            @Override
            protected float[] getTitleBackgroundColor( int nodeId, boolean selected )
            {
                int level = tree.getLevel( nodeId );
                float[] color = new float[4];
                scale.toColor( level / 5f, color );

                if ( selected )
                {
                    color[0] *= 0.4f;
                    color[1] *= 0.4f;
                    color[2] *= 0.4f;
                }

                return color;
            }
        };

        painter.setTreeMapData( tree );
        plot.getLayoutCenter( ).addPainter( painter, Plot2D.DATA_LAYER );
        plot.getLayoutCenter( ).addPainter( new TreeMapHoverPainter( painter ), Plot2D.FOREGROUND_LAYER );

        plot.setAbsoluteMinX( 0 );
        plot.setAbsoluteMaxX( 100 );
        plot.setAbsoluteMinY( 0 );
        plot.setAbsoluteMaxY( 100 );

        plot.getAxisX( ).setUpdateMode( UpdateMode.FixedPixel );
        plot.getAxisY( ).setUpdateMode( UpdateMode.FixedPixel );

        plot.setMinX( plot.getAxisX( ).getAbsoluteMin( ) );
        plot.setMaxX( plot.getAxisX( ).getAbsoluteMax( ) );
        plot.setMinY( plot.getAxisY( ).getAbsoluteMin( ) );
        plot.setMaxY( plot.getAxisY( ).getAbsoluteMax( ) );

        painter.setLayout( new SquarifiedLayout( ) );

        plot.getLayoutCenter( ).addPainter( new FpsPainter( ) );

        return plot;
    }

    static NestedTreeMap createLargeGeoTree( ) throws Exception
    {
        NestedTreeMap tree = new NestedTreeMap( );
        tree.setRoot( 0 );
        tree.setTitle( 0, "US" );

        int idGenerator = 100;
        Map<String, Integer> stateIdMap = new TreeMap<String, Integer>( );
        Map<String, Integer> countyIdMap = new TreeMap<String, Integer>( );

        InputStream fileStream = StreamOpener.resource.openForRead( "data/us_inc_civil_pop_2009.zip" );

        ZipInputStream zipStream = new ZipInputStream( fileStream );
        // advance to the first entry
        zipStream.getNextEntry( );

        BufferedReader reader = new BufferedReader( new InputStreamReader( zipStream ) );

        // skip first two lines
        reader.readLine( );
        reader.readLine( );
        String line = null;
        while ( ( line = reader.readLine( ) ) != null )
        {
            String[] parts = line.split( "\t" );
            String level = parts[0];
            String county = parts[1];
            String city = parts[2];
            String state = parts[3];
            int population = Integer.parseInt( parts[15] );

            String text = "Total Pop: " + population;

            if ( "0".equals( level ) )
            {
                int id = idGenerator++;
                stateIdMap.put( state, id );
                tree.addChild( 0, id, population, state );
                tree.setText( id, text );
            }
            else if ( "1".equals( level ) )
            {
                int stateId = stateIdMap.get( state );

                int id = idGenerator++;
                countyIdMap.put( county, id );
                tree.addChild( stateId, id, population, "(no county)".equals( county ) ? city : county );
                tree.setText( id, text );
            }
            else if ( "2".equals( level ) )
            {
                int countyId = countyIdMap.get( county );

                int id = idGenerator++;
                tree.addChild( countyId, id, population, city );
                tree.setText( id, text );
            }
        }

        reader.close( );
        return tree;
    }

    class TreeMapHoverPainter extends CursorTextPainter
    {
        SimpleTreeMapPainter painter;

        Integer selectedLeafId;

        TreeMapHoverPainter( SimpleTreeMapPainter painter )
        {
            this.painter = painter;
        }

        @Override
        public void doPaintTo( GlimpseContext context )
        {
            Axis2D axis = getAxis2D( context );
            double x = axis.getAxisX( ).getSelectionCenter( );
            double y = axis.getAxisY( ).getSelectionCenter( );

            selectedLeafId = painter.getLeafAt( axis, x, y );

            if ( selectedLeafId != null )
            {
                super.doPaintTo( context );
            }
        }

        @Override
        protected String getTextX( Axis2D axis )
        {
            NestedTreeMap tree = painter.getTreeMapData( );
            StringBuilder builder = new StringBuilder( );
            int id = selectedLeafId;

            while ( id != tree.getRoot( ) )
            {
                String title = tree.getTitle( id );
                builder.append( title );
                builder.append( " < " );
                id = tree.getParent( id );
            }

            builder.append( tree.getTitle( id ) );
            return builder.toString( );
        }

        @Override
        protected String getTextY( Axis2D axis )
        {
            double size = painter.getTreeMapData( ).getSize( selectedLeafId );
            return String.format( "Population: %d", ( int ) size );
        }
    }
}
