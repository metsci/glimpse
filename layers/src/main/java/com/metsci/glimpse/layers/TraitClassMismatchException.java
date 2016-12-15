package com.metsci.glimpse.layers;

/**
 * Thrown when a {@link Trait} is required, and the specified {@link View} has a trait for the
 * specified key, but the existing trait is not an instance of the specified class.
 * <p>
 * This usually indicates that a trait has been mistakenly set for the wrong trait key.
 */
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
