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
package com.metsci.glimpse.dspl.canonical;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import com.metsci.glimpse.dspl.parser.column.TableColumn;
import com.metsci.glimpse.dspl.schema.Concept;
import com.metsci.glimpse.dspl.schema.DataSet;
import com.metsci.glimpse.dspl.util.DsplException;

public class Physical
{
    public final static String datasetPath = "src/main/resources/dspl/canonical/metron/physical_units.xml";

    public static final class PhysicalPropertyConcept
    {
        private PhysicalPropertyConcept( )
        {
        }

        public static final String name = "physical_property";
        public static final String propertyText = "property_text";
        public static final String isFundamental = "is_fundamental";

        public static final Concept get( DataSet dataset ) throws DsplException, JAXBException, IOException
        {
            return dataset.getConcept( name );
        }

        public static TableColumn getValues( Concept concept ) throws DsplException, JAXBException, IOException
        {
            return concept.getTableData( ).getColumn( name );
        }

        public static TableColumn getPropretyText( Concept concept ) throws DsplException, JAXBException, IOException
        {
            return concept.getTableData( ).getColumn( propertyText );
        }

        public static TableColumn getIsFundamental( Concept concept ) throws DsplException, JAXBException, IOException
        {
            return concept.getTableData( ).getColumn( isFundamental );
        }
    }

    public static final class PhysicalUnitConcept
    {
        private PhysicalUnitConcept( )
        {
        }

        public static final String name = "physical_unit";
        public static final String physicalProperty = "physical_property";
        public static final String physicalUnitSystem = "physical_unit_system";
        public static final String unitText = "unit_text";
        public static final String unitTextSingular = "unit_text_singular";
        public static final String symbol = "symbol";
        public static final String symbolPosition = "symbol_position";

        public static final Concept get( DataSet dataset ) throws DsplException, JAXBException, IOException
        {
            return dataset.getConcept( name );
        }

        public static TableColumn getValues( Concept concept ) throws DsplException, JAXBException, IOException
        {
            return concept.getTableData( ).getColumn( name );
        }

        public static TableColumn getPhysicalProperty( Concept concept ) throws DsplException, JAXBException, IOException
        {
            return concept.getTableData( ).getColumn( physicalProperty );
        }

        public static TableColumn getPhysicalUnitSystem( Concept concept ) throws DsplException, JAXBException, IOException
        {
            return concept.getTableData( ).getColumn( physicalUnitSystem );
        }

        public static TableColumn getUnitText( Concept concept ) throws DsplException, JAXBException, IOException
        {
            return concept.getTableData( ).getColumn( unitText );
        }

        public static TableColumn getUnitTextSingular( Concept concept ) throws DsplException, JAXBException, IOException
        {
            return concept.getTableData( ).getColumn( unitTextSingular );
        }

        public static TableColumn getSymbol( Concept concept ) throws DsplException, JAXBException, IOException
        {
            return concept.getTableData( ).getColumn( symbol );
        }

        public static TableColumn getSymbolPosition( Concept concept ) throws DsplException, JAXBException, IOException
        {
            return concept.getTableData( ).getColumn( symbolPosition );
        }
    }

    public static final class PhysicalUnitSystemConcept
    {
        private PhysicalUnitSystemConcept( )
        {
        }

        public static final String name = "physical_unit_system";
        public static final String unitSystemText = "unit_system_text";

        public static final Concept get( DataSet dataset ) throws DsplException, JAXBException, IOException
        {
            return dataset.getConcept( name );
        }

        public static TableColumn getValues( Concept concept ) throws DsplException, JAXBException, IOException
        {
            return concept.getTableData( ).getColumn( name );
        }

        public static TableColumn getUnitSystemText( Concept concept ) throws DsplException, JAXBException, IOException
        {
            return concept.getTableData( ).getColumn( unitSystemText );
        }
    }

    public static final class PhysicalMagnitudeConcept
    {
        private PhysicalMagnitudeConcept( )
        {
        }

        public static final String name = "physical_magnitude";
        public static final String physicalUnit = "physical_unit";

        public static final Concept get( DataSet dataset ) throws DsplException, JAXBException, IOException
        {
            return dataset.getConcept( name );
        }

        public static TableColumn getValues( Concept concept ) throws DsplException, JAXBException, IOException
        {
            return concept.getTableData( ).getColumn( name );
        }

        public static TableColumn getPhysicalUnit( Concept concept ) throws DsplException, JAXBException, IOException
        {
            return concept.getTableData( ).getColumn( physicalUnit );
        }
    }

    public static final class FundamentalProperties
    {
        private FundamentalProperties( )
        {
        }

        public static final String length = "LENGTH";
        public static final String mass = "MASS";
        public static final String time = "TIME";
        public static final String current = "CURRENT";
        public static final String amount = "AMOUNT";
        public static final String luminousIntensity = "LUMINOUS_INTENSITY";
        public static final String planarAngle = "PLANAR_ANGLE";
        public static final String solidAngle = "SOLID_ANGLE";
    }

    public static class DerivedProperties
    {
        private DerivedProperties( )
        {
        }

        public static final String speed = "SPEED";
    }

    public static final class PhysicalUnits
    {
        private PhysicalUnits( )
        {
        }

        public static final String internationalKnots = "INTERNATIONAL_KNOTS";
        public static final String meters = "METERS";
        public static final String nauticalMiles = "INTERNATIONAL_NAUTICAL_MILES";
        public static final String seconds = "SECONDS";
        public static final String metersPerSecond = "METERS_PER_SECOND";
        public static final String knots = "KNOTS";
        public static final String radians = "RADIANS";
        public static final String steradians = "STERADIANS";
        public static final String degrees = "DEGREES";
    }
}
