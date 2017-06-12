package com.metsci.glimpse.wizard;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import com.metsci.glimpse.wizard.listener.PageModelListener;

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
     * This may be used to pre-populate the WizardPageModel.
     * 
     * @param page
     */
    public void addPage( WizardPage<D> page );
    
    public void removePage( WizardPage<D> page );
    public void removePageById( Object id );
    
    /**
     * Returns the page with the provided unique identifier.
     * 
     * @param id the unique identifier for a page
     * @return the page associated with the provided id
     */
    public WizardPage<D> getPageById( Object id );

    /**
     * Returns the next page which should be visited.
     * <p>
     * This decision can depend on the history of pages visited thus far, as well as
     * the current value of the Wizard data.
     * 
     * @param visitHistory the sequence of pages visited thus far, ending in the current page
     * @param data the wizard data
     * @return the next page to visit
     */
    public WizardPage<D> getNextPage( LinkedList<WizardPage<D>> visitHistory, D data );

    /**
     * Returns all the pages in the model.
     */
    public Collection<WizardPage<D>> getPages( );

    public void addListener( PageModelListener<D> listener );
    
    public void removeListener( PageModelListener<D> listener );
    
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
