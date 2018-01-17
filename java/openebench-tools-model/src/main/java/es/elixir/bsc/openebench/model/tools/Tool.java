/**
 * *****************************************************************************
 * Copyright (C) 2017 ELIXIR ES, Spanish National Bioinformatics Institute (INB)
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

package es.elixir.bsc.openebench.model.tools;

import java.net.URI;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.json.bind.annotation.JsonbCreator;
import javax.json.bind.annotation.JsonbProperty;

/**
 * @author Dmitry Repchevsky
 */

public class Tool extends LD {
    
    private String name;
    private String version;
    private ZonedDateTime timestamp;
    
    private String xid;
    private List<String> altIDs;

    private URI homepage;
    private List<URI> repositories;
    private String brief;
    private String description;
    
    private String license;
    private String maturity;
    private String cost;
    
    private Dependencies dependencies;
    private Support support;
    private Community community;
    private Distributions distributions;
    private Documentation documentation;
    private Semantics semantics;
    private List<Publication> publications;
    private List<Contact> contacts;
    private List<Credit> credits;

    @JsonbCreator
    public Tool(@JsonbProperty("@id") URI id, @JsonbProperty("@type") String type) {
        super(id, type);
    }

    @JsonbProperty("name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    @JsonbProperty("version")
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @JsonbProperty("@timestamp")
    public ZonedDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(ZonedDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    @JsonbProperty("@xid")
    public String getExternalId() {
        return xid;
    }

    public void setExternalId(String xid) {
        this.xid = xid;
    }

    @JsonbProperty("alt_ids")
    public List<String> getAlternativeIDs() {
        if (altIDs == null) {
            altIDs = new ArrayList<>();
        }
        return altIDs;
    }
    
    @JsonbProperty("alt_ids")
    public void setAlternativeIDs(List<String> altIDs) {
        this.altIDs = altIDs;
    }

    @JsonbProperty("homepage")
    public URI getHomepage() {
        return homepage;
    }

    public void setHomepage(URI homepage) {
        this.homepage = homepage;
    }
    
    @JsonbProperty("repositories")
    public List<URI> getRepositories() {
        if (repositories == null) {
            repositories = new ArrayList<>();
        }
        return repositories;
    }

    @JsonbProperty("repositories")
    public void setRepositories(List<URI> repositories) {
        this.repositories = repositories;
    }

    @JsonbProperty("brief")
    public String getShortDescription() {
        return brief;
    }

    public void setShortDescription(String brief) {
        this.brief = brief;
    }

    @JsonbProperty("description")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @JsonbProperty("license")
    public String getLicense() {
        return license;
    }

    public void setLicense(String license) {
        this.license = license;
    }

    @JsonbProperty("maturity")
    public String getMaturity() {
        return maturity;
    }

    public void setMaturity(String maturity) {
        this.maturity = maturity;
    }

    @JsonbProperty("cost")
    public String getCost() {
        return cost;
    }

    public void setCost(String cost) {
        this.cost = cost;
    }

    @JsonbProperty("build")
    public Dependencies getDependencies() {
        return dependencies;
    }

    public void setDependencies(Dependencies dependencies) {
        this.dependencies = dependencies;
    }

    @JsonbProperty("support")
    public Support getSupport() {
        return support;
    }

    public void setSupport(Support support) {
        this.support = support;
    }

    @JsonbProperty("community")
    public Community getCommunity() {
        return community;
    }

    public void setCommunity(Community community) {
        this.community = community;
    }

    @JsonbProperty("distributions")
    public Distributions getDistributions() {
        return distributions;
    }

    public void setDistributions(Distributions distributions) {
        this.distributions = distributions;
    }

    @JsonbProperty("documentation")
    public Documentation getDocumentation() {
        return documentation;
    }

    public void setDocumentation(Documentation documentation) {
        this.documentation = documentation;
    }
    
    @JsonbProperty("semantics")
    public Semantics getSemantics() {
        return semantics;
    }

    public void setSemantics(Semantics semantics) {
        this.semantics = semantics;
    }

    @JsonbProperty("publications")
    public List<Publication> getPublications() {
        if (publications == null) {
            publications = new ArrayList<>();
        }
        return publications;
    }
    
    @JsonbProperty("publications")
    public void setPublications(List<Publication> publications) {
        this.publications = publications;
    }
    
    @JsonbProperty("contacts")
    public List<Contact> getContacts() {
        if (contacts == null) {
            contacts = new ArrayList<>();
        }
        return contacts;
    }

    @JsonbProperty("contacts")
    public void setContacts(List<Contact> contacts) {
        this.contacts = contacts;
    }

    @JsonbProperty("credits")
    public List<Credit> getCredits() {
        if (credits == null) {
            credits = new ArrayList<>();
        }
        return credits;
    }
    
    @JsonbProperty("credits")
    public void setCredits(List<Credit> credits) {
        this.credits = credits;
    }
}
