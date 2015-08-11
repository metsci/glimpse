package com.metsci.glimpse.layout.matcher;

import java.util.List;

import com.metsci.glimpse.context.GlimpseTarget;

public interface TargetStackMatcher
{
    public boolean matches( List<GlimpseTarget> stack );

}
