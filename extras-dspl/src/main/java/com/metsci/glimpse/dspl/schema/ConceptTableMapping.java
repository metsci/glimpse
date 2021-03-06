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
//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.3-hudson-jaxb-ri-2.2.3-3-
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a>
// Any modifications to this file will be lost upon recompilation of the source schema.
// Generated on: 2011.12.05 at 11:09:09 AM EST
//

package com.metsci.glimpse.dspl.schema;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 *
 *         A mapping to a table that provides data for a concept.
 *
 *
 * <p>Java class for ConceptTableMapping complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="ConceptTableMapping">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="mapConcept" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;attribute name="toColumn" use="required" type="{http://schemas.google.com/dspl/2010}LocalId" />
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="mapProperty" maxOccurs="unbounded" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;attribute name="ref" use="required" type="{http://schemas.google.com/dspl/2010}LocalId" />
 *                 &lt;attribute name="toColumn" use="required" type="{http://schemas.google.com/dspl/2010}LocalId" />
 *                 &lt;attribute ref="{http://www.w3.org/XML/1998/namespace}lang"/>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *       &lt;attribute name="ref" use="required" type="{http://schemas.google.com/dspl/2010}LocalId" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType( XmlAccessType.FIELD )
@XmlType( name = "ConceptTableMapping", propOrder = { "mapConcept", "mapProperty" } )
public class ConceptTableMapping
{

    protected ConceptTableMapping.MapConcept mapConcept;
    protected List<ConceptTableMapping.MapProperty> mapProperty;
    @XmlAttribute( name = "ref", required = true )
    @XmlJavaTypeAdapter( CollapsedStringAdapter.class )
    protected String ref;

    /**
     * Gets the value of the mapConcept property.
     *
     * @return
     *     possible object is
     *     {@link ConceptTableMapping.MapConcept }
     *
     */
    public ConceptTableMapping.MapConcept getMapConcept( )
    {
        return mapConcept;
    }

    /**
     * Sets the value of the mapConcept property.
     *
     * @param value
     *     allowed object is
     *     {@link ConceptTableMapping.MapConcept }
     *
     */
    public void setMapConcept( ConceptTableMapping.MapConcept value )
    {
        this.mapConcept = value;
    }

    /**
     * Gets the value of the mapProperty property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the mapProperty property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMapProperty().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ConceptTableMapping.MapProperty }
     *
     *
     */
    public List<ConceptTableMapping.MapProperty> getMapProperty( )
    {
        if ( mapProperty == null )
        {
            mapProperty = new ArrayList<ConceptTableMapping.MapProperty>( );
        }
        return this.mapProperty;
    }

    /**
     * Gets the value of the ref property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getRef( )
    {
        return ref;
    }

    /**
     * Sets the value of the ref property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setRef( String value )
    {
        this.ref = value;
    }

    /**
     * <p>Java class for anonymous complex type.
     *
     * <p>The following schema fragment specifies the expected content contained within this class.
     *
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;attribute name="toColumn" use="required" type="{http://schemas.google.com/dspl/2010}LocalId" />
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     *
     *
     */
    @XmlAccessorType( XmlAccessType.FIELD )
    @XmlType( name = "" )
    public static class MapConcept
    {

        @XmlAttribute( name = "toColumn", required = true )
        @XmlJavaTypeAdapter( CollapsedStringAdapter.class )
        protected String toColumn;

        /**
         * Gets the value of the toColumn property.
         *
         * @return
         *     possible object is
         *     {@link String }
         *
         */
        public String getToColumn( )
        {
            return toColumn;
        }

        /**
         * Sets the value of the toColumn property.
         *
         * @param value
         *     allowed object is
         *     {@link String }
         *
         */
        public void setToColumn( String value )
        {
            this.toColumn = value;
        }

    }

    /**
     * <p>Java class for anonymous complex type.
     *
     * <p>The following schema fragment specifies the expected content contained within this class.
     *
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;attribute name="ref" use="required" type="{http://schemas.google.com/dspl/2010}LocalId" />
     *       &lt;attribute name="toColumn" use="required" type="{http://schemas.google.com/dspl/2010}LocalId" />
     *       &lt;attribute ref="{http://www.w3.org/XML/1998/namespace}lang"/>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     *
     *
     */
    @XmlAccessorType( XmlAccessType.FIELD )
    @XmlType( name = "" )
    public static class MapProperty
    {

        @XmlAttribute( name = "ref", required = true )
        @XmlJavaTypeAdapter( CollapsedStringAdapter.class )
        protected String ref;
        @XmlAttribute( name = "toColumn", required = true )
        @XmlJavaTypeAdapter( CollapsedStringAdapter.class )
        protected String toColumn;
        @XmlAttribute( name = "lang", namespace = "http://www.w3.org/XML/1998/namespace" )
        @XmlJavaTypeAdapter( CollapsedStringAdapter.class )
        @XmlSchemaType( name = "language" )
        protected String lang;

        /**
         * Gets the value of the ref property.
         *
         * @return
         *     possible object is
         *     {@link String }
         *
         */
        public String getRef( )
        {
            return ref;
        }

        /**
         * Sets the value of the ref property.
         *
         * @param value
         *     allowed object is
         *     {@link String }
         *
         */
        public void setRef( String value )
        {
            this.ref = value;
        }

        /**
         * Gets the value of the toColumn property.
         *
         * @return
         *     possible object is
         *     {@link String }
         *
         */
        public String getToColumn( )
        {
            return toColumn;
        }

        /**
         * Sets the value of the toColumn property.
         *
         * @param value
         *     allowed object is
         *     {@link String }
         *
         */
        public void setToColumn( String value )
        {
            this.toColumn = value;
        }

        /**
         *
         *                 The language/locale of the values in the mapped column.
         *                 See [BCP 47] for possible values of the xml:lang
         *                 attribute.
         *
         *
         * @return
         *     possible object is
         *     {@link String }
         *
         */
        public String getLang( )
        {
            return lang;
        }

        /**
         * Sets the value of the lang property.
         *
         * @param value
         *     allowed object is
         *     {@link String }
         *
         */
        public void setLang( String value )
        {
            this.lang = value;
        }

    }

}
