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
package com.metsci.glimpse.util.units.time.format;

import static java.util.Arrays.asList;

import java.math.BigDecimal;
import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.metsci.glimpse.util.GeneralUtils;

/**
 * Formats and parses TimeStamps according to a specified format string, in the spirit of trusty
 * old printf. This is similar to {@link java.text.SimpleDateFormat}, but with a few improvements:
 * <ul>
 *     <li>Instances are thread-safe
 *     <li>Fractional seconds are handled more intuitively
 *     <li>Time-zone is associated with the format, not with the date/time object
 *     <li>Formatting options are specified printf-style (e.g. <code>%3S</code> instead of <code>SSS</code>)
 * </ul>
 *
 * The following format terms are defined:
 * <blockquote>
 * <table border=0 cellspacing=3 cellpadding=0>
 *     <tr bgcolor="#ccccff">
 *         <th align=left>Letter
 *         <th align=left>Description
 *     <tr>
 *         <td><code>y</code>
 *         <td>Year
 *     <tr>
 *         <td><code>M</code>
 *         <td>Month number (1-12)
 *     <tr>
 *         <td><code>N</code>
 *         <td>Month name
 *     <tr>
 *         <td><code>d</code>
 *         <td>Day of month (1-31)
 *     <tr>
 *         <td><code>j</code>
 *         <td>Day of year (1-366)
 *     <tr>
 *         <td><code>H</code>
 *         <td>Hour in day (0-23)
 *     <tr>
 *         <td><code>m</code>
 *         <td>Minute in hour (0-59)
 *     <tr>
 *         <td><code>S</code>
 *         <td>Second in minute (includes fractional part)
 *     <tr>
 *         <td><code>s</code>
 *         <td>Whole second in minute (floored toward zero)
 *     <tr>
 *         <td><code>z</code>
 *         <td>Timezone name
 * </table>
 * </blockquote>
 *
 * A good basic example is {@link TimeStampFormat.iso8601}:
 * <blockquote>
 * <table border=0 cellspacing=3 cellpadding=0>
 *     <tr bgcolor="#ccccff">
 *         <th align=left>Format
 *         <th align=left>Output
 *     <tr>
 *         <td><code>%y-%M-%dT%H:%m:%SZ</code>
 *         <td><code>2008-06-11T13:40:12.5439231Z</code>
 * </table>
 * </blockquote>
 *
 * Any characters not inside a format term -- such as the "T", "Z", dashes, and colons in the
 * above example -- will simply show up in as literals in the output string. Just like printf.
 * So if you want date and time separated by a space, and "UTC" instead of "Z":
 * <blockquote>
 * <table border=0 cellspacing=3 cellpadding=0>
 *     <tr bgcolor="#ccccff">
 *         <th align=left>Format
 *         <th align=left>Output
 *     <tr>
 *         <td><code>%y-%M-%d %H:%m:%S UTC</code>
 *         <td><code>2008-06-11 13:40:12.5439231 UTC</code>
 * </table>
 * </blockquote>
 *
 * To make text fields all uppercase or all lowercase, use <code>^</code> or <code>/</code>:
 * <blockquote>
 * <table border=0 cellspacing=3 cellpadding=0>
 *     <tr bgcolor="#ccccff">
 *         <th align=left>Format
 *         <th align=left>Output
 *     <tr>
 *         <td><code>%N</code>
 *         <td><code>January</code>
 *     <tr>
 *         <td><code>%^N</code>
 *         <td><code>JANUARY</code>
 *     <tr>
 *         <td><code>%/N</code>
 *         <td><code>january</code>
 * </table>
 * </blockquote>
 *
 * For a two-digit year (see note below), use <code>2</code>:
 * <blockquote>
 * <table border=0 cellspacing=3 cellpadding=0>
 *     <tr bgcolor="#ccccff">
 *         <th align=left>Format
 *         <th align=left>Output
 *     <tr>
 *         <td><code>%y</code>
 *         <td><code>2008</code>
 *     <tr>
 *         <td><code>%2y</code>
 *         <td><code>08</code>
 * </table>
 * </blockquote>
 *
 * To abbreviate the text month, use <code>3</code>:
 * <blockquote>
 * <table border=0 cellspacing=3 cellpadding=0>
 *     <tr bgcolor="#ccccff">
 *         <th align=left>Format
 *         <th align=left>Output
 *     <tr>
 *         <td><code>%N</code>
 *         <td><code>January</code>
 *     <tr>
 *         <td><code>%3N</code>
 *         <td><code>Jan</code>
 * </table>
 * </blockquote>
 *
 * To change padding of numeric fields, use <code>&lt;</code>, <code>&gt;</code>, or <code>!</code>:
 * <blockquote>
 * <table border=0 cellspacing=3 cellpadding=0>
 *     <tr bgcolor="#ccccff">
 *         <th align=left>Format
 *         <th align=left>Output
 *     <tr>
 *         <td><code>%H:%m</code>
 *         <td><code>04:10</code>
 *     <tr>
 *         <td><code>%&lt;H:%m</code>
 *         <td><code>4 :10</code>
 *     <tr>
 *         <td><code>%&gt;H:%m</code>
 *         <td><code>&nbsp;4:10</code>
 *     <tr>
 *         <td><code>%!H:%m</code>
 *         <td><code>4:10</code>
 * </table>
 * </blockquote>
 *
 * Specify the number of decimal places used by <code>%S</code> with a digit:
 * <blockquote>
 * <table border=0 cellspacing=3 cellpadding=0>
 *     <tr bgcolor="#ccccff">
 *         <th align=left>Format
 *         <th align=left>Output
 *     <tr>
 *         <td><code>%S</code>
 *         <td><code>12.5439231</code>
 *     <tr>
 *         <td><code>%3S</code>
 *         <td><code>12.544</code>
 * </table>
 * </blockquote>
 *
 *
 * Other features that would be nice:
 * <ul>
 *     <li>text day of week (<code>%E</code> --> <code>Tuesday</code>, <code>%3E</code> --> <code>Tue</code>)
 *     <li>hour in am/pm (<code>1-12</code>)
 *     <li>am/pm
 * </ul>
 *
 *
 * <h4>Two-Digit Years</h4>
 * <p>
 * When parsing a two-digit year, the result is shifted to fall between <code>baseYear</code> and
 * <code>baseYear+99</code>. The default base year is 1970. To explicitly specify a base year, use
 * one of the constructors that take a base-year argument.
 *
 *
 * <h4>Rollover and the %S Field</h4>
 * <p>
 * The <code>%S</code> field is special: its precision affects when other fields (potentially all
 * of them) roll over to the next value. For instance, for the time <code>1999/12/31 23:59:59.999</code>,
 * note how every field can change, solely because of a change in the precision of the <code>%S</code>
 * field.
 * <blockquote>
 * <table border=0 cellspacing=3 cellpadding=0>
 *     <tr bgcolor="#ccccff">
 *         <th align=left>Format
 *         <th align=left>Output
 *     <tr>
 *         <td><code>%y-%M-%d %H:%m:%S</code>
 *         <td><code>1999/12/31 23:59:59.999</code>
 *     <tr>
 *         <td><code>%y-%M-%d %H:%m:%0S</code>
 *         <td><code>2000/01/01 00:00:00</code>
 * </table>
 * </blockquote>
 * If a format string does not contain a <code>%S</code> field, other fields will behave as though
 * there were a full-precision <code>%S</code>.
 * <p>
 * If (for some strange reason) a format string contains multiple <code>%S</code> fields, the
 * precision of the rightmost one is used for figuring rollover.
 */
public class TimeStampFormatStandard implements TimeStampFormat
{
    private final ThreadLocal<Calendar> calendars;

    private final int baseForTwoDigitYears;

    private final Field[] fields;
    private final Pattern parsePattern;
    private final String formatString;
    private final int precision;

    public TimeStampFormatStandard( String format, String timeZoneName )
    {
        this( format, TimeZone.getTimeZone( timeZoneName ) );
    }

    public TimeStampFormatStandard( String format, String timeZoneName, int baseForTwoDigitYears )
    {
        this( format, TimeZone.getTimeZone( timeZoneName ), baseForTwoDigitYears );
    }

    public TimeStampFormatStandard( String format, final TimeZone timeZone )
    {
        this( format, timeZone, 1970 );
    }

    public TimeStampFormatStandard( String format, final TimeZone timeZone, int baseForTwoDigitYears )
    {
        calendars = new ThreadLocal<Calendar>( )
        {
            public Calendar initialValue( )
            {
                return Calendar.getInstance( timeZone );
            }
        };

        this.baseForTwoDigitYears = baseForTwoDigitYears;

        List<Field> fieldsList = new LinkedList<Field>( );
        StringBuilder patternBuilder = new StringBuilder( );
        StringBuilder formatBuilder = new StringBuilder( );
        int floatSecondsPrecision = -1;
        for ( int i = 0; i < format.length( ); )
        {
            char c = format.charAt( i );
            i++;

            if ( c != '%' )
            {
                formatBuilder.append( c );
                patternBuilder.append( c );
            }
            else if ( c == '%' && i < format.length( ) && format.charAt( i ) == '%' )
            {
                i++;
                formatBuilder.append( "%%" );
                patternBuilder.append( '%' );
            }
            else
            {
                char fieldCode = 0;
                StringBuilder fieldFlags = new StringBuilder( );
                while ( i < format.length( ) )
                {
                    char c2 = format.charAt( i );
                    i++;

                    if ( Character.isLetter( c2 ) )
                    {
                        fieldCode = c2;
                        break;
                    }
                    else
                    {
                        fieldFlags.append( c2 );
                    }
                }
                if ( fieldCode == 0 ) throw new IllegalArgumentException( "Unclosed format specifier: format = " + format );

                Field field = newField( fieldCode, fieldFlags.toString( ) );
                fieldsList.add( field );
                patternBuilder.append( field.getRegexSpecifier( ) );
                formatBuilder.append( field.getFormatSpecifier( ) );

                // See "Rollover and the %S Field" in the class comment
                if ( field instanceof FloatSecondField ) floatSecondsPrecision = ( ( FloatSecondField ) field ).precision;
            }
        }

        fields = fieldsList.toArray( new Field[0] );
        parsePattern = Pattern.compile( patternBuilder.toString( ) );
        formatString = formatBuilder.toString( );
        precision = floatSecondsPrecision;
    }

    public Field newField( char code, String flags )
    {
        switch ( code )
        {
            case 'y':
                return new YearField( flags, baseForTwoDigitYears );
            case 'M':
                return new NumericMonthField( flags );
            case 'N':
                return new TextMonthField( flags );
            case 'd':
                return new DayOfMonthField( flags );
            case 'j':
                return new DayOfYearField( flags );
            case 'H':
                return new Hour24Field( flags );
            case 'm':
                return new MinuteField( flags );
            case 's':
                return new FloorSecondField( flags );
            case 'S':
                return new FloatSecondField( flags );
            case 'z':
                return new TimeZoneField( flags );
            default:
                throw new IllegalArgumentException( "Unrecognized field code: " + code );
        }
    }

    @Override
    public BigDecimal parse( String string )
    {
        try
        {
            Matcher matcher = parsePattern.matcher( string );
            if ( !matcher.matches( ) ) throw new TimeStampParseException( string );

            Calendar calendar = calendars.get( );
            calendar.setTimeInMillis( 0 );

            // Calendar.setTimeZone() recomputes fields based on the new
            // timezone and the posix-millis time. So set the timezone
            // before setting the other fields.
            for ( int i = 0; i < fields.length; i++ )
            {
                if ( !isTimeZoneField( fields[i] ) ) continue;

                String fieldString = matcher.group( i + 1 );
                fields[i].putValue( fieldString, calendar );
            }

            BigDecimal offset = BigDecimal.ZERO;
            for ( int i = 0; i < fields.length; i++ )
            {
                if ( isTimeZoneField( fields[i] ) ) continue;

                String fieldString = matcher.group( i + 1 );
                BigDecimal remainder = fields[i].putValue( fieldString, calendar );
                offset = offset.add( remainder );
            }

            BigDecimal posixSeconds = BigDecimal.valueOf( calendar.getTimeInMillis( ), 3 );
            return posixSeconds.add( offset );
        }
        catch ( NumberFormatException e )
        {
            throw new TimeStampParseException( string, e );
        }
    }

    private static boolean isTimeZoneField( Field field )
    {
        return ( field instanceof TimeZoneField );
    }

    @Override
    public String format( BigDecimal posixSeconds )
    {
        // See "Rollover and the %S Field" in the class comment
        if ( precision >= 0 ) posixSeconds = posixSeconds.scaleByPowerOfTen( precision ).setScale( 0, BigDecimal.ROUND_HALF_UP ).scaleByPowerOfTen( -precision );

        Calendar calendar = calendars.get( );
        long posixMillis = posixSeconds.scaleByPowerOfTen( 3 ).setScale( 0, BigDecimal.ROUND_FLOOR ).longValue( );
        calendar.setTimeInMillis( posixMillis );

        Object[] fieldValues = new Object[fields.length];
        for ( int i = 0; i < fields.length; i++ )
            fieldValues[i] = fields[i].getValue( posixSeconds, calendar );

        return String.format( formatString, fieldValues );
    }

    private static interface Field
    {
        String getRegexSpecifier( );

        String getFormatSpecifier( );

        Object getValue( BigDecimal posixSeconds, Calendar calendar );

        /**
         * @throws NumberFormatException if valueString cannot be parsed
         */
        BigDecimal putValue( String valueString, Calendar calendar );
    }

    private static class CalendarField implements Field
    {
        protected final int calendarField;
        protected final int numDigits;
        protected final Padding padding;

        public CalendarField( int calendarField, int numDigits, Padding padding )
        {
            this.calendarField = calendarField;
            this.numDigits = numDigits;
            this.padding = padding;
        }

        public String getFormatSpecifier( )
        {
            switch ( padding )
            {
                case SPACES_ON_LEFT:
                    return "%" + numDigits + "d";
                case SPACES_ON_RIGHT:
                    return "%-" + numDigits + "d";
                case NONE:
                    return "%d";
                default:
                    return "%0" + numDigits + "d";
            }
        }

        public String getRegexSpecifier( )
        {
            switch ( padding )
            {
                case SPACES_ON_LEFT:
                    return "\\s*(\\d{1," + numDigits + "})";
                case SPACES_ON_RIGHT:
                    return "(\\d{1," + numDigits + "})\\s*";
                case NONE:
                    return "(\\d{1," + numDigits + "})";
                default:
                    return "(\\d{" + numDigits + "})";
            }
        }

        public Integer getValue( BigDecimal posixSeconds, Calendar calendar )
        {
            return calendar.get( calendarField );
        }

        public BigDecimal putValue( String valueString, Calendar calendar )
        {
            calendar.set( calendarField, Integer.parseInt( valueString ) );
            return BigDecimal.ZERO;
        }
    }

    private static class YearField extends CalendarField
    {
        private final boolean twoDigit;
        private final int baseForTwoDigitYears;

        public YearField( String flags, int baseForTwoDigitYears )
        {
            this( flags.contains( "2" ), baseForTwoDigitYears, getPadding( flags ) );
        }

        public YearField( boolean twoDigit, int baseForTwoDigitYears, Padding padding )
        {
            super( Calendar.YEAR, ( twoDigit ? 2 : 4 ), padding );
            this.twoDigit = twoDigit;
            this.baseForTwoDigitYears = baseForTwoDigitYears;
        }

        public Integer getValue( BigDecimal posixSeconds, Calendar calendar )
        {
            Integer fourDigit = super.getValue( posixSeconds, calendar );
            return ( twoDigit ? fourDigit % 100 : fourDigit );
        }

        public BigDecimal putValue( String valueString, Calendar calendar )
        {
            String fourDigit = ( twoDigit ? twoDigitYearToFour( valueString ) : valueString );
            return super.putValue( fourDigit, calendar );
        }

        private String twoDigitYearToFour( String s2 )
        {
            int n2 = Integer.parseInt( s2 );
            int n4 = ( ( baseForTwoDigitYears / 100 ) * 100 ) + n2;
            if ( n4 < baseForTwoDigitYears )
            {
                n4 += 100;
            }
            return Integer.toString( n4 );
        }
    }

    private static class NumericMonthField extends CalendarField
    {
        public NumericMonthField( String flags )
        {
            super( Calendar.MONTH, 2, getPadding( flags ) );
        }

        public Integer getValue( BigDecimal posixSeconds, Calendar calendar )
        {
            // Calendar uses 0 for January
            return calendar.get( calendarField ) + 1;
        }

        public BigDecimal putValue( String valueString, Calendar calendar )
        {
            // Calendar uses 0 for January
            calendar.set( calendarField, Integer.parseInt( valueString ) - 1 );
            return BigDecimal.ZERO;
        }
    }

    private static class DayOfMonthField extends CalendarField
    {
        public DayOfMonthField( String flags )
        {
            super( Calendar.DAY_OF_MONTH, 2, getPadding( flags ) );
        }
    }

    private static class DayOfYearField extends CalendarField
    {
        public DayOfYearField( String flags )
        {
            super( Calendar.DAY_OF_YEAR, 3, getPadding( flags ) );
        }
    }

    private static class Hour24Field extends CalendarField
    {
        public Hour24Field( String flags )
        {
            super( Calendar.HOUR_OF_DAY, 2, getPadding( flags ) );
        }
    }

    private static class MinuteField extends CalendarField
    {
        public MinuteField( String flags )
        {
            super( Calendar.MINUTE, 2, getPadding( flags ) );
        }
    }

    private static class FloorSecondField extends CalendarField
    {
        public FloorSecondField( String flags )
        {
            super( Calendar.SECOND, 2, getPadding( flags ) );
        }
    }

    private static class TextMonthField implements Field
    {
        private static final String[] longMonthNames;
        private static final String[] shortMonthNames;
        static
        {
            DateFormatSymbols symbols = new DateFormatSymbols( );
            longMonthNames = symbols.getMonths( );
            shortMonthNames = symbols.getShortMonths( );
        }

        private final boolean abbreviate;
        private final Case capitalization;

        public TextMonthField( String flags )
        {
            abbreviate = flags.contains( "3" );
            capitalization = getCase( flags );
        }

        public String getFormatSpecifier( )
        {
            return "%s";
        }

        public String getRegexSpecifier( )
        {
            return ( abbreviate ? "\\s*([a-zA-Z]{3})\\s*" : "\\s*([a-zA-Z]{3,})\\s*" );
        }

        public String getValue( BigDecimal posixSeconds, Calendar calendar )
        {
            int month = calendar.get( Calendar.MONTH );
            String monthName = ( abbreviate ? shortMonthNames[month] : longMonthNames[month] );
            switch ( capitalization )
            {
                case UPPERCASE:
                    return monthName.toUpperCase( );
                case LOWERCASE:
                    return monthName.toLowerCase( );
                default:
                    return monthName;
            }
        }

        public BigDecimal putValue( String valueString, Calendar calendar )
        {
            int month = -1;
            String[] monthNames = ( abbreviate ? shortMonthNames : longMonthNames );
            for ( int i = 0; i < monthNames.length; i++ )
            {
                if ( monthNames[i].equalsIgnoreCase( valueString ) )
                {
                    month = i;
                    break;
                }
            }
            calendar.set( Calendar.MONTH, month );

            return BigDecimal.ZERO;
        }
    }

    private static class TimeZoneField implements Field
    {
        private static final Set<String> recognizedTimeZoneIds = new HashSet<String>( asList( TimeZone.getAvailableIDs( ) ) );

        private final Case capitalization;

        public TimeZoneField( String flags )
        {
            capitalization = getCase( flags );
        }

        public String getFormatSpecifier( )
        {
            return "%s";
        }

        public String getRegexSpecifier( )
        {
            return "\\s*(\\S+)\\s*";
        }

        public String getValue( BigDecimal posixSeconds, Calendar calendar )
        {
            TimeZone tz = calendar.getTimeZone( );
            boolean daylightSavings = tz.inDaylightTime( calendar.getTime( ) );
            String tzString = tz.getDisplayName( daylightSavings, TimeZone.SHORT );
            switch ( capitalization )
            {
                case UPPERCASE:
                    return tzString.toUpperCase( );
                case LOWERCASE:
                    return tzString.toLowerCase( );
                default:
                    return tzString;
            }
        }

        public BigDecimal putValue( String valueString, Calendar calendar )
        {
            calendar.setTimeZone( getTimeZone( valueString ) );
            return BigDecimal.ZERO;
        }

        public static TimeZone getTimeZone( String id )
        {
            if ( !recognizedTimeZoneIds.contains( id ) ) throw new IllegalArgumentException( "Unrecognized time-zone id: " + id );
            return TimeZone.getTimeZone( id );
        }
    }

    private static class FloatSecondField implements Field
    {
        private static final BigDecimal ONE_HUNDRED = new BigDecimal( 100 );
        protected final int precision;

        public FloatSecondField( String flags )
        {
            precision = getDigit( flags, -1 );
        }

        public String getFormatSpecifier( )
        {
            return "%s";
        }

        public String getRegexSpecifier( )
        {
            // Greedily match up to precision digits after the decimal point,
            // then reluctantly match any number after that.
            boolean fullPrecision = ( precision < 0 );
            String greedyMax0 = ( fullPrecision ? "" : "" + precision );
            String greedyMax1 = ( fullPrecision ? "" : "" + Math.max( 1, precision ) );
            return "\\s*(\\d{1,2}(?:\\.\\d{0," + greedyMax0 + "}\\d*?)?|\\.\\d{1," + greedyMax1 + "}\\d*?)\\s*";
        }

        public String getValue( BigDecimal posixSeconds, Calendar calendar )
        {
            // Use Calendar to find the minute-floor, instead of doing seconds % 60.
            // This way we stay consistent with Calendar, and don't have to rely on
            // assumptions about its handling of leap seconds.
            long minuteFloorMillis = calendar.getTimeInMillis( ) - 1000 * calendar.get( Calendar.SECOND ) - calendar.get( Calendar.MILLISECOND );
            BigDecimal minuteFloorSeconds = BigDecimal.valueOf( minuteFloorMillis, 3 );
            BigDecimal seconds = GeneralUtils.stripTrailingZeros( posixSeconds.subtract( minuteFloorSeconds ) );

            if ( precision < 0 )
            {
                String string = seconds.toPlainString( );
                int numWholeDigits = string.indexOf( '.' );
                if ( numWholeDigits < 0 ) numWholeDigits = string.length( );
                switch ( numWholeDigits )
                {
                    case 0:
                        return "00" + string;
                    case 1:
                        return "0" + string;
                    default:
                        return string;
                }
            }
            else if ( seconds.compareTo( BigDecimal.ONE ) < 0 )
            {
                // Workaround for a Java bug:
                //
                //     String.format("%02.0", new BigDecimal("0.605"))
                //
                // should return "01", but as of 1.6.0_13, actually returns "0.605".
                // Similar things happen for any value in (-1, +1), except possibly
                // for zero.
                //
                // To work around, do String.format(100 + seconds), then chop off the
                // first character.
                //
                BigDecimal secondsPlus = seconds.add( ONE_HUNDRED );

                // 3 chars for the whole number, possibly 1 for the decimal point
                int width = precision + 3 + ( precision > 0 ? 1 : 0 );
                return String.format( "%0" + width + "." + precision + "f", secondsPlus ).substring( 1 );
            }
            else
            {
                // 2 chars for the whole number, possibly 1 for the decimal point
                int width = precision + 2 + ( precision > 0 ? 1 : 0 );
                return String.format( "%0" + width + "." + precision + "f", seconds );
            }
        }

        public BigDecimal putValue( String valueString, Calendar calendar )
        {
            if ( !isPlainDecimal( valueString ) ) throw new NumberFormatException( "Illegal seconds string: " + valueString );
            BigDecimal valueSeconds = new BigDecimal( valueString );

            BigDecimal calendarSecond = valueSeconds.setScale( 0, BigDecimal.ROUND_FLOOR );
            calendar.set( Calendar.SECOND, calendarSecond.intValue( ) );

            BigDecimal calendarMilli = valueSeconds.subtract( calendarSecond ).scaleByPowerOfTen( 3 ).setScale( 0, BigDecimal.ROUND_FLOOR );
            calendar.set( Calendar.MILLISECOND, calendarMilli.intValue( ) );

            BigDecimal remainderSeconds = valueSeconds.subtract( valueSeconds.setScale( 3, BigDecimal.ROUND_FLOOR ) );
            return remainderSeconds;
        }

        private static boolean isPlainDecimal( String string )
        {
            boolean seenDecimalPoint = false;
            for ( int i = 0; i < string.length( ); i++ )
            {
                char c = string.charAt( i );
                if ( !seenDecimalPoint && c == '.' )
                    seenDecimalPoint = true;
                else if ( !Character.isDigit( c ) ) return false;
            }
            return true;
        }
    }

    private static enum Padding
    {
        ZEROS, SPACES_ON_LEFT, SPACES_ON_RIGHT, NONE
    }

    private static Padding getPadding( String flags )
    {
        for ( int i = 0; i < flags.length( ); i++ )
        {
            switch ( flags.charAt( i ) )
            {
                case '<':
                    return Padding.SPACES_ON_RIGHT;
                case '>':
                    return Padding.SPACES_ON_LEFT;
                case '!':
                    return Padding.NONE;
            }
        }
        return Padding.ZEROS;
    }

    private static int getDigit( String flags, int defaultValue )
    {
        for ( int i = 0; i < flags.length( ); i++ )
        {
            // Parsing a digit to an integer should always succeed
            char c = flags.charAt( i );
            if ( Character.isDigit( c ) ) return Integer.parseInt( String.valueOf( c ) );
        }
        return defaultValue;
    }

    private static enum Case
    {
        NORMAL, LOWERCASE, UPPERCASE
    }

    private static Case getCase( String flags )
    {
        for ( int i = 0; i < flags.length( ); i++ )
        {
            switch ( flags.charAt( i ) )
            {
                case '^':
                    return Case.UPPERCASE;
                case '/':
                    return Case.LOWERCASE;
            }
        }
        return Case.NORMAL;
    }

}
