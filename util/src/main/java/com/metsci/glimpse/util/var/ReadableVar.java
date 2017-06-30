package com.metsci.glimpse.util.var;

public interface ReadableVar<V> extends Listenable<VarEvent>
{

    V v( );

}
