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
