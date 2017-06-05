package com.metsci.glimpse.util.var;

public interface Dispatcher
{

    void fireForSubtree( Var<?> root, VarEvent ev );

}
