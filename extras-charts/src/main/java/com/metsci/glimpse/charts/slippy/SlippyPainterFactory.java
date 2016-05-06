package com.metsci.glimpse.charts.slippy;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.metsci.glimpse.util.geo.projection.GeoProjection;

/**
 * TODO look into license/attribution requirements
 * 
 * @author oren
 */
public class SlippyPainterFactory {

    private SlippyPainterFactory() {}

    public static SlippyMapTilePainter getOpenStreetMaps(GeoProjection geoProj, Path cacheDir) {
        List<String> prefixes = new ArrayList<String>();
        prefixes.add("http://a.tile.openstreetmap.org/");
        prefixes.add("http://b.tile.openstreetmap.org/");
        prefixes.add("http://c.tile.openstreetmap.org/");
        return new SlippyMapTilePainter(geoProj, prefixes, 8, cacheDir, 18);
    }
    
    public static SlippyMapTilePainter getMapQuestMaps(GeoProjection geoProj, Path cacheDir) {
        List<String> prefixes = new ArrayList<String>();
        for (int i = 1; i <= 4; i++) {
            prefixes.add("http://otile"+i+".mqcdn.com/tiles/1.0.0/osm/");
        }
        return new SlippyMapTilePainter(geoProj, prefixes, 8, cacheDir, 17);
    }
    
    public static SlippyMapTilePainter getMapQuestImagery(GeoProjection geoProj, Path cacheDir, boolean inUS) {
        List<String> prefixes = new ArrayList<String>();
        for (int i = 1; i <= 4; i++) {
            prefixes.add("http://otile"+i+".mqcdn.com/tiles/1.0.0/sat/");
        }
        return new SlippyMapTilePainter(geoProj, prefixes, 8, cacheDir, inUS ? 16 : 11);
    }
    
    public static SlippyMapTilePainter getCartoMapLight(GeoProjection geoProj, Path cacheDir) {
        List<String> prefixes = new ArrayList<String>();
        prefixes.add("http://a.basemaps.cartocdn.com/light_all/");
        prefixes.add("http://b.basemaps.cartocdn.com/light_all/");
        prefixes.add("http://c.basemaps.cartocdn.com/light_all/");
        return new SlippyMapTilePainter(geoProj, prefixes, 8, cacheDir, 18);
    }
    

    public static SlippyMapTilePainter getCartoMapDark(GeoProjection geoProj, Path cacheDir) {
        List<String> prefixes = new ArrayList<String>();
        prefixes.add("http://a.basemaps.cartocdn.com/dark_all/");
        prefixes.add("http://b.basemaps.cartocdn.com/dark_all/");
        prefixes.add("http://c.basemaps.cartocdn.com/dark_all/");
        return new SlippyMapTilePainter(geoProj, prefixes, 8, cacheDir, 18);
    }
}
