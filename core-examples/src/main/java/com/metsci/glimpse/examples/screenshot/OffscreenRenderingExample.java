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
package com.metsci.glimpse.examples.screenshot;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;
import javax.media.opengl.GLOffscreenAutoDrawable;
import javax.media.opengl.GLProfile;

import com.metsci.glimpse.canvas.FBOGlimpseCanvas;
import com.metsci.glimpse.examples.axis.WrappedAxisExample;
import com.metsci.glimpse.gl.util.GLUtils;
import com.metsci.glimpse.layout.GlimpseLayout;
import com.metsci.glimpse.support.settings.SwingLookAndFeel;

public class OffscreenRenderingExample
{
    public static void main( String[] args ) throws Exception
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
        GlimpseLayout layout = new WrappedAxisExample( ).getLayout( );

        // add the layout to the offscreen canvas
        canvas.addLayout( layout );

        // draw the canvas to a BufferedImage and write the image to a file
        BufferedImage image = canvas.toBufferedImage( );
        ImageIO.write( image, "PNG", new File( "OffscreenRenderingExample.png" ) );
    }
}
