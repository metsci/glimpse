package com.metsci.glimpse.layers;

import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLRunnable;

import com.metsci.glimpse.context.GlimpseContext;

/**
 * Just like {@link GLRunnable}, but takes a {@link GlimpseContext} instead of a {@link GLAutoDrawable}.
 */
public interface GlimpseRunnable
{

    /**
     * Just like {@link GLRunnable#run(GLAutoDrawable)}, but takes a {@link GlimpseContext} instead of a {@link GLAutoDrawable}.
     */
    boolean run( GlimpseContext context );

}
