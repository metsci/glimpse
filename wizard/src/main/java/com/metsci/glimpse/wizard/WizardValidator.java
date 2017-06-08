package com.metsci.glimpse.wizard;

import java.util.Collection;

/**
 * Validators apply a set of checks to a provided data object, and report problems in the form of WizardErrors.
 * <p>
 * The provided data object should not be modified as part of the validation.
 * 
 * @author ulman
 *
 * @param <D>
 */
public interface WizardValidator<D>
{
    public Collection<WizardError> validate( D data );
}