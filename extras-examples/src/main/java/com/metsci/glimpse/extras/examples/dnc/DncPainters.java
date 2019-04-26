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
package com.metsci.glimpse.extras.examples.dnc;

import static com.metsci.glimpse.dnc.DncProjections.dncPlateCarree;

import java.io.File;
import java.io.IOException;

import com.metsci.glimpse.dnc.DncPainter;
import com.metsci.glimpse.dnc.DncPainterSettingsImpl;
import com.metsci.glimpse.dnc.convert.Flat2Render.RenderCache;
import com.metsci.glimpse.dnc.convert.Flat2Render.RenderCacheConfig;
import com.metsci.glimpse.dnc.proj.DncProjection;

public class DncPainters
{

	public static DncPainter newPlateCarreeDncPainter( String flatParentPath ) throws IOException
	{
		File flatParentDir = new File( flatParentPath );
		return newPlateCarreeDncPainter( flatParentDir, new File( flatParentDir.getParent( ), "DNC_RENDER" ) );
	}

	public static DncPainter newPlateCarreeDncPainter( String flatParentPath, String renderParentPath ) throws IOException
	{
		return newPlateCarreeDncPainter( new File( flatParentPath ), new File( renderParentPath ) );
	}

	public static DncPainter newPlateCarreeDncPainter( File flatParentDir, File renderParentDir ) throws IOException
	{
		return newDncPainter( flatParentDir, renderParentDir, dncPlateCarree );
	}

	public static DncPainter newDncPainter( File flatParentDir, File renderParentDir, DncProjection projection ) throws IOException
	{
		RenderCacheConfig cacheConfig = new RenderCacheConfig( );
		cacheConfig.flatParentDir = flatParentDir;
		cacheConfig.renderParentDir = renderParentDir;
		cacheConfig.proj = projection;
		RenderCache cache = new RenderCache( cacheConfig, 4 );

		DncPainter painter = new DncPainter( cache, new DncPainterSettingsImpl( cacheConfig.proj ) );
		painter.activateCoverages( "lim", "nav", "cul", "iwy", "obs", "hyd", "por", "ecr", "lcr", "env", "rel", "coa" );
		return painter;
	}

}
