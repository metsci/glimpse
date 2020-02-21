/*
 * Copyright (c) 2020, Metron, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of Metron, Inc. nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL METRON, INC. BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.metsci.glimpse.docking;

import java.awt.Component;

import com.metsci.glimpse.docking.xml.GroupArrangement;

public interface DockingGroupListener
{

    void addedView( Tile tile, View view );

    void removedView( Tile tile, View view );

    void selectedView( Tile tile, View view );

    void addedLeaf( MultiSplitPane docker, Component leaf );

    void removedLeaf( MultiSplitPane docker, Component leaf );

    void movedDivider( MultiSplitPane docker, SplitPane splitPane );

    void maximizedLeaf( MultiSplitPane docker, Component leaf );

    void unmaximizedLeaf( MultiSplitPane docker, Component leaf );

    void restoredTree( MultiSplitPane docker );

    void addedWindow( DockingGroup group, DockingWindow window );

    /**
     * Called when the user tries to close a window in the {@link DockingGroup}, e.g. by clicking the
     * close button in the window's title bar.
     * <p>
     * Depending on the {@link DockingFrameCloseOperation}, this call might be followed automatically by
     * calls to {@link #disposingAllWindows(DockingGroup)}, {@link #disposingWindow(DockingGroup, DockingWindow)},
     * and/or {@link #disposedWindow(DockingGroup, DockingWindow)}.
     */
    void userRequestingDisposeWindow( DockingGroup group, DockingWindow window );

    void disposingAllWindows( DockingGroup group );

    void disposingWindow( DockingGroup group, DockingWindow window );

    void disposedWindow( DockingGroup group, DockingWindow window );

    void userRequestingCloseView( DockingGroup group, View view );

    void closingView( DockingGroup group, View view );

    void closedView( DockingGroup group, View view );

    void settingArrangement( DockingGroup group, GroupArrangement groupArr );

    void setArrangement( DockingGroup group, GroupArrangement groupArr );

}
