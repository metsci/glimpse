/*
 * Copyright (c) 2019, Metron, Inc.
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
package com.metsci.glimpse.topo;

import static com.metsci.glimpse.core.gl.util.GLUtils.deleteBuffers;
import static com.metsci.glimpse.core.gl.util.GLUtils.deleteTextures;

import com.jogamp.opengl.GL;

import com.metsci.glimpse.topo.io.TopoDataType;

public class TopoDeviceTile
{

    /**
     * North edge of data, INCLUDING border rows.
     */
    public final double northLat_RAD;

    /**
     * South edge of data, INCLUDING border rows.
     */
    public final double southLat_RAD;

    /**
     * East edge of data, INCLUDING border columns.
     */
    public final double eastLon_RAD;

    /**
     * West edge of data, INCLUDING border columns.
     */
    public final double westLon_RAD;

    public final int texture;
    public final TopoDataType textureDataType;

    public final int xyBuffer;
    public final int numVertices;

    public long frameNumOfLastUse;


    public TopoDeviceTile( double northLat_RAD,
                           double southLat_RAD,
                           double eastLon_RAD,
                           double westLon_RAD,

                           int texture,
                           TopoDataType textureDataType,

                           int xyBuffer,
                           int numVertices,

                           long frameNum )
    {
        this.northLat_RAD = northLat_RAD;
        this.southLat_RAD = southLat_RAD;
        this.eastLon_RAD = eastLon_RAD;
        this.westLon_RAD = westLon_RAD;

        this.texture = texture;
        this.textureDataType = textureDataType;

        this.xyBuffer = xyBuffer;
        this.numVertices = numVertices;

        this.frameNumOfLastUse = frameNum;
    }

    public void dispose( GL gl )
    {
        deleteBuffers( gl, this.xyBuffer );
        deleteTextures( gl, this.texture );
    }

}
