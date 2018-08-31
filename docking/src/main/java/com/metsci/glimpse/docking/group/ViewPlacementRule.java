package com.metsci.glimpse.docking.group;

import java.util.Set;

import com.metsci.glimpse.docking.group.frame.ViewPlacerMultiframe;
import com.metsci.glimpse.docking.xml.GroupArrangement;

public interface ViewPlacementRule
{

    <T> T placeView( GroupArrangement planArr, Set<String> existingViewIds, ViewPlacerMultiframe<T> placer );

}
