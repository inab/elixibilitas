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
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

/**
 * 
 * @author Dmitry Repchevsky
 */

@XmlType(name = "", propOrder = {"summary",
                                 "functions",
                                 "labels",
                                 "links",
                                 "downloads",
                                 "documentations",
                                 "publications",
                                 "credits"})
@XmlRootElement(name = "tool")
public class Tool {

    private Summary summary;
    private List<Function> functions;
    private Labels labels;
    private List<Link> links;
    private List<Download> downloads;
    private List<Documentation> documentations;
    private List<Publication> publications;
    private List<Credit> credits;


    @XmlTransient
    public String getId() {
        if (summary == null) {
            return null;
        }
        
        String currieID = summary.getCurieID();
        if (currieID == null) {
            final String toolID = summary.getToolID();
            currieID = "biotools:" + toolID;
        }
        final List<String> versions = summary.getVersions();
        if (versions == null || versions.isEmpty()) {
            return currieID;
        }
        
        return currieID + ":" + versions.get(0);
    }
    
    @XmlElement(required = true)
    public Summary getSummary() {
        return summary;
    }

    public void setSummary(Summary summary) {
        this.summary = summary;
    }

    @XmlElement(name = "function", required = true)
    public List<Function> getFunctions() {
        if (functions == null) {
            functions = new ArrayList<>();
        }
        return functions;
    }

    @XmlElement
    public Labels getLabels() {
        return labels;
    }

    public void setLabels(Labels labels) {
        this.labels = labels;
    }

    @XmlElement(name = "link")
    public List<Link> getLinks() {
        if (links == null) {
            links = new ArrayList<>();
        }
        return links;
    }

    @XmlElement(name = "download")
    public List<Download> getDownloads() {
        if (downloads == null) {
            downloads = new ArrayList<>();
        }
        return downloads;
    }

    @XmlElement(name = "documentation")
    public List<Documentation> getDocumentations() {
        if (documentations == null) {
            documentations = new ArrayList<>();
        }
        return documentations;
    }

    @XmlElement(name = "publication")
    public List<Publication> getPublications() {
        if (publications == null) {
            publications = new ArrayList<>();
        }
        return publications;
    }

    @XmlElement(name = "credit")
    public List<Credit> getCredits() {
        if (credits == null) {
            credits = new ArrayList<>();
        }
        return credits;
    }
}
