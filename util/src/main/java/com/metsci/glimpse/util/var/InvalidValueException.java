package com.metsci.glimpse.util.var;

public class InvalidValueException extends RuntimeException
{

    public <V> InvalidValueException( Var<V> var, V invalidValue )
    {
        super( "Value was rejected by this Var's validate function: var = " + var + ", value = " + invalidValue );
    }

}
