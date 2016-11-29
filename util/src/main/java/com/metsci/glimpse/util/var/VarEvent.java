package com.metsci.glimpse.util.var;

/**
 * This class deliberately lacks fields having to do with the Var's value, to
 * encourage (well, to force) listeners to query the Var directly. This helps
 * avoid bugs in "nested modification" situations, in which one modification
 * triggers a listener that makes a second modification. In such a case, some
 * listeners can get notified of the modifications out of order.
 */
public class VarEvent
{

    public final boolean ongoing;


    public VarEvent( boolean ongoing )
    {
        this.ongoing = ongoing;
    }

}
