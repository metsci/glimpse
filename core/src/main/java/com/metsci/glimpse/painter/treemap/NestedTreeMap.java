/*
 * Copyright (c) 2016, Metron, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of Metron, Inc. nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL METRON, INC. BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.metsci.glimpse.painter.treemap;

import java.util.ArrayList;
import java.util.List;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

/**
 * The TreeMap datastructure for the {@link AbstractTreeMapPainter}. This
 * contains the structure, size information and text for each node.
 *
 * @author borkholder
 */
public class NestedTreeMap
{
    private Node root;
    private Int2ObjectMap<Node> map;

    private int versionId = 0;

    public int getRoot( )
    {
        return root.id;
    }

    /**
     * Clears the tree if there is any data and creates a new root.
     */
    public void setRoot( int id )
    {
        root = new Node( id );
        map = new Int2ObjectOpenHashMap<Node>( );
        map.put( id, root );
        versionId++;
    }

    /**
     * Gets the level in the tree, root is 0 and the level increases from there.
     */
    public int getLevel( int id )
    {
        int level = 0;
        Node node = getNode( id );
        while ( node != root )
        {
            level++;
            node = node.parent;
        }

        return level;
    }

    /**
     * Makes {@code parentId} to the parent of {@code childId}.
     */
    public void addChild( int parentId, int childId, double size )
    {
        addChild( parentId, childId, size, null );
    }

    /**
     * Makes {@code parentId} to the parent of {@code childId}.
     */
    public void addChild( int parentId, int childId, double size, String title )
    {
        Node child = new Node( childId );
        child.size = size;
        child.title = title;
        map.put( childId, child );
        getNode( parentId ).addChild( child );
        versionId++;
    }

    public void setSize( int id, double size )
    {
        getNode( id ).size = size;
        versionId++;
    }

    public double getSize( int id )
    {
        return getNode( id ).size;
    }

    public void setTitle( int id, String title )
    {
        getNode( id ).title = title;
        versionId++;
    }

    public String getTitle( int id )
    {
        return getNode( id ).title;
    }

    public void setText( int id, String text )
    {
        getNode( id ).text = text;
        versionId++;
    }

    public String getText( int id )
    {
        return getNode( id ).text;
    }

    /**
     * Gets the id of the parent of the given node. If {@code childId} is the
     * root, {@code childId} is returned.
     */
    public int getParent( int childId )
    {
        if ( childId == root.id )
        {
            return childId;
        }
        else
        {
            return getNode( childId ).parent.id;
        }
    }

    /**
     * Removes the child and all descendants.
     */
    public void removeChild( int childId )
    {
        Node child = map.get( childId );
        removeAllDescendants( child.parent, child );
        versionId++;
    }

    protected void removeAllDescendants( Node parent, Node child )
    {
        if ( parent.removeChild( child ) )
        {
            map.remove( child.id );
            if ( child.children != null )
            {
                for ( Node grandchild : child.children )
                {
                    removeAllDescendants( child, grandchild );
                }
            }
        }
    }

    /**
     * Returns true if there are no children of this node.
     */
    public boolean isLeaf( int id )
    {
        return getNode( id ).children == null;
    }

    public int[] getChildren( int id )
    {
        if ( isLeaf( id ) )
        {
            return new int[0];
        }

        Node node = getNode( id );

        List<Node> children = node.children;
        int[] array = new int[children.size( )];
        for ( int i = 0; i < children.size( ); i++ )
        {
            array[i] = children.get( i ).id;
        }

        return array;
    }

    public double[] getSizesOfChildren( int id )
    {
        if ( isLeaf( id ) )
        {
            return new double[0];
        }

        Node node = getNode( id );

        List<Node> children = node.children;
        double[] sizes = new double[children.size( )];
        for ( int i = 0; i < children.size( ); i++ )
        {
            sizes[i] = children.get( i ).size;
        }

        return sizes;
    }

    public boolean isEmpty( )
    {
        return root == null;
    }

    protected Node getNode( int id )
    {
        return map.get( id );
    }

    int getVersion( )
    {
        return versionId;
    }

    private static class Node
    {
        int id;

        List<Node> children;

        Node parent;

        double size;

        String title;

        String text;

        public Node( int id )
        {
            this.id = id;
        }

        public void addChild( Node child )
        {
            if ( children == null )
            {
                children = new ArrayList<NestedTreeMap.Node>( );
            }

            children.add( child );
            child.parent = this;
        }

        public boolean removeChild( Node child )
        {
            if ( children != null )
            {
                boolean removed = children.remove( child );
                if ( children.isEmpty( ) )
                {
                    children = null;
                }

                child.parent = null;
                return removed;
            }
            else
            {
                return false;
            }
        }

        @Override
        public boolean equals( Object obj )
        {
            if ( this == obj )
            {
                return true;
            }
            else if ( obj instanceof Node )
            {
                return ( ( Node ) obj ).id == id;
            }
            else
            {
                return false;
            }
        }

        @Override
        public int hashCode( )
        {
            return id;
        }
    }
}
