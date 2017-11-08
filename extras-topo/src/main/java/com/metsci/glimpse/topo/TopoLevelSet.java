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
package com.metsci.glimpse.topo;

import static com.metsci.glimpse.util.logging.LoggerUtils.getLogger;
import static com.metsci.glimpse.util.logging.LoggerUtils.logWarning;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import com.google.common.collect.ImmutableList;
import com.metsci.glimpse.topo.io.TopoDataFile;
import com.metsci.glimpse.topo.io.TopoDataset;
import com.metsci.glimpse.util.primitives.sorted.SortedDoubles;
import com.metsci.glimpse.util.primitives.sorted.SortedDoublesArray;
import com.metsci.glimpse.util.primitives.sorted.SortedDoublesModifiable;

public class TopoLevelSet
{
    private static final Logger logger = getLogger( TopoLevelSet.class );


    public static TopoLevelSet createTopoLevels( TopoDataset dataset, int maxRowsPerBand, int maxColsPerTile )
    {
        Set<TopoLevel> levels = new LinkedHashSet<>( );
        for ( TopoDataFile file : dataset.levels )
        {
            try
            {
                TopoLevel level = new TopoLevel( file, maxRowsPerBand, maxColsPerTile );
                levels.add( level );
            }
            catch ( Exception e )
            {
                // TODO: Allow exception to be reported to user
                logWarning( logger, "Failed to load topo level: file = " + file.dataFile, e );
            }
        }
        return new TopoLevelSet( levels );
    }


    public final ImmutableList<TopoLevel> levels;
    public final SortedDoubles cellSizes_DEG;


    protected TopoLevelSet( Collection<? extends TopoLevel> levels )
    {
        List<TopoLevel> levels0 = new ArrayList<>( );
        SortedDoublesModifiable cellSizes0_DEG = new SortedDoublesArray( );
        for ( TopoLevel level : levels )
        {
            int i = cellSizes0_DEG.add( level.cellSize_DEG );
            levels0.add( i, level );
        }
        this.levels = ImmutableList.copyOf( levels0 );
        this.cellSizes_DEG = cellSizes0_DEG;
    }

    public int size( )
    {
        return this.levels.size( );
    }

    public TopoLevel get( int levelNum )
    {
        return this.levels.get( levelNum );
    }

    public void dispose( )
    {
        for ( TopoLevel level : this.levels )
        {
            level.dispose( );
        }
    }

}
