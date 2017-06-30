package com.metsci.glimpse.util.var;

import java.util.ArrayList;
import java.util.List;

public class Txn
{

    protected static class SubtreeModification
    {
        public final Var<?> root;
        public final VarEvent ev;

        public SubtreeModification( Var<?> root, VarEvent ev )
        {
            this.root = root;
            this.ev = ev;
        }
    }


    protected final List<SubtreeModification> subtreeMods;


    public Txn( )
    {
        this.subtreeMods = new ArrayList<>( );
    }

    public void recordSubtreeMod( Var<?> root, VarEvent ev )
    {
        this.subtreeMods.add( new SubtreeModification( root, ev ) );
    }

    public void commit( )
    {
        for ( SubtreeModification mod : this.subtreeMods )
        {
            mod.root.commitForSubtree( );
        }
        for ( SubtreeModification mod : this.subtreeMods )
        {
            mod.root.fireForSubtree( mod.ev );
        }
        this.subtreeMods.clear( );
    }

    public void rollback( )
    {
        for ( SubtreeModification mod : this.subtreeMods )
        {
            mod.root.rollbackForSubtree( );
        }
        this.subtreeMods.clear( );
    }

}
