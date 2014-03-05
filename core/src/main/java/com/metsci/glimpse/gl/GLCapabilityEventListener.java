package com.metsci.glimpse.gl;

import static com.metsci.glimpse.gl.util.GLCapabilityUtils.*;
import static java.util.logging.Level.*;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLContext;
import javax.media.opengl.GLEventListener;

public class GLCapabilityEventListener implements GLEventListener
{
    private final Logger logger;
    private final String name;

    public GLCapabilityEventListener( Logger logger, String name )
    {
        this.logger = logger;
        this.name = name;
    }

    public GLCapabilityEventListener( String name )
    {
        this( Logger.getLogger( GLCapabilityEventListener.class.getName( ) ), name );
    }

    public GLCapabilityEventListener( Logger logger )
    {
        this( logger, "" );
    }

    public GLCapabilityEventListener( )
    {
        this( Logger.getLogger( GLCapabilityEventListener.class.getName( ) ), "" );
    }

    @Override
    public void init( GLAutoDrawable drawable )
    {
        String prefix = "init()";

        if ( name != null && name.length( ) > 0 ) prefix += " on" + name;

        prefix = prefix + ": ";

        GLContext context = drawable.getContext( );

        logGLVersionInfo( logger, INFO, context, true );
        logGLMaximumValues( logger, INFO, context );
        logGLBufferProperties( logger, Level.INFO, context, prefix );
        logGLExtensions( logger, INFO, context, false );
        logGLExtensions( logger, INFO, context, true );
    }

    @Override
    public void dispose( GLAutoDrawable drawable )
    {
        // do nothing
    }

    @Override
    public void display( GLAutoDrawable drawable )
    {
        // do nothing
    }

    @Override
    public void reshape( GLAutoDrawable drawable, int x, int y, int width, int height )
    {
        // do nothing
    }

}
