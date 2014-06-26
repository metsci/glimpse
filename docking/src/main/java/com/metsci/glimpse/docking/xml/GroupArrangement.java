package com.metsci.glimpse.docking.xml;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement( name="group" )
@XmlType( name="Group" )
public class GroupArrangement
{

    @XmlElementWrapper( name="frames" )
    @XmlElement( name="frame" )
    public List<FrameArrangement> frameArrs = new ArrayList<>( );

}
