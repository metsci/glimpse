package com.metsci.glimpse.wizard.error;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import com.metsci.glimpse.wizard.Wizard;
import com.metsci.glimpse.wizard.WizardError;
import com.metsci.glimpse.wizard.WizardPage;

public class ErrorTableModel extends AbstractTableModel
{
    private static final long serialVersionUID = 1L;

    protected List<WizardError> errors;
    protected Wizard<?> controller;
    protected boolean showPage;

    public ErrorTableModel( Wizard<?> controller, boolean showPage )
    {
        this.controller = controller;
        this.showPage = showPage;
        this.errors = Collections.emptyList( );
    }

    public void setErrors( Collection<WizardError> errors )
    {
        this.errors = new ArrayList<>( errors );
        this.fireTableDataChanged( );
    }

    public WizardError getError( int row )
    {
        return this.errors.get( row );
    }

    @Override
    public Class<?> getColumnClass( int column )
    {
        switch ( column )
        {
            case 0:
                return WizardError.class;
            case 1:
                return WizardPage.class;
            default:
                return super.getColumnClass( column );
        }
    }

    @Override
    public String getColumnName( int column )
    {
        switch ( column )
        {
            case 0:
                return "Description";
            case 1:
                return "Page";
            default:
                return super.getColumnName( column );
        }
    }

    @Override
    public boolean isCellEditable( int row, int column )
    {
        return false;
    }

    @Override
    public int getRowCount( )
    {
        return this.errors.size( );
    }

    @Override
    public int getColumnCount( )
    {
        return showPage ? 2 : 1;
    }

    @Override
    public Object getValueAt( int rowIndex, int columnIndex )
    {
        WizardError error = getError( rowIndex );

        switch ( columnIndex )
        {
            case 0:
                return error;
            case 1:
                Object pageId = error.getPageId( );
                WizardPage<?> page = controller.getPageModel( ).getPageById( pageId );
                return page;
            default:
                return super.getColumnName( columnIndex );
        }
    }
}
