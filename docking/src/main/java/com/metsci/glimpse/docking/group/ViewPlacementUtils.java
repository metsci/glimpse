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
