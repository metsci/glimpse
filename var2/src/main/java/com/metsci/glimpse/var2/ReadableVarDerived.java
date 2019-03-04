package com.metsci.glimpse.var2;

import static com.metsci.glimpse.var2.VarUtils.completedListenable;
import static com.metsci.glimpse.var2.VarUtils.doAddPairListener;
import static com.metsci.glimpse.var2.VarUtils.doHandleImmediateFlag;
import static com.metsci.glimpse.var2.VarUtils.filterListenable;
import static com.metsci.glimpse.var2.VarUtils.filterListener;
import static com.metsci.glimpse.var2.VarUtils.listenable;
import static com.metsci.glimpse.var2.VarUtils.mapCollection;
import static java.util.Arrays.asList;

import java.util.Collection;
import java.util.Set;

import com.metsci.glimpse.util.var.Disposable;

public abstract class ReadableVarDerived<V> implements ReadableVar<V>
{

    protected final Listenable ongoingRaw;
    protected final Listenable completedRaw;
    protected final Listenable allRaw;

    protected final Listenable ongoingFiltered;
    protected final Listenable completedFiltered;
    protected final Listenable allFiltered;


    @SafeVarargs
    public ReadableVarDerived( ListenablePair... listenables )
    {
        this( asList( listenables ) );
    }

    public ReadableVarDerived( Collection<? extends ListenablePair> listenables )
    {
        this.ongoingRaw = ongoingListenable( listenables );
        this.completedRaw = completedListenable( listenables );
        this.allRaw = listenable( this.ongoingRaw, this.completedRaw );

        this.completedFiltered = filterListenable( this.completedRaw, this::v );
        this.ongoingFiltered = filterListenable( this.ongoingRaw, this::v );
        this.allFiltered = filterListenable( this.allRaw, this::v );
    }

    @Deprecated
    protected static Listenable ongoingListenable( Collection<? extends ListenablePair> pairs )
    {
        return listenable( mapCollection( pairs, ListenablePair::ongoing ) );
    }

    @Override
    public abstract V v( );

    @Deprecated
    @Override
    public Listenable ongoing( )
    {
        return this.ongoingFiltered;
    }

    @Override
    public Listenable completed( )
    {
        return this.completedFiltered;
    }

    @Override
    public Listenable all( )
    {
        return this.allFiltered;
    }

    @Override
    public Disposable addListener( Set<? extends ListenerFlag> flags, ListenablePairListener listener )
    {
        return doHandleImmediateFlag( flags, listener, flags2 ->
        {
            ListenablePairListener listener2 = filterListener( listener, this::v );
            return doAddPairListener( this.ongoingRaw, this.completedRaw, flags2, listener2 );
        } );
    }

}
