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
package com.metsci.glimpse.examples.dspl;

import java.io.IOException;

import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;

import com.metsci.glimpse.dspl.DsplParser;
import com.metsci.glimpse.dspl.schema.Concept;
import com.metsci.glimpse.dspl.schema.ConceptInfo;
import com.metsci.glimpse.dspl.schema.DataSet;
import com.metsci.glimpse.dspl.schema.Slice;
import com.metsci.glimpse.dspl.util.DsplException;
import com.metsci.glimpse.dspl.util.DsplHelper;

import static com.metsci.glimpse.dspl.util.DataSetFactory.*;

/**
 * XXX: Needs cleanup.
 *
 * @author ulman
 */
public class ProgrammaticDsplExample
{
    public static void main( String[] args ) throws JAXBException, IOException, DsplException
    {
        //// Create a Parser ////

        DsplParser parser = new DsplParser( );
        parser.setNetworkMode( false );

        //// Create a DataSet ////

        String namespace = "https://www.metsci.com/experimental";
        String[] imports = { "http://www.google.com/publicdata/dataset/google/entity", "http://www.google.com/publicdata/dataset/google/quantity" };

        DataSet dataset = newDataset( namespace, newInfo( "test", "test", null ), null, imports );

        //// Create a Concept ////

        ConceptInfo dimensionConceptInfo = newConceptInfo( "Dimension Concept", null );
        Concept dimensionConcept = newConcept( dataset, "dimension_concept", dimensionConceptInfo, "http://www.google.com/publicdata/dataset/google/entity", "entity" );

        dataset.getConcepts( ).getConcept( ).add( dimensionConcept );

        //// Create another Concept ////

        ConceptInfo metricConceptInfo = newConceptInfo( "Metric Concept", null );
        Concept metricConcept = newConcept( dataset, "metric_concept", metricConceptInfo, "http://www.google.com/publicdata/dataset/google/quantity", "magnitude" );

        dataset.getConcepts( ).getConcept( ).add( metricConcept );

        //// Create a Slice ////

        QName[] sliceDimensions = new QName[] { new QName( namespace, "dimension_concept" ) };
        QName[] sliceMetrics = new QName[] { new QName( namespace, "metric_concept" ) };
        Slice slice = newSlice( "experimental_slice", sliceDimensions, sliceMetrics );

        dataset.getSlices( ).getSlice( ).add( slice );

        //// Finalize Construction of the DataSet ////

        DsplHelper.linkDataset( parser, dataset );
    }
}
