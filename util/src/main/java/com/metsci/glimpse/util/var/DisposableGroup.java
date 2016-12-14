package com.metsci.glimpse.util.var;

import java.util.ArrayList;
import java.util.Collection;

public class DisposableGroup implements Disposable
{

    protected final Collection<Disposable> members;


    public DisposableGroup( )
    {
        this.members = new ArrayList<>( );
    }

    public void add( Disposable member )
    {
        this.members.add( member );
    }

    public void remove( Disposable member )
    {
        this.members.remove( member );
    }

    public void clear( )
    {
        this.members.clear( );
    }

    @Override
    public void dispose( )
    {
        for ( Disposable member : this.members )
        {
            member.dispose( );
        }
    }

}
