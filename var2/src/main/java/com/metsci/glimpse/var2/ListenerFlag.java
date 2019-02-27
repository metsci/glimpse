package com.metsci.glimpse.var2;

import com.google.common.collect.ImmutableSet;

public interface ListenerFlag
{

    public static final ImmutableSet<ListenerFlag> EMPTY_FLAGS = flags( );

    public static final ListenerFlag IMMEDIATE = new ListenerFlagSimple( "IMMEDIATE" );
    public static final ListenerFlag ONCE = new ListenerFlagSimple( "ONCE" );

    public static ListenerFlag ORDER( int order )
    {
        return new ListenerFlagOrder( order );
    }

    public static ImmutableSet<ListenerFlag> flags( ListenerFlag... flags )
    {
        return ImmutableSet.copyOf( flags );
    }

    public static class ListenerFlagSimple implements ListenerFlag
    {
        public final String description;

        public ListenerFlagSimple( String description )
        {
            this.description = description;
        }

        @Override
        public String toString( )
        {
            return this.description;
        }
    }

    public static class ListenerFlagOrder implements ListenerFlag
    {
        public final int order;

        public ListenerFlagOrder( int order )
        {
            this.order = order;
        }

        @Override
        public String toString( )
        {
            return ( "ORDER(" + this.order + ")" );
        }
    }

}
