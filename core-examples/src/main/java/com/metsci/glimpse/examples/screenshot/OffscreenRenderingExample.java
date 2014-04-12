package com.metsci.glimpse.examples.screenshot;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.media.opengl.GLOffscreenAutoDrawable;
import javax.media.opengl.GLProfile;

import com.metsci.glimpse.canvas.FBOGlimpseCanvas;
import com.metsci.glimpse.examples.basic.HeatMapExample;
import com.metsci.glimpse.gl.util.GLUtils;
import com.metsci.glimpse.plot.ColorAxisPlot2D;
import com.metsci.glimpse.support.settings.SwingLookAndFeel;

public class OffscreenRenderingExample
{
    public static void main( String[] args ) throws IOException
    {
        GLProfile glProfile = GLUtils.getDefaultGLProfile( );
        
        // generate a GLContext by constructing a small offscreen framebuffer
        final GLOffscreenAutoDrawable glDrawable = GLUtils.newOffscreenDrawable( glProfile );

        // create an offscreen GlimpseCanvas which shares an OpenGL context with the above drawable
        // (its size is 1000 by 1000 pixels)
        final FBOGlimpseCanvas canvas = new FBOGlimpseCanvas( glDrawable.getContext( ), 1000, 1000 );
        
        // set the Glimpse look and feed of the canvas just like we would for an onscreen canvas
        canvas.setLookAndFeel( new SwingLookAndFeel( ) );
        
        // use one of the previous examples to build a simple plot to draw
        ColorAxisPlot2D layout = new HeatMapExample( ).getLayout( );

        // add the layout to the offscreen canvas
        canvas.addLayout( layout );
        
        // draw the canvas to a BufferedImage and write the image to a file
        BufferedImage image = canvas.toBufferedImage( );
        ImageIO.write( image, "PNG", new File( "OffscreenRenderingExample.png" ) );
    }
}
