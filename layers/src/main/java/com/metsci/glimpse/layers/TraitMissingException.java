package com.metsci.glimpse.layers;

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
