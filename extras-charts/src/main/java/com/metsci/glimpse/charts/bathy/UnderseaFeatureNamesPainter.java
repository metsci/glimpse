package com.metsci.glimpse.charts.bathy;

import static com.metsci.glimpse.painter.base.GlimpsePainterBase.getAxis2D;
import static com.metsci.glimpse.support.font.FontUtils.getDefaultBold;
import static com.metsci.glimpse.support.font.FontUtils.getDefaultPlain;
import static com.metsci.glimpse.util.logging.LoggerUtils.logWarning;
import static java.nio.file.Files.copy;

import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.geotools.geometry.GeometryBuilder;
import org.geotools.geometry.text.WKTParser;
import org.opengis.geometry.Geometry;
import org.opengis.geometry.aggregate.MultiPoint;
import org.opengis.geometry.coordinate.LineString;
import org.opengis.geometry.coordinate.Position;
import org.opengis.geometry.primitive.Point;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;

import com.metsci.glimpse.axis.Axis2D;
import com.metsci.glimpse.com.jogamp.opengl.util.awt.TextRenderer;
import com.metsci.glimpse.context.GlimpseContext;
import com.metsci.glimpse.painter.group.DelegatePainter;
import com.metsci.glimpse.painter.info.AnnotationPainter;
import com.metsci.glimpse.painter.info.AnnotationPainter.Annotation;
import com.metsci.glimpse.painter.info.SimpleTextPainter.HorizontalPosition;
import com.metsci.glimpse.painter.info.SimpleTextPainter.VerticalPosition;
import com.metsci.glimpse.support.color.GlimpseColor;
import com.metsci.glimpse.util.GlimpseDataPaths;
import com.metsci.glimpse.util.geo.LatLonGeo;
import com.metsci.glimpse.util.geo.projection.GeoProjection;
import com.metsci.glimpse.util.geo.projection.MercatorProjection;
import com.metsci.glimpse.util.quadtree.QuadTreeObjects;
import com.metsci.glimpse.util.vector.Vector2d;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

public class UnderseaFeatureNamesPainter extends DelegatePainter
{
    private static final Logger LOGGER = Logger.getLogger( UnderseaFeatureNamesPainter.class.getName( ) );

    public static final String DOWNLOAD_URL = "https://www.ngdc.noaa.gov/gazetteer/feature/export?aoi=&name=&featureType=&proposer.id=&discoverer.id=&meeting=&status=&format=csv";
    public static final String CACHE_FILE = "NOAA_Gazeteer.csv";

    private AnnotationPainter smallAnnotationPainter;
    private AnnotationPainter bigAnnotationPainter;
    private Int2ObjectMap<Annotation2> id2Annotation;
    private QuadTreeObjects<Annotation2> quadTree;

    private Rectangle2D.Double lastAxis;

    public UnderseaFeatureNamesPainter( GeoProjection projection ) throws IOException, NoSuchAuthorityCodeException, FactoryException, ParseException
    {
        lastAxis = new Rectangle2D.Double( );
        TextRenderer renderer = new TextRenderer( getDefaultPlain( 14 ) );
        smallAnnotationPainter = new AnnotationPainter( renderer );
        renderer = new TextRenderer( getDefaultBold( 16 ) );
        bigAnnotationPainter = new AnnotationPainter( renderer );
        addPainter( smallAnnotationPainter );
        addPainter( bigAnnotationPainter );
        id2Annotation = new Int2ObjectOpenHashMap<>( );
        quadTree = new QuadTreeObjects<Annotation2>( 10 )
        {
            @Override
            public float x( Annotation2 v )
            {
                return v.getX( );
            }

            @Override
            public float y( Annotation2 v )
            {
                return v.getY( );
            }
        };

        File file = getCachedDataFile( );

        int groupId = 0;
        int id = 0;

        GeometryBuilder bldr = new GeometryBuilder( "EPSG:4326" );
        WKTParser wktParser = new WKTParser( bldr );
        CSVParser parser = new CSVParser( new FileReader( file ), CSVFormat.RFC4180.withSkipHeaderRecord( ).withHeader( ) );
        for ( CSVRecord record : parser )
        {
            groupId++;
            String name = record.get( 0 );
            String type = record.get( 1 );

            String wkt = record.get( 9 );
            if ( wkt.contains( "MULTIPOINT" ) )
            {
                // geotools WKT parser is broken for MULTIPOINT
                wkt = wkt.replace( "((", "^" ).replace( "))", "$" ).replace( "(", "" ).replace( ")", "" ).replace( "^", "(" ).replace( "$", ")" );
            }

            Geometry geom;
            try
            {
                geom = wktParser.parse( wkt );
            }
            catch ( IllegalArgumentException | ParseException ex )
            {
                logWarning( LOGGER, "Failed to parse WKT line %,d '%s'", parser.getCurrentLineNumber( ), wkt );
                continue;
            }

            if ( geom instanceof Point )
            {
                Point pt = ( Point ) geom;
                double[] coord = pt.getDirectPosition( ).getCoordinate( );
                LatLonGeo ll = LatLonGeo.fromDeg( coord[1], coord[0] );
                Vector2d v = projection.project( ll );
                id++;
                add( id, groupId, name, type, v.getX( ), v.getY( ) );
            }
            else if ( geom instanceof LineString )
            {
                LineString ls = ( LineString ) geom;
                for ( Position pt : ls.getControlPoints( ) )
                {
                    double[] coord = pt.getDirectPosition( ).getCoordinate( );
                    LatLonGeo ll = LatLonGeo.fromDeg( coord[1], coord[0] );
                    Vector2d v = projection.project( ll );
                    id++;
                    add( id, groupId, name, type, v.getX( ), v.getY( ) );
                }
            }
            else if ( geom instanceof MultiPoint )
            {
                MultiPoint mp = ( MultiPoint ) geom;
                for ( Point pt : mp.getElements( ) )
                {
                    double[] coord = pt.getDirectPosition( ).getCoordinate( );
                    LatLonGeo ll = LatLonGeo.fromDeg( coord[1], coord[0] );
                    Vector2d v = projection.project( ll );
                    id++;
                    add( id, groupId, name, type, v.getX( ), v.getY( ) );
                }
            }

        }

        parser.close( );
    }

    @Override
    public void paintTo( GlimpseContext context )
    {
        checkNewState( context );
        super.paintTo( context );
    }

    private void checkNewState( GlimpseContext context )
    {
        Axis2D axis = getAxis2D( context );

        if ( lastAxis.getMinX( ) != axis.getMinX( ) ||
                lastAxis.getMaxX( ) != axis.getMaxX( ) ||
                lastAxis.getMinY( ) != axis.getMinY( ) ||
                lastAxis.getMaxY( ) != axis.getMaxY( ) )
        {
            lastAxis = new Rectangle2D.Double( axis.getMinX( ), axis.getMinY( ), axis.getMaxX( ) - axis.getMinX( ), axis.getMaxY( ) - axis.getMinY( ) );
            smallAnnotationPainter.clearAnnotations( );
            bigAnnotationPainter.clearAnnotations( );

            List<Annotation2> toView = new ArrayList<>( );
            double xStep = 0.99 * lastAxis.getWidth( );
            double yStep = 0.99 * lastAxis.getHeight( );
            for ( double x = axis.getMinX( ); x < axis.getMaxX( ); x += xStep )
            {
                float minX = ( float ) x, maxX = ( float ) ( x + xStep );
                for ( double y = axis.getMinY( ); y < axis.getMaxY( ); y += yStep )
                {
                    float minY = ( float ) y, maxY = ( float ) ( y + xStep );
                    quadTree.search( minX, maxX, minY, maxY, toView );

                    toView.sort( ( a, b ) -> Integer.compare( a.priority, b.priority ) );
                    if ( toView.size( ) > 0 )
                    {
                        for ( Annotation2 an : toView.subList( 0, Math.min( 30, toView.size( ) ) ) )
                        {
                            if ( an.priority < 3 )
                            {
                                bigAnnotationPainter.addAnnotation( an );
                            }
                            else
                            {
                                smallAnnotationPainter.addAnnotation( an );
                            }
                        }
                    }

                    toView.clear( );
                }
            }
        }
    }

    private int getPriority( String type )
    {
        String[] orderedTypes = new String[] {
                "continental slope",
                "channel",
                "sea channel",
                "seachannel",
                "pass",
                "canyon",
                "plain",
                "shelf",
                "shoal",
                "trench",
                "plateau",
                "reef",
                "seamount",
                "ridge",
                "valley",
                "deep",
                "ground",
                "basin",
                "abyssal plain",
                "apron",
                "bank",
                "borderland",
                "caldera",
                "cap",
                "cone",
                "discordance",
                "escarpment",
                "fan",
                "fracture zone",
                "gap",
                "guyot",
                "hill",
                "hole",
                "knoll",
                "levee",
                "mound",
                "mud volcano",
                "peak",
                "promontory",
                "province",
                "rift",
                "rise",
                "saddle",
                "salt dome province",
                "sand ridge province",
                "seabight",
                "sill",
                "slope",
                "spur",
                "tablemount",
                "terrace",
                "trough",
        };

        type = type.toLowerCase( );
        for ( int i = 0; i < orderedTypes.length; i++ )
        {
            if ( type.startsWith( orderedTypes[i] ) )
            {
                return i;
            }
        }

        return orderedTypes.length;
    }

    private void add( int id, int groupId, String text, String type, double x, double y )
    {
        int priority = getPriority( type );
        Annotation2 an = new Annotation2( groupId, priority, text + " " + type, x, y );
        an.setHorizontalPosition( HorizontalPosition.Center );
        an.setVerticalPosition( VerticalPosition.Bottom );
        an.setColor( GlimpseColor.getWhite( ) );
        id2Annotation.put( id, an );
        quadTree.add( an );
    }

    private static File getCachedDataFile( ) throws IOException
    {
        File file = new File( GlimpseDataPaths.glimpseSharedDataDir, CACHE_FILE );
        if ( file.isFile( ) )
        {
            return file;
        }

        file = new File( GlimpseDataPaths.glimpseUserDataDir, CACHE_FILE );
        if ( file.isFile( ) )
        {
            return file;
        }

        try (InputStream is = new URL( DOWNLOAD_URL ).openStream( ))
        {
            copy( is, file.toPath( ) );
        }

        return file;
    }

    public static class Annotation2 extends Annotation
    {
        public final int groupId;
        public final int priority;

        public Annotation2( int groupId, int priority, String text, double x, double y )
        {
            super( text, ( float ) x, ( float ) y );
            this.priority = priority;
            this.groupId = groupId;
        }
    }

    public static void main( String[] args ) throws IOException, NoSuchAuthorityCodeException, FactoryException, ParseException
    {
        new UnderseaFeatureNamesPainter( new MercatorProjection( ) );
    }
}
