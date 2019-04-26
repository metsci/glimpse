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

import static com.google.common.base.Objects.equal;
import static java.lang.Double.parseDouble;
import static java.lang.Float.parseFloat;
import static java.lang.Integer.parseInt;
import static java.lang.Short.parseShort;

import java.util.function.Function;
import java.util.regex.Pattern;

public class DncGeosymAttributeComparison implements DncGeosymAttributeExpression
{

    public static boolean isNoValueUnparsed(String s) { return s.equals("NULL") || s.isEmpty(); }
    public static boolean isNoValueString(String v) { return v.isEmpty(); }
    public static boolean isNoValueDouble(double v) { return Double.isNaN(v); }
    public static boolean isNoValueFloat(float v) { return Float.isNaN(v); }
    public static boolean isNoValueInteger(int v) { return v == -2147483648; }
    public static boolean isNoValueShort(short v) { return v == -32768; }



    public final String lhsAttr;
    public final String comparisonOp;
    public final String rhsUnparsed;


    public DncGeosymAttributeComparison(String attr, String comparisonOp, String unparsedComparisonValue)
    {
        this.lhsAttr = attr;
        this.comparisonOp = comparisonOp;
        this.rhsUnparsed = unparsedComparisonValue;
    }

    @Override
    public boolean eval(Function<String,Object> featureAttrs, Function<String,Object> externalAttrs)
    {
        Object lhs = (isExternalAttr(lhsAttr) ? externalAttrs.apply(lhsAttr) : featureAttrs.apply(lhsAttr));

        if (lhs == null) return evalNull( comparisonOp, isRhsNull(rhsUnparsed, externalAttrs) );
        else if (lhs instanceof String) return evalString( (String)lhs, comparisonOp, parseRhsString(rhsUnparsed, externalAttrs) );
        else if (lhs instanceof Double) return evalDouble( (Double)lhs, comparisonOp, parseRhsDouble(rhsUnparsed, externalAttrs) );
        else if (lhs instanceof Float) return evalFloat( (Float)lhs, comparisonOp, parseRhsFloat(rhsUnparsed, externalAttrs) );
        else if (lhs instanceof Integer) return evalInteger( (Integer)lhs, comparisonOp, parseRhsInteger(rhsUnparsed, externalAttrs) );
        else if (lhs instanceof Short) return evalShort( (Short)lhs, comparisonOp, parseRhsShort(rhsUnparsed, externalAttrs) );
        else throw new RuntimeException("Unrecognized type of lhs: type = " + lhs.getClass().getName());
    }

    public static boolean evalNull(String comparisonOp, boolean isRhsNull)
    {
        if ("=".equals(comparisonOp)) return isRhsNull;
        else if ("<>".equals(comparisonOp)) return !isRhsNull;
        else return false;
    }

    public static boolean evalString(String lhs, String comparisonOp, String rhs)
    {
        if ("=".equals(comparisonOp)) return equal(lhs, rhs);
        else if ("<>".equals(comparisonOp)) return !equal(lhs, rhs);
        else throw new RuntimeException("Unrecognized string comparison operator: " + comparisonOp);
    }

    public static boolean evalDouble(Double lhs, String comparisonOp, Double rhs)
    {
        if ("=".equals(comparisonOp)) return equal(lhs, rhs);
        else if ("<>".equals(comparisonOp)) return !equal(lhs, rhs);
        else if ("<".equals(comparisonOp)) return lhs < rhs;
        else if (">".equals(comparisonOp)) return lhs > rhs;
        else if ("<=".equals(comparisonOp)) return lhs <= rhs;
        else if (">=".equals(comparisonOp)) return lhs >= rhs;
        else throw new RuntimeException("Unrecognized double comparison operator: " + comparisonOp);
    }

    public static boolean evalFloat(Float lhs, String comparisonOp, Float rhs)
    {
        if ("=".equals(comparisonOp)) return equal(lhs, rhs);
        else if ("<>".equals(comparisonOp)) return !equal(lhs, rhs);
        else if ("<".equals(comparisonOp)) return lhs < rhs;
        else if (">".equals(comparisonOp)) return lhs > rhs;
        else if ("<=".equals(comparisonOp)) return lhs <= rhs;
        else if (">=".equals(comparisonOp)) return lhs >= rhs;
        else throw new RuntimeException("Unrecognized float comparison operator: " + comparisonOp);
    }

    public static boolean evalInteger(Integer lhs, String comparisonOp, Integer rhs)
    {
        if ("=".equals(comparisonOp)) return equal(lhs, rhs);
        else if ("<>".equals(comparisonOp)) return !equal(lhs, rhs);
        else if ("<".equals(comparisonOp)) return lhs < rhs;
        else if (">".equals(comparisonOp)) return lhs > rhs;
        else if ("<=".equals(comparisonOp)) return lhs <= rhs;
        else if (">=".equals(comparisonOp)) return lhs >= rhs;
        else throw new RuntimeException("Unrecognized integer comparison operator: " + comparisonOp);
    }

    public static boolean evalShort(Short lhs, String comparisonOp, Short rhs)
    {
        if ("=".equals(comparisonOp)) return equal(lhs, rhs);
        else if ("<>".equals(comparisonOp)) return !equal(lhs, rhs);
        else if ("<".equals(comparisonOp)) return lhs < rhs;
        else if (">".equals(comparisonOp)) return lhs > rhs;
        else if ("<=".equals(comparisonOp)) return lhs <= rhs;
        else if (">=".equals(comparisonOp)) return lhs >= rhs;
        else throw new RuntimeException("Unrecognized short comparison operator: " + comparisonOp);
    }

    public static boolean isRhsNull(String s, Function<String,Object> externalAttrs)
    {
        if (isNoValueUnparsed(s))
        {
            return true;
        }
        else if (isExternalAttr(s))
        {
            return (externalAttrs.apply(s) == null);
        }
        else if (isNumber(s))
        {
            try
            {
                if (isNoValueDouble(parseDouble(s))) return true;
            }
            catch (NumberFormatException e)
            { }

            try
            {
                if (isNoValueFloat(parseFloat(s))) return true;
            }
            catch (NumberFormatException e)
            { }

            try
            {
                if (isNoValueInteger(parseInt(s))) return true;
            }
            catch (NumberFormatException e)
            { }

            try
            {
                if (isNoValueShort(parseShort(s))) return true;
            }
            catch (NumberFormatException e)
            { }

            return false;
        }
        else if (isString(s))
        {
            return isNoValueString(s);
        }
        else
        {
            throw new RuntimeException("Unrecognized type of rhs: rhs = " + s);
        }
    }

    public static String parseRhsString(String s, Function<String,Object> externalAttrs)
    {
        if (isNoValueUnparsed(s))
        {
            return null;
        }
        else if (isExternalAttr(s))
        {
            Object v = externalAttrs.apply(s);
            return (v instanceof String ? (String) v : null);
        }
        else
        {
            // Strip off the quotes
            String v = s.substring(1, s.length()-1);
            return (isNoValueString(v) ? null : v);
        }
    }

    public static Double parseRhsDouble(String s, Function<String,Object> externalAttrs)
    {
        if (isNoValueUnparsed(s))
        {
            return null;
        }
        else if (isExternalAttr(s))
        {
            Object v = externalAttrs.apply(s);
            return (v instanceof Number ? ((Number) v).doubleValue() : null);
        }
        else
        {
            double v = parseDouble(s);
            return (isNoValueDouble(v) ? null : v);
        }
    }

    public static Float parseRhsFloat(String s, Function<String,Object> externalAttrs)
    {
        if (isNoValueUnparsed(s))
        {
            return null;
        }
        else if (isExternalAttr(s))
        {
            Object v = externalAttrs.apply(s);
            return (v instanceof Number ? ((Number) v).floatValue() : null);
        }
        else
        {
            float v = parseFloat(s);
            return (isNoValueFloat(v) ? null : v);
        }
    }

    public static Integer parseRhsInteger(String s, Function<String,Object> externalAttrs)
    {
        if (isNoValueUnparsed(s))
        {
            return null;
        }
        else if (isExternalAttr(s))
        {
            Object v = externalAttrs.apply(s);
            return (v instanceof Number ? ((Number) v).intValue() : null);
        }
        else
        {
            int v = parseInt(s);
            return (isNoValueInteger(v) ? null : v);
        }
    }

    public static Short parseRhsShort(String s, Function<String,Object> externalAttrs)
    {
        if (isNoValueUnparsed(s))
        {
            return null;
        }
        else if (isExternalAttr(s))
        {
            Object v = externalAttrs.apply(s);
            return (v instanceof Number ? ((Number) v).shortValue() : null);
        }
        else
        {
            short v = parseShort(s);
            return (isNoValueShort(v) ? null : v);
        }
    }

    public static boolean isString(String s)
    {
        return (s.startsWith("\"") && s.endsWith("\""));
    }

    public static boolean isExternalAttr(String s)
    {
        return (!isNoValueUnparsed(s) && !isNumber(s) && !isString(s) && s.length() == 4);
    }

    public static boolean isNumber(String s)
    {
        return numberPattern.matcher(s).matches();
    }

    public static final Pattern numberPattern = Pattern.compile(numberRegex());

    /**
     * Regexp from the javadoc of {@link Double#valueOf(String)}.
     */
    public static String numberRegex()
    {
        final String Digits     = "(\\p{Digit}+)";

        final String HexDigits  = "(\\p{XDigit}+)";

        // An exponent is 'e' or 'E' followed by an optionally signed decimal integer
        final String Exp        = "[eE][+-]?"+Digits;

        return ("[\\x00-\\x20]*"+  // Optional leading "whitespace"
                "[+-]?(" +         // Optional sign character
                "NaN|" +           // "NaN" string
                "Infinity|" +      // "Infinity" string

                // A decimal floating-point string representing a finite positive
                // number without a leading sign has at most five basic pieces:
                // Digits . Digits ExponentPart FloatTypeSuffix
                //
                // Since this method allows integer-only strings as input
                // in addition to strings of floating-point literals, the
                // two sub-patterns below are simplifications of the grammar
                // productions from the Java Language Specification, 2nd
                // edition, section 3.10.2.

                // Digits ._opt Digits_opt ExponentPart_opt FloatTypeSuffix_opt
                "((("+Digits+"(\\.)?("+Digits+"?)("+Exp+")?)|"+

                // . Digits ExponentPart_opt FloatTypeSuffix_opt
                "(\\.("+Digits+")("+Exp+")?)|"+

                // Hexadecimal strings
                "((" +
                // 0[xX] HexDigits ._opt BinaryExponent FloatTypeSuffix_opt
                "(0[xX]" + HexDigits + "(\\.)?)|" +

                // 0[xX] HexDigits_opt . HexDigits BinaryExponent FloatTypeSuffix_opt
                "(0[xX]" + HexDigits + "?(\\.)" + HexDigits + ")" +

                ")[pP][+-]?" + Digits + "))" +
                "[fFdD]?))" +
                "[\\x00-\\x20]*");// Optional trailing "whitespace"
    }

    @Override
    public String toString()
    {
        return lhsAttr + comparisonOp + rhsUnparsed;
    }

}
