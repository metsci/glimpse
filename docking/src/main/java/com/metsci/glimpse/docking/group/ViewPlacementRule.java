/*
 * Copyright (c) 2016, Metron, Inc.
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
package com.metsci.glimpse.docking.group;

import java.util.Set;

import com.metsci.glimpse.docking.DockingGroup;
import com.metsci.glimpse.docking.group.frame.DockingGroupMultiframe;
import com.metsci.glimpse.docking.group.frame.ViewPlacerMultiframe;
import com.metsci.glimpse.docking.xml.GroupArrangement;

public interface ViewPlacementRule
{

    /**
     * It is fine for implementations to down-cast the {@code placer} argument, as
     * long as it is done with care. The {@code placer} will be an instance of a
     * predictable subclass, based on the {@link DockingGroup} from which this rule
     * is being invoked. For example, when invoked by a {@link DockingGroupMultiframe},
     * the rule will be given a {@link ViewPlacerMultiframe}.
     * <p>
     * Implementations should make exactly one call to one of the {@code placer} methods,
     * and return the result. For example:
     * <pre>
     * <code>
     * ViewPlacementRule rule = ( planArr, existingViewIds, placer ) ->
     * {
     *     DockerArrangementTile tile = ...;
     *     int viewNum = ...;
     *     return placer.addToTile( tile, viewNum );
     * };
     * </code>
     * </pre>
     * <strong>NOTE:</strong> This method could use generics to declare that the return
     * type should match the type parameter of {@code placer}. However, Java does not allow
     * lambda syntax when there are type parameters involved, so using generics would make
     * this interface more cumbersome to work with.
     */
    Object placeView( GroupArrangement planArr, Set<String> existingViewIds, ViewPlacer<?> placer );

}
