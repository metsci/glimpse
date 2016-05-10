package com.metsci.glimpse.charts.slippy;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.metsci.glimpse.util.geo.projection.GeoProjection;

/**
 * TODO look into license/attribution requirements
 * 
 * @author oren
 */
public class SlippyPainterFactory {

    //TODO maybe include default cache directory in user home
    private SlippyPainterFactory() {}
    
    public static final Path CACHE_ROOT = Paths.get(System.getProperty("user.home")).resolve(".glimpse-slippy-cache");


    public static SlippyMapTilePainter getOpenStreetMaps(GeoProjection geoProj) {
        return getOpenStreetMaps(geoProj, CACHE_ROOT.resolve("osm-maps"));
    }
    
    public static SlippyMapTilePainter getOpenStreetMaps(GeoProjection geoProj, Path cacheDir) {
        List<String> prefixes = new ArrayList<String>();
        prefixes.add("http://a.tile.openstreetmap.org/");
        prefixes.add("http://b.tile.openstreetmap.org/");
        prefixes.add("http://c.tile.openstreetmap.org/");
        return new SlippyMapTilePainter(geoProj, prefixes, 8, cacheDir, 18);
    }

    public static SlippyMapTilePainter getMapQuestMaps(GeoProjection geoProj) {
        return getMapQuestMaps(geoProj, CACHE_ROOT.resolve("mapquest-map"));
    }
    
    public static SlippyMapTilePainter getMapQuestMaps(GeoProjection geoProj, Path cacheDir) {
        List<String> prefixes = new ArrayList<String>();
        for (int i = 1; i <= 4; i++) {
            prefixes.add("http://otile"+i+".mqcdn.com/tiles/1.0.0/osm/");
        }
        return new SlippyMapTilePainter(geoProj, prefixes, 8, cacheDir, 17);
    }

    public static SlippyMapTilePainter getMapQuestImagery(GeoProjection geoProj, boolean inUS) {
        return getMapQuestImagery(geoProj, CACHE_ROOT.resolve("mapquest-sat"), inUS);
    }
    
    public static SlippyMapTilePainter getMapQuestImagery(GeoProjection geoProj, Path cacheDir, boolean inUS) {
        List<String> prefixes = new ArrayList<String>();
        for (int i = 1; i <= 4; i++) {
            prefixes.add("http://otile"+i+".mqcdn.com/tiles/1.0.0/sat/");
        }
        return new SlippyMapTilePainter(geoProj, prefixes, 8, cacheDir, inUS ? 16 : 11);
    }

    public static SlippyMapTilePainter getCartoMap(GeoProjection geoProj, boolean light, boolean labels) {
        String cacheStr = "cartodb-" + (light ? "light" : "dark") + (labels ? "-all" : "-nolabels");
        return getCartoMap(geoProj, CACHE_ROOT.resolve(cacheStr), light, labels);
    }
    
    public static SlippyMapTilePainter getCartoMap(GeoProjection geoProj, Path cacheDir, boolean light, boolean labels) {
        String type = (light ? "light" : "dark") + (labels ? "_all" : "_nolabels");
        String template = "http://%s.basemaps.cartocdn.com/"+type+"/";
        List<String> prefixes = new ArrayList<String>();
        prefixes.add(String.format(template, "a"));
        prefixes.add(String.format(template, "b"));
        prefixes.add(String.format(template, "c"));
        return new SlippyMapTilePainter(geoProj, prefixes, 8, cacheDir, 18);
    }
}
