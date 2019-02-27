package com.metsci.glimpse.var2;

import static com.metsci.glimpse.var2.ListenerFlag.IMMEDIATE;
import static com.metsci.glimpse.var2.ListenerFlag.ONCE;

import java.util.Collection;

import com.metsci.glimpse.var2.ListenerFlag.ListenerFlagOrder;

public class ListenerFlagSet
{

    public final boolean immediate;
    public final int order;
    public final boolean once;


    public ListenerFlagSet( Collection<? extends ListenerFlag> flags )
    {
        this.immediate = flags.contains( IMMEDIATE );
        this.order = findOrder( flags );
        this.once = flags.contains( ONCE );
    }

    public static <F extends ListenerFlag> F findFlag( Collection<? extends ListenerFlag> flags, Class<F> clazz )
    {
        for ( ListenerFlag flag : flags )
        {
            if ( clazz.isInstance( flag ) )
            {
                return clazz.cast( flag );
            }
        }
        return null;
    }

    public static int findOrder( Collection<? extends ListenerFlag> flags )
    {
        ListenerFlagOrder flag = findFlag( flags, ListenerFlagOrder.class );
        return ( flag == null ? 0 : flag.order );
    }

}
