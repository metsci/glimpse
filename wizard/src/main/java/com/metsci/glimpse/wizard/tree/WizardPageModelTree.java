package com.metsci.glimpse.wizard.tree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import com.metsci.glimpse.wizard.Wizard;
import com.metsci.glimpse.wizard.WizardPage;
import com.metsci.glimpse.wizard.WizardPageModel;
import com.metsci.glimpse.wizard.listener.PageModelListener;

public class WizardPageModelTree<D> implements WizardPageModel<D>
{
    // the parent/child organization of the pages
    protected WizardTreeNode<D> pageTreeRoot;

    // a map for looking up pages by their unique id
    protected Map<Object, WizardTreeNode<D>> pageMap;

    protected List<PageModelListener> listeners;

    
    protected Wizard<D> wizard;
    
    

    public WizardPageModelTree( )
    {
        this.pageTreeRoot = new WizardTreeNode<D>( null );
        this.pageMap = new HashMap<>( );
        this.listeners = new CopyOnWriteArrayList<>( );

    }
    
    @Override
    public void addListener( PageModelListener listener )
    {
        this.listeners.add( listener );

    }
    
    @Override
    public void removeListener( PageModelListener listener )
    {
        this.listeners.remove( listener );
    }
    

    @Override
    public void setWizard( Wizard<D> wizard )
    {
        this.wizard = wizard;
    }

    /**
     * Add a page to the model.
     * <p>
     * The insert index represents the index among the page's peers (children of its parent)
     * that the new page will be inserted at.
     * 
     * @param page the new page to insert
     * @param insertIndex the index among the page's peers to insert the new page at
     */
    public void addPage( WizardPage<D> page, int insertIndex )
    {
        WizardTreeNode<D> childNode = new WizardTreeNode<>( page );
        WizardPage<D> parent = this.getPage( page.getParentId( ) );

        if ( parent != null )
        {
            WizardTreeNode<D> parentNode = this.pageMap.get( parent.getId( ) );
            parentNode.insert( childNode, insertIndex );
        }
        else
        {
            this.pageTreeRoot.insert( childNode, insertIndex );
        }

        this.pageMap.put( page.getId( ), childNode );
    }

    public void addPage( WizardPage<D> page )
    {
        WizardTreeNode<D> childNode = new WizardTreeNode<>( page );
        WizardPage<D> parent = this.getPage( page.getParentId( ) );

        if ( parent != null )
        {
            WizardTreeNode<D> parentNode = this.pageMap.get( parent.getId( ) );
            parentNode.add( childNode );
        }
        else
        {
            this.pageTreeRoot.add( childNode );
        }

        this.pageMap.put( page.getId( ), childNode );
        
        this.firePagesAdded( Collections.singleton( page.getId( ) ) );
    }

    public void removePage( WizardPage<D> page )
    {
        removePage( page.getId( ) );
    }

    public void removePage( Object pageId )
    {
        WizardTreeNode<D> node = this.getNode( pageId );

        if ( node != null )
        {
            node.removeFromParent( );
    
            this.pageMap.remove( pageId );
            
            this.firePagesRemoved( Collections.singleton( pageId ) );
        }
    }

    public WizardTreeNode<D> getNode( WizardPage<D> page )
    {
        return this.getNode( page.getId( ) );
    }

    public WizardTreeNode<D> getNode( Object id )
    {
        return this.pageMap.get( id );
    }

    @Override
    public WizardPage<D> getPage( Object id )
    {
        WizardTreeNode<D> node = this.pageMap.get( id );
        return node == null ? null : node.getPage( );
    }

    @Override
    public WizardPage<D> getNextPage( List<Object> visitHistory, D data )
    {
        WizardPage<D> currentPage = this.getCurrentPage( visitHistory );

        Enumeration<?> enumeration = this.pageTreeRoot.preorderEnumeration( );

        while ( enumeration.hasMoreElements( ) )
        {
            WizardPage<D> page = this.getNextPage( enumeration );

            if ( currentPage == null || currentPage.equals( page ) )
            {
                return this.getNextPage( enumeration );
            }
        }

        return null;
    }

    @SuppressWarnings( "unchecked" )
    @Override
    public List<WizardPage<D>> getPages( )
    {
        List<WizardPage<D>> list = new ArrayList<>( );

        // fill the list model with nodes from the tree in a preorder traversal
        Enumeration<?> items = this.pageTreeRoot.preorderEnumeration( );
        items.nextElement( ); // skip the root
        while ( items.hasMoreElements( ) )
        {
            WizardTreeNode<D> node = ( WizardTreeNode<D> ) items.nextElement( );
            list.add( node.getPage( ) );
        }

        return list;
    }

    public WizardTreeNode<D> getPageTree( )
    {
        return this.pageTreeRoot;
    }

    protected WizardPage<D> getCurrentPage( List<Object> path )
    {
        WizardTreeNode<D> currentNode = this.getCurrentNode( path );
        return currentNode.getPage( );
    }

    protected WizardTreeNode<D> getCurrentNode( List<Object> path )
    {
        if ( !path.isEmpty( ) )
        {
            Object currentPageId = path.get( path.size( ) - 1 );
            return this.pageMap.get( currentPageId );
        }
        else
        {
            return this.pageTreeRoot;
        }
    }

    @SuppressWarnings( "unchecked" )
    protected WizardPage<D> getNextPage( Enumeration<?> enumeration )
    {
        if ( enumeration.hasMoreElements( ) )
        {
            WizardTreeNode<D> node = ( WizardTreeNode<D> ) enumeration.nextElement( );
            return node.getPage( );
        }
        else
        {
            return null;
        }
    }
    
    protected void firePagesAdded( Collection<Object> pageIds )
    {
        for ( PageModelListener listener : this.listeners )
        {
            listener.onPagesAdded( pageIds );
        }
    }
    
    protected void firePagesRemoved( Collection<Object> pageIds )
    {
        for ( PageModelListener listener : this.listeners )
        {
            listener.onPagesRemoved( pageIds );
        }
    }
}
