package com.metsci.glimpse.worldwind.tile;

import static com.metsci.glimpse.util.logging.LoggerUtils.logWarning;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.layers.AbstractLayer;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.PreRenderable;
import gov.nasa.worldwind.render.Renderable;
import gov.nasa.worldwind.util.OGLStackHandler;

import java.util.logging.Logger;

import javax.media.opengl.GL;
import javax.media.opengl.GLContext;

import com.metsci.glimpse.canvas.GlimpseCanvas;
import com.metsci.glimpse.gl.GLSimpleFrameBufferObject;
import com.metsci.glimpse.layout.GlimpseLayout;
import com.metsci.glimpse.worldwind.canvas.SimpleOffscreenCanvas;

/**
 * GlimpseSurfaceTile uses TextureSurfaceTile to display the output of Glimpse
 * offscreen rendering onto the surface of the Worldwind globe.
 * 
 * @author ulman
 */
public class GlimpseSurfaceTile extends AbstractLayer implements Renderable, PreRenderable
{
    private static final Logger logger = Logger.getLogger( GlimpseSurfaceTile.class.getSimpleName( ) );

    protected GlimpseLayout layout;
    protected int width, height;
    protected Iterable<? extends LatLon> corners;

    protected SimpleOffscreenCanvas offscreenCanvas;
    protected TextureSurfaceTile tile;
    protected GLContext context;

    public GlimpseSurfaceTile( GlimpseLayout layout, int width, int height, Iterable<? extends LatLon> corners )
    {
        this.layout = layout;
        this.width = width;
        this.height = height;
        this.corners = corners;
        
        this.offscreenCanvas = new SimpleOffscreenCanvas( width, height, false, false );
    }
    
    public GlimpseCanvas getGlimpseCanvas( )
    {
        return this.offscreenCanvas;
    }

    @Override
    public void preRender( DrawContext dc )
    {
        if ( tile == null )
        {

            if ( context == null )
            {
                GLContext oldcontext = dc.getGLContext( );
                context = dc.getGLDrawable( ).createContext( oldcontext );
            }

            this.offscreenCanvas.initialize( context );
        }

        drawOffscreen( dc );

        if ( tile == null )
        {
            int textureHandle = offscreenCanvas.getFrameBuffer( ).getTextureId( );
            tile = newTextureSurfaceTile( textureHandle, corners );
        }
    }
    
    protected TextureSurfaceTile newTextureSurfaceTile( int textureHandle, Iterable<? extends LatLon> corners )
    {
        return new TextureSurfaceTile( textureHandle, corners );
    }
    
    @Override
    protected void doRender( DrawContext dc )
    {
        tile.render( dc );

    }

    protected void drawOffscreen( DrawContext dc )
    {
        context.makeCurrent( );
        try
        {
            drawOffscreen( dc.getGLContext( ) );
        }
        finally
        {
            dc.getGLContext( ).makeCurrent( );
        }
    }

    protected void drawOffscreen( GLContext glContext )
    {
        GLSimpleFrameBufferObject fbo = offscreenCanvas.getFrameBuffer( );
        OGLStackHandler stack = new OGLStackHandler( );
        GL gl = glContext.getGL( );

        stack.pushAttrib( gl, GL.GL_ALL_ATTRIB_BITS );
        stack.pushClientAttrib( gl, ( int ) GL.GL_ALL_CLIENT_ATTRIB_BITS );
        stack.pushTexture( gl );
        stack.pushModelview( gl );
        stack.pushProjection( gl );

        fbo.bind( glContext );
        try
        {
            layout.paintTo( offscreenCanvas.getGlimpseContext( ) );
        }
        catch ( Exception e )
        {
            logWarning( logger, "Trouble drawing to offscreen buffer", e );
        }
        finally
        {
            fbo.unbind( glContext );
            stack.pop( gl );
        }
    }
}
