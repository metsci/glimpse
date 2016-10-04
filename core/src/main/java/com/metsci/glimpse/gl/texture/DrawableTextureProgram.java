package com.metsci.glimpse.gl.texture;

import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.gl.GLStreamingBuffer;

/**
 * A minimum set of operations for drawing textured triangles.<p>
 *
 * Intended for use with {@code DrawableTexture}.
 *
 * @author ulman
 */
public interface DrawableTextureProgram
{
    public void begin( GlimpseContext context, float xMin, float xMax, float yMin, float yMax );

    public void draw( GlimpseContext context, int mode, GLStreamingBuffer xyVbo, GLStreamingBuffer sVbo, int first, int count );

    public void draw( GlimpseContext context, int mode, int xyVbo, int sVbo, int first, int count );

    public void end( GlimpseContext context );

    public void dispose( GlimpseContext context );
}
