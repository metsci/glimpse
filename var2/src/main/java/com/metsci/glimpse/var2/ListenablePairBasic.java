package com.metsci.glimpse.var2;

import static com.metsci.glimpse.var2.VarUtils.listenable;

public class ListenablePairBasic implements ListenablePair
{

    protected final ListenableBasic ongoing;
    protected final ListenableBasic completed;
    protected final Listenable all;

    public ListenablePairBasic( )
    {
        this.ongoing = new ListenableBasic( );
        this.completed = new ListenableBasic( );
        this.all = listenable( this.ongoing, this.completed );
    }

    public void fire( boolean ongoing )
    {
        ( ongoing ? this.ongoing : this.completed ).fire( );
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
