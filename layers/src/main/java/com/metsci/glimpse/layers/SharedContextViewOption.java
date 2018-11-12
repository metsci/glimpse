package com.metsci.glimpse.layers;

import com.jogamp.opengl.GLContext;

public class SharedContextViewOption implements ViewOption {
    public final GLContext context;

    public SharedContextViewOption(GLContext context) {
        this.context = context;
    }
}
