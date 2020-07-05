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

    @Override
    public boolean showErrors( )
    {
        // Page aggregates errors from all other wizard pages
        // A no errors indicator for this page would conflict with errors in the table
        return false;
    }
}
