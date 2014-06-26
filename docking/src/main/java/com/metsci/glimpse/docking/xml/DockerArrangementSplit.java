package com.metsci.glimpse.docking.xml;

import javax.xml.bind.annotation.XmlType;

@XmlType( name="Split" )
public class DockerArrangementSplit extends DockerArrangementNode
{

    public boolean arrangeVertically = false;
    public double splitFrac = 0.5;
    public DockerArrangementNode childA = null;
    public DockerArrangementNode childB = null;

}
