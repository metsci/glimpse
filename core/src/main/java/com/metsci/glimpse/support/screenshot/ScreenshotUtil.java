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
package com.metsci.glimpse.support.screenshot;

import static com.metsci.glimpse.gl.util.GLPBufferUtils.createPixelBuffer;

import java.io.File;
import java.io.IOException;

import javax.media.opengl.GLContext;
import javax.media.opengl.GLException;

import com.jogamp.opengl.util.awt.Screenshot;
import com.metsci.glimpse.canvas.FrameBufferGlimpseCanvas;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.gl.GLSimpleFrameBufferObject;
import com.metsci.glimpse.layout.GlimpseLayout;

public class ScreenshotUtil
{
    protected GLContext context;
    protected FrameBufferGlimpseCanvas frameBuffer;

    public ScreenshotUtil( )
    {
        this.context = createPixelBuffer( 1, 1 ).getContext( );
        this.frameBuffer = new FrameBufferGlimpseCanvas( 1, 1, context );
    }

    public ScreenshotUtil( GLContext _context )
    {
        this.context = createPixelBuffer( 1, 1, _context ).getContext( );
        this.frameBuffer = new FrameBufferGlimpseCanvas( 1, 1, this.context );
    }

    public FrameBufferGlimpseCanvas getGlimpseCanvas( )
    {
        return this.frameBuffer;
    }

    public void captureScreenshot( GlimpseLayout layout, File outputFile, int width, int height ) throws GLException, IOException
    {
        // resize the frame buffer canvas if necessary
        frameBuffer.resize( width, height );

        // get a GlimpseContext and GLContext for the frame buffer canvas
        GlimpseContext context = frameBuffer.getGlimpseContext( );
        GLSimpleFrameBufferObject fbo = frameBuffer.getFrameBuffer( );
        GLContext glContext = context.getGLContext( );

        // make the frame buffer canvas current and bind it to the glcontext
        // so that gl drawing operations will draw into the frame buffer
        glContext.makeCurrent( );
        fbo.bind( glContext );

        // paint the provided layout into the frame buffer canvas
        layout.paintTo( context );

        // save the screenshot
        Screenshot.writeToFile( outputFile, width, height );

        // unbind the frame buffer and release the gl context
        fbo.unbind( glContext );
        glContext.release( );
    }
}
