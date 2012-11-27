package com.metsci.glimpse.worldwind.tile;

import gov.nasa.worldwind.geom.Extent;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.Renderable;
import gov.nasa.worldwind.render.SurfaceTile;
import gov.nasa.worldwind.util.Logging;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.media.opengl.GL;

/**
 * A SurfaceTile which renders imagery from an OpenGL texture handle.
 * 
 * @author ulman
 */
public class TextureSurfaceTile implements SurfaceTile, Renderable
{

    protected int textureHandle;
    protected Sector sector;
    protected List<LatLon> corners;

    protected List<TextureSurfaceTile> thisList = Collections.singletonList( this );

    public TextureSurfaceTile( int textureHandle, Sector sector )
    {
        this( textureHandle, ( Iterable<? extends LatLon> ) sector );
    }

    public TextureSurfaceTile( int textureHandle, Iterable<? extends LatLon> corners )
    {
        this.textureHandle = textureHandle;

        this.initializeGeometry( corners );
    }
    
    public void setCorners( Iterable<? extends LatLon> corners )
    {
    	this.initializeGeometry( corners );
    }

    protected void initializeGeometry( Iterable<? extends LatLon> corners )
    {
        this.corners = new ArrayList<LatLon>( 4 );
        for ( LatLon ll : corners )
        {
            this.corners.add( ll );
        }

        this.sector = Sector.boundingSector( this.corners );
    }

    /////////////// Renderable methods ///////////////

    @Override
    public void render( DrawContext dc )
    {
        GL gl = dc.getGL( );

        gl.glPushAttrib( GL.GL_POLYGON_BIT );
        gl.glPolygonMode( GL.GL_FRONT, GL.GL_FILL );
        gl.glEnable( GL.GL_CULL_FACE );
        gl.glCullFace( GL.GL_BACK );

        try
        {
            dc.getGeographicSurfaceTileRenderer( ).renderTiles( dc, this.thisList );
        }
        finally
        {
            gl.glPopAttrib( );
        }
    }

    /////////////// SurfaceTile methods ///////////////

    @Override
    public boolean bind( DrawContext dc )
    {
        GL gl = dc.getGL( );
        
        gl.glBindTexture( GL.GL_TEXTURE_2D, textureHandle );

        // these settings make fine line drawing against a transparent background appear much more natural
        // but can make other rendering look too jagged/crisp
        
//        gl.glTexParameteri( GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_NEAREST);
//        gl.glTexParameteri( GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_NEAREST);
//        gl.glTexParameteri( GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP_TO_BORDER);
//        gl.glTexParameteri( GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP_TO_BORDER);
//        
//        gl.glBlendFuncSeparate( GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA, GL.GL_ONE, GL.GL_ONE_MINUS_SRC_ALPHA );
//        gl.glEnable( GL.GL_BLEND );
        
        return true;
    }

    @Override
    public void applyInternalTransform( DrawContext dc, boolean textureIdentityActive )
    {
        // do nothing
    }

    @Override
    public Sector getSector( )
    {
        return sector;
    }

    @Override
    public List<? extends LatLon> getCorners( )
    {
        return corners;
    }

    @Override
    public Extent getExtent( DrawContext dc )
    {
        if ( dc == null )
        {
            String msg = Logging.getMessage( "nullValue.DrawContextIsNull" );
            Logging.logger( ).severe( msg );
            throw new IllegalArgumentException( msg );
        }

        return Sector.computeBoundingCylinder( dc.getGlobe( ), dc.getVerticalExaggeration( ), this.getSector( ) );
    }
}
