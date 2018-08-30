package com.metsci.glimpse.docking.group;

import java.util.Set;

import com.metsci.glimpse.docking.xml.GroupArrangement;

public interface ViewPlacementRule
{

    ViewPlacement getPlacement( GroupArrangement planArr, Set<String> existingViewIds );

}
