package com.metsci.glimpse.timing;

import static com.metsci.glimpse.gl.util.GLCapabilityUtils.*;
import static com.metsci.glimpse.util.logging.LoggerUtils.*;
import static java.util.logging.Level.*;

import java.util.logging.Logger;

import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;

import com.metsci.glimpse.canvas.GlimpseCanvas;

public class GLVersionLogger implements GLEventListener
{
    private static final Logger logger = getLogger( GLVersionLogger.class );

    public static <T extends GlimpseCanvas> T addGLVersionLogger( T canvas )
    {
        canvas.getGLDrawable( ).addGLEventListener( new GLVersionLogger( ) );
        return canvas;
    }

    @Override
    public void init( GLAutoDrawable drawable )
    {
        logGLVersionInfo( logger, INFO, drawable.getContext( ) );
    }

    @Override
    public void reshape( GLAutoDrawable drawable, int x, int y, int width, int height )
    { }

    @Override
    public void display( GLAutoDrawable drawable )
    { }

    @Override
    public void dispose( GLAutoDrawable drawable )
    { }

}
