package com.metsci.glimpse.layers;

import com.metsci.glimpse.util.var.Var;

public abstract class Trait
{

    public final boolean isLinkage;
    public final Var<Trait> parent;


    protected Trait( boolean isLinkage )
    {
        this.isLinkage = isLinkage;

        this.parent = new Var<>( null, ( candidate ) ->
        {
            if ( candidate == null )
            {
                return true;
            }
            else if ( isLinkage )
            {
                // A linkage cannot have a non-null parent
                return false;
            }
            else if ( !candidate.isLinkage )
            {
                // Only a linkage can be a parent
                return false;
            }
            else
            {
                return this.isValidParent( candidate );
            }
        } );
    }

    protected abstract boolean isValidParent( Trait parent );

    public abstract Trait copy( boolean isLinkage );

}
