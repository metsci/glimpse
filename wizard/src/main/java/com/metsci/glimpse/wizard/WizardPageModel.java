package com.metsci.glimpse.wizard;

import java.util.Collection;
import java.util.List;

/**
 * Interface representing the data model for a {@link Wizard}.
 * This model tracks all the {@link WizardPage}s for the Wizard and controls
 * the order at which the user moves between pages (via {@link #getNextPage(List, Object)}.
 * 
 * @author ulman
 */
public interface WizardPageModel<D>
{
    /**
     * Set the wizard associated with the UI.
     */
    public void setWizard( Wizard<D> wizard );

    /**
     * Add a new page to the model.
     * <p>
     * This may be used to pre-populate the WizardPageModel with all the 
     * 
     * @param page
     */
    public void addPage( WizardPage<D> page );
    
    /**
     * Returns the page with the provided unique identifier.
     * 
     * @param id the unique identifier for a page
     * @return the page associated with the provided id
     */
    public WizardPage<D> getPage( Object id );

    /**
     * Returns the next page which should be visited.
     * <p>
     * This decision can depend on the history of pages visited thus far, as well as
     * the current value of the Wizard data.
     * 
     * @param path the history of page ids dor pages visited thus far
     * @param data the wizard data
     * @return the next page to visit
     */
    public WizardPage<D> getNextPage( List<Object> visitHistory, D data );

    /**
     * Returns all the pages in the model.
     */
    public Collection<WizardPage<D>> getPages( );

    /**
     * Dispose this model and call {@link WizardPage#dispose()} on all pages in the model.
     */
    public default void dispose( )
    {
        for ( WizardPage<D> page : getPages( ) )
        {
            page.dispose( );
        }
    }
}
