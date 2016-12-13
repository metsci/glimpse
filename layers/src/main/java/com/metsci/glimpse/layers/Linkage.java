package com.metsci.glimpse.layers;

public class Linkage
{

    protected final Trait master;
    protected final boolean isImplicit;


    public Linkage( Trait master, boolean isImplicit )
    {
        this.master = master;
        this.isImplicit = isImplicit;
    }

    public boolean canAdd( Trait trait )
    {
        return trait.parent( ).validateFn.test( this.master );
    }

    public void add( Trait trait )
    {
        trait.parent( ).set( this.master );
    }

    public Trait create( )
    {
        return this.master.createClone( );
    }

}
