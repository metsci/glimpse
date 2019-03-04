package com.metsci.glimpse.var2;

import static com.metsci.glimpse.var2.VarUtils.completedListenable;
import static com.metsci.glimpse.var2.VarUtils.doAddPairListener;
import static com.metsci.glimpse.var2.VarUtils.doHandleImmediateFlag;
import static com.metsci.glimpse.var2.VarUtils.listenable;
import static com.metsci.glimpse.var2.VarUtils.mapCollection;
import static java.util.Arrays.asList;

import java.util.Collection;
import java.util.Set;

import com.metsci.glimpse.util.var.Disposable;

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

    @Deprecated
    protected static Listenable ongoingListenable( Collection<? extends ListenablePair> pairs )
    {
        return listenable( mapCollection( pairs, ListenablePair::ongoing ) );
    }

    @Deprecated
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

    @Override
    public Disposable addListener( Set<? extends ListenerFlag> flags, ListenablePairListener listener )
    {
        return doHandleImmediateFlag( flags, listener, flags2 ->
        {
            return doAddPairListener( this.ongoing, this.completed, flags2, listener );
        } );
    }

}
