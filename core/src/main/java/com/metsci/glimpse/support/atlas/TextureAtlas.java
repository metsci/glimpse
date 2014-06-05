//
// Adapted from com.sun.opengl.util.j2d.TextRenderer, which is covered
// by the following license:
//

/*
 * Copyright (c) 2006 Sun Microsystems, Inc. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * - Redistribution of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * - Redistribution in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * Neither the name of Sun Microsystems, Inc. or the names of
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES,
 * INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN
 * MICROSYSTEMS, INC. ("SUN") AND ITS LICENSORS SHALL NOT BE LIABLE FOR
 * ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR
 * DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN OR
 * ITS LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR
 * DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE
 * DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY,
 * ARISING OUT OF THE USE OF OR INABILITY TO USE THIS SOFTWARE, EVEN IF
 * SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *
 * You acknowledge that this software is not designed or intended for use
 * in the design, construction, operation or maintenance of any nuclear
 * facility.
 *
 * Sun gratefully acknowledges that this software was originally authored
 * and developed by Kenneth Bradley Russell and Christopher John Kline.
 */
package com.metsci.glimpse.support.atlas;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantLock;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLException;
import javax.media.opengl.glu.GLU;

import com.jogamp.opengl.util.awt.TextureRenderer;
import com.jogamp.opengl.util.packrect.BackingStoreManager;
import com.jogamp.opengl.util.packrect.Rect;
import com.jogamp.opengl.util.packrect.RectVisitor;
import com.jogamp.opengl.util.packrect.RectanglePacker;
import com.jogamp.opengl.util.texture.TextureCoords;
import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.support.atlas.support.ImageData;
import com.metsci.glimpse.support.atlas.support.ImageDataExternal;
import com.metsci.glimpse.support.atlas.support.ImageDataInternal;
import com.metsci.glimpse.support.atlas.support.ImageDrawer;
import com.metsci.glimpse.support.atlas.support.TextureAtlasUpdateListener;

/**
 * Stores a large number of images or icons which are packed into a single
 * OpenGL texture. This allows VBO draw methods like glDrawArrays to be
 * used to draw thousands of images from the TextureAtlas simultaneously.
 * 
 * @author ulman
 */
public class TextureAtlas
{
    // note: RectanglePacker won't grow in width, only in height
    // so INITIAL_WIDTH setting constrains total size of texture atlas
    private static final int INITIAL_WIDTH = 2048;
    private static final int INITIAL_HEIGHT = 2048;
    private static final float MAX_VERTICAL_FRAGMENTATION = 0.7f;

    // internal lock ensuring thread-safe access
    private ReentrantLock lock;

    // Queue of items waiting to be added to the TextureAtlas
    // the next time beginRendering( ) is called
    private Map<Object, ImageDataExternal> additionQueue;
    // Queue of item ids waiting to be deleted from the TextureAtlas
    private Set<Object> deletionQueue;

    private Collection<TextureAtlasUpdateListener> updateListeners;

    // Map of images currently present in the TextureAtlas.
    // Rect is an internal class used by RectanglePacker. However
    // an Object field is provided for external use. We use this
    // to store an associated ImageData object.
    private Map<Object, Rect> imageMap;

    // Encapsulates logic for packing and reorganizing rectangular
    // images onto underlying OpenGL texture
    private RectanglePacker packer;
    // Handles underlying OpenGL texture which stores image data
    // location of images is handled by the RectanglePacker
    private TextureRenderer cachedBackingStore;

    // TextureAtlas images are drawn using this Graphics2D context
    private Graphics2D cachedGraphics;

    // Need to keep track of whether we're in a beginRendering() /
    // endRendering() cycle so we can re-enter the exact same state if
    // we have to reallocate the backing store
    private boolean inBeginEndPair;
    private boolean isOrthoMode;
    private int beginRenderingWidth;
    private int beginRenderingHeight;
    private boolean beginRenderingDepthTestDisabled;

    // Whether GL_LINEAR filtering is enabled for the backing store
    private boolean smoothing;
    private boolean isExtensionAvailable_GL_VERSION_1_5;
    private boolean checkFor_isExtensionAvailable_GL_VERSION_1_5;
    private boolean mipmap;
    private boolean haveMaxSize;

    /**
     * Constructs a new TextureAtlas with the provided initial width and height
     * in pixels. The TextureAtlas will grow in height automatically as needed, up
     * to the maximum texture size supported by the graphics hardware.
     * 
     * Note: the TextureAtlas will not currently grow in width.
     *
     * If the smoothing parameter is true, rendered pixel colors will be interpolated
     * among adjacent texels. Otherwise, pixel colors will be assigned from the nearest
     * texel.
     *
     * @param initalWidth
     * @param initialHeight
     * @param smoothing
     */
    public TextureAtlas( int initialWidth, int initialHeight, boolean smoothing )
    {
        this.packer = new RectanglePacker( new Manager( ), initialWidth, initialHeight );
        this.additionQueue = new HashMap<Object, ImageDataExternal>( );
        this.deletionQueue = new HashSet<Object>( );
        this.imageMap = new HashMap<Object, Rect>( );
        this.updateListeners = new CopyOnWriteArrayList<TextureAtlasUpdateListener>( );
        this.lock = new ReentrantLock( );
        this.smoothing = smoothing;
    }

    /**
     * Constructs a new TextureAtlas with the provided initial width and height
     * in pixels, with smoothing enabled.
     *
     * @see #TextureAtlas( int, int, boolean )
     */
    public TextureAtlas( int initialWidth, int initialHeight )
    {
        this( initialWidth, initialHeight, true );

    }

    /**
     * Constructs a new TextureAtlas with default initial width and height, with
     * smoothing enabled.
     *
     * @see #TextureAtlas( int, int, boolean )
     */
    public TextureAtlas( )
    {
        this( INITIAL_WIDTH, INITIAL_HEIGHT, true );
    }

    /**
     * Registers a TextureAtlasUpdateListener with the TextureAtlas. This listener is
     * notified when the TextureAtlas compacts itself. This compaction, done to make
     * space in the TextureAtlas for additional images/icons, may cause images to change
     * location within the atlas. This may require updates to external data structures storing
     * texture coordinates that reference locations in the atlas.
     * 
     * @param listener
     */
    public void addListener( TextureAtlasUpdateListener listener )
    {
        this.updateListeners.add( listener );
    }

    public void removeListener( TextureAtlasUpdateListener listener )
    {
        this.updateListeners.remove( listener );
    }

    //////////////////////////////////////////////////////////////
    ///    Image Addition / Deletion / Modification Methods    ///
    //////////////////////////////////////////////////////////////

    /**
     * Adds an image, defined by a BufferedImage, to the TextureAtlas. The icon id can be
     * any object (most often a String) which uniquely identifies the image.
     * 
     * When an icon is displayed at a fixed point in data/axis space, that point must be
     * fixed to a specific pixel on the image. The centerX and centerY arguments specify
     * this center pixel. A centerX/centerY of (0,0) indicates that the icon should be centered
     * on the lower left pixel.
     * 
     * @param id the unique identifier for the image
     * @param image a BufferedImage to be loaded into the texture
     * @param centerX the center x pixel of the image
     * @param centerY the center y pixel of the image
     */
    public void loadImage( Object id, final BufferedImage image, int centerX, int centerY )
    {
        ImageDrawer drawer = new ImageDrawer( )
        {
            @Override
            public void drawImage( Graphics2D g, int width, int height )
            {
                g.drawImage( image, 0, 0, null );
            }
        };

        int height = image.getHeight( );
        int width = image.getWidth( );

        loadImage( id, width, height, centerX, centerY, drawer );
    }

    /**
     * Adds an image, defined by a BufferedImage, to the TextureAtlas. The icon id can be
     * any object (most often a String) which uniquely identifies the image.
     * 
     * @param id the unique identifier for the image
     * @param image a BufferedImage to be loaded into the texture
     * @see #loadImage( Object, BufferedImage, int, int )
     */
    public void loadImage( Object id, final BufferedImage image )
    {
        int height = image.getHeight( );
        int width = image.getWidth( );

        loadImage( id, image, width / 2, height / 2 );
    }

    /**
     * Adds an image, defined by an arbitrary Java2D drawing routine, to the TextureAtlas. The
     * width and height arguments define a canvas size, and the ImageDrawer defines a routine
     * which will be used to draw icon graphics onto the canvas.
     * 
     * @param id the unique identifier for the image
     * @param width the width of the image
     * @param height the height of the image
     * @param drawer a Java2D drawing routine which defines the image
     */
    public void loadImage( Object id, int width, int height, ImageDrawer drawer )
    {
        loadImage( id, width, height, width / 2, height / 2, drawer );
    }

    /**
     * Adds an image, defined by an arbitrary Java2D drawing routine, to the TextureAtlas. The
     * width and height arguments define a canvas size, and the ImageDrawer defines a routine
     * which will be used to draw icon graphics onto the canvas.
     * 
     * @param id the unique identifier for the image
     * @param width the width of the image
     * @param height the height of the image
     * @param centerX the center x pixel of the image
     * @param centerY the center y pixel of the image
     * @param drawer a Java2D drawing routine which defines the image
     * @see #loadImage( Object, int, int, int, int, ImageDrawer )
     */
    public void loadImage( Object id, int width, int height, int centerX, int centerY, ImageDrawer drawer )
    {
        this.lock.lock( );
        try
        {
            Rect rect = imageMap.get( id );
            if ( rect != null )
            {
                throw new IllegalArgumentException( String.format( "Image id \"%s\" already exists.", id ) );
            }

            this.additionQueue.put( id, new ImageDataExternal( id, centerX, centerY, width, height, drawer ) );
        }
        finally
        {
            this.lock.unlock( );
        }
    }

    /**
     * Removes an image from the TextureAtlas based on its unique identifier (which is often a String).
     * The space used by the image will be automatically reclaimed and available for other images.
     * 
     * @param id the unique identifier for the image to be deleted
     */
    public void deleteImage( Object id )
    {
        this.lock.lock( );
        try
        {
            // we'll happily try to delete an image which doesn't exist (nothing will happen)
            this.deletionQueue.add( id );
        }
        finally
        {
            this.lock.unlock( );
        }
    }

    //////////////////////////////////////////////////////////////
    ///                  Image Query Methods                   ///
    //////////////////////////////////////////////////////////////

    /**
     * Verifies whether or not an image has been loaded into the TextureAtlas. An image is not
     * available immediately after loadImage( ) has been called. At least one intervening
     * {@link TextureAtlas#beginRendering( )} must have been made before {@link TextureAtlas#getImageData( Object )}
     * will return data for the image.
     * 
     * @param id the unique identifier for the image
     * @return true if the image has been loaded into the atlas
     */
    public boolean isImageLoaded( Object id )
    {
        this.lock.lock( );
        try
        {
            return imageMap.containsKey( id );
        }
        finally
        {
            this.lock.unlock( );
        }
    }

    /**
     * Returns information about an image in the TextureAtlas. This information includes texture coordinates,
     * which can be used to manually draw icons from the atlas. This information can also be used by
     * external painters like {@link com.metsci.glimpse.support.atlas.painter.IconPainter} which are
     * backed by a TextureAtlas.
     * 
     * @param id the unique identifier for the image
     * @return a ImageData handle with size and texture coordinate information about the image
     */
    public ImageData getImageData( Object id )
    {
        this.lock.lock( );
        try
        {
            Rect rect = imageMap.get( id );
            if ( rect == null )
            {
                throw new IllegalArgumentException( String.format( "Image id \"%s\" does not exist.", id ) );
            }

            // return an immutable view into the internal image data
            return new ImageData( ( ImageDataInternal ) rect.getUserData( ) );
        }
        finally
        {
            this.lock.unlock( );
        }
    }

    //////////////////////////////////////////////////////////////
    ///                Image Rendering Methods                 ///
    //////////////////////////////////////////////////////////////

    /**
     * @see #drawImage( GL, Object, Axis2D, float, float, float, float, int, int )
     */
    public void drawImage( GL2 gl, Object id, Axis2D axis, double positionX, double positionY )
    {
        drawImage( gl, id, axis, positionX, positionY, 1.0 );
    }

    /**
     * @see #drawImage( GL, Object, Axis2D, float, float, float, float, int, int )
     */
    public void drawImage( GL2 gl, Object id, Axis2D axis, double positionX, double positionY, double scale )
    {
        drawImage( gl, id, axis, positionX, positionY, scale, scale );
    }

    /**
     * @see #drawImage( GL, Object, Axis2D, float, float, float, float, int, int )
     */
    public void drawImage( GL2 gl, Object id, Axis2D axis, double positionX, double positionY, double scaleX, double scaleY )
    {
        ImageDataInternal data = getImageDataInternal( id );
        double ppvX = axis.getAxisX( ).getPixelsPerValue( );
        double ppvY = axis.getAxisY( ).getPixelsPerValue( );
        drawImage( gl, ppvX, ppvY, data, positionX, positionY, scaleX, scaleY, data.getCenterX( ), data.getCenterY( ) );
    }

    /**
     * Draws an image from the TextureAtlas using the given GL handle. The icon is
     * centered on the provided positionX, positionY in axis space.
     * 
     * Note: this OpenGL immediate-mode icon drawing directly from the TextureAtlas
     * is provided as a convenience, but is slow and inefficient when drawing many
     * icons (thousands or more). For those cases, see
     * {@link com.metsci.glimpse.support.atlas.painter.IconPainter}. For even more
     * specific use cases, custom painters may be required.
     * 
     * @param gl handle from the current OpenGL context
     * @param id an icon loaded into the atlas using a loadImage() method
     * @param axis
     * @param positionX the x position in axis space of the center pixel in the image
     * @param positionY the y position in axis space of the center pixel in the image
     * @param scaleX the scale factor in the x direction
     * @param scaleY the scale factor in the y direction
     * @param offsetX overrides the image x offset specified when the image was loaded
     * @param offsetY overrides the image y offset specified when the image was loaded
     */
    public void drawImage( GL2 gl, Object id, Axis2D axis, double positionX, double positionY, double scaleX, double scaleY, int centerX, int centerY )
    {
        ImageDataInternal data = getImageDataInternal( id );
        double ppvX = axis.getAxisX( ).getPixelsPerValue( );
        double ppvY = axis.getAxisY( ).getPixelsPerValue( );
        drawImage( gl, ppvX, ppvY, data, positionX, positionY, scaleX, scaleY, centerX, centerY );
    }

    /**
     * @see #drawImageAxisX( GL, Object, Axis1D, float, float, float, float, int, int )
     */
    public void drawImageAxisX( GL2 gl, Object id, Axis1D axis, double positionX, double positionY )
    {
        ImageDataInternal data = getImageDataInternal( id );
        double ppvX = axis.getPixelsPerValue( );
        drawImage( gl, ppvX, 1.0, data, positionX, positionY, 1.0, 1.0, data.getCenterX( ), data.getCenterY( ) );
    }

    /**
     * Draws an image from the TextureAtlas with the x position specified in axis space
     * and the y position specified in pixel space.
     * 
     * This is most often used by a {@link com.metsci.glimpse.painter.base.GlimpseDataPainter1D}
     * to paint onto a {@link com.metsci.glimpse.layout.GlimpseAxisLayoutX}.
     * 
     * @param gl handle from the current OpenGL context
     * @param id an icon loaded into the atlas using a loadImage() method
     * @param axis the 1D horizontal axis
     * @param positionX the x position along the axis of the image center
     * @param positionY the y position in pixel space of the image center
     * @param scaleX the scale factor in the x direction
     * @param scaleY the scale factor in the y direction
     * @param offsetX overrides the image x offset specified when the image was loaded
     * @param offsetY overrides the image y offset specified when the image was loaded
     */
    public void drawImageAxisX( GL2 gl, Object id, Axis1D axis, double positionX, double positionY, double scaleX, double scaleY, int centerX, int centerY )
    {
        ImageDataInternal data = getImageDataInternal( id );
        double ppvX = axis.getPixelsPerValue( );
        drawImage( gl, ppvX, 1.0, data, positionX, positionY, scaleX, scaleY, centerX, centerY );
    }

    /**
     * Draws an image from the TextureAtlas with the y position specified in axis space
     * and the x position specified in pixel space.
     * 
     * This is most often used by a {@link com.metsci.glimpse.painter.base.GlimpseDataPainter1D}
     * to paint onto a {@link com.metsci.glimpse.layout.GlimpseAxisLayoutY}.
     * 
     * @see #drawImageAxisX( GL, Object, Axis1D, float, float, float, float, int, int )
     */
    public void drawImageAxisY( GL2 gl, Object id, Axis1D axis, double positionX, double positionY )
    {
        ImageDataInternal data = getImageDataInternal( id );
        double ppvY = axis.getPixelsPerValue( );
        drawImage( gl, 1.0, ppvY, data, positionX, positionY, 1.0, 1.0, data.getCenterX( ), data.getCenterY( ) );
    }

    /**
     * @see #drawImageAxisX( GL, Object, Axis1D, float, float, float, float, int, int )
     */
    public void drawImageAxisY( GL2 gl, Object id, Axis1D axis, double positionX, double positionY, double scaleX, double scaleY, int centerX, int centerY )
    {
        ImageDataInternal data = getImageDataInternal( id );
        double ppvY = axis.getPixelsPerValue( );
        drawImage( gl, 1.0, ppvY, data, positionX, positionY, scaleX, scaleY, centerX, centerY );
    }

    public void drawImage( GL2 gl, Object id, int positionX, int positionY, double scaleX, double scaleY, int centerX, int centerY )
    {
        ImageDataInternal data = getImageDataInternal( id );
        drawImage( gl, 1.0, 1.0, data, positionX, positionY, scaleX, scaleY, centerX, centerY );
    }

    public void drawImage( GL2 gl, Object id, int positionX, int positionY, double scaleX, double scaleY )
    {
        ImageDataInternal data = getImageDataInternal( id );
        drawImage( gl, 1.0, 1.0, data, positionX, positionY, scaleX, scaleY, data.getCenterX( ), data.getCenterY( ) );
    }

    public void drawImage( GL2 gl, Object id, int positionX, int positionY )
    {
        ImageDataInternal data = getImageDataInternal( id );
        drawImage( gl, 1.0, 1.0, data, positionX, positionY, 1.0, 1.0, data.getCenterX( ), data.getCenterY( ) );
    }

    protected void drawImage( GL2 gl, double ppvX, double ppvY, ImageDataInternal data, double positionX, double positionY, double scaleX, double scaleY, int centerX, int centerY )
    {
        double vppX = 1.0 / ppvX;
        double vppY = 1.0 / ppvY;

        // NOTE: the rectangles managed by the packer have their
        // origin at the upper-left but the TextureRenderer's origin is
        // at its lower left!!!

        // NOTE: we use the buffered width because that is how the texture coordinates
        // are returned (with the buffer included, to ensure we get the whole image)
        // we're still protected from adjacent images by their buffers

        int width = data.getBufferedWidth( );
        int height = data.getBufferedHeight( );
        TextureCoords texCoords = data.getTextureCoordinates( );

        // Align the leftmost point of the baseline to the (x, y, z) coordinate requested
        float minX = ( float ) ( positionX - ( centerX + data.getBufferX( ) ) * vppX * scaleX );
        float minY = ( float ) ( positionY - ( height - centerY - data.getBufferY( ) ) * vppY * scaleY );

        float maxX = minX + ( float ) ( width * vppX * scaleX );
        float maxY = minY + ( float ) ( height * vppY * scaleY );

        // copied from com.sun.opengl.util.j2d.TextureRenderer#draw3DRect
        gl.glBegin( GL2.GL_QUADS );
        try
        {
            gl.glTexCoord2f( texCoords.left( ), texCoords.bottom( ) );
            gl.glVertex2f( minX, minY );
            gl.glTexCoord2f( texCoords.right( ), texCoords.bottom( ) );
            gl.glVertex2f( maxX, minY );
            gl.glTexCoord2f( texCoords.right( ), texCoords.top( ) );
            gl.glVertex2f( maxX, maxY );
            gl.glTexCoord2f( texCoords.left( ), texCoords.top( ) );
            gl.glVertex2f( minX, maxY );
        }
        finally
        {
            gl.glEnd( );
        }
    }

    /**
     * Readies the TextureAtlas for drawing. The backing texture is bound and OpenGL
     * state is configured for textured quad rendering. {@link drawImage( GL, Object, Axis2D, float, float )}
     * must be called while between calls to {@beginRendering()} and {endRendering()}.
     * 
     * @see com.sun.opengl.util.j2d.TextRenderer#begin3DRendering( )
     */
    public void beginRendering( ) throws GLException
    {
        beginRendering( false, 0, 0, false );
    }

    /**
     * Resets OpenGL state. Every call to {@beginRendering()} should be followed by
     * a call to {endRendering()}.
     * 
     * @see com.sun.opengl.util.j2d.TextRenderer#end3DRendering( )
     */
    public void endRendering( ) throws GLException
    {
        endRendering( false );
    }

    /**
     * Disposes of the OpenGL resources associated with this TextureAtlas.
     * 
     * @see com.sun.opengl.util.j2d.TextRenderer#disposeAttached( )
     */
    public void dispose( ) throws GLException
    {
        packer.dispose( );
        packer = null;
        cachedBackingStore = null;
        cachedGraphics = null;
    }

    //////////////////////////////////////////////////////////////
    ///           Internals Only Beyond This Point             ///
    //////////////////////////////////////////////////////////////

    private ImageDataInternal getImageDataInternal( Object id )
    {
        Rect rect = imageMap.get( id );
        if ( rect == null )
        {
            throw new IllegalArgumentException( String.format( "Image id \"%s\" does not exist.", id ) );
        }

        return ( ImageDataInternal ) rect.getUserData( );
    }

    private Graphics2D getGraphics2D( )
    {
        TextureRenderer renderer = getBackingStore( );

        if ( cachedGraphics == null )
        {
            cachedGraphics = renderer.createGraphics( );
            cachedGraphics.setComposite( AlphaComposite.Src );
            cachedGraphics.setColor( Color.WHITE );
        }

        return cachedGraphics;
    }

    private void beginRendering( boolean ortho, int width, int height, boolean disableDepthTestForOrtho )
    {
        updateImages( );

        inBeginEndPair = true;
        isOrthoMode = ortho;
        beginRenderingWidth = width;
        beginRenderingHeight = height;
        beginRenderingDepthTestDisabled = disableDepthTestForOrtho;

        if ( ortho )
        {
            getBackingStore( ).beginOrthoRendering( width, height, disableDepthTestForOrtho );
        }
        else
        {
            getBackingStore( ).begin3DRendering( );
        }

        GL2 gl = GLU.getCurrentGL( ).getGL2( );

        // Push client attrib bits used by the pipelined quad renderer
        gl.glPushClientAttrib( ( int ) GL2.GL_ALL_CLIENT_ATTRIB_BITS );

        if ( !haveMaxSize )
        {
            // Query OpenGL for the maximum texture size and set it in the
            // RectanglePacker to keep it from expanding too large
            int[] sz = new int[1];
            gl.glGetIntegerv( GL2.GL_MAX_TEXTURE_SIZE, sz, 0 );

            packer.setMaxSize( sz[0], sz[0] );
            haveMaxSize = true;
        }

        // Disable future attempts to use mipmapping if TextureRenderer
        // doesn't support it
        if ( mipmap && !getBackingStore( ).isUsingAutoMipmapGeneration( ) )
        {
            mipmap = false;
        }
    }

    // move the images which have been queued for addition into the texture atlas
    // this must be done with a GLContext active because we write to an underlying texture
    private void updateImages( )
    {
        lock.lock( );
        try
        {
            for ( ImageDataExternal addImage : additionQueue.values( ) )
            {
                newImage0( addImage );
            }

            for ( Object deleteImage : deletionQueue )
            {
                Rect rect = imageMap.get( deleteImage );
                if ( rect == null ) continue; // image is already deleted, no need to do anything

                ImageDataInternal data = ( ImageDataInternal ) rect.getUserData( );
                data.delete( ); // mark for deletion next time compaction of the texture atlas is performed
            }

            additionQueue.clear( );
            deletionQueue.clear( );
        }
        finally
        {
            lock.unlock( );
        }
    }

    private void newImage0( ImageDataExternal data )
    {
        Object id = data.getId( );

        Rect rect = imageMap.get( data.getId( ) );
        if ( rect != null )
        {
            // This should never actually happen (it should be prevented by checks in addImage )
            throw new IllegalArgumentException( String.format( "Image id \"%s\" already exists.", id ) );
        }

        int width = data.getWidth( );
        int height = data.getHeight( );
        int centerX = data.getCenterX( );
        int centerY = data.getCenterY( );

        // draw the new image onto the backing store
        Graphics2D g = getGraphics2D( );

        // leave one pixel around the edge to prevent bleeding into adjacent images
        // this makes the width and height 2 larger and means the actual origin of the
        // image is one pixel away from the edge of the rectangle
        ImageDataInternal imageData = new ImageDataInternal( id, centerX, centerY, 1, 1, width, height );
        rect = new Rect( 0, 0, imageData.getBufferedWidth( ), imageData.getBufferedHeight( ), imageData );

        // store the image location in the texture atlas packer and in
        // our map of id to location so that we can find it again
        // adding the rectangle to the packer will adjust its x and y position
        packer.add( rect );
        imageMap.put( id, rect );

        // save the image texture coordinates for easy access off the OpenGL thread
        updateTextureCoordinates( rect, imageData, imageData.getBufferedWidth( ), imageData.getBufferedHeight( ) );

        // Re-fetch the Graphics2D in case the addition of the rectangle
        // caused the old backing store to be thrown away
        g = getGraphics2D( );

        // Clear out the area we're going to draw into
        g.setComposite( AlphaComposite.Clear );
        g.fillRect( rect.x( ), rect.y( ), rect.w( ), rect.h( ) );
        g.setComposite( AlphaComposite.Src );

        // the origin of the image within the texture atlas
        int x = rect.x( ) + imageData.getBufferX( );
        int y = rect.y( ) + imageData.getBufferY( );

        // save the transform and transform the context
        // so that the ImageDrawer can draw relative to 0,0
        AffineTransform transform = g.getTransform( );
        g.translate( x, y );

        //XXX mask drawing area: http://stackoverflow.com/questions/1241253/inside-clipping-with-java-graphics

        // delegate drawing of the image to the provided ImageDrawer
        data.getImageDrawer( ).drawImage( g, width, height );

        // reset the transform
        g.setTransform( transform );

        // Mark this region of the TextureRenderer as dirty
        getBackingStore( ).markDirty( rect.x( ), rect.y( ), rect.w( ), rect.h( ) );
    }

    private void updateTextureCoordinates( TextureRenderer backingStore, Rect rect, ImageDataInternal imageData, int width, int height )
    {
        // save the image texture coordinates for easy access off the OpenGL thread
        // (this is done to make the user's life easier, however it introduces an
        // implementation complication because we will have to update these values
        // whenever the backing store is reorganized)
        int texX = rect.x( );
        int texY = backingStore.getHeight( ) - rect.y( ) - height;
        TextureCoords textureCoordinates = backingStore.getTexture( ).getSubImageTexCoords( texX, texY, texX + width, texY + height );
        imageData.setTextureCoordinates( textureCoordinates );
    }

    private void updateTextureCoordinates( Rect rect, ImageDataInternal imageData, int width, int height )
    {
        updateTextureCoordinates( getBackingStore( ), rect, imageData, width, height );
    }

    private void updateTextureCoordinates( TextureRenderer backingStore, Rect rect )
    {
        ImageDataInternal imageData = ( ImageDataInternal ) rect.getUserData( );
        int width = imageData.getBufferedWidth( );
        int height = imageData.getBufferedHeight( );

        updateTextureCoordinates( backingStore, rect, imageData, width, height );
    }

    /**
    * emzic: here the call to glBindBuffer crashes on certain graphicscard/driver combinations
    * this is why the ugly try-catch block has been added, which falls back to the old textrenderer
    * 
    * @param ortho
    * @throws GLException
    */
    private void endRendering( boolean ortho ) throws GLException
    {
        inBeginEndPair = false;

        GL2 gl = GLU.getCurrentGL( ).getGL2( );

        // Pop client attrib bits used by the pipelined quad renderer
        gl.glPopClientAttrib( );

        // The OpenGL spec is unclear about whether this changes the
        // buffer bindings, so preemptively zero out the GL_ARRAY_BUFFER
        // binding
        if ( is15Available( gl ) )
        {
            try
            {
                gl.glBindBuffer( GL2.GL_ARRAY_BUFFER, 0 );
            }
            catch ( Exception e )
            {
                isExtensionAvailable_GL_VERSION_1_5 = false;
            }
        }

        if ( ortho )
        {
            getBackingStore( ).endOrthoRendering( );
        }
        else
        {
            getBackingStore( ).end3DRendering( );
        }
    }

    private void clearUnusedEntries( )
    {
        final java.util.List<Rect> deadRects = new ArrayList<Rect>( );

        // Iterate through the contents of the backing store, removing
        // text strings that haven't been used recently
        packer.visit( new RectVisitor( )
        {
            public void visit( Rect rect )
            {
                ImageDataInternal data = ( ImageDataInternal ) rect.getUserData( );

                if ( data.isMarkedForDelete( ) )
                {
                    deadRects.add( rect );
                }
            }
        } );

        for ( Iterator<Rect> iter = deadRects.iterator( ); iter.hasNext( ); )
        {
            Rect r = iter.next( );
            packer.remove( r );
            imageMap.remove( ( ( ImageDataInternal ) r.getUserData( ) ).getId( ) );
        }

        // If we removed dead rectangles this cycle, try to do a compaction
        float frag = packer.verticalFragmentationRatio( );

        if ( !deadRects.isEmpty( ) && ( frag > MAX_VERTICAL_FRAGMENTATION ) )
        {
            packer.compact( );
        }
    }

    private TextureRenderer getBackingStore( )
    {
        TextureRenderer renderer = ( TextureRenderer ) packer.getBackingStore( );

        if ( renderer != cachedBackingStore )
        {
            // Backing store changed since last time; discard any cached Graphics2D
            if ( cachedGraphics != null )
            {
                cachedGraphics.dispose( );
                cachedGraphics = null;
            }

            cachedBackingStore = renderer;
        }

        return cachedBackingStore;
    }

    private boolean is15Available( GL gl )
    {
        if ( !checkFor_isExtensionAvailable_GL_VERSION_1_5 )
        {
            isExtensionAvailable_GL_VERSION_1_5 = gl.isExtensionAvailable( "GL_VERSION_1_5" );
            checkFor_isExtensionAvailable_GL_VERSION_1_5 = true;
        }
        return isExtensionAvailable_GL_VERSION_1_5;
    }

    class Manager implements BackingStoreManager
    {
        private Graphics2D g;

        public Object allocateBackingStore( int w, int h )
        {
            TextureRenderer renderer = new TextureRenderer( w, h, true, mipmap );
            renderer.setSmoothing( smoothing );
            return renderer;
        }

        public void deleteBackingStore( Object backingStore )
        {
            ( ( TextureRenderer ) backingStore ).dispose( );
        }

        public boolean preExpand( Rect cause, int attemptNumber )
        {
            // Only try this one time; clear out potentially obsolete entries
            // NOTE: this heuristic and the fact that it clears the used bit
            // of all entries seems to cause cycling of entries in some
            // situations, where the backing store becomes small compared to
            // the amount of text on the screen (see the TextFlow demo) and
            // the entries continually cycle in and out of the backing
            // store, decreasing performance. If we added a little age
            // information to the entries, and only cleared out entries
            // above a certain age, this behavior would be eliminated.
            // However, it seems the system usually stabilizes itself, so
            // for now we'll just keep things simple. Note that if we don't
            // clear the used bit here, the backing store tends to increase
            // very quickly to its maximum size, at least with the TextFlow
            // demo when the text is being continually re-laid out.
            if ( attemptNumber == 0 )
            {
                clearUnusedEntries( );

                return true;
            }

            return false;
        }

        public boolean additionFailed( Rect cause, int attemptNumber )
        {
            // Heavy hammer -- might consider doing something different
            packer.clear( );
            imageMap.clear( );

            if ( attemptNumber == 0 )
            {
                return true;
            }

            return false;
        }

        public void beginMovement( Object oldBackingStore, Object newBackingStore )
        {
            // Exit the begin / end pair if necessary
            if ( inBeginEndPair )
            {
                GL2 gl = GLU.getCurrentGL( ).getGL2( );

                // Pop client attrib bits used by the pipelined quad renderer
                gl.glPopClientAttrib( );

                // The OpenGL spec is unclear about whether this changes the
                // buffer bindings, so preemptively zero out the GL_ARRAY_BUFFER
                // binding
                if ( is15Available( gl ) )
                {
                    try
                    {
                        gl.glBindBuffer( GL2.GL_ARRAY_BUFFER, 0 );
                    }
                    catch ( Exception e )
                    {
                        isExtensionAvailable_GL_VERSION_1_5 = false;
                    }
                }

                if ( isOrthoMode )
                {
                    ( ( TextureRenderer ) oldBackingStore ).endOrthoRendering( );
                }
                else
                {
                    ( ( TextureRenderer ) oldBackingStore ).end3DRendering( );
                }
            }

            TextureRenderer newRenderer = ( TextureRenderer ) newBackingStore;
            g = newRenderer.createGraphics( );
        }

        public void move( Object oldBackingStore, Rect oldLocation, Object newBackingStore, Rect newLocation )
        {
            TextureRenderer oldRenderer = ( TextureRenderer ) oldBackingStore;
            TextureRenderer newRenderer = ( TextureRenderer ) newBackingStore;

            if ( oldRenderer == newRenderer )
            {
                // Movement on the same backing store -- easy case
                g.copyArea( oldLocation.x( ), oldLocation.y( ), oldLocation.w( ), oldLocation.h( ), newLocation.x( ) - oldLocation.x( ), newLocation.y( ) - oldLocation.y( ) );
            }
            else
            {
                // Need to draw from the old renderer's image into the new one
                Image img = oldRenderer.getImage( );
                g.drawImage( img, newLocation.x( ), newLocation.y( ), newLocation.x( ) + newLocation.w( ), newLocation.y( ) + newLocation.h( ), oldLocation.x( ), oldLocation.y( ), oldLocation.x( ) + oldLocation.w( ), oldLocation.y( ) + oldLocation.h( ), null );
            }

            // copy the user data to the newLocation and put the new location in the image map
            ImageDataInternal data = ( ImageDataInternal ) oldLocation.getUserData( );
            newLocation.setUserData( oldLocation.getUserData( ) );
            imageMap.put( data.getId( ), newLocation );

            // updated the cached texture coordinates
            updateTextureCoordinates( newRenderer, newLocation );
        }

        public void endMovement( Object oldBackingStore, Object newBackingStore )
        {
            g.dispose( );

            // Sync the whole surface
            TextureRenderer newRenderer = ( TextureRenderer ) newBackingStore;
            newRenderer.markDirty( 0, 0, newRenderer.getWidth( ), newRenderer.getHeight( ) );

            // Re-enter the begin / end pair if necessary
            if ( inBeginEndPair )
            {
                if ( isOrthoMode )
                {
                    ( ( TextureRenderer ) newBackingStore ).beginOrthoRendering( beginRenderingWidth, beginRenderingHeight, beginRenderingDepthTestDisabled );
                }
                else
                {
                    ( ( TextureRenderer ) newBackingStore ).begin3DRendering( );
                }

                //XXX is this necessary? we're not using pipelined quad renderer...
                // Push client attrib bits used by the pipelined quad renderer
                GL2 gl = GLU.getCurrentGL( ).getGL2( );
                gl.glPushClientAttrib( ( int ) GL2.GL_ALL_CLIENT_ATTRIB_BITS );
            }

            // notify update listeners that the TextureAtlas was reorganized
            for ( TextureAtlasUpdateListener listener : updateListeners )
            {
                listener.reorganized( );
            }
        }

        @Override
        public boolean canCompact( )
        {
            // TODO Not sure about this one. --ttran17
            return true;
        }
    }
}
