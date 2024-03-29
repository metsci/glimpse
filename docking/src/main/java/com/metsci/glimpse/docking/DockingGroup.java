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

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.metsci.glimpse.docking.group.DockingGroupBase;
import com.metsci.glimpse.docking.group.ViewPlacementRule;
import com.metsci.glimpse.docking.xml.GroupArrangement;
import com.metsci.glimpse.util.var.Disposable;

/**
 * Docking group API for use by client code.
 * <p>
 * The internals of the docking code are written in terms of {@link DockingGroupBase},
 * which has several additional public methods.
 */
public interface DockingGroup
{

    DockingTheme theme( );

    Disposable addListener( DockingGroupListener listener );

    void removeListener( DockingGroupListener listener );

    List<? extends DockingWindow> windows( );

    Map<String,View> views( );

    void addViewPlacement( String viewId, ViewPlacementRule placementRule );

    void addView( View view );

    void addViews( View... views );

    void addViews( Collection<View> views );

    void selectView( View view );

    void closeView( View view );

    void setArrangement( GroupArrangement groupArr );

    /**
     * Equivalent to {@link #captureArrangement(boolean)} with {@code false} for the
     * {@code includePlanViews} arg.
     */
    GroupArrangement captureArrangement( );

    /**
     * The {@code includePlanViews} arg indicates whether or not the returned
     * arrangement should include the planned placements for views that don't
     * currently exist.
     */
    GroupArrangement captureArrangement( boolean includePlanViews );

    void setVisible( boolean visible );

    boolean isVisible( );

    void dispose( );

}
