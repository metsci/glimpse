package com.metsci.glimpse.canvas;

import javax.media.opengl.GLAutoDrawable;

import com.jogamp.newt.opengl.GLWindow;

public interface NewtGlimpseCanvas extends GlimpseCanvas
{
    public GLAutoDrawable getGLDrawable( );
    public GLWindow getGLWindow( );
}
