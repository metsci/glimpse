package com.metsci.glimpse.examples.icon;

import static com.metsci.glimpse.util.logging.LoggerUtils.logInfo;

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
import com.metsci.glimpse.support.atlas.TextureAtlas;
import com.metsci.glimpse.support.atlas.painter.IconPainter;
import com.metsci.glimpse.support.atlas.painter.IconPainter.PickResult;
import com.metsci.glimpse.support.color.GlimpseColor;
import com.metsci.glimpse.support.selection.SpatialSelectionListener;

public class IconPainterExample implements GlimpseLayoutProvider
{
    private static final Logger logger = Logger.getLogger( IconPainterExample.class.getSimpleName( ) );

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
        final IconPainter iconPainter = new IconPainter( atlas );

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
        TextureAtlasTestExample.loadTextureAtlas( atlas );

        // use the IconPainter to draw the icon "image7" from the TextureAtlas
        // four times at four different positions on the screen: (0,0), (20,20), (30,30), and (40,40)
        iconPainter.addIcons( "group1", "image9", new float[] { 0, 20, 30, 40 }, new float[] { 0, 20, 30, 40 } );

        // respond to mouse clicks by adding new icons
        layout.addGlimpseMouseListener( new GlimpseMouseListener( )
        {
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
                Axis2D axis = event.getAxis2D( );
                float x = ( float ) axis.getAxisX( ).screenPixelToValue( event.getX( ) );
                float y = ( float ) axis.getAxisY( ).screenPixelToValue( axis.getAxisY( ).getSizePixels( ) - event.getY( ) );

                if ( event.isButtonDown( MouseButton.Button1 ) )
                {
                    iconPainter.addIcon( "group2", "image7", 0.5f, x, y );
                }
                else if ( event.isButtonDown( MouseButton.Button2 ) )
                {
                    iconPainter.addIcon( "group2", "glimpse", 0.5f, x, y );
                }
                else if ( event.isButtonDown( MouseButton.Button3 ) )
                {
                    iconPainter.addIcon( "group2", "image9", x, y );
                }
            }

            @Override
            public void mouseReleased( GlimpseMouseEvent event )
            {
            }
        } );

        // add painters to the layout
        layout.addPainter( new BackgroundPainter( ).setColor( GlimpseColor.getWhite( ) ) );
        layout.addPainter( new NumericXYAxisPainter( ) );
        layout.addPainter( iconPainter );

        return layout;
    }
}
