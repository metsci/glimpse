/*
 * Copyright (c) 2019, Metron, Inc.
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
package com.metsci.glimpse.wizard;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.metsci.glimpse.wizard.listener.PageModelListener;

public class WizardPageModelSimple<D> implements WizardPageModel<D>
{

    protected List<PageModelListener<D>> listeners;

    protected Wizard<D> wizard;

    // a map for looking up pages by their unique id
    protected LinkedList<WizardPage<D>> pages;

    public WizardPageModelSimple( )
    {
        this.pages = new LinkedList<>( );
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
     * Add pages to the model in the order they are to appear
     */
    @Override
    public void addPage( WizardPage<D> page )
    {
        this.pages.add( page );

        this.firePagesAdded( Collections.singleton( page ) );
    }

    @Override
    public void removePage( WizardPage<D> page )
    {
        this.pages.remove( page );

        this.firePagesRemoved( Collections.singleton( page ) );
    }

    @Override
    public void removePageById( Object id )
    {
        WizardPage<D> page = getPageById( id );
        if ( page != null )
        {
            removePage( page );
        }
    }

    @Override
    public WizardPage<D> getPageById( Object id )
    {
        return this.pages.stream( ).filter( p -> p.getId( ).equals( id ) ).findAny( ).orElse( null );
    }

    @Override
    public WizardPage<D> getNextPage( LinkedList<WizardPage<D>> visitHistory, D data )
    {
        WizardPage<D> mostRecentPage = visitHistory.peekLast( );
        if ( mostRecentPage == null )
        {
            // Show the first page
            return this.pages.peekFirst( );
        }
        else
        {
            int idx = this.pages.indexOf( mostRecentPage );
            if ( idx >= this.pages.size( ) - 1 )
            {
                return null;
            }
            else
            {
                return this.pages.get( idx + 1 );
            }
        }
    }

    @Override
    public Collection<WizardPage<D>> getPages( )
    {
        return this.pages;
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
