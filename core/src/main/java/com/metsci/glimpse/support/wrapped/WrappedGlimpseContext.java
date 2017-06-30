package com.metsci.glimpse.support.wrapped;

import static com.metsci.glimpse.support.wrapped.Wrapper2D.NOOP_WRAPPER_2D;

import javax.media.opengl.GLContext;

import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.context.GlimpseContextImpl;

public class WrappedGlimpseContext extends GlimpseContextImpl
{

    public static Wrapper2D getWrapper2D( GlimpseContext context )
    {
        if ( context instanceof WrappedGlimpseContext )
        {
            return ( ( WrappedGlimpseContext ) context ).wrapper;
        }
        else
        {
            return NOOP_WRAPPER_2D;
        }
    }


    protected final Wrapper2D wrapper;


    public WrappedGlimpseContext( GLContext glContext, int[] scale, Wrapper2D wrapper )
    {
        super( glContext, scale );
        this.wrapper = wrapper;
    }

}
