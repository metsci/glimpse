/*
 * Copyright 2012 JogAmp Community. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 *    1. Redistributions of source code must retain the above copyright notice, this list of
 *       conditions and the following disclaimer.
 *
 *    2. Redistributions in binary form must reproduce the above copyright notice, this list
 *       of conditions and the following disclaimer in the documentation and/or other materials
 *       provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY JogAmp Community ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL JogAmp Community OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those of the
 * authors and should not be interpreted as representing official policies, either expressed
 * or implied, of JogAmp Community.
 */
package com.metsci.glimpse.jogamp.opengl.util.awt.text;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GLProfile;

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
