package com.metsci.glimpse.util.var;

import java.util.function.Consumer;

public interface Listenable<T>
{

    Runnable addListener( boolean runImmediately, Runnable runnable );

    void removeListener( Runnable runnable );

    Consumer<T> addListener( boolean runImmediately, Consumer<T> consumer );

    void removeListener( Consumer<T> consumer );

}
