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
     * <p>
     * The force parameter indicates when the provided data should override user entries in the page's
     * data entry fields. In some cases, if the user has only entered partial information, that
     * partial information should remain if the user leaves and re-enters the page. In many use
     * cases this field can be ignored.
     * 
     */
    public void setData( D data, boolean force );

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
    
    /**
     * Callback method called by the Wizard when this page is visited.
     */
    public void onEnter( );
    
    /**
     * Callback method called by the Wizard when this page stops being visited.
     */
    public void onExit( );
}
