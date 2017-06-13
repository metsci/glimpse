package com.metsci.glimpse.wizard.page;

import java.util.Collection;
import java.util.Collections;

import com.metsci.glimpse.wizard.Wizard;
import com.metsci.glimpse.wizard.WizardError;
import com.metsci.glimpse.wizard.error.ErrorTablePanel;

public class FinalReviewPage<D> extends DescriptionWizardPage<D>
{
    protected ErrorTablePanel<D> table;
    protected Wizard<D> wizard;

    public FinalReviewPage( Wizard<D> wizard )
    {
        this( wizard, "description/FinalReviewPage-description.html" );
    }

    public FinalReviewPage( Wizard<D> wizard, String descriptionFile )
    {
        this( wizard, "FinalReviewPage", null, "Final Review", descriptionFile );
    }
    
    public FinalReviewPage( Wizard<D> wizard, Object id, Object parentId, String title, String descriptionFile )
    {
        super( id, parentId, title, descriptionFile );

        this.wizard = wizard;
        
        init( );
    }

    public void init( )
    {
        this.container.removeAll( );
        this.table = new ErrorTablePanel<D>( wizard );
        this.container.add( this.table, "push, grow" );

        this.wizard.addErrorsUpdatedListener( ( ) ->
        {
            table.setErrors( wizard.getErrors( ) );
        } );
    }

    @Override
    public void onEnter( )
    {
        // once this page is visited, mark all pages as visited
        this.wizard.visitAll( );
    }

    @Override
    public void setData( D data, boolean force )
    {
        // do nothing
    }

    @Override
    public D updateData( D data )
    {
        return data;
    }

    @Override
    public Collection<WizardError> getErrors( )
    {
        return Collections.emptySet( );
    }
}
