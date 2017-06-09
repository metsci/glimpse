package com.metsci.glimpse.wizard;

import static java.util.Collections.unmodifiableList;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import com.google.common.collect.Multimap;
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
    
    protected List<Runnable> errorListeners;
    protected List<WizardValidator<D>> validators;
    
    public Wizard( WizardPageModelTree<D> model, WizardUITree<D> ui )
    {
        this.errorListeners = new CopyOnWriteArrayList<>( );
        this.validators = new CopyOnWriteArrayList<>( );
        
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
        WizardPage<D> nextPage = this.model.getNextPage( unmodifiableList( this.pageHistory ), this.data );
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
        /*
        // save the settings for the current page
        if ( currentPage != null )
        {
            this.savePageSettings( currentPage );

            currentPage.onLeave( );
            this.firePageLeave( currentPage );
        }
        */
    }

    protected void doEnterPage( WizardPage<D> page )
    {
        /*
        this.isVisited.add( page.getId( ) );

        // update the page fields with the settings
        page.setData( this.data );

        // run save page settings to generate errors
        // (this call should not actually modify the settings as none of the page
        // controls have changed since the previous call)
        this.savePageSettings( page );

        // update the display to reflect the new page
        page.onEnter( );
        this.firePageEnter( page );

        // if there is no history, disable the ability to move backward
        this.prevAction.setEnabled( this.pageHistory.size( ) > 1 );
        // if there is a valid next page, enable the next action
        this.nextAction.setEnabled( this.pageFactory.getNextPage( this.pageHistory, this.settings ) != null );

        this.getContainer( ).revalidate( );
        this.getContainer( ).repaint( );
        */
    }
    
    public void addValidator( WizardValidator<D> validator )
    {
        this.validators.add( validator );
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
    public void addErrorListener( Runnable listener )
    {
        this.errorListeners.add( listener );
    }
    
    public void removeErrorListener( Runnable listener )
    {
        this.errorListeners.remove( listener );
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
        return errors.values( ).stream( )
                .filter( error -> error.getPageId( ) == null || this.isVisited.contains( error.getPageId( ) ) )
                .collect( Collectors.toList( ) );
    }

    public Collection<WizardError> getErrors( WizardPage<D> page )
    {
        return getErrors( page.getId( ) );
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
    
    protected void addError0( WizardError error )
    {
        this.errors.put( error.getPageId( ), error );
    }
    
    protected void fireErrorsUpdated( )
    {
        for ( Runnable listener : this.errorListeners )
        {
            listener.run( );
        }
    }
}
