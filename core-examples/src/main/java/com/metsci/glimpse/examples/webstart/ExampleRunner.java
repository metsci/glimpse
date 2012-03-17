package com.metsci.glimpse.examples.webstart;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

@SuppressWarnings( "serial" )
public class ExampleRunner extends JSplitPane
{
    private JList exampleList;
    private JTextArea codeArea;
    private JButton runExampleButton;

    private Class<?> exampleClass;

    public ExampleRunner( )
    {
        setOrientation( JSplitPane.HORIZONTAL_SPLIT );

        exampleList = new JList( );

        codeArea = new JTextArea( );
        codeArea.setFont( Font.decode( "COURIER" ) );
        codeArea.setEditable( false );

        runExampleButton = new JButton( "Run Example" );
        runExampleButton.setEnabled( false );

        exampleList.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
        exampleList.addListSelectionListener( new ListSelectionListener( )
        {
            @Override
            public void valueChanged( ListSelectionEvent e )
            {
                selectClass( ( Class<?> ) exampleList.getSelectedValue( ) );
            }
        } );
        exampleList.setCellRenderer( new DefaultListCellRenderer( )
        {
            @Override
            public Component getListCellRendererComponent( JList list, Object value, int index, boolean isSelected, boolean cellHasFocus )
            {
                Class<?> clazz = ( Class<?> ) value;
                value = clazz.getSimpleName( );

                return super.getListCellRendererComponent( list, value, index, isSelected, cellHasFocus );
            }
        } );

        runExampleButton.addActionListener( new ActionListener( )
        {
            @Override
            public void actionPerformed( ActionEvent e )
            {
                runExample( exampleClass );
            }
        } );

        JPanel rightPanel = new JPanel( new BorderLayout( ) );
        rightPanel.add( new JScrollPane( codeArea ), BorderLayout.CENTER );

        JPanel buttonPanel = new JPanel( new BorderLayout( ) );
        buttonPanel.add( runExampleButton, BorderLayout.EAST );

        rightPanel.add( buttonPanel, BorderLayout.SOUTH );

        setLeftComponent( new JScrollPane( exampleList ) );
        setRightComponent( rightPanel );
    }

    public void populateExamples( )
    {
        new SwingWorker<Collection<Class<?>>, Void>( )
        {
            @Override
            protected Collection<Class<?>> doInBackground( ) throws Exception
            {
                return getExamples( );
            }

            @Override
            protected void done( )
            {
                try
                {
                    Collection<Class<?>> classes = get( );

                    DefaultListModel model = new DefaultListModel( );
                    for ( Class<?> exampleClass : classes )
                    {
                        model.addElement( exampleClass );
                    }

                    exampleList.setModel( model );
                }
                catch ( Exception e )
                {
                    JOptionPane.showMessageDialog( SwingUtilities.getWindowAncestor( ExampleRunner.this ), e.getMessage( ), "Error!", JOptionPane.ERROR_MESSAGE );
                }
            }
        }.execute( );
    }

    private void runExample( final Class<?> clazz )
    {
        new SwingWorker<Void, Void>( )
        {
            @Override
            protected Void doInBackground( ) throws Exception
            {
                Method mainMethod = exampleClass.getMethod( "main", String[].class );
                mainMethod.invoke( null, new Object[] { new String[ 0 ] } );
                return null;
            }

            @Override
            protected void done( )
            {
                try
                {
                    get( );
                }
                catch ( Exception e )
                {
                    JOptionPane.showMessageDialog( SwingUtilities.getWindowAncestor( ExampleRunner.this ), e.getMessage( ), "Error!", JOptionPane.ERROR_MESSAGE );
                }
            }
        }.execute( );
    }

    private void selectClass( final Class<?> clazz )
    {
        runExampleButton.setEnabled( false );
        new SwingWorker<String, Void>( )
        {
            @Override
            protected String doInBackground( ) throws Exception
            {
                return getSource( clazz );
            }

            @Override
            protected void done( )
            {
                try
                {
                    codeArea.setText( get( ) );
                    exampleClass = clazz;
                    runExampleButton.setEnabled( true );
                }
                catch ( Exception e )
                {
                    JOptionPane.showMessageDialog( SwingUtilities.getWindowAncestor( ExampleRunner.this ), e.getMessage( ), "Error!", JOptionPane.ERROR_MESSAGE );
                }
            }
        }.execute( );
    }

    private String getSource( Class<?> clazz ) throws IOException
    {
        String file = clazz.getName( ).replace( '.', '/' ) + ".java";
        String eol = System.getProperty( "line.separator" );
        StringBuilder builder = new StringBuilder( );

        String line = null;
        InputStream in = ExampleRunner.class.getClassLoader( ).getResourceAsStream( file );
        if ( in == null )
        {
            throw new FileNotFoundException( "Source file not found for " + clazz.getName( ) );
        }

        BufferedReader reader = new BufferedReader( new InputStreamReader( in ) );
        while ( ( line = reader.readLine( ) ) != null )
        {
            builder.append( line );
            builder.append( eol );
        }

        reader.close( );
        return builder.toString( );
    }

    private Collection<Class<?>> getExamples( ) throws ClassNotFoundException, IOException
    {
        List<Class<?>> exampleClasses = new ArrayList<Class<?>>( );

        URL url = ExampleRunner.class.getResource( ExampleRunner.class.getSimpleName( ) + ".class" );
        URLConnection connection = url.openConnection( );
        if ( connection instanceof JarURLConnection )
        {
            JarFile jar = ( ( JarURLConnection ) connection ).getJarFile( );

            Enumeration<JarEntry> entries = jar.entries( );
            while ( entries.hasMoreElements( ) )
            {
                JarEntry entry = entries.nextElement( );
                String name = entry.getName( );
                if ( name.endsWith( "Example.class" ) )
                {
                    String className = name.replace( '/', '.' ).substring( 0, name.length( ) - ".class".length( ) );
                    Class<?> clazz = Class.forName( className );
                    exampleClasses.add( clazz );
                }
            }
        }
        else
        {
            throw new UnsupportedOperationException( "Must run as jar to use JarFileConnection" );
        }

        return exampleClasses;
    }
}
