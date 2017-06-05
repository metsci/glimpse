package com.metsci.glimpse.util.var;

public class InlineDispatcher implements Dispatcher
{

    public static final InlineDispatcher inlineDispatcher = new InlineDispatcher( );


    protected InlineDispatcher( )
    { }

    @Override
    public void fireForSubtree( Var<?> root, VarEvent ev )
    {
        root.fireForSubtree( ev );
    }

}
