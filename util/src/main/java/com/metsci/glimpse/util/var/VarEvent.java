package com.metsci.glimpse.util.var;

/**
 * This class deliberately lacks fields having to do with the Var's value, to
 * encourage (well, to force) listeners to query the Var directly. This helps
 * avoid bugs in "nested modification" situations, in which one modification
 * triggers a listener that makes a second modification. In such a case, some
 * listeners can get notified of the modifications out of order.
 * <p>
 * The best way to pass value information to listeners is for each listener to
 * query current value of the Var, and to keep track of the value the Var had
 * the last time the listener was fired. Some helper functions can be found in
 * {@link VarUtils}.
 */
public class VarEvent
{

    public final boolean ongoing;


    public VarEvent( boolean ongoing )
    {
        this.ongoing = ongoing;
    }

}
