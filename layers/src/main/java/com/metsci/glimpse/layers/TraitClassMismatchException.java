package com.metsci.glimpse.layers;

public class TraitClassMismatchException extends RuntimeException
{

    public final String traitKey;
    public final Class<? extends Trait> traitClass;
    public final Trait invalidTrait;

    public TraitClassMismatchException( String traitKey, Class<? extends Trait> traitClass, Trait invalidTrait )
    {
        super( "Trait class mismatch: key = " + traitKey + ", required-class = " + traitClass.getName( ) + ", actual-class = " + invalidTrait.getClass( ).getName( ) );

        this.traitKey = traitKey;
        this.traitClass = traitClass;
        this.invalidTrait = invalidTrait;
    }

}
