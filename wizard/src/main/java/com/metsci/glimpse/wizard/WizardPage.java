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
package com.metsci.glimpse.wizard;

import java.awt.Container;
import java.util.Collection;

/**
 * A single page of a Wizard.
 * 
 * @author ulman
 *
 * @param <D> the type of data displayed / manipulated by this wizard
 */
public interface WizardPage<D>
{
    /**
     * Unique identifier for the Page.
     */
    public Object getId( );
    
    /**
     * Unique identifier for this Page's parent Page.
     * <p>
     * This method may return null if this is a top level page.
     */
    public Object getParentId( );

    /**
     * A short (one or two word) title for the Page.
     */
    public String getTitle( );

    /**
     * The container holding the Swing components which make up the Page.
     */
    public Container getContainter( );

    /**
     * Dispose of any resources on the page.
     * <p>
     * For example, if the WizardPage contains a GlimpseCanvas it should be disposed here.
     */
    public void dispose( );

    /**
     * Update this page to display the provided data.
     * <p>
     * The provided data object should not be modified here.
     * <p>
     * The force parameter indicates when the provided data should override user entries in the page's
     * data entry fields. In some cases, if the user has only entered partial information, that
     * partial information should remain if the user leaves and re-enters the page. In many use
     * cases this field can be ignored.
     * 
     */
    public void setData( D data, boolean force );

    /**
     * Update the provided data to reflect the information entered by the user on this page.
     * <p>
     * If data is mutable, the data provided as a method argument may be modified and returned.
     */
    public D updateData( D data );

    /**
     * If any information the user has entered is invalid, {@link WizardError}s can be returned
     * to indicate problems.
     * 
     * @return a collection of problems with the data the user has entered
     */
    public Collection<WizardError> getErrors( );
    
    /**
     * Callback method called by the Wizard when this page is visited.
     */
    public void onEnter( );
    
    /**
     * Callback method called by the Wizard when this page stops being visited.
     */
    public void onExit( );
}
