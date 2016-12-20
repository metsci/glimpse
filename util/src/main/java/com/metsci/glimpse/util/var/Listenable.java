package com.metsci.glimpse.util.var;

import java.util.function.Consumer;

public interface Listenable<T>
{

    Disposable addListener( boolean runImmediately, Runnable runnable );

    Disposable addListener( boolean runImmediately, Consumer<T> consumer );

}
