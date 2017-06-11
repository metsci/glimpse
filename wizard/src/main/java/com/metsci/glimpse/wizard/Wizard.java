package com.metsci.glimpse.wizard;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.metsci.glimpse.wizard.listener.ErrorsUpdatedListener;
import com.metsci.glimpse.wizard.listener.PageEnteredListener;
import com.metsci.glimpse.wizard.listener.PageExitedListener;
import com.metsci.glimpse.wizard.listener.WizardCancelledListener;
import com.metsci.glimpse.wizard.listener.WizardFinishedListener;
import com.metsci.glimpse.wizard.tree.WizardPageModelTree;
import com.metsci.glimpse.wizard.tree.WizardUITree;

public class Wizard<D>
{
    // the data object containing the state of the Wizard
    // this is the value being modified by the Wizard
    protected D data;
    
    // set containing the page ids of pages which have been viewed at least once by the user
    protected final Set<Object> isVisited = new HashSet<>( );
    // a history of the pages which have been visited by the user
    protected LinkedList<Object> pageHistory;
    
    // errors returned by the wizard pages
    protected Multimap<Object, WizardError> errors;
    
    protected WizardUI<D> ui;
    protected WizardPageModel<D> model;
    
    protected List<ErrorsUpdatedListener> errorListeners;
    protected List<PageEnteredListener<D>> pageEnterListeners;
    protected List<PageExitedListener<D>> pageExitListeners;
    protected List<WizardCancelledListener> cancelledListeners;
    protected List<WizardFinishedListener> finishedListeners;

    public Wizard( WizardPageModelTree<D> model, WizardUITree<D> ui )
    {
        this.errors = HashMultimap.create( );
        
        this.errorListeners = new CopyOnWriteArrayList<>( );
        this.pageEnterListeners = new CopyOnWriteArrayList<>( );
        this.pageExitListeners = new CopyOnWriteArrayList<>( );
        this.cancelledListeners = new CopyOnWriteArrayList<>( );
        this.finishedListeners = new CopyOnWriteArrayList<>( );

        this.pageHistory = new LinkedList<>( );
        
        this.model = model;
        this.ui = ui;
        
        this.ui.setWizard( this );
        this.model.setWizard( this );
    }
    
    public Wizard( )
    {
        this( new WizardPageModelTree<>( ), new WizardUITree<>( ) );
    }
    
    public void finish( )
    {
        this.fireFinished( );
    }
    
    public void cancel( )
    {
        this.fireCancelled( );
    }
    
    public D getData( )
    {
        return this.data;
    }
    
    public WizardPage<D> visitPreviousPage( )
    {
        this.doLeavePage( this.getCurrentPage( ) );
        this.pageHistory.removeLast( );
        WizardPage<D> nextPage = this.model.getPage( this.pageHistory.getLast( ) );
        this.doEnterPage( nextPage );
        
        return nextPage;
    }

    public WizardPage<D> visitNextPage( )
    {
        this.doLeavePage( this.getCurrentPage( ) );
        WizardPage<D> nextPage = this.model.getNextPage( this.getPageHistory( ), this.data );
        this.pageHistory.add( nextPage.getId( ) );
        this.doEnterPage( nextPage );

        return nextPage;
    }

    public WizardPage<D> visitPage( WizardPage<D> page )
    {
        this.doLeavePage( this.getCurrentPage( ) );
        this.pageHistory.add( page.getId( ) );
        this.doEnterPage( page );
        
        return page;
    }

    protected void doLeavePage( WizardPage<D> currentPage )
    {
        // save the settings for the current page
        if ( currentPage != null )
        {
            this.updateData( currentPage );
            this.firePageExited( currentPage );
        }
    }

    protected void doEnterPage( WizardPage<D> page )
    {
        this.isVisited.add( page.getId( ) );

        // update the page fields with the settings
        page.setData( this.data );

        // update errors for the page
        this.setErrors( page );
        
        // have the UI show the page
        this.ui.show( page );

        // update the display to reflect the new page
        this.firePageEntered( page );
    }
    
    /**
     * @return the currently displayed page
     */
    public WizardPage<D> getCurrentPage( )
    {
        if ( this.pageHistory.isEmpty( ) )
        {
            return null;
        }
        else
        {
            return this.model.getPage( this.pageHistory.getLast( ) );
        }
    }
    
    /**
     * Add a listener which is notified when the wizard errors change.
     */
    public void addErrorsUpdatedListener( ErrorsUpdatedListener listener )
    {
        this.errorListeners.add( listener );
    }
    
    public void removeErrorsUpdatedListener( ErrorsUpdatedListener listener )
    {
        this.errorListeners.remove( listener );
    }
    
    public void addPageEnteredListener( PageEnteredListener<D> listener )
    {
        this.pageEnterListeners.add( listener );
    }
    
    public void removePageEnteredListener( PageEnteredListener<D> listener )
    {
        this.pageEnterListeners.remove( listener );
    }
    
    public void addPageExitedListener( PageExitedListener<D> listener )
    {
        this.pageExitListeners.add( listener );
    }
    
    public void removePageExitedListener( PageExitedListener<D> listener )
    {
        this.pageExitListeners.remove( listener );
    }
    
    public void addCancelledListener( WizardCancelledListener listener )
    {
        this.cancelledListeners.add( listener );
    }
    
    public void removeCancelledListener( WizardCancelledListener listener )
    {
        this.cancelledListeners.remove( listener );
    }
    
    public void addFinishedListener( WizardFinishedListener listener )
    {
        this.finishedListeners.add( listener );
    }
    
    public void removeFinishedListener( WizardCancelledListener listener )
    {
        this.finishedListeners.remove( listener );
    }
    
    public WizardPageModel<D> getPageModel( )
    {
        return this.model;
    }
    
    public WizardUI<D> getUI( )
    {
        return this.ui;
    }
    
    public Collection<WizardError> getErrors( )
    {
        // only return errors pertaining to visited pages
        return this.errors.values( ).stream( )
                .filter( error -> error.getPageId( ) == null || this.isVisited.contains( error.getPageId( ) ) )
                .collect( Collectors.toList( ) );
    }

    public Collection<WizardError> getErrors( WizardPage<D> page )
    {
        return this.getErrors( page.getId( ) );
    }
    
    public Collection<WizardError> getErrors( Object id )
    {
        if ( this.isVisited.contains( id ) )
        {
            return Collections.unmodifiableCollection( this.errors.get( id ) );
        }
        else
        {
            return Collections.emptyList( );
        }
    }

    public void setErrors( Collection<WizardError> errors )
    {
        this.errors.clear( );
        
        for ( WizardError error : errors )
        {
            this.addError0( error );
        }
        
        this.fireErrorsUpdated( );
    }

    public void clearErrors( )
    {
        this.errors.clear( );
        
        this.fireErrorsUpdated( );
    }

    public void addErrors( Collection<WizardError> errors )
    {
        for ( WizardError error : errors )
        {
            this.addError0( error );
        }

        this.fireErrorsUpdated( );
    }

    public void addError( WizardError error )
    {
        this.addError0( error );

        this.fireErrorsUpdated( );
    }
    
    public LinkedList<WizardPage<D>> getPageHistory( )
    {
        return new LinkedList<>( this.pageHistory
                .stream( )
                .map( this.model::getPage )
                .collect( Collectors.toList( ) ) );
    }
    
    protected void addError0( WizardError error )
    {
        this.errors.put( error.getPageId( ), error );
    }
    
    protected void updateData( WizardPage<D> page )
    {
        if ( this.isVisited( page.getId( ) ) )
        {
            this.data = page.updateData( this.data );
        }
    }
    
    protected void setErrors( WizardPage<D> page )
    {
        if ( this.isVisited( page.getId( ) ) )
        {
            Collection<WizardError> pageErrors = page.getErrors( );
            this.errors.replaceValues( page.getId( ), pageErrors );
        }
    }
    
    protected boolean isVisited( Object pageId )
    {
        return this.isVisited.contains( pageId );
    }
    
    protected void fireErrorsUpdated( )
    {
        for ( ErrorsUpdatedListener listener : this.errorListeners )
        {
            listener.onErrorsUpdated( );
        }
    }
    
    protected void firePageEntered( WizardPage<D> page )
    {
        for ( PageEnteredListener<D> listener : this.pageEnterListeners )
        {
            listener.onPageEntered( page );
        }
    }
    
    protected void firePageExited( WizardPage<D> page )
    {
        for ( PageExitedListener<D> listener : this.pageExitListeners )
        {
            listener.onPageExited( page );
        }
    }
    
    protected void fireFinished( )
    {
        for ( WizardFinishedListener listener : this.finishedListeners )
        {
            listener.finished( );
        }
    }
    
    protected void fireCancelled( )
    {
        for ( WizardCancelledListener listener : this.cancelledListeners )
        {
            listener.cancelled( );
        }
    }
}
