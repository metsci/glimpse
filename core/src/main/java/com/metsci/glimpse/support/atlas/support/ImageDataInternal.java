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
package com.metsci.glimpse.support.atlas.support;

import com.jogamp.opengl.util.texture.TextureCoords;

/**
 * Metadata for each image in the TextureAtlas. Used to store data about images which
 * have already been incorporated into the TextureAtlas. For images waiting in the queue
 * to be added to the TextureAtlas, see ImageDataExternal.
 *
 * @author ulman
 * @see com.sun.opengl.util.j2d.TextRenderer$TextData
 * @see ImageDataExternal
 */
public class ImageDataInternal
{
    // unique identifier for the image data
    private Object id;

    // the coordinates of the "center" of the image relative to the lower left pixel
    // (not necessarily width/2, height/2)
    private int centerX;
    private int centerY;

    // the x and y location of the lower left of the image relative to the whole texture atlas
    private int locationX;
    private int locationY;

    // the number of blank pixels around the edge of the image
    private int bufferX;
    private int bufferY;

    // the size of the image (not including buffer)
    private int sizeX;
    private int sizeY;

    private TextureCoords texCoords;

    private boolean delete = false; // whether the texture is no longer needed

    public ImageDataInternal( Object id, int centerX, int centerY, int bufferX, int bufferY, int width, int height )
    {
        this.id = id;

        this.centerX = centerX;
        this.centerY = centerY;

        this.bufferX = bufferX;
        this.bufferY = bufferY;

        this.sizeX = width;
        this.sizeY = height;
    }

    public void setTextureCoordinates( TextureCoords textureCoordinates )
    {
        this.texCoords = textureCoordinates;
    }

    public Object getId( )
    {
        return id;
    }

    public int getBufferX( )
    {
        return bufferX;
    }

    public int getBufferY( )
    {
        return bufferY;
    }

    public int getLocationX( )
    {
        return locationX;
    }

    public int getLocationY( )
    {
        return locationY;
    }

    public int getCenterX( )
    {
        return centerX;
    }

    public int getCenterY( )
    {
        return centerY;
    }

    public int getBufferedWidth( )
    {
        return sizeX + 2 * bufferX;
    }

    public int getBufferedHeight( )
    {
        return sizeY + 2 * bufferY;
    }

    public int getWidth( )
    {
        return sizeX;
    }

    public int getHeight( )
    {
        return sizeY;
    }

    public TextureCoords getTextureCoordinates( )
    {
        return texCoords;
    }

    public boolean isMarkedForDelete( )
    {
        return delete;
    }

    public void delete( )
    {
        delete = true;
    }
}
