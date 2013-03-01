package com.metsci.glimpse.plot.timeline.group;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.metsci.glimpse.plot.stacked.PlotInfo;
import com.metsci.glimpse.plot.stacked.PlotInfoImpl;

public class GroupUtilities
{
    public static void getSortedPlots( Collection<PlotInfo> toVisitUnsorted, List<PlotInfo> accumulator )
    {
        if ( toVisitUnsorted == null || toVisitUnsorted.isEmpty( ) ) return;

        List<PlotInfo> toVisitSorted = new ArrayList<PlotInfo>( );
        toVisitSorted.addAll( toVisitUnsorted );
        Collections.sort( toVisitSorted, PlotInfoImpl.getComparator( ) );

        for ( PlotInfo info : toVisitSorted )
        {
            accumulator.add( info );

            if ( info instanceof GroupInfo )
            {
                GroupInfo group = ( GroupInfo ) info;
                getSortedPlots( group.getChildPlots( ), accumulator );
            }
        }
    }
}
