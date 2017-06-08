package com.metsci.glimpse.wizard;

import java.awt.Container;
import java.util.Collection;

/**
 * A single page of a Wizard.
 * 
 * @author ulman
 *
 * @param <D> the type of data displayed / manipulated by this wizard
 */
public interface WizardPage<D>
{
    /**
     * Unique identifier for the Page.
     */
    public Object getId( );
    
    /**
     * Unique identifier for this Page's parent Page.
     * <p>
     * This method may return null if this is a top level page.
     */
    public Object getParentId( );

    /**
     * A short (one or two word) title for the Page.
     */
    public String getTitle( );

    /**
     * The container holding the Swing components which make up the Page.
     */
    public Container getContainter( );

    /**
     * Dispose of any resources on the page.
     * <p>
     * For example, if the WizardPage contains a GlimpseCanvas it should be disposed here.
     */
    public void dispose( );

    /**
     * Update this page to display the provided data.
     * <p>
     * The provided data object should not be modified here.
     */
    public void setData( D data );

    /**
     * Update the provided data to reflect the information entered by the user on this page.
     * <p>
     * If data is mutable, the data provided as a method argument may be modified and returned.
     */
    public D updateData( D data );

    /**
     * If any information the user has entered is invalid, {@link WizardError}s can be returned
     * to indicate problems.
     * 
     * @return a collection of problems with the data the user has entered
     */
    public Collection<WizardError> getErrors( );
}
