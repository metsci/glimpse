package com.metsci.glimpse.docking.group;

import static com.google.common.base.Objects.equal;
import static com.metsci.glimpse.docking.MiscUtils.intersection;
import static com.metsci.glimpse.docking.group.ArrangementUtils.findViewIds;
import static java.lang.Math.max;
import static java.lang.Math.min;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.metsci.glimpse.docking.DockingGroup;
import com.metsci.glimpse.docking.View;
import com.metsci.glimpse.docking.xml.DockerArrangementNode;
import com.metsci.glimpse.docking.xml.DockerArrangementTile;
import com.metsci.glimpse.docking.xml.GroupArrangement;

public class ViewPlacementUtils
{

    /**
     * This method does not currently support changing the placement of existing views. If there
     * is an existing view for the specified {@code viewId}, an exception will be thrown.
     * <p>
     * The {@link ViewPlacement} returned by {@code placementRule} will be used for its
     * {@link ViewPlacement#placeView(GroupArrangement, String)} method only. (In most cases --
     * but NOT in all cases -- this means that {@code placementRule} doesn't need to worry about
     * arguments called {@code planFrame} or {@code planTile}, and can simply use {@code null} for
     * those args. But it depends on the particular implementation of {@link ViewPlacement}.)
     */
    public static GroupArrangement rebuildPlanArr( DockingGroupBase group, GroupArrangement planArr, ViewPlacementRule placementRule, String viewId )
    {
        // Remember which viewIds currently exist
        Set<String> existingViewIds = existingViewIds( group );
        if ( existingViewIds.contains( viewId ) )
        {
            // TODO: Maybe remove the existing view, insert placement, and re-add
            throw new UnsupportedOperationException( "This method does not currently support changing the placement of an existing view" );
        }

        // Create a new arrangement, starting with existing views
        GroupArrangement newPlanArr = existingArr;

        // Add viewIds that don't exist, but do have planned placements
        for ( String planViewId : findViewIds( planArr ) )
        {
            // Skip the new viewId here, because it will be added below
            if ( !existingViewIds.contains( planViewId ) && !equal( planViewId, viewId ) )
            {
                addToArr( newPlanArr, planArr, planViewId );
            }
        }

        // Add the new view
        addToArr( newPlanArr, placementRule, existingViewIds, viewId );

        // Return complete arrangement
        return newPlanArr;
    }

    public static GroupArrangement rebuildPlanArr( DockingGroupBase group, GroupArrangement planArr )
    {
        // Remember which viewIds currently exist
        Set<String> existingViewIds = existingViewIds( group );

        // Create a new arrangement, starting with existing views
        GroupArrangement newPlanArr = existingArr;

        // Add viewIds that don't exist, but do have planned placements
        for ( String planViewId : findViewIds( planArr ) )
        {
            if ( !existingViewIds.contains( planViewId ) )
            {
                addToArr( newPlanArr, planArr, planViewId );
            }
        }

        // Return complete arrangement
        return newPlanArr;
    }

    public static Set<String> existingViewIds( DockingGroup group )
    {
        Set<String> viewIds = new LinkedHashSet<>( );
        for ( View view : group.views( ) )
        {
            viewIds.add( view.viewId );
        }
        return viewIds;
    }

    public static int chooseViewNum( List<String> planViewIds, List<String> existingViewIds, String newViewId )
    {
        Set<String> viewIdsBefore = new LinkedHashSet<>( );
        Set<String> viewIdsAfter = new LinkedHashSet<>( );

        boolean beforeNewViewId = true;
        for ( String viewId : planViewIds )
        {
            if ( viewId.equals( newViewId ) )
            {
                beforeNewViewId = false;
            }
            else if ( beforeNewViewId )
            {
                viewIdsBefore.add( viewId );
            }
            else
            {
                viewIdsAfter.add( viewId );
            }
        }

        int viewNumBefore = -1;
        int viewNumAfter = existingViewIds.size( );

        for ( int i = 0; i < existingViewIds.size( ); i++ )
        {
            String viewId = existingViewIds.get( i );

            if ( viewIdsBefore.contains( viewId ) )
            {
                viewNumBefore = max( viewNumBefore, i );
            }

            if ( viewIdsAfter.contains( viewId ) )
            {
                viewNumAfter = min( viewNumAfter, i );
            }
        }

        if ( viewNumBefore < viewNumAfter )
        {
            return ( viewNumBefore + 1 );
        }
        else
        {
            return existingViewIds.size( );
        }
    }

    public static DockerArrangementTile findSimilarArrTile( Map<DockerArrangementNode,Set<String>> subtreeViewIds, Set<String> viewIds )
    {
        return ( DockerArrangementTile ) findSimilarArrNode( subtreeViewIds, viewIds, true );
    }

    public static DockerArrangementNode findSimilarArrNode( Map<DockerArrangementNode,Set<String>> subtreeViewIds, Set<String> viewIds )
    {
        return findSimilarArrNode( subtreeViewIds, viewIds, false );
    }

    public static DockerArrangementNode findSimilarArrNode( Map<DockerArrangementNode,Set<String>> subtreeViewIds, Set<String> viewIds, boolean requireTile )
    {
        DockerArrangementNode bestNode = null;
        int bestCommonCount = 0;
        int bestExtraneousCount = 0;

        for ( Entry<DockerArrangementNode,Set<String>> en : subtreeViewIds.entrySet( ) )
        {
            DockerArrangementNode node = en.getKey( );
            Set<String> nodeViewIds = en.getValue( );

            if ( !requireTile || node instanceof DockerArrangementTile )
            {
                int commonCount = intersection( nodeViewIds, viewIds ).size( );
                int extraneousCount = nodeViewIds.size( ) - commonCount;
                if ( commonCount > bestCommonCount || ( commonCount == bestCommonCount && extraneousCount < bestExtraneousCount ) )
                {
                    bestNode = node;
                    bestCommonCount = commonCount;
                    bestExtraneousCount = extraneousCount;
                }
            }
        }

        return bestNode;
    }

}
