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
package com.metsci.glimpse.dspl.util;

import java.io.IOException;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;

import com.metsci.glimpse.dspl.DsplParser;
import com.metsci.glimpse.dspl.schema.Concept;
import com.metsci.glimpse.dspl.schema.Concept.Type;
import com.metsci.glimpse.dspl.schema.ConceptInfo;
import com.metsci.glimpse.dspl.schema.DataSet;
import com.metsci.glimpse.dspl.schema.DataSet.Concepts;
import com.metsci.glimpse.dspl.schema.DataSet.Import;
import com.metsci.glimpse.dspl.schema.DataSet.Slices;
import com.metsci.glimpse.dspl.schema.DataSet.Tables;
import com.metsci.glimpse.dspl.schema.DataSet.Topics;
import com.metsci.glimpse.dspl.schema.DataType;
import com.metsci.glimpse.dspl.schema.Info;
import com.metsci.glimpse.dspl.schema.Slice;
import com.metsci.glimpse.dspl.schema.SliceConceptRef;
import com.metsci.glimpse.dspl.schema.Table;
import com.metsci.glimpse.dspl.schema.Value;
import com.metsci.glimpse.dspl.schema.Values;

public class DataSetFactory
{
    public static DataSet newDataset( String namespace, String... importNamespaces )
    {
        return newDataset( namespace, null, null, importNamespaces );
    }

    public static DataSet newDataset( String namespace, Info datasetInfo, Info providerInfo, String... importNamespaces )
    {
        DataSet dataset = new DataSet( );
        dataset.setTargetNamespace( namespace );

        if ( providerInfo != null ) dataset.setProvider( providerInfo );
        if ( datasetInfo != null ) dataset.setInfo( datasetInfo );

        List<Import> importList = dataset.getImport( );
        for ( String importNamespace : importNamespaces )
        {
            Import datasetImport = new Import( );
            datasetImport.setNamespace( importNamespace );
            importList.add( datasetImport );
        }

        Concepts concepts = new Concepts( );
        dataset.setConcepts( concepts );

        Slices slices = new Slices( );
        dataset.setSlices( slices );

        Tables tables = new Tables( );
        dataset.setTables( tables );

        Topics topics = new Topics( );
        dataset.setTopics( topics );

        return dataset;
    }

    public static void linkDataset( DsplParser parser, DataSet dataset ) throws JAXBException, IOException, DsplException
    {
        DsplHelper.linkDataset( parser, dataset );
    }

    public static Info newInfo( String name, String description, String url )
    {
        Info info = new Info( );

        if ( name != null ) info.setName( newValues( name ) );
        if ( description != null ) info.setDescription( newValues( description ) );
        if ( url != null ) info.setUrl( newValues( url ) );

        return info;
    }

    public static ConceptInfo newConceptInfo( String name, String description )
    {
        ConceptInfo info = new ConceptInfo( );

        if ( name != null ) info.setName( newValues( name ) );
        if ( description != null ) info.setDescription( newValues( description ) );

        return info;
    }

    public static ConceptInfo newConceptInfo( String name, String description, String url, String pluralName, String totalName, String singularNonCapitalizedName, String pluralNonCapitalizedName )
    {
        ConceptInfo info = new ConceptInfo( );

        if ( name != null ) info.setName( newValues( name ) );
        if ( description != null ) info.setDescription( newValues( description ) );
        if ( url != null ) info.setUrl( newValues( url ) );
        if ( pluralName != null ) info.setPluralName( newValues( pluralName ) );
        if ( totalName != null ) info.setTotalName( newValues( totalName ) );
        if ( singularNonCapitalizedName != null ) info.setSingularNonCapitalizedName( newValues( singularNonCapitalizedName ) );
        if ( pluralNonCapitalizedName != null ) info.setPluralNonCapitalizedName( newValues( pluralNonCapitalizedName ) );

        return info;
    }

    public static Values newValues( String value )
    {
        Values values = new Values( );
        List<Value> valueList = values.getValue( );

        Value valueTag = new Value( );
        valueTag.setLang( "en" );
        valueTag.setValue( value );
        valueList.add( valueTag );

        return values;
    }

    public static Concept newConcept( DataSet dataset, String id, DataType type )
    {
        return newConcept( dataset, id, type, null, null, null );
    }

    public static Concept newConcept( DataSet dataset, String id, DataType type, ConceptInfo info )
    {
        return newConcept( dataset, id, type, info, null, null );
    }

    public static Concept newConcept( DataSet dataset, String id, ConceptInfo info, String parentNamespace, String parentId )
    {
        return newConcept( dataset, id, null, info, parentNamespace, parentId );
    }

    public static Concept newConcept( DataSet dataset, String id, DataType type, ConceptInfo info, String parentNamespace, String parentId )
    {
        Concept concept = new Concept( );
        concept.setId( id );

        concept.setDataSet( dataset );

        if ( info != null )
        {
            concept.setInfo( info );
        }

        if ( type != null )
        {
            Type typeTag = new Type( );
            typeTag.setRef( type );
            concept.setType( typeTag );
        }

        if ( parentNamespace != null && parentId != null )
        {
            QName parentConcept = new QName( parentNamespace, parentId );
            concept.setExtends( parentConcept );
        }

        return concept;
    }

    public static Slice newSlice( String id, Concept[] dimensions, Concept[] metrics )
    {
        QName[] dimensionRefs = new QName[dimensions.length];
        for ( int i = 0; i < dimensions.length; i++ )
        {
            Concept dimension = dimensions[i];
            dimensionRefs[i] = new QName( dimension.getDataSet( ).getTargetNamespace( ), dimension.getId( ) );
        }

        QName[] metricRefs = new QName[metrics.length];
        for ( int i = 0; i < metrics.length; i++ )
        {
            Concept metric = metrics[i];
            metricRefs[i] = new QName( metric.getDataSet( ).getTargetNamespace( ), metric.getId( ) );
        }

        return newSlice( id, dimensionRefs, metricRefs );
    }

    public static Slice newSlice( String id, QName[] dimensions, QName[] metrics )
    {
        return newSlice( id, null, dimensions, metrics );
    }

    public static Slice newSlice( String id, Info info, QName[] dimensions, QName[] metrics )
    {
        Slice slice = new Slice( );
        slice.setId( id );

        if ( info == null ) slice.setInfo( info );

        List<SliceConceptRef> dimensionList = slice.getDimension( );
        for ( QName dimension : dimensions )
        {
            SliceConceptRef dimensionConceptRef = new SliceConceptRef( );
            dimensionConceptRef.setConceptRef( dimension );
            dimensionList.add( dimensionConceptRef );
        }

        List<SliceConceptRef> metricList = slice.getMetric( );
        for ( QName metric : metrics )
        {
            SliceConceptRef metricConceptRef = new SliceConceptRef( );
            metricConceptRef.setConceptRef( metric );
            metricList.add( metricConceptRef );
        }

        return slice;
    }

    public static QName newQName( String namespace, String id )
    {
        return new QName( namespace, id );
    }

    public static Table.Column newColumn( String id, DataType type )
    {
        Table.Column column = new Table.Column( );
        column.setId( id );
        column.setType( type );
        return column;
    }
}
