package com.metsci.glimpse.docking.group;

import java.util.Set;

import com.metsci.glimpse.docking.group.frame.ViewPlacerMultiframe;
import com.metsci.glimpse.docking.xml.GroupArrangement;

public interface ViewPlacementRule
{

    /**
     * <strong>NOTE:</strong> This method should really be declared with a type
     * parameter, but lambda syntax is not allowed when there are type parameters.
     * <p>
     * Implementations should make exactly one call to a {@code placer} method,
     * and return the value that the {@code placer} method returned. For example:
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
     */
    Object placeView( GroupArrangement planArr, Set<String> existingViewIds, ViewPlacerMultiframe<?> placer );

}
