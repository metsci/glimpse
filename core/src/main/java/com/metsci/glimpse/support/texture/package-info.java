/**
 * Extensions to the basic {@link com.metsci.glimpse.gl.texture.Texture} subclasses.
 * Because OpenGL implementations often have a maximum texture size, this package
 * provide Glimpse texture wrappers which automatically break themselves up
 * into smaller OpenGL textures if they are too large to be loaded onto the graphics card as a single.
 * The package also provides textures with the ability to project themselves into non-rectangular
 * shapes.
 */
package com.metsci.glimpse.support.texture;
