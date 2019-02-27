package com.metsci.glimpse.var2;

import static com.metsci.glimpse.var2.ListenerFlag.EMPTY_FLAGS;
import static com.metsci.glimpse.var2.ListenerFlag.flags;

import java.util.Set;
import java.util.function.Consumer;

import com.metsci.glimpse.util.var.Disposable;

public interface Notifier<T> extends Listenable
{

    Disposable addListener( Set<? extends ListenerFlag> flags, Consumer<? super T> listener );

    default Disposable addListener( ListenerFlag flag, Consumer<? super T> listener )
    {
        return this.addListener( flags( flag ), listener );
    }

    default Disposable addListener( Consumer<? super T> listener )
    {
        return this.addListener( EMPTY_FLAGS, listener );
    }

    @Override
    default Disposable addListener( Set<? extends ListenerFlag> flags, Runnable listener )
    {
        return this.addListener( flags, t -> listener.run( ) );
    }

}
