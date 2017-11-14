package com.metsci.glimpse.topo;

public class TopoPainterConfig
{

    public static final TopoPainterConfig topoPainterConfig_DEFAULT = new TopoPainterConfig( 2048, 2048, 1, 1, 1, true, false );


    public final int maxRowsPerBand;
    public final int maxColsPerTile;
    public final int hTileDisposalsPerFrame;
    public final int dTileDisposalsPerFrame;
    public final int tileXfersPerFrame;
    public final boolean preloadLowerResTiles;
    public final boolean preloadHigherResTiles;


    public TopoPainterConfig( int maxRowsPerBand,
                              int maxColsPerTile,
                              int hTileDisposalsPerFrame,
                              int dTileDisposalsPerFrame,
                              int tileXfersPerFrame,
                              boolean preloadLowerResTiles,
                              boolean preloadHigherResTiles )
    {
        this.maxRowsPerBand = maxRowsPerBand;
        this.maxColsPerTile = maxColsPerTile;
        this.hTileDisposalsPerFrame = hTileDisposalsPerFrame;
        this.dTileDisposalsPerFrame = dTileDisposalsPerFrame;
        this.tileXfersPerFrame = tileXfersPerFrame;
        this.preloadLowerResTiles = preloadLowerResTiles;
        this.preloadHigherResTiles = preloadHigherResTiles;
    }

    public String toLongString( String linePrefix )
    {
        StringBuilder s = new StringBuilder( );

        s.append( linePrefix ).append( "maxRowsPerBand:         " ).append( this.maxRowsPerBand ).append( "\n" );
        s.append( linePrefix ).append( "maxColsPerTile:         " ).append( this.maxColsPerTile ).append( "\n" );
        s.append( linePrefix ).append( "hTileDisposalsPerFrame: " ).append( this.hTileDisposalsPerFrame ).append( "\n" );
        s.append( linePrefix ).append( "dTileDisposalsPerFrame: " ).append( this.dTileDisposalsPerFrame ).append( "\n" );
        s.append( linePrefix ).append( "tileXfersPerFrame:      " ).append( this.tileXfersPerFrame ).append( "\n" );
        s.append( linePrefix ).append( "preloadLowerResTiles:   " ).append( this.preloadLowerResTiles ).append( "\n" );
        s.append( linePrefix ).append( "preloadHigherResTiles:  " ).append( this.preloadHigherResTiles ).append( "\n" );

        return s.toString( );
    }

}
