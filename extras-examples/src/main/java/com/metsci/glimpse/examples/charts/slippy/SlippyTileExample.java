package com.metsci.glimpse.examples.charts.slippy;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JMenuBar;
import javax.swing.JRadioButton;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import com.metsci.glimpse.axis.listener.mouse.AxisMouseListener;
import com.metsci.glimpse.axis.painter.label.AxisUnitConverters;
import com.metsci.glimpse.charts.slippy.SlippyAxisMouseListener2D;
import com.metsci.glimpse.charts.slippy.SlippyMapTilePainter;
import com.metsci.glimpse.charts.slippy.SlippyPainterFactory;
import com.metsci.glimpse.examples.Example;
import com.metsci.glimpse.layout.GlimpseLayout;
import com.metsci.glimpse.layout.GlimpseLayoutProvider;
import com.metsci.glimpse.painter.geo.ScalePainter;
import com.metsci.glimpse.plot.MultiAxisPlot2D;
import com.metsci.glimpse.util.geo.LatLonGeo;
import com.metsci.glimpse.util.geo.projection.GeoProjection;
import com.metsci.glimpse.util.geo.projection.TangentPlane;
import com.metsci.glimpse.util.units.Length;

public class SlippyTileExample implements GlimpseLayoutProvider {
    
    public static void main(String[] args) throws Exception {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (UnsupportedLookAndFeelException e) { }
        SlippyTileExample slippy = new SlippyTileExample();
        Example example = Example.showWithSwing(slippy);
        example.getFrame().setJMenuBar(slippy.mapToolBar);
    }

    private JMenuBar mapToolBar;

    @Override
    public GlimpseLayout getLayout() throws Exception {
        
        final GeoProjection geoProj = new TangentPlane(LatLonGeo.fromDeg(38.958374, -77.358548));
        final boolean inUS = true;
        
        final MultiAxisPlot2D mapPlot = new MultiAxisPlot2D() {
            @Override
            protected AxisMouseListener createAxisMouseListenerXY( ) {
                return new SlippyAxisMouseListener2D(geoProj);
            }
        };
        double rad = Length.fromKilometers(1);
        mapPlot.getCenterAxis().lockAspectRatioXY(1);
        mapPlot.getCenterAxis().set(-rad, rad, -rad, rad);
        
        final SlippyMapTilePainter mapPainter = SlippyPainterFactory.getMapQuestMaps(geoProj);
        mapPlot.addPainter(mapPainter);
        
        final SlippyMapTilePainter satPainter = SlippyPainterFactory.getMapQuestImagery(geoProj, inUS);
        mapPlot.addPainter(satPainter);
        satPainter.setVisible(false);

        final SlippyMapTilePainter cartoLightPainter = SlippyPainterFactory.getCartoMap(geoProj, true, true);
        mapPlot.addPainter(cartoLightPainter);
        cartoLightPainter.setVisible(false);

        final SlippyMapTilePainter cartoDarkPainter = SlippyPainterFactory.getCartoMap(geoProj, false, false);
        mapPlot.addPainter(cartoDarkPainter);
        cartoDarkPainter.setVisible(false);
        
        ScalePainter scalePainter = new ScalePainter();
        scalePainter.setUnitConverter(AxisUnitConverters.suShownAsMeters);
        scalePainter.setUnitLabel("m");
        mapPlot.addPainter(scalePainter);
        
        this.mapToolBar = new JMenuBar();
        ButtonGroup group = new ButtonGroup();
        final JRadioButton mapCheckBox = new JRadioButton("Map Layer", mapPainter.isVisible());
        final JRadioButton satcheckBox = new JRadioButton("Imagery Layer", satPainter.isVisible());
        final JRadioButton cartoLightCheckBox = new JRadioButton("CartoDBLight (Labels)", cartoLightPainter.isVisible());
        final JRadioButton cartoDarkcheckBox = new JRadioButton("CartoDBDark (No Labels)", cartoDarkPainter.isVisible());
        
        group.add(mapCheckBox);
        group.add(satcheckBox);
        group.add(cartoLightCheckBox);
        group.add(cartoDarkcheckBox);
        
        ActionListener l = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mapPainter.setVisible(mapCheckBox.isSelected());
                satPainter.setVisible(satcheckBox.isSelected());
                cartoLightPainter.setVisible(cartoLightCheckBox.isSelected());
                cartoDarkPainter.setVisible(cartoDarkcheckBox.isSelected());
            }
        };
        
        mapToolBar.add(mapCheckBox);
        mapToolBar.add(satcheckBox);
        mapToolBar.add(cartoLightCheckBox);
        mapToolBar.add(cartoDarkcheckBox);
        
        mapCheckBox.addActionListener(l);
        satcheckBox.addActionListener(l);
        cartoLightCheckBox.addActionListener(l);
        cartoDarkcheckBox.addActionListener(l);
        
        return mapPlot;
    }

}
