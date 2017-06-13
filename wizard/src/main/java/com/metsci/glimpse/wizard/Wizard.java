package com.metsci.glimpse.wizard;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.SwingUtilities;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.metsci.glimpse.wizard.listener.DataUpdatedListener;
import com.metsci.glimpse.wizard.listener.ErrorsUpdatedListener;
import com.metsci.glimpse.wizard.listener.PageEnteredListener;
import com.metsci.glimpse.wizard.listener.PageExitedListener;
import com.metsci.glimpse.wizard.listener.PageModelListener;
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
    protected Multimap<Object, WizardError> pageErrors;

    // errors added programmatically by the user
    protected Multimap<Object, WizardError> userErrors;

    protected WizardUI<D> ui;
    protected WizardPageModel<D> model;

    protected List<ErrorsUpdatedListener> errorListeners;
    protected List<PageEnteredListener<D>> pageEnterListeners;
    protected List<PageExitedListener<D>> pageExitListeners;
    protected List<DataUpdatedListener<D>> dataUpdatedListeners;
    protected List<WizardCancelledListener> cancelledListeners;
    protected List<WizardFinishedListener> finishedListeners;

    public Wizard( D data, WizardPageModelTree<D> model, WizardUITree<D> ui )
    {
        this.data = data;

        this.pageErrors = HashMultimap.create( );
        this.userErrors = HashMultimap.create( );

        this.errorListeners = new CopyOnWriteArrayList<>( );
        this.pageEnterListeners = new CopyOnWriteArrayList<>( );
        this.pageExitListeners = new CopyOnWriteArrayList<>( );
        this.dataUpdatedListeners = new CopyOnWriteArrayList<>( );
        this.cancelledListeners = new CopyOnWriteArrayList<>( );
        this.finishedListeners = new CopyOnWriteArrayList<>( );

        this.pageHistory = new LinkedList<>( );

        this.model = model;
        this.ui = ui;

        this.ui.setWizard( this );
        this.model.setWizard( this );

        this.model.addListener( new PageModelListener<D>( )
        {
            @Override
            public void onPagesAdded( Collection<WizardPage<D>> addedPages )
            {
            }

            @Override
            public void onPagesRemoved( Collection<WizardPage<D>> removedPages )
            {
                // clear errors for deleted pages
                for ( WizardPage<D> page : removedPages )
                {
                    pageErrors.get( page.getId( ) ).clear( );
                    userErrors.get( page.getId( ) ).clear( );
                }

                fireErrorsUpdated( );
            }
        } );
    }

    public Wizard( )
    {
        this( null, new WizardPageModelTree<>( ), new WizardUITree<>( ) );
    }

    /**
     * Reinitialized the Wizard to its default state. This is mainly used to
     * allow reuse of the Wizard dialog since it takes a few seconds to construct initially.
     * 
     * @param settings the new initial settings to edit
     */
    public void reset( D data )
    {
        assert( SwingUtilities.isEventDispatchThread( ) );
        
        this.isVisited.clear( );
        this.pageHistory.clear( );
        this.clearErrors( );
        this.setData( data );
    }
    
    public void finish( )
    {
        assert( SwingUtilities.isEventDispatchThread( ) );

        this.fireFinished( );
    }

    public void cancel( )
    {
        assert( SwingUtilities.isEventDispatchThread( ) );

        this.fireCancelled( );
    }

    public D getData( )
    {
        return this.data;
    }

    public void setData( D data )
    {
        assert( SwingUtilities.isEventDispatchThread( ) );

        this.data = data;
        
        // apply the new settings to each page
        this.getPageModel( ).getPages( ).forEach( ( page ) ->
        {
            page.setData( data, true );
        } );

        this.fireDataUpdated( data );
    }

    public void visitAll( )
    {
        assert( SwingUtilities.isEventDispatchThread( ) );

        this.getPageModel( ).getPages( ).stream( ).forEach(  p ->
        {
            this.isVisited.add( p.getId( ) );
            p.setData( this.data, false );
            Collection<WizardError> pageErrors = p.getErrors( );
            this.pageErrors.replaceValues( p.getId( ), pageErrors );
        } );
        
        this.fireErrorsUpdated( );
    }
    

    public WizardPage<D> visitPreviousPage( )
    {
        assert( SwingUtilities.isEventDispatchThread( ) );

        this.doLeavePage( this.getCurrentPage( ) );
        this.pageHistory.removeLast( );
        WizardPage<D> nextPage = this.model.getPageById( this.pageHistory.getLast( ) );
        this.doEnterPage( nextPage );

        return nextPage;
    }

    public WizardPage<D> visitNextPage( )
    {
        assert( SwingUtilities.isEventDispatchThread( ) );

        this.doLeavePage( this.getCurrentPage( ) );
        WizardPage<D> nextPage = this.model.getNextPage( this.getPageHistory( ), this.data );
        this.pageHistory.add( nextPage.getId( ) );
        this.doEnterPage( nextPage );

        return nextPage;
    }

    /**
     * Make the provide page the current Wizard page.
     */
    public void visitPage( WizardPage<D> page )
    {
        assert( SwingUtilities.isEventDispatchThread( ) );

        this.doLeavePage( this.getCurrentPage( ) );
        this.pageHistory.add( page.getId( ) );
        this.doEnterPage( page );
    }

    /**
     * Update the wizard data based on the state of the current page.
     */
    public void updateData( WizardPage<D> page )
    {
        assert( SwingUtilities.isEventDispatchThread( ) );

        this.data = page.updateData( this.data );
        
        this.fireDataUpdated( data );
    }

    /**
     * Recalculates errors for the provided page.
     */
    public void setErrors( WizardPage<D> page )
    {
        assert( SwingUtilities.isEventDispatchThread( ) );

        if ( this.isVisited( page.getId( ) ) )
        {
            Collection<WizardError> pageErrors = page.getErrors( );
            this.pageErrors.replaceValues( page.getId( ), pageErrors );

            this.fireErrorsUpdated( );
        }
    }
    
    /**
     * @return the currently displayed page
     */
    public WizardPage<D> getCurrentPage( )
    {
        assert( SwingUtilities.isEventDispatchThread( ) );

        if ( this.pageHistory.isEmpty( ) )
        {
            return null;
        }
        else
        {
            return this.model.getPageById( this.pageHistory.getLast( ) );
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

    public void addDataUpdatedListener( DataUpdatedListener<D> listener )
    {
        this.dataUpdatedListeners.add( listener );
    }

    public void removeDataUpdatedListener( DataUpdatedListener<D> listener )
    {
        this.dataUpdatedListeners.remove( listener );
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
        assert( SwingUtilities.isEventDispatchThread( ) );

        return this.model;
    }

    public WizardUI<D> getUI( )
    {
        assert( SwingUtilities.isEventDispatchThread( ) );

        return this.ui;
    }

    // only return errors pertaining to visited pages
    public Collection<WizardError> getErrors( )
    {
        assert( SwingUtilities.isEventDispatchThread( ) );

        return Stream.concat( this.pageErrors.values( ).stream( ), this.userErrors.values( ).stream( ) )
                .filter( error -> error.getPageId( ) == null || this.isVisited.contains( error.getPageId( ) ) )
                .collect( Collectors.toList( ) );
    }

    public Collection<WizardError> getErrors( WizardPage<?> page )
    {
        assert( SwingUtilities.isEventDispatchThread( ) );

        return this.getErrors( page.getId( ) );
    }

    public Collection<WizardError> getErrors( Object id )
    {
        assert( SwingUtilities.isEventDispatchThread( ) );

        if ( this.isVisited.contains( id ) )
        {
            List<WizardError> errors = new ArrayList<WizardError>( );
            errors.addAll( this.pageErrors.get( id ) );
            errors.addAll( this.userErrors.get( id ) );
            return Collections.unmodifiableList( errors );
        }
        else
        {
            return Collections.emptyList( );
        }
    }

    public void setErrors( Collection<WizardError> errors )
    {
        assert( SwingUtilities.isEventDispatchThread( ) );

        this.userErrors.clear( );

        for ( WizardError error : errors )
        {
            this.addError0( error );
        }

        this.fireErrorsUpdated( );
    }

    public void clearErrors( )
    {
        assert( SwingUtilities.isEventDispatchThread( ) );

        this.pageErrors.clear( );
        this.userErrors.clear( );

        this.fireErrorsUpdated( );
    }

    public void addErrors( Collection<WizardError> errors )
    {
        assert( SwingUtilities.isEventDispatchThread( ) );

        for ( WizardError error : errors )
        {
            this.addError0( error );
        }

        this.fireErrorsUpdated( );
    }

    public void addError( WizardError error )
    {
        assert( SwingUtilities.isEventDispatchThread( ) );

        this.addError0( error );

        this.fireErrorsUpdated( );
    }

    public LinkedList<WizardPage<D>> getPageHistory( )
    {
        assert( SwingUtilities.isEventDispatchThread( ) );

        return new LinkedList<>( this.pageHistory
                .stream( )
                .map( this.model::getPageById )
                .collect( Collectors.toList( ) ) );
    }

    public void dispose( )
    {
        assert( SwingUtilities.isEventDispatchThread( ) );

        this.model.dispose( );
        this.ui.dispose( );
    }

    /**
     * Update Wizard data to reflect edits made to the provided page and recalculate errors associated with the page.
     * 
     * @param page the page to update
     */
    protected void updatePage( WizardPage<D> page )
    {
        // update the page fields with the settings
        page.setData( this.data, false );

        // update errors for the page
        this.setErrors( page );
    }

    protected void doLeavePage( WizardPage<D> currentPage )
    {
        // save the settings for the current page
        if ( currentPage != null )
        {
            this.data = currentPage.updateData( this.data );
            this.fireDataUpdated( data );
            this.firePageExited( currentPage );
            currentPage.onExit( );
        }
    }

    protected void doEnterPage( WizardPage<D> page )
    {
        this.isVisited.add( page.getId( ) );

        // update the page fields with the settings
        this.updatePage( page );
        
        // have the UI show the page
        this.ui.show( page );

        // notify listeners
        this.firePageEntered( page );
        page.onEnter( );
    }

    protected void addError0( WizardError error )
    {
        this.userErrors.put( error.getPageId( ), error );
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

    protected void fireDataUpdated( D data )
    {
        for ( DataUpdatedListener<D> listener : this.dataUpdatedListeners )
        {
            listener.dataUpdated( data );
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
