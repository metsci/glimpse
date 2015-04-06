---
layout: default
title: Glimpse 2.2.0 Release
---

<p>Glimpse 2.2.0 provides incremental bug fixes and feature enhancements as well as a number of new features:</p>
<table border="0" cellspacing="3" cellpadding="2">
    <tr>
        <td><a href="https://github.com/metsci/glimpse/tree/master/util/src/main/java/com/metsci/glimpse/util/units/time/format">TimeStampFormat</a></td>
        <td>More format options are recognized by TimeStampFormatStandard.</td>
    </tr>
    
    <tr>
        <td><a href="https://github.com/metsci/glimpse/blob/master/core/src/main/java/com/metsci/glimpse/plot/EmptyPlot2D.java">EmptyPlot2D</a></td>
        <td>Simple plot with no axes or title, just lots of space for data.</td>
    </tr>
    
    <tr>
        <td><a href="https://github.com/metsci/glimpse/blob/master/core/src/main/java/com/metsci/glimpse/painter/track/StaticParticlePainter.java">StaticParticlePainter</a></td>
        <td>Painter similar to TrackPainter, but more efficient for large numbers of static tracks where each track has positions at the same set of time points.</td>
    </tr>
    
    <tr>
        <td><a href="https://github.com/metsci/glimpse/blob/master/core/src/main/java/com/metsci/glimpse/support/swing/NewtSwingEDTGlimpseCanvas.java">New Canvas</a></td>
        <td>NewtSwingEDTGlimpseCanvas allows use of the well maintained NewtCanvasAWT while still performing OpenGL rendering on the Swing EDT. This sacrifices performance for a greatly simplified threading model.</td>
    </tr>
    
    <tr>
        <td><a href="https://github.com/metsci/glimpse-prerelease/blob/master/docking-examples/src/main/java/com/metsci/glimpse/docking/GlimpseDockingExample.java">Docking Frames</a></td>
        <td>Improvements and Bug-Fixes to Glimpse's lightweight docking framework.</td>
    </tr>
    
    <tr>
        <td><a href="https://github.com/metsci/glimpse-prerelease/blob/master/core-examples/src/main/java/com/metsci/glimpse/examples/axis/WrappedAxisExample.java">Wrapped Plots</a></td>
        <td>Plots and axes can be set to repeat on a regular interval.</td>
    </tr>
</table>
