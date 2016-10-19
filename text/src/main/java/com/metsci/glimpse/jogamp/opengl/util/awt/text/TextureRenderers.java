package com.metsci.glimpse.jogamp.opengl.util.awt.text;

import javax.media.opengl.GL;
import javax.media.opengl.GLProfile;

public class TextureRenderers
{
    /**
     * Prevents instantiation.
     */
    private TextureRenderers( )
    {
        // pass
    }

    /**
     * Creates a {@link GlyphRenderer} based on the current OpenGL context.
     *
     * @param gl Current OpenGL context
     * @return New glyph renderer for the given context, not null
     * @throws NullPointerException if context is null
     * @throws UnsupportedOperationException if GL is unsupported
     */
    /*@Nonnull*/
    public static TextureRenderer get( /*@Nonnull*/ final GL gl )
    {
        Check.notNull( gl, "GL cannot be null" );

        final GLProfile profile = gl.getGLProfile( );

        if ( profile.isGL3( ) )
        {
            return new TextureRendererGL30( gl.getGL3( ) );
        }
        else
        {
            throw new UnsupportedOperationException( "Profile currently unsupported" );
        }
    }
}
