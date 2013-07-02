/*
 * Copyright (c) 2012, Metron, Inc.
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
package com.metsci.glimpse.dspl.util;

import static com.metsci.glimpse.util.GeneralUtils.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import com.metsci.glimpse.dspl.DsplParser;
import com.metsci.glimpse.dspl.schema.Concept;
import com.metsci.glimpse.dspl.schema.DataSet;
import com.metsci.glimpse.dspl.schema.Slice;
import com.metsci.glimpse.dspl.schema.SliceConceptRef;

public class DsplSliceUtils
{
    public static Concept getCompatibleMetric( Slice slice, String namespace, String local, boolean nullOnException ) throws JAXBException, IOException, DsplException
    {
        // get the dataset file and dspl parser associated with this slice
        DataSet dataset = slice.getDataSet( );
        DsplParser parser = dataset.getParser( );

        // get the canonical concept (with its hard-coded namespace and identifier)
        Concept canonicalConcept = parser.getConcept( namespace, local );

        if ( canonicalConcept == null )
        {
            throw new DsplException( "Could not load Metron canonical Concept %s from Namespace %s.", namespace, local );
        }

        // determine if the slice defines a concept which extends canonicalConcept
        Concept sliceConcept = slice.getCompatibleMetric( canonicalConcept );

        if ( sliceConcept == null )
        {
            if ( nullOnException )
                return null;
            else
                throw new DsplException( "Slice %s does not define a Dimension Concept which extends %s.", slice.getId( ), canonicalConcept.getId( ) );
        }

        return sliceConcept;
    }

    public static Concept getCompatibleDimension( Slice slice, String namespace, String local, boolean nullOnException ) throws JAXBException, IOException, DsplException
    {
        // get the dataset file and dspl parser associated with this slice
        DataSet dataset = slice.getDataSet( );
        DsplParser parser = dataset.getParser( );

        // get the canonical concept (with its hard-coded namespace and identifier)
        Concept canonicalConcept = parser.getConcept( namespace, local );

        if ( canonicalConcept == null )
        {
            throw new DsplException( "Could not load Metron canonical Concept %s from Namespace %s.", namespace, local );
        }

        // determine if the slice defines a concept which extends canonicalConcept
        Concept sliceConcept = slice.getCompatibleDimension( canonicalConcept );

        if ( sliceConcept == null )
        {
            if ( nullOnException )
                return null;
            else
                throw new DsplException( "Slice %s does not define a Dimension Concept which extends %s.", slice.getId( ), canonicalConcept.getId( ) );
        }

        return sliceConcept;
    }

    public static abstract class SlicePattern
    {
        public abstract boolean matches( Slice slice ) throws JAXBException, IOException, DsplException;

        public List<Slice> find( DataSet dataset ) throws JAXBException, IOException, DsplException
        {
            List<Slice> compatibleSlices = newArrayList( );

            if ( dataset.getSlices( ) != null ) for ( Slice slice : dataset.getSlices( ).getSlice( ) )
                if ( matches( slice ) ) compatibleSlices.add( slice );

            return compatibleSlices;
        }
    }

    public static class SimpleSlicePattern extends SlicePattern
    {
        private final List<ConceptPattern> dimensionPatterns;
        private final List<ConceptPattern> metricPatterns;

        public SimpleSlicePattern( List<ConceptPattern> dimensionPatterns, List<ConceptPattern> metricPatterns )
        {
            this.dimensionPatterns = new ArrayList<ConceptPattern>( dimensionPatterns );
            this.metricPatterns = new ArrayList<ConceptPattern>( metricPatterns );
        }

        @Override
        public boolean matches( Slice slice ) throws JAXBException, IOException, DsplException
        {
            // check for one and only one compatible dimension for each dimension pattern
            for ( ConceptPattern pattern : dimensionPatterns )
            {
                List<Concept> matches = pattern.findDimensions( slice );

                if ( matches.size( ) != 1 ) return false;
            }

            // check for one and only one compatible metric for each metric pattern
            for ( ConceptPattern pattern : metricPatterns )
            {
                List<Concept> matches = pattern.findMetrics( slice );

                if ( matches.size( ) != 1 ) return false;
            }

            return true;
        }
    }

    public static abstract class ConceptPattern
    {
        public abstract boolean matches( Concept concept );

        public List<Concept> findDimensions( Slice slice ) throws JAXBException, IOException, DsplException
        {
            List<Concept> matches = newArrayList( );
            List<SliceConceptRef> refs = slice.getDimension( );
            for ( SliceConceptRef ref : refs )
                if ( matches( ref.getConcept( ) ) ) matches.add( ref.getConcept( ) );

            return matches;
        }

        public List<Concept> findMetrics( Slice slice ) throws JAXBException, IOException, DsplException
        {
            List<Concept> matches = newArrayList( );
            List<SliceConceptRef> refs = slice.getMetric( );
            for ( SliceConceptRef ref : refs )
                if ( matches( ref.getConcept( ) ) ) matches.add( ref.getConcept( ) );

            return matches;
        }

        public Concept findDimension( Slice slice ) throws JAXBException, IOException, DsplException
        {
            List<Concept> matches = findDimensions( slice );

            if ( matches.size( ) != 1 ) return null;

            return matches.get( 0 );
        }

        public Concept findMetric( Slice slice ) throws JAXBException, IOException, DsplException
        {
            List<Concept> matches = findMetrics( slice );

            if ( matches.size( ) != 1 ) return null;

            return matches.get( 0 );
        }
    }

    public static class SimpleConceptPattern extends ConceptPattern
    {
        public final String namespace;
        public final String localId;

        public SimpleConceptPattern( Concept concept )
        {
            this.namespace = concept.getDataSet( ).getTargetNamespace( );
            this.localId = concept.getId( );
        }

        public SimpleConceptPattern( String namespace, String localId )
        {
            this.namespace = namespace;
            this.localId = localId;
        }

        @Override
        public boolean matches( Concept concept )
        {
            if ( idMatches( concept ) ) return true;

            Concept parentConcept = concept;
            while ( ( parentConcept = parentConcept.getParentConcept( ) ) != null )
                if ( idMatches( parentConcept ) ) return true;

            return false;
        }

        private boolean idMatches( Concept concept )
        {
            boolean namespaceMatch = concept.getDataSet( ).getTargetNamespace( ).contentEquals( namespace );
            boolean idMatch = concept.getId( ).contentEquals( localId );
            return namespaceMatch && idMatch;
        }

        @Override
        public String toString( )
        {
            return "[ namespace = " + namespace + ",  localId = " + localId + "]";
        }
    }
}
