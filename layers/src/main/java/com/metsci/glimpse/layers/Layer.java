package com.metsci.glimpse.layers;

import java.util.Map;

public interface Layer
{

    String getTitle( );

    boolean isVisible( );

    void setVisible( boolean visible );

    Map<? extends LayeredView,? extends LayerRepr> reprs( );

    void installTo( LayeredView view );

    void uninstallFrom( LayeredView view, boolean isReinstall );

}
