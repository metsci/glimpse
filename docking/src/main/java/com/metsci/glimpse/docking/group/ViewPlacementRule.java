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
