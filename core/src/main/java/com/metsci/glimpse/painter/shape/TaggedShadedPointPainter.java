package com.metsci.glimpse.painter.shape;

import java.io.IOException;

import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.axis.tagged.TaggedAxis1D;
import com.metsci.glimpse.axis.tagged.shader.TaggedPointShader;
import com.metsci.glimpse.support.shader.SimplePointShader;

public class TaggedShadedPointPainter extends ShadedPointPainter
{
    public TaggedShadedPointPainter( TaggedAxis1D colorAxis, TaggedAxis1D sizeAxis ) throws IOException
    {
        super( colorAxis, sizeAxis );
    }

    @Override
    protected SimplePointShader newShader( Axis1D colorAxis, Axis1D sizeAxis ) throws IOException
    {
        return new TaggedPointShader( 0, 1, (TaggedAxis1D) colorAxis, (TaggedAxis1D) sizeAxis );
    }
}