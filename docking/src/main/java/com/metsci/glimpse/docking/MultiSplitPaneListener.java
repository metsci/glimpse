package com.metsci.glimpse.docking;

import java.awt.Component;

public interface MultiSplitPaneListener
{

    void addedLeaf( Component leaf );

    void removedLeaf( Component leaf );

    void movedDivider( SplitPane splitPane );

    void maximizedLeaf( Component leaf );

    void unmaximizedLeaf( Component leaf );

    void restoredTree( );

}
