package com.metsci.glimpse.painter.texture;

import com.metsci.glimpse.gl.texture.DrawableTextureProgram;

public interface HeatMapProgram extends DrawableTextureProgram
{

    void setAlpha( float alpha );

    void setDiscardNan( boolean discardNan );

    void setDiscardBelow( boolean discardBelow );

    void setDiscardAbove( boolean discardAbove );

}
