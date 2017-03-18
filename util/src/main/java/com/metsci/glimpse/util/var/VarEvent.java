package com.metsci.glimpse.util.var;

/**
 * This class deliberately lacks fields having to do with the Var's value, to
 * encourage (well, to force) listeners to query the Var directly. This helps
 * avoid bugs in situations with nested modification, in which one modification
 * triggers a listener that makes a second modification. In such a case, some
 * listeners can get notified of the modifications out of order.
 * <p>
 * If a listener needs to know the new value, it should query the Var directly.
 * If a listener needs to know the old value as well, the listener itself must
 * keep track of the value the Var had the last time <em>that listener</em> was
 * fired. <em>This may be different for different listeners</em>, especially in
 * a situation with nested modification.
 * <p>
 * Some helper functions, especially for listeners needing to know the Var's old
 * value, can be found in {@link VarUtils}.
 */
public class VarEvent
{

    public final boolean ongoing;


    public VarEvent( boolean ongoing )
    {
        this.ongoing = ongoing;
    }

}
