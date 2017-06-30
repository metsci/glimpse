package com.metsci.glimpse.layers;

/**
 * Thrown when a {@link Trait} is required, but is not found in the specified {@link View}.
 */
public class TraitMissingException extends RuntimeException
{

    public final String traitKey;
    public final Class<? extends Trait> traitClass;

    public TraitMissingException( String traitKey, Class<? extends Trait> traitClass )
    {
        super( "Trait is missing: key = " + traitKey + ", required-class = " + traitClass.getName( ) );

        this.traitKey = traitKey;
        this.traitClass = traitClass;
    }

}
