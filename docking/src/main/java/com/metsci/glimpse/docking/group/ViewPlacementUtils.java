package com.metsci.glimpse.docking.group;

import static com.metsci.glimpse.docking.MiscUtils.intersection;
import static com.metsci.glimpse.docking.group.ArrangementUtils.findViewIds;
import static java.lang.Math.max;
import static java.lang.Math.min;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.metsci.glimpse.docking.xml.DockerArrangementNode;
import com.metsci.glimpse.docking.xml.DockerArrangementTile;
import com.metsci.glimpse.docking.xml.GroupArrangement;

public class ViewPlacementUtils
{

    /**
     * Returns the set of viewIds which don't currently exist, but do appear in the plan.
     */
    public static Set<String> futureViewIds( DockingGroupBase group, GroupArrangement planArr )
    {
        Set<String> result = findViewIds( planArr );
        for ( String viewId : group.views( ).keySet( ) )
        {
            result.remove( viewId );
        }
        return result;
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
