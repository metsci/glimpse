/*
 * Copyright (c) 2019, Metron, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of Metron, Inc. nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL METRON, INC. BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.metsci.glimpse.dnc.geosym;

import static com.metsci.glimpse.dnc.geosym.DncGeosymAttributeExpressions.alwaysTrue;
import static com.metsci.glimpse.dnc.geosym.DncGeosymAttributeExpressions.buildAttributeExpression;
import static com.metsci.glimpse.dnc.geosym.DncGeosymLabelLocation.appendToPrevious;
import static com.metsci.glimpse.util.GeneralUtils.newArrayList;
import static com.metsci.glimpse.util.GeneralUtils.newHashMap;
import static com.metsci.glimpse.util.io.StreamOpener.resource;
import static com.metsci.glimpse.util.units.Angle.degreesToRadians;
import static java.lang.Double.parseDouble;
import static java.lang.Float.parseFloat;
import static java.lang.Integer.parseInt;
import static java.lang.Math.cos;
import static java.lang.Math.max;
import static java.lang.Math.sin;

import java.awt.Color;
import java.awt.Font;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.swing.SwingConstants;

import com.metsci.glimpse.support.color.GlimpseColor;
import com.metsci.glimpse.support.font.FontUtils;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

public class DncGeosymIo
{
    public static final String geosymAssignmentPath = "geosym/assignments/";
    public static final String geosymAttrExprsFile = "attexp.txt";
    public static final String geosymCodesFile = "code.txt";
    public static final String geosymFullAssignmentsFile = "fullsym.txt";
    public static final String geosymSimplifiedAssignmentsFile = "simpsym.txt";
    public static final String geosymLabelJoinsFile = "textjoin.txt";
    public static final String geosymLabelLocationsFile = "textloc.txt";
    public static final String geosymTextAbbreviationsFile = "textabbr.txt";
    public static final String geosymTextStylesFile = "textchar.txt";


    public static Object2IntMap<String> readSymbolAssignmentHeader(BufferedReader reader) throws IOException
    {
        // Skip file description line
        reader.readLine();

        Object2IntMap<String> columnNums = new Object2IntOpenHashMap<String>();
        for (int columnNum = 0; true; columnNum++)
        {
            String line = reader.readLine();
            if (line == null) throw new RuntimeException("File header is incomplete");
            if (line.equals(";")) break;

            // The -1 means: don't discard trailing empty tokens
            String[] tokens = line.split("=", -1);
            columnNums.put(tokens[0], columnNum);
        }
        return columnNums;
    }

    public static BufferedReader resourceReader(String location) throws IOException
    {
        return new BufferedReader(new InputStreamReader(resource.openForRead(location)));
    }

    public static BufferedReader geosymReader(String filename) throws IOException
    {
        return resourceReader(geosymAssignmentPath + filename);
    }

    public static Map<String,DncGeosymLineAreaStyle> readGeosymLineAreaStyles(String location) throws IOException
    {
        BufferedReader reader = null;
        try
        {
            reader = resourceReader(location);
            return readGeosymLineAreaStyles(reader);
        }
        finally
        {
            if (reader != null) reader.close();
        }
    }

    public static Map<String,DncGeosymLineAreaStyle> readGeosymLineAreaStyles(BufferedReader reader) throws IOException
    {
        Map<String,DncGeosymLineAreaStyle> styles = newHashMap();
        while (true)
        {
            String line = reader.readLine();
            if (line == null) break;

            if (line.trim().isEmpty()) continue;
            if (line.startsWith("#")) continue;

            // The -1 means: don't discard trailing empty tokens
            String[] tokens = line.split(",", -1);

            String symbolId = tokens[0];
            String symbolType = tokens[1];

            // XXX: Yuck
            float lineWidthFactor = 2; // pixels per millimeter
            float lineWidth = (tokens[2].isEmpty() ? 1 : max(1, lineWidthFactor*parseFloat(tokens[2])));

            float[] lineRgba = (tokens[3].isEmpty() ? GlimpseColor.getBlack() : GlimpseColor.fromColorAwt(Color.decode(tokens[3])));

            String lineStipplePattern0 = tokens[4];
            String lineStippleFactor0 = tokens[5];
            boolean hasLineStipple = (!lineStipplePattern0.isEmpty() && !lineStippleFactor0.isEmpty());
            int lineStippleFactor = (hasLineStipple ? parseInt(tokens[5]) : -1);
            short lineStipplePattern = (hasLineStipple ? Integer.decode(lineStipplePattern0).shortValue() : 0);

            float[] fillRgba = (tokens[6].isEmpty() ? GlimpseColor.getBlack() : GlimpseColor.fromColorAwt(Color.decode(tokens[6])));

            styles.put(symbolId, new DncGeosymLineAreaStyle(symbolId, symbolType, lineWidth, lineRgba, hasLineStipple, lineStippleFactor, lineStipplePattern, fillRgba));
        }
        return styles;
    }

    public static Int2ObjectMap<DncGeosymAssignment> readDncSymbolAssignments() throws IOException
    {
        return readDncSymbolAssignments(geosymFullAssignmentsFile);
    }

    public static Int2ObjectMap<DncGeosymAssignment> readDncSymbolAssignments(String filename) throws IOException
    {
        BufferedReader attrExprsReader = null;
        BufferedReader codesReader = null;
        BufferedReader textStylesReader = null;
        BufferedReader textAbbrevsReader = null;
        BufferedReader labelLocationsReader = null;
        BufferedReader labelJoinsReader = null;
        BufferedReader assignmentsReader = null;
        try
        {
            codesReader = geosymReader(geosymCodesFile);
            List<DncGeosymCode> codes = readGeosymCodes(codesReader);

            int productId = findDncProductId(filename, codes);

            Int2ObjectMap<String> featureDelinCodes = findFeatureDelineationCodes(filename, codes);

            attrExprsReader = geosymReader(geosymAttrExprsFile);
            textStylesReader = geosymReader(geosymTextStylesFile);
            textAbbrevsReader = geosymReader(geosymTextAbbreviationsFile);
            labelLocationsReader = geosymReader(geosymLabelLocationsFile);
            labelJoinsReader = geosymReader(geosymLabelJoinsFile);
            Int2ObjectMap<String> fontNameCodes = findFontNameCodes(codes);
            Int2ObjectMap<String> fontStyleCodes = findFontStyleCodes(codes);
            Int2ObjectMap<String> labelJustifyCodes = findLabelJustifyCodes(codes);
            Int2ObjectMap<Int2ObjectMap<String>> textAbbrevs = readGeosymTextAbbreviations(textAbbrevsReader);
            Int2ObjectMap<DncGeosymTextStyle> textStyles = readGeosymTextStyles(textStylesReader, fontNameCodes, fontStyleCodes, textAbbrevs);
            Int2ObjectMap<DncGeosymLabelLocation> labelLocations = readGeosymLabelLocations(labelLocationsReader, labelJustifyCodes);
            Int2ObjectMap<DncGeosymLabelJoin> labelJoins = readGeosymLabelJoins(labelJoinsReader, textStyles, labelLocations);

            Int2ObjectMap<String> attrComparisonCodes = findAttrComparisonCodes(codes);
            Int2ObjectMap<String> attrExprConnectorCodes = findAttrExprConnectorCodes(codes);
            Int2ObjectMap<DncGeosymAttributeExpression> attrExprs = readGeosymAttributeExpressions(attrExprsReader, attrComparisonCodes, attrExprConnectorCodes);

            assignmentsReader = geosymReader(filename);
            return readGeosymAssignments(assignmentsReader, productId, featureDelinCodes, attrExprs, labelJoins);
        }
        finally
        {
            if (attrExprsReader != null) attrExprsReader.close();
            if (codesReader != null) codesReader.close();
            if (textStylesReader != null) textStylesReader.close();
            if (textAbbrevsReader != null) textAbbrevsReader.close();
            if (labelLocationsReader != null) labelLocationsReader.close();
            if (labelJoinsReader != null) labelJoinsReader.close();
            if (assignmentsReader != null) assignmentsReader.close();
        }
    }

    public static Int2ObjectMap<DncGeosymAssignment> readGeosymAssignments(BufferedReader reader, int productIdFilter, Int2ObjectMap<String> featureDelinCodes, Int2ObjectMap<DncGeosymAttributeExpression> attrExprs, Int2ObjectMap<DncGeosymLabelJoin> labelJoins) throws IOException
    {
        Int2ObjectMap<DncGeosymAssignment> assignments = new Int2ObjectOpenHashMap<DncGeosymAssignment>();
        Object2IntMap<String> columnNums = readSymbolAssignmentHeader(reader);
        while (true)
        {
            String line = reader.readLine();
            if (line == null) break;

            // The -1 means: don't discard trailing empty tokens
            String[] tokens = line.split("\\|", -1);

            String productId = tokens[columnNums.get("pid")];
            if (!productId.isEmpty() && parseInt(productId) != productIdFilter) continue;

            int assignmentId = parseInt( tokens[columnNums.get("id")] );
            String fcode = tokens[columnNums.get("fcode")];
            String delineation = featureDelinCodes.get( parseInt( tokens[columnNums.get("delin")] ) );
            String coverageType = tokens[columnNums.get("cov")];

            DncGeosymAttributeExpression attrExpr = attrExprs.get(assignmentId);
            if (attrExpr == null) attrExpr = alwaysTrue;

            String pointSymbolId = tokens[columnNums.get("pointsym")];
            String lineSymbolId = tokens[columnNums.get("linesym")];
            String areaSymbolId = tokens[columnNums.get("areasym")];
            int displayPriority = parseInt( tokens[columnNums.get("dispri")] );

            String orientationAttr = tokens[columnNums.get("orient")];

            String[] labelAttrs = tokens[columnNums.get("labatt")].split(",", -1);
            String[] labelJoinIds = tokens[columnNums.get("txrowid")].split(",", -1);
            List<DncGeosymLabelMaker> labelMakers = newArrayList();
            for (int i = 0; i < labelAttrs.length; i++)
            {
                String labelAttr = labelAttrs[i];
                if (labelAttr.isEmpty()) continue;

                DncGeosymLabelJoin labelJoin = labelJoins.get( parseIntOrFallback( labelJoinIds[i], -1 ) );
                if (labelJoin.labelLocation == appendToPrevious)
                {
                    DncGeosymLabelMaker previous = labelMakers.remove( labelMakers.size() - 1 );
                    labelMakers.add( previous.with( labelAttr, labelJoin.textStyle ) );
                }
                else
                {
                    labelMakers.add( new DncGeosymLabelMaker( labelAttr, labelJoin.textStyle, labelJoin.labelLocation ) );
                }
            }

            DncGeosymAssignment assignment = new DncGeosymAssignment(assignmentId, fcode, delineation, coverageType, attrExpr, pointSymbolId, lineSymbolId, areaSymbolId, displayPriority, orientationAttr, labelMakers);
            assignments.put(assignmentId, assignment);
        }
        return assignments;
    }

    public static class DncGeosymLabelJoin
    {
        public final DncGeosymTextStyle textStyle;
        public final DncGeosymLabelLocation labelLocation;

        public DncGeosymLabelJoin(DncGeosymTextStyle textStyle, DncGeosymLabelLocation labelLocation)
        {
            this.textStyle = textStyle;
            this.labelLocation = labelLocation;
        }
    }

    public static Int2ObjectMap<DncGeosymLabelJoin> readGeosymLabelJoins(BufferedReader reader, Int2ObjectMap<DncGeosymTextStyle> textStyles, Int2ObjectMap<DncGeosymLabelLocation> labelLocations) throws IOException
    {
        Int2ObjectMap<DncGeosymLabelJoin> joins = new Int2ObjectOpenHashMap<DncGeosymLabelJoin>();
        Object2IntMap<String> columnNums = readSymbolAssignmentHeader(reader);
        while (true)
        {
            String line = reader.readLine();
            if (line == null) break;

            // The -1 means: don't discard trailing empty tokens
            String[] tokens = line.split("\\|", -1);

            int joinId = parseInt( tokens[columnNums.get("id")] );
            DncGeosymTextStyle textStyle = textStyles.get( parseInt( tokens[columnNums.get("textcharid")] ) );
            DncGeosymLabelLocation labelLocation = labelLocations.get( parseInt( tokens[columnNums.get("textlocid")] ) );

            DncGeosymLabelJoin join = new DncGeosymLabelJoin(textStyle, labelLocation);
            joins.put(joinId, join);
        }
        return joins;
    }

    public static Int2ObjectMap<Color> readGeosymColors(String location) throws IOException
    {
        BufferedReader reader = null;
        try
        {
            reader = resourceReader(location);
            return readGeosymColors(reader);
        }
        finally
        {
            if (reader != null) reader.close();
        }
    }

    public static Int2ObjectMap<Color> readGeosymColors(BufferedReader reader) throws IOException
    {
        Int2ObjectMap<Color> rgbas = new Int2ObjectOpenHashMap<Color>();
        Object2IntMap<String> columnNums = readSymbolAssignmentHeader(reader);
        while (true)
        {
            String line = reader.readLine();
            if (line == null) break;

            // The -1 means: don't discard trailing empty tokens
            String[] tokens = line.split("\\|", -1);

            int index = parseInt( tokens[columnNums.get("index")] );
            int r = parseInt( tokens[columnNums.get("red")] );
            int g = parseInt( tokens[columnNums.get("green")] );
            int b = parseInt( tokens[columnNums.get("blue")] );

            rgbas.put(index, new Color(r, g, b));
        }
        return rgbas;
    }

    public static Int2ObjectMap<DncGeosymTextStyle> readGeosymTextStyles(BufferedReader reader, Int2ObjectMap<String> fontNameCodes, Int2ObjectMap<String> fontStyleCodes, Int2ObjectMap<Int2ObjectMap<String>> textAbbrevs) throws IOException
    {
        Int2ObjectMap<DncGeosymTextStyle> styles = new Int2ObjectOpenHashMap<DncGeosymTextStyle>();
        Object2IntMap<String> columnNums = readSymbolAssignmentHeader(reader);
        while (true)
        {
            String line = reader.readLine();
            if (line == null) break;

            // The -1 means: don't discard trailing empty tokens
            String[] tokens = line.split("\\|", -1);

            int styleId = parseInt( tokens[columnNums.get("id")] );

            // XXX: Honor fontName and fontStyle
            //String fontName = fontNameCodes.get( parseInt( tokens[columnNums.get("tfont")] ) );
            //String fontStyle = fontStyleCodes.get( parseInt( tokens[columnNums.get("tstyle")] ) );
            float pointSize = parseFloat( tokens[columnNums.get("tsize")] );
            Font font = FontUtils.getDefaultPlain(pointSize);

            int colorId = parseInt( tokens[columnNums.get("tcolor")] );

            String prefix = parseHexCharOrFallback( tokens[columnNums.get("tprepend")], "" );
            String suffix = parseHexCharOrFallback( tokens[columnNums.get("tappend")], "" );
            Int2ObjectMap<String> abbrevs = textAbbrevs.get( parseIntOrFallback( tokens[columnNums.get("abindexid")], -1 ) );

            DncGeosymTextStyle style = new DncGeosymTextStyle(font, colorId, prefix, suffix, abbrevs);
            styles.put(styleId, style);
        }
        return styles;
    }

    public static Int2ObjectMap<Int2ObjectMap<String>> readGeosymTextAbbreviations(BufferedReader reader) throws IOException
    {
        while (true)
        {
            String line = reader.readLine();
            if (line == null) break;
            if (line.trim().equals(";")) break;
        }

        Pattern beginBlockPattern = Pattern.compile("^[0-9]+:$");

        Int2ObjectMap<Int2ObjectMap<String>> abbrevs = new Int2ObjectOpenHashMap<Int2ObjectMap<String>>();
        Int2ObjectMap<String> currentAbbrevs = null;
        while (true)
        {
            String line = reader.readLine();
            if (line == null) break;

            if (beginBlockPattern.matcher(line).matches())
            {
                int newBlockNum = parseInt(line.split(":", -1)[0]);
                if (!abbrevs.containsKey(newBlockNum)) abbrevs.put(newBlockNum, new Int2ObjectOpenHashMap<String>());
                currentAbbrevs = abbrevs.get(newBlockNum);
            }
            else
            {
                // The -1 means: don't discard trailing empty tokens
                String[] tokens = line.split("\\|", -1);

                int id = parseInt(tokens[0]);
                String string = tokens[1];
                currentAbbrevs.put(id, string);
            }
        }
        return abbrevs;
    }

    public static Int2ObjectMap<DncGeosymLabelLocation> readGeosymLabelLocations(BufferedReader reader, Int2ObjectMap<String> labelJustifyCodes) throws IOException
    {
        Int2ObjectMap<DncGeosymLabelLocation> locations = new Int2ObjectOpenHashMap<DncGeosymLabelLocation>();
        locations.put(-1, appendToPrevious);

        Object2IntMap<String> columnNums = readSymbolAssignmentHeader(reader);
        while (true)
        {
            String line = reader.readLine();
            if (line == null) break;

            // The -1 means: don't discard trailing empty tokens
            String[] tokens = line.split("\\|", -1);

            int locationId = parseInt( tokens[columnNums.get("id")] );
            String justify = labelJustifyCodes.get( parseInt( tokens[columnNums.get("tjust")] ) );


            // XXX: Ugly
            int hAlign = SwingConstants.CENTER;
            int vAlign = SwingConstants.CENTER;
            boolean forSoundings = false;
            if (justify.equals("Sounding Text"))
            {
                forSoundings = true;
            }
            else
            {
                if (justify.startsWith("Bottom ")) vAlign = SwingConstants.BOTTOM;
                else if (justify.startsWith("Top ")) vAlign = SwingConstants.TOP;

                if (justify.endsWith(" Left")) hAlign = SwingConstants.LEFT;
                else if (justify.endsWith(" Right")) hAlign = SwingConstants.RIGHT;
            }


            double offsetDistance_MM = parseDouble( tokens[columnNums.get("tdist")] );
            double offsetDirection_RAD = degreesToRadians( parseDouble( tokens[columnNums.get("tdir")] ) );
            double xOffset_MM = offsetDistance_MM * sin(offsetDirection_RAD);
            double yOffset_MM = offsetDistance_MM * cos(offsetDirection_RAD);

            DncGeosymLabelLocation location = new DncGeosymLabelLocation(xOffset_MM, yOffset_MM, hAlign, vAlign, forSoundings);
            locations.put(locationId, location);
        }
        return locations;
    }

    public static Int2ObjectMap<DncGeosymAttributeExpression> readGeosymAttributeExpressions(BufferedReader reader, Int2ObjectMap<String> comparisonOpCodes, Int2ObjectMap<String> connectorCodes) throws IOException
    {
        int workingAssignmentId = -1;
        int recentSequenceNum = 0;
        List<String> workingConnectorOps = newArrayList();
        List<DncGeosymAttributeComparison> workingComparisons = newArrayList();

        Int2ObjectMap<DncGeosymAttributeExpression> expressions = new Int2ObjectOpenHashMap<DncGeosymAttributeExpression>();
        Object2IntMap<String> columnNums = readSymbolAssignmentHeader(reader);
        while (true)
        {
            String line = reader.readLine();
            if (line == null) break;

            // The -1 means: don't discard trailing empty tokens
            String[] tokens = line.split("\\|", -1);

            int assignmentId = parseInt( tokens[columnNums.get("cond_index")] );
            if (workingAssignmentId != -1 && assignmentId != workingAssignmentId) throw new RuntimeException("Unexpected row id: " + line);
            workingAssignmentId = assignmentId;

            int sequenceNum = parseInt( tokens[columnNums.get("seq")] );
            if (sequenceNum <= recentSequenceNum) throw new RuntimeException("Unexpected sequence number: " + line);
            recentSequenceNum = sequenceNum;

            String attr = tokens[columnNums.get("att")];
            String comparisonOp = comparisonOpCodes.get( parseInt( tokens[columnNums.get("oper")] ) );
            String comparisonValue = tokens[columnNums.get("value")];
            workingComparisons.add( new DncGeosymAttributeComparison( attr, comparisonOp, comparisonValue ) );

            String connectorOp = connectorCodes.get( parseInt( tokens[columnNums.get("connector")] ) );
            if (!"None".equals(connectorOp))
            {
                workingConnectorOps.add(connectorOp);
            }
            else
            {
                if (!workingComparisons.isEmpty())
                {
                    expressions.put( assignmentId, buildAttributeExpression(workingConnectorOps, workingComparisons) );
                }

                workingAssignmentId = -1;
                recentSequenceNum = 0;
                workingConnectorOps = newArrayList();
                workingComparisons = newArrayList();
            }
        }
        if (workingAssignmentId != -1) throw new RuntimeException();

        return expressions;
    }

    public static int findDncProductId(String contextFilename, List<DncGeosymCode> codes)
    {
        for (DncGeosymCode code : codes)
        {
            if (code.filename.equals(contextFilename) && code.attribute.equals("pid") && code.description.equals("DNC")) return code.value;
        }
        throw new RuntimeException("Could not find code for product DNC");
    }

    public static Int2ObjectMap<String> findFeatureDelineationCodes(String contextFilename, List<DncGeosymCode> codes)
    {
        Int2ObjectMap<String> delins = new Int2ObjectOpenHashMap<String>();
        for (DncGeosymCode code : codes)
        {
            if (code.filename.equals(contextFilename) && code.attribute.equals("delin"))
            {
                delins.put(code.value, code.description);
            }
        }
        return delins;
    }

    public static Int2ObjectMap<String> findFontNameCodes(List<DncGeosymCode> codes)
    {
        Int2ObjectMap<String> fonts = new Int2ObjectOpenHashMap<String>();
        for (DncGeosymCode code : codes)
        {
            if (code.filename.equals(geosymTextStylesFile) && code.attribute.equals("tfont"))
            {
                fonts.put(code.value, code.description);
            }
        }
        return fonts;
    }

    public static Int2ObjectMap<String> findFontStyleCodes(List<DncGeosymCode> codes)
    {
        Int2ObjectMap<String> styles = new Int2ObjectOpenHashMap<String>();
        for (DncGeosymCode code : codes)
        {
            if (code.filename.equals(geosymTextStylesFile) && code.attribute.equals("tstyle"))
            {
                styles.put(code.value, code.description);
            }
        }
        return styles;
    }

    public static Int2ObjectMap<String> findAttrComparisonCodes(List<DncGeosymCode> codes)
    {
        Int2ObjectMap<String> attrComparisons = new Int2ObjectOpenHashMap<String>();
        for (DncGeosymCode code : codes)
        {
            if (code.filename.equals(geosymAttrExprsFile) && code.attribute.equals("oper"))
            {
                attrComparisons.put(code.value, code.description);
            }
        }
        return attrComparisons;
    }

    public static Int2ObjectMap<String> findAttrExprConnectorCodes(List<DncGeosymCode> codes)
    {
        Int2ObjectMap<String> attrExprConnectors = new Int2ObjectOpenHashMap<String>();
        for (DncGeosymCode code : codes)
        {
            if (code.filename.equals(geosymAttrExprsFile) && code.attribute.equals("connector"))
            {
                attrExprConnectors.put(code.value, code.description);
            }
        }
        return attrExprConnectors;
    }

    public static Int2ObjectMap<String> findLabelJustifyCodes(List<DncGeosymCode> codes)
    {
        Int2ObjectMap<String> justifications = new Int2ObjectOpenHashMap<String>();
        for (DncGeosymCode code : codes)
        {
            if (code.filename.equals(geosymLabelLocationsFile) && code.attribute.equals("tjust"))
            {
                justifications.put(code.value, code.description);
            }
        }
        return justifications;
    }

    public static List<DncGeosymCode> readGeosymCodes(BufferedReader reader) throws IOException
    {
        Object2IntMap<String> columnNums = readSymbolAssignmentHeader(reader);
        List<DncGeosymCode> codes = newArrayList();
        while (true)
        {
            String line = reader.readLine();
            if (line == null) break;

            // The -1 means: don't discard trailing empty tokens
            String[] tokens = line.split("\\|", -1);

            String filename = tokens[columnNums.get("file")];
            String attribute = tokens[columnNums.get("attribute")];
            int value = parseInt( tokens[columnNums.get("value")] );
            String description = tokens[columnNums.get("description")];

            codes.add( new DncGeosymCode(filename, attribute, value, description) );
        }
        return codes;
    }

    public static String parseHexCharOrFallback(String string, String fallback)
    {
        try
        {
            return String.valueOf((char) parseInt(string, 16));
        }
        catch (NumberFormatException e)
        {
            return fallback;
        }
    }

    public static int parseIntOrFallback(String string, int fallback)
    {
        try
        {
            return parseInt(string);
        }
        catch (NumberFormatException e)
        {
            return fallback;
        }
    }

}
