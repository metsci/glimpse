package com.metsci.glimpse.var2;

import static com.metsci.glimpse.var2.ListenerFlag.EMPTY_FLAGS;
import static com.metsci.glimpse.var2.ListenerFlag.flags;

import java.util.Set;

import com.metsci.glimpse.util.var.Disposable;

public interface Listenable
{

    Disposable addListener( Set<? extends ListenerFlag> flags, Runnable listener );

    default Disposable addListener( ListenerFlag flag, Runnable listener )
    {
        return this.addListener( flags( flag ), listener );
    }

    default Disposable addListener( Runnable listener )
    {
        return this.addListener( EMPTY_FLAGS, listener );
    }

}
