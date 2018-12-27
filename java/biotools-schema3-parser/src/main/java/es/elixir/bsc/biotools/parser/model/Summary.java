/**
 * *****************************************************************************
 * Copyright (C) 2016 ELIXIR ES, Spanish National Bioinformatics Institute (INB)
 * and Barcelona Supercomputing Center (BSC)
 *
 * Modifications to the initial code base are copyright of their respective
 * authors, or their employers as appropriate.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 *****************************************************************************
 */

package es.elixir.bsc.biotools.parser.model;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * 
 * @author Dmitry Repchevsky
 */

@XmlType(name = "", propOrder = {"name",
                                 "description",
                                 "homepage",
                                 "toolID",
                                 "curieID",
                                 "versions",
                                 "otherIDs"})
public class Summary {

    private String name;
    private String description;
    private String homepage;
    private String toolID;
    private String curieID;
    private List<String> versions;
    private List<ExternalId> otherIDs;

    @XmlElement(required = true)
    @XmlSchemaType(name = "nameType", namespace = "http://bio.tools")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @XmlElement(required = true)
    @XmlSchemaType(name = "textType", namespace = "http://bio.tools")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @XmlElement(required = true)
    @XmlSchemaType(name = "urlType", namespace = "http://bio.tools")
    public String getHomepage() {
        return homepage;
    }

    public void setHomepage(String homepage) {
        this.homepage = homepage;
    }

    @XmlElement(name = "biotoolsID", namespace = "http://bio.tools")
    @XmlSchemaType(name = "biotoolsIdType", namespace = "http://bio.tools")
    public String getToolID() {
        return toolID;
    }

    public void setToolID(String toolID) {
        this.toolID = toolID;
    }

    @XmlElement(name = "biotoolsCURIE", namespace = "http://bio.tools")
    @XmlSchemaType(name = "anyURI")
    public String getCurieID() {
        return curieID;
    }

    public void setCurieID(String curieID) {
        this.curieID = curieID;
    }
    
    @XmlElement(name = "version")
    @XmlSchemaType(name = "versionType", namespace = "http://bio.tools")
    public List<String> getVersions() {
        if (versions == null) {
            versions = new ArrayList<>();
        }
        return versions;
    }
    
    @XmlElement(name = "otherID")
    public List<ExternalId> getOtherIDs() {
        if (otherIDs == null) {
            otherIDs = new ArrayList<>();
        }
        return otherIDs;
    }
}
