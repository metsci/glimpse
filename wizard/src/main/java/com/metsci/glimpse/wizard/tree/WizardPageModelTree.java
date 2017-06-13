package com.metsci.glimpse.wizard.tree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.SwingUtilities;

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

    protected List<PageModelListener<D>> listeners;

    protected Wizard<D> wizard;
    
    public WizardPageModelTree( )
    {
        this.pageTreeRoot = new WizardTreeNode<D>( null );
        this.pageMap = new HashMap<>( );
        this.listeners = new CopyOnWriteArrayList<>( );

    }
    
    @Override
    public void addListener( PageModelListener<D> listener )
    {
        this.listeners.add( listener );
    }
    
    @Override
    public void removeListener( PageModelListener<D> listener )
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
        assert( SwingUtilities.isEventDispatchThread( ) );

        WizardTreeNode<D> childNode = new WizardTreeNode<>( page );
        WizardPage<D> parent = this.getPageById( page.getParentId( ) );

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

    @Override
    public void addPage( WizardPage<D> page )
    {
        assert( SwingUtilities.isEventDispatchThread( ) );

        WizardTreeNode<D> childNode = new WizardTreeNode<>( page );
        WizardPage<D> parent = this.getPageById( page.getParentId( ) );

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
        
        this.firePagesAdded( Collections.singleton( page ) );
    }

    public void removePage( WizardPage<D> page )
    {
        assert( SwingUtilities.isEventDispatchThread( ) );

        this.removePageById( page.getId( ) );
    }

    public void removePageById( Object pageId )
    {
        assert( SwingUtilities.isEventDispatchThread( ) );

        WizardTreeNode<D> node = this.getNode( pageId );

        if ( node != null )
        {
            node.removeFromParent( );
    
            this.pageMap.remove( pageId );
            
            this.firePagesRemoved( Collections.singleton( node.getPage( ) ) );
        }
    }

    public WizardTreeNode<D> getNode( WizardPage<D> page )
    {
        assert( SwingUtilities.isEventDispatchThread( ) );

        return this.getNode( page.getId( ) );
    }

    public WizardTreeNode<D> getNode( Object id )
    {
        assert( SwingUtilities.isEventDispatchThread( ) );

        return this.pageMap.get( id );
    }

    @Override
    public WizardPage<D> getPageById( Object id )
    {
        assert( SwingUtilities.isEventDispatchThread( ) );

        WizardTreeNode<D> node = this.pageMap.get( id );
        return node == null ? null : node.getPage( );
    }

    @Override
    public WizardPage<D> getNextPage( LinkedList<WizardPage<D>> visitHistory, D data )
    {
        assert( SwingUtilities.isEventDispatchThread( ) );

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
        assert( SwingUtilities.isEventDispatchThread( ) );

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
    
    protected WizardPage<D> getCurrentPage( LinkedList<WizardPage<D>> path )
    {
        return path.isEmpty( ) ? null : path.getLast( );
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
    
    protected void firePagesAdded( Collection<WizardPage<D>> pageIds )
    {
        for ( PageModelListener<D> listener : this.listeners )
        {
            listener.onPagesAdded( pageIds );
        }
    }
    
    protected void firePagesRemoved( Collection<WizardPage<D>> pageIds )
    {
        for ( PageModelListener<D> listener : this.listeners )
        {
            listener.onPagesRemoved( pageIds );
        }
    }
}
