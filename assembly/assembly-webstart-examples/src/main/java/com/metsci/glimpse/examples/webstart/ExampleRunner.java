package com.metsci.glimpse.examples.webstart;

import java.awt.BorderLayout;
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
import java.util.Map;
import java.util.TreeMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import jsyntaxpane.DefaultSyntaxKit;

<<<<<<< HEAD
import com.metsci.glimpse.examples.Example;

=======
>>>>>>> enhancement to Brandon's webstart Glimpse examples to include line numbering and syntax highlighting
@SuppressWarnings( "serial" )
public class ExampleRunner extends JSplitPane
{
    private JTree exampleTree;
    private JEditorPane codeArea;
    private JButton runExampleButton;

    private Class<?> exampleClass;

    public ExampleRunner( )
    {
        // initialize jsyntaxpane syntax highlighting
        DefaultSyntaxKit.initKit();
        
        setOrientation( JSplitPane.HORIZONTAL_SPLIT );

        exampleTree = new JTree( new DefaultTreeModel( new DefaultMutableTreeNode( "Loading ..." ) ) );
<<<<<<< HEAD

        DefaultSyntaxKit.initKit( );

        codeArea = new JEditorPane( );
=======
        
        codeArea = new JEditorPane( );
        JScrollPane scrollPane = new JScrollPane( codeArea );
        codeArea.setContentType("text/java");
        codeArea.setFont( Font.decode( "COURIER" ) );
>>>>>>> enhancement to Brandon's webstart Glimpse examples to include line numbering and syntax highlighting
        codeArea.setEditable( false );

        runExampleButton = new JButton( "Run Example" );
        runExampleButton.setEnabled( false );

        exampleTree.getSelectionModel( ).setSelectionMode( TreeSelectionModel.SINGLE_TREE_SELECTION );
        exampleTree.addTreeSelectionListener( new TreeSelectionListener( )
        {
            @Override
            public void valueChanged( TreeSelectionEvent e )
            {
                if ( exampleTree.getSelectionCount( ) > 0 )
                {
                    TreePath selectionPath = exampleTree.getSelectionPath( );
                    DefaultMutableTreeNode node = ( DefaultMutableTreeNode ) selectionPath.getLastPathComponent( );
                    if ( node.getUserObject( ) instanceof Class<?> )
                    {
                        Class<?> clazz = ( Class<?> ) node.getUserObject( );
                        selectClass( clazz );
                    }
                    else
                    {
                        selectClass( null );
                    }
                }
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
        rightPanel.add( scrollPane, BorderLayout.CENTER );

        JPanel buttonPanel = new JPanel( new BorderLayout( ) );
        buttonPanel.add( runExampleButton, BorderLayout.WEST );

        rightPanel.add( buttonPanel, BorderLayout.SOUTH );

        setLeftComponent( new JScrollPane( exampleTree ) );
        setRightComponent( rightPanel );
        setDividerLocation( 300 );

        codeArea.setContentType( "text/java" );
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
                    exampleTree.setModel( createTreeModel( classes ) );
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
                mainMethod.invoke( null, new Object[] { new String[0] } );
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

    private TreeModel createTreeModel( Collection<Class<?>> examples )
    {
        Package rootPackage = Package.getPackage( "com.metsci.glimpse.examples" );
        SimpleNode root = new SimpleNode( rootPackage );

        Map<String, SimpleNode> packageNodes = new TreeMap<String, SimpleNode>( );
        packageNodes.put( rootPackage.getName( ), root );

        for ( Class<?> example : examples )
        {
            Package p = example.getPackage( );
            SimpleNode packageNode = getPackageNode( p, packageNodes );
            packageNode.add( new SimpleNode( example ) );
        }

        return new DefaultTreeModel( root );
    }

    private SimpleNode getPackageNode( Package p, Map<String, SimpleNode> packageNodes )
    {
        SimpleNode node = packageNodes.get( p.getName( ) );
        if ( node == null )
        {
            Package pp = getParentPackage( p );
            SimpleNode parentNode = getPackageNode( pp, packageNodes );
            node = new SimpleNode( p );
            parentNode.add( node );

            packageNodes.put( p.getName( ), node );
        }

        return node;
    }

    private Package getParentPackage( Package child )
    {
        String name = child.getName( );
        String parentName = name.substring( 0, name.lastIndexOf( '.' ) );
        return Package.getPackage( parentName );
    }

    private void selectClass( final Class<?> clazz )
    {
        runExampleButton.setEnabled( false );
        if ( clazz == null )
        {
            codeArea.setText( "" );
            return;
        }

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

                    // scroll to top
                    codeArea.setCaretPosition( 0 );

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
        InputStream in = Example.class.getClassLoader( ).getResourceAsStream( file );
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

        URL url = Example.class.getResource( Example.class.getSimpleName( ) + ".class" );
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

                    // special case
                    if ( clazz.getSimpleName( ).equals( "Example" ) )
                    {
                        continue;
                    }
                    else
                    {
                        exampleClasses.add( clazz );
                    }
                }
            }
        }
        else
        {
            throw new UnsupportedOperationException( "Must run as jar to use JarFileConnection" );
        }

        return exampleClasses;
    }

    private static class SimpleNode extends DefaultMutableTreeNode
    {
        SimpleNode( Object object )
        {
            super( object );
        }

        @Override
        public String toString( )
        {
            Object o = getUserObject( );
            if ( o instanceof Package )
            {
                String name = ( ( Package ) o ).getName( );
                return name.substring( name.lastIndexOf( '.' ) + 1 );
            }
            else if ( o instanceof Class )
            {
                return ( ( Class<?> ) o ).getSimpleName( );
            }
            else
            {
                return String.valueOf( o );
            }
        }
    }
}
