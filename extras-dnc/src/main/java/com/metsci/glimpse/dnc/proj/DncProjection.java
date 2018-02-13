package com.metsci.glimpse.dnc.proj;

public interface DncProjection
{

    String configString( );

    double suggestedPpvMultiplier( );

    boolean canProjectLibrary( int databaseNum, String libraryName, double minLat_DEG, double maxLat_DEG, double minLon_DEG, double maxLon_DEG );

    void projectPos( double lat_DEG, double lon_DEG, float[] result, int resultOffset );

    double projectAzimuth_MATHRAD( double x, double y, double azimuth_MATHRAD );

}
