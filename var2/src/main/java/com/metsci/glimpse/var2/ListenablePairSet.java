package com.metsci.glimpse.var2;

import static com.metsci.glimpse.var2.VarUtils.completedListenable;
import static com.metsci.glimpse.var2.VarUtils.listenable;
import static com.metsci.glimpse.var2.VarUtils.ongoingListenable;
import static java.util.Arrays.asList;

import java.util.Collection;

public class ListenablePairSet implements ListenablePair
{

    protected final Listenable ongoing;
    protected final Listenable completed;
    protected final Listenable all;


    @SafeVarargs
    public ListenablePairSet( ListenablePair... members )
    {
        this( asList( members ) );
    }

    public ListenablePairSet( Collection<? extends ListenablePair> members )
    {
        this.ongoing = ongoingListenable( members );
        this.completed = completedListenable( members );
        this.all = listenable( this.ongoing, this.completed );
    }

    @Override
    public Listenable ongoing( )
    {
        return this.ongoing;
    }

    @Override
    public Listenable completed( )
    {
        return this.completed;
    }

    @Override
    public Listenable all( )
    {
        return this.all;
    }

}
