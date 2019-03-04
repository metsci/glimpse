package com.metsci.glimpse.var2;

class VarTestUtils
{

    public static <V> VarFiring<V> f( boolean ongoing, V value )
    {
        return new VarFiring<>( ongoing, value );
    }

    public static <V> OldNewPairFiring<V> f( boolean ongoing, V vOld, V vNew )
    {
        return new OldNewPairFiring<>( ongoing, vOld, vNew );
    }

}
