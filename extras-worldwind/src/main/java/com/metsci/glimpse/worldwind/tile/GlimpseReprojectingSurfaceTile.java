package com.metsci.glimpse.worldwind.tile;

import static com.metsci.glimpse.util.logging.LoggerUtils.logWarning;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.util.OGLStackHandler;

import java.awt.Dimension;
import java.util.List;
import java.util.logging.Logger;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLContext;

import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.gl.GLSimpleFrameBufferObject;
import com.metsci.glimpse.layout.GlimpseAxisLayout2D;
import com.metsci.glimpse.layout.GlimpseLayout;
import com.metsci.glimpse.painter.decoration.BackgroundPainter;
import com.metsci.glimpse.painter.texture.ShadedTexturePainter;
import com.metsci.glimpse.support.projection.FlatProjection;
import com.metsci.glimpse.support.projection.GeoReprojection;
import com.metsci.glimpse.support.texture.ExternalTextureProjected2D;
import com.metsci.glimpse.support.texture.TextureProjected2D;
import com.metsci.glimpse.util.geo.projection.GeoProjection;
import com.metsci.glimpse.worldwind.canvas.SimpleOffscreenCanvas;

public class GlimpseReprojectingSurfaceTile extends GlimpseResizingSurfaceTile
{
    private static final Logger logger = Logger.getLogger( GlimpseReprojectingSurfaceTile.class.getSimpleName( ) );

    private static double REPROJECT_DISCRETIZE_FRACTION = 0.01;

    protected SimpleOffscreenCanvas reprojectCanvas;
    protected ShadedTexturePainter texturePainter;
    protected GlimpseAxisLayout2D reprojectLayout;

    protected TextureProjected2D texture;
    protected GeoReprojection reproject;

    protected GeoProjection projectionTo;

    protected double minX, maxX, minY, maxY;

    public GlimpseReprojectingSurfaceTile( GlimpseLayout layout, Axis2D axes, GeoProjection from, GeoProjection to, int preferredWidth, int preferredHeight, double minLat, double maxLat, double minLon, double maxLon )
    {
        super( layout, axes, from, 8192, 8192, preferredWidth, preferredHeight, minLat, maxLat, minLon, maxLon );

        init( to );
    }

    public GlimpseReprojectingSurfaceTile( GlimpseLayout layout, Axis2D axes, GeoProjection from, GeoProjection to, int preferredWidth, int preferredHeight, List<LatLon> corners )
    {
        super( layout, axes, from, 8192, 8192, preferredWidth, preferredHeight, corners );

        init( to );
    }

    public GlimpseReprojectingSurfaceTile( GlimpseLayout layout, Axis2D axes, GeoProjection from, GeoProjection to, int maxWidth, int maxHeight, int preferredWidth, int preferredHeight, double minLat, double maxLat, double minLon, double maxLon )
    {
        super( layout, axes, from, maxWidth, maxHeight, preferredWidth, preferredHeight, minLat, maxLat, minLon, maxLon );

        init( to );
    }

    public GlimpseReprojectingSurfaceTile( GlimpseLayout layout, Axis2D axes, GeoProjection from, GeoProjection to, int maxWidth, int maxHeight, int preferredWidth, int preferredHeight, List<LatLon> corners )
    {
        super( layout, axes, from, maxWidth, maxHeight, preferredWidth, preferredHeight, corners );

        init( to );
    }

    protected void init( GeoProjection to )
    {
        this.projectionTo = to;
        this.reprojectCanvas = new SimpleOffscreenCanvas( width, height, false, false, context );
        this.texturePainter = new ShadedTexturePainter( );
        this.reprojectLayout = new GlimpseAxisLayout2D( new Axis2D( ) );

        this.reprojectLayout.addPainter( new BackgroundPainter( ).setColor( 0f, 0f, 0f, 0f ) );
        this.reprojectLayout.addPainter( this.texturePainter );

    }

    @Override
    protected int getTextureHandle( )
    {
        return reprojectCanvas.getFrameBuffer( ).getTextureId( );
    }

    @Override
    protected void resizeCanvas( int width, int height )
    {
        super.resizeCanvas( width, height );
        reprojectCanvas.resize( width, height );
        updateProjection( width, height );
    }

    protected void updateProjection( int width, int height )
    {
        minX = axes.getMinX( );
        maxX = axes.getMaxX( );
        minY = axes.getMinY( );
        maxY = axes.getMaxY( );

        // the scene was drawn onto the offscreenCanvas using the bounds specified by axes
        // create a projection which allows us to map to axis coordinates given
        // a 0.0 to 1.0 relative coordinate in the offscreenCanvas texture
        FlatProjection flat = new FlatProjection( axes );

        // build the reprojection (an instance of com.metsci.glimpse.support.projection.Projection)
        reproject = new GeoReprojection( flat, projection, projectionTo, REPROJECT_DISCRETIZE_FRACTION );

        // set the reprojection on the texture
        if ( texture != null )
        {
            texture.setProjection( reproject );
        }

        // set reprojectLayout axes to reflect the lat/lon bounds of the surface tile
        // in the "to" projection (for Worldwind, this projection is usually a
        // PlateCareeProjection)
        setAxes( reprojectLayout.getAxis( ), bounds, projectionTo );
    }

    @Override
    protected void drawOffscreen( GLContext glContext )
    {
        // draw the GlimpseLayout into the offscreenCanvas
        super.drawOffscreen( glContext );

        // get a handle to the texture offscreenCanvas drew into
        if ( texture == null )
        {
            reprojectCanvas.initialize( context );

            GLSimpleFrameBufferObject fbo = offscreenCanvas.getFrameBuffer( );
            Dimension dim = fbo.getDimension( );
            int width = (int) dim.getWidth( );
            int height = (int) dim.getHeight( );
            int texHandle = fbo.getTextureId( );
            
            texture = new ExternalTextureProjected2D( texHandle, width, height, false )
            {
                @Override
                protected void prepare_glState( GL gl )
                {
                    gl.glEnable( GL.GL_TEXTURE_2D );
                    gl.glDisable( GL.GL_BLEND );
                }
            };
            
            texturePainter.removeAllDrawableTextures( );
            texturePainter.addDrawableTexture( texture );

            if ( reproject != null )
            {
                texture.setProjection( reproject );
            }
        }

        // rebuild the projection if the axes changed
        if ( didAxesChange( axes ) )
        {
            updateProjection( currentWidth, currentHeight );
        }

        GLSimpleFrameBufferObject fbo = reprojectCanvas.getFrameBuffer( );
        OGLStackHandler stack = new OGLStackHandler( );
        GL gl = glContext.getGL( );

        stack.pushAttrib( gl, GL2.GL_ALL_ATTRIB_BITS );
        stack.pushClientAttrib( gl, ( int ) GL2.GL_ALL_CLIENT_ATTRIB_BITS );
        stack.pushTexture( gl );
        stack.pushModelview( gl );
        stack.pushProjection( gl );

        fbo.bind( glContext );
        try
        {
            reprojectLayout.paintTo( reprojectCanvas.getGlimpseContext( ) );
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

    protected boolean didAxesChange( Axis2D axis )
    {
        return ! ( axis.getMinX( ) == minX && axis.getMaxX( ) == maxX && axis.getMinY( ) == minY && axis.getMaxY( ) == maxY );
    }
}
