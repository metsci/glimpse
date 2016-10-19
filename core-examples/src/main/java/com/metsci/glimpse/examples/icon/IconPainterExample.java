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
package com.metsci.glimpse.examples.icon;

import static com.metsci.glimpse.util.logging.LoggerUtils.*;

import java.util.Collection;
import java.util.logging.Logger;

import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.axis.listener.mouse.AxisMouseListener2D;
import com.metsci.glimpse.axis.painter.NumericXYAxisPainter;
import com.metsci.glimpse.event.mouse.GlimpseMouseEvent;
import com.metsci.glimpse.event.mouse.GlimpseMouseListener;
import com.metsci.glimpse.event.mouse.MouseButton;
import com.metsci.glimpse.examples.Example;
import com.metsci.glimpse.layout.GlimpseAxisLayout2D;
import com.metsci.glimpse.layout.GlimpseLayout;
import com.metsci.glimpse.layout.GlimpseLayoutProvider;
import com.metsci.glimpse.painter.decoration.BackgroundPainter;
import com.metsci.glimpse.painter.info.FpsPainter;
import com.metsci.glimpse.support.atlas.TextureAtlas;
import com.metsci.glimpse.support.atlas.painter.IconPainter;
import com.metsci.glimpse.support.atlas.painter.IconPainter.PickResult;
import com.metsci.glimpse.support.color.GlimpseColor;
import com.metsci.glimpse.support.selection.SpatialSelectionListener;

/**
 * Demonstrates how to use IconPainter.
 *
 * IconPainter can efficiently render large numbers of icons in fixed
 * locations in data space by leveraging a TextureAtlas to store the icon images.
 *
 * Picking support is also provided, allowing the IconPainter to report on
 * the identity of icons under the current mouse location.
 *
 * Tested with 100,000 individual icons on Nvidia GTX 480. Less with a large number
 * of icon groups.
 *
 * @author ulman
 */
public class IconPainterExample implements GlimpseLayoutProvider
{
    private static final Logger logger = Logger.getLogger( IconPainterExample.class.getName( ) );

    public static void main( String[] args ) throws Exception
    {
        Example.showWithSwing( new IconPainterExample( ) );
    }

    @Override
    public GlimpseLayout getLayout( ) throws Exception
    {
        // create a GlimpseLayout and attach an AxisMouseListener to it
        // so that the axis bounds respond to mouse interaction
        GlimpseLayout layout = new GlimpseAxisLayout2D( new Axis2D( ) );
        layout.addGlimpseMouseAllListener( new AxisMouseListener2D( ) );

        // create a TextureAtlas and an IconPainter which uses the
        // TextureAtlas as its store of icon images
        TextureAtlas atlas = new TextureAtlas( 256, 256 );
        final IconPainter iconPainter = new IconPainter( );
        iconPainter.addIconGroup( "group1", atlas );
        iconPainter.addIconGroup( "group2", atlas );

        // enable picking support on the IconPainter
        // picking support is currently limited to a single GlimpseLayout
        // here that's fine because we're only adding the IconPainter to
        // a single GlimpseLayout
        iconPainter.setPickingEnabled( layout );
        iconPainter.addSpatialSelectionListener( new SpatialSelectionListener<PickResult>( )
        {
            @Override
            public void selectionChanged( Collection<PickResult> newSelectedPoints )
            {
                logInfo( logger, "Selection: %s", newSelectedPoints );
            }
        } );

        // load some icons into the TextureAtlas
        TextureAtlasExample.loadTextureAtlas( atlas );

        // use the IconPainter to draw the icon "image7" from the TextureAtlas
        // four times at four different positions on the screen: (0,0), (20,20), (30,30), and (40,40)
        iconPainter.addIcons( "group1", "image9", new float[] { 0, 20, 30, 40 }, new float[] { 0, 20, 30, 40 }, new float[] { ( float ) Math.PI / 3, ( float ) -Math.PI / 3, 0, 0 }, new float[] { 1, 1, 1, 1 } );

        // respond to mouse clicks by adding new icons
        layout.addGlimpseMouseListener( new GlimpseMouseListener( )
        {
            float rot = 0;

            @Override
            public void mouseEntered( GlimpseMouseEvent event )
            {
            }

            @Override
            public void mouseExited( GlimpseMouseEvent event )
            {
            }

            @Override
            public void mousePressed( GlimpseMouseEvent event )
            {
                float x = ( float ) event.getAxisCoordinatesX( );
                float y = ( float ) event.getAxisCoordinatesY( );

                if ( event.isButtonDown( MouseButton.Button1 ) )
                {
                    iconPainter.addIcon( "group2", "image7", x, y, rot, 0.5f );
                }
                else if ( event.isButtonDown( MouseButton.Button2 ) )
                {
                    iconPainter.addIcon( "group2", "glimpse", x, y, rot, 0.5f );
                }
                else if ( event.isButtonDown( MouseButton.Button3 ) )
                {
                    iconPainter.addIcon( "group2", "image9", x, y, rot, 1 );
                }

                rot += Math.PI / 12;
            }

            @Override
            public void mouseReleased( GlimpseMouseEvent event )
            {
            }
        } );

        // add painters to the layout
        layout.addPainter( new BackgroundPainter( ).setColor( GlimpseColor.getGray( ) ) );
        layout.addPainter( iconPainter );
        layout.addPainter( new NumericXYAxisPainter( ) );
        layout.addPainter( new FpsPainter( ) );

        return layout;
    }
}
