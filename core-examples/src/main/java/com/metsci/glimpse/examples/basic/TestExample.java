package com.metsci.glimpse.examples.basic;

import java.awt.Color;

//import import com.metsci.glimpse.com.jogamp.opengl.util.awt.TextRenderer;
import com.metsci.glimpse.com.jogamp.opengl.util.awt.TextRenderer;
import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.context.GlimpseBounds;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.examples.Example;
import com.metsci.glimpse.layout.GlimpseAxisLayout2D;
import com.metsci.glimpse.layout.GlimpseLayout;
import com.metsci.glimpse.layout.GlimpseLayoutProvider;
import com.metsci.glimpse.painter.base.GlimpsePainterBase;
import com.metsci.glimpse.support.font.FontUtils;

public class TestExample implements GlimpseLayoutProvider
{
    public static void main( String[] args ) throws Exception
    {
        Example.showWithSwing( new TestExample( ) );
    }

    @Override
    public GlimpseLayout getLayout( )
    {
        GlimpseAxisLayout2D layout = new GlimpseAxisLayout2D( new Axis2D( ) );

        layout.addPainter( new GlimpsePainterBase( )
        {
            protected TextRenderer textRenderer = new TextRenderer( FontUtils.getDefaultPlain( 36.0f ) );

            @Override
            protected void doPaintTo( GlimpseContext context )
            {
                GlimpseBounds bounds = getBounds( context );

                int height = bounds.getHeight( );
                int width = bounds.getWidth( );

                textRenderer.beginRendering( width, height );
                try
                {
                    textRenderer.setColor( Color.blue );
                    textRenderer.draw( "Test", width / 2, height / 2 );
                }
                finally
                {
                    textRenderer.endRendering( );
                }
            }

            @Override
            protected void doDispose( GlimpseContext context )
            {
            }
        } );

        return layout;
    }

}
