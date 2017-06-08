package com.metsci.glimpse.wizard.page;

import java.awt.Container;
import java.util.Objects;
import java.util.UUID;

import javax.swing.JPanel;

import com.metsci.glimpse.wizard.WizardPage;

public abstract class SimpleWizardPage<D> implements WizardPage<D>
{
    protected String title;
    protected JPanel container;
    protected Object id;
    protected Object parentId;

    public SimpleWizardPage( String title )
    {
        this( UUID.randomUUID( ), null, title );
    }

    public SimpleWizardPage( Object parentId, String title )
    {
        this( UUID.randomUUID( ), parentId, title );
    }

    public SimpleWizardPage( Object id, Object parentId, String title )
    {
        this.title = title;

        this.container = new JPanel( );
        this.id = id;
        this.parentId = parentId;
    }

    public Object getParentId( )
    {
        return this.parentId;
    }

    @Override
    public Object getId( )
    {
        return id;
    }

    @Override
    public String getTitle( )
    {
        return title;
    }

    @Override
    public Container getContainter( )
    {
        return container;
    }

    @Override
    public String toString( )
    {
        return title;
    }

    @Override
    public void dispose( )
    {
        // by default do nothing
    }

    @Override
    public int hashCode( )
    {
        return Objects.hashCode( id );
    }

    @Override
    public boolean equals( Object obj )
    {
        if ( this == obj ) return true;
        if ( obj == null ) return false;
        if ( getClass( ) != obj.getClass( ) ) return false;
        SimpleWizardPage<?> other = ( SimpleWizardPage<?> ) obj;
        if ( !Objects.equals( id, other.id ) ) return false;
        return true;
    }
}