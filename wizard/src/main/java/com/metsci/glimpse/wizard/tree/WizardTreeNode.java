package com.metsci.glimpse.wizard.tree;

import javax.swing.tree.DefaultMutableTreeNode;

import com.metsci.glimpse.wizard.WizardPage;

@SuppressWarnings( "serial" )
public class WizardTreeNode<D> extends DefaultMutableTreeNode
{
    public WizardTreeNode( WizardPage<D> page )
    {
        super( page );
    }

    @SuppressWarnings( "unchecked" )
    public WizardPage<D> getPage( )
    {
        return ( WizardPage<D> ) getUserObject( );
    }

    @Override
    public int hashCode( )
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( ( userObject == null ) ? 0 : userObject.hashCode( ) );
        return result;
    }

    @Override
    public boolean equals( Object obj )
    {
        if ( this == obj ) return true;
        if ( obj == null ) return false;
        if ( getClass( ) != obj.getClass( ) ) return false;
        WizardTreeNode<?> other = ( WizardTreeNode<?> ) obj;
        if ( userObject == null )
        {
            if ( other.userObject != null ) return false;
        }
        else if ( !userObject.equals( other.userObject ) ) return false;
        return true;
    }
}