package com.metsci.glimpse.docking.xml;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;

@XmlType( name="Tile" )
public class DockerArrangementTile extends DockerArrangementNode
{

    @XmlElementWrapper( name="views" )
    @XmlElement( name="view" )
    public List<String> viewIds = new ArrayList<>( );

    @XmlElement( name="selectedView" )
    public String selectedViewId = null;

    public boolean isMaximized = false;

}
