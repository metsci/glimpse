package com.metsci.glimpse.util.var;

import java.util.ArrayList;
import java.util.List;

public class Batch implements Dispatcher
{

    protected static class Entry
    {
        public final Var<?> root;
        public final VarEvent event;

        public Entry( Var<?> root, VarEvent event )
        {
            this.root = root;
            this.event = event;
        }
    }


    protected final List<Entry> queue;


    public Batch( )
    {
        this.queue = new ArrayList<>( );
    }

    @Override
    public void fireForSubtree( Var<?> root, VarEvent ev )
    {
        this.queue.add( new Entry( root, ev ) );
    }

    public void apply( )
    {
        for ( Entry en : this.queue )
        {
            en.root.fireForSubtree( en.event );
        }
        this.queue.clear( );
    }

}
