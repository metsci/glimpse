package com.metsci.glimpse.docking.xml;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType( name="Frame" )
public class FrameArrangement
{

    public int x = 50;
    public int y = 50;
    public int width = 800;
    public int height = 600;

    @XmlElement( name="docker" )
    public DockerArrangementNode dockerArr = null;

}
