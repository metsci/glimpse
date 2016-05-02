/*
 * Copyright (c) 2016, Metron, Inc.
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

import static com.metsci.glimpse.util.GeneralUtils.newArrayList;
import static com.metsci.glimpse.util.GeneralUtils.newUnmodifiableList;
import static com.metsci.glimpse.util.units.Length.metersToFeet;
import static java.lang.String.format;
import static java.util.Arrays.asList;

import java.util.List;

import com.google.common.base.Function;

public class DncGeosymLabelMaker
{

    public static class DncGeosymLabelMakerEntry
    {
        public final String attr;
        public final DncGeosymTextStyle textStyle;
        public final boolean forSoundings;

        public DncGeosymLabelMakerEntry(String attr, DncGeosymTextStyle textStyle, boolean forSoundings)
        {
            this.attr = attr;
            this.textStyle = textStyle;
            this.forSoundings = forSoundings;
        }

        public String getLabelText(Function<String,Object> lookupAttrValue)
        {
            String text = getLabelText( lookupAttrValue.apply( attr ), textStyle, forSoundings );
            return (text == null || text.isEmpty() ? null : text);
        }

        public static String getLabelText(Object labelAttrValue, DncGeosymTextStyle textStyle, boolean forSoundings)
        {
            if (labelAttrValue == null)
            {
                return null;
            }

            else if (labelAttrValue instanceof String)
            {
                String s = (String) labelAttrValue;
                if (s.isEmpty()) return null;
                if (s.equalsIgnoreCase("N/A")) return null;
                if (s.equalsIgnoreCase("None")) return null;
                if (s.equalsIgnoreCase("UNK")) return null;

                return textStyle.prefix + s + textStyle.suffix;
            }

            else if (forSoundings && labelAttrValue instanceof Number)
            {
                double sounding_FT = metersToFeet( ((Number) labelAttrValue).doubleValue() );
                return format("%s%.0f%s", textStyle.prefix, sounding_FT, textStyle.suffix);
            }

            else if (textStyle.abbrevs != null && labelAttrValue instanceof Number)
            {
                String s = textStyle.abbrevs.get( ((Number) labelAttrValue).intValue() );
                return textStyle.prefix + s + textStyle.suffix;
            }

            else
            {
                return textStyle.prefix + labelAttrValue.toString() + textStyle.suffix;
            }
        }
    }

    public static boolean forSoundings(DncGeosymLabelLocation labelLocation)
    {
        return (labelLocation != null && labelLocation.forSoundings);
    }

    public static DncGeosymLabelMakerEntry newEntry(String attr, DncGeosymTextStyle textStyle, DncGeosymLabelLocation labelLocation)
    {
        return newEntry(attr, textStyle, forSoundings(labelLocation));
    }

    public static DncGeosymLabelMakerEntry newEntry(String attr, DncGeosymTextStyle textStyle, boolean forSoundings)
    {
        return new DncGeosymLabelMakerEntry(attr, textStyle, forSoundings);
    }



    public final List<DncGeosymLabelMakerEntry> entries;
    public final DncGeosymLabelLocation labelLocation;


    public DncGeosymLabelMaker(String attr, DncGeosymTextStyle textStyle, DncGeosymLabelLocation labelLocation)
    {
        this( asList( newEntry(attr, textStyle, labelLocation) ), labelLocation );
    }

    public DncGeosymLabelMaker(List<DncGeosymLabelMakerEntry> entries, DncGeosymLabelLocation labelLocation)
    {
        this.entries = newUnmodifiableList(entries);
        this.labelLocation = labelLocation;
    }

    public DncGeosymLabelMaker with(String attr, DncGeosymTextStyle textStyle)
    {
        List<DncGeosymLabelMakerEntry> newEntries = newArrayList(entries);
        newEntries.add( newEntry(attr, textStyle, labelLocation) );
        return new DncGeosymLabelMaker(newEntries, labelLocation);
    }

}
