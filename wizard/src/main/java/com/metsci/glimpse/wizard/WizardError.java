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

import java.io.Serializable;

/**
 * Describes an error with the specification of a scenario.
 * <p>
 * Possibly contains a link to the editor page which needs to be modified to fix the error.
 *
 * @author ulman
 */
public class WizardError implements Comparable<WizardError>, Serializable
{
    private static final long serialVersionUID = 1L;

    protected Object pageId;

    protected String description;
    protected WizardErrorType type;

    public WizardError( WizardErrorType type, String description )
    {
        this( null, type, description );
    }

    public WizardError( Object pageId, WizardErrorType type, String description )
    {
        this.pageId = pageId;
        this.description = description;
        this.type = type;
    }

    public WizardError withPageId( Object pageId )
    {
        return new WizardError( pageId, this.type, this.description );
    }

    public WizardError withDescription( String description )
    {
        return new WizardError( this.pageId, this.type, description );
    }

    public Object getPageId( )
    {
        return pageId;
    }

    public String getDescription( )
    {
        return description;
    }

    public WizardErrorType getType( )
    {
        return type;
    }

    @Override
    public int compareTo( WizardError o )
    {
        return Integer.compare( type.ordinal( ), o.type.ordinal( ) );
    }
}
