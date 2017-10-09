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

package es.elixir.bsc.elixibilitas.model.metrics;

import javax.json.bind.annotation.JsonbProperty;

/**
 * @author Dmitry Repchevsky
 */

public class Project {

    private Identity identity;
    private Website website;
    private Build build;
    private License license;
    private Documentation documentation;
    private Boolean description;
    private Boolean concept;
    private Boolean rationale;
    private Boolean architecture;
    private Boolean publications;
    private Boolean readme;

    @JsonbProperty("identity")
    public Identity getIdentity() {
        return identity;
    }

    public void setIdentity(Identity identity) {
        this.identity = identity;
    }

    @JsonbProperty("website")
    public Website getWebsite() {
        return website;
    }

    public void setWebsite(Website website) {
        this.website = website;
    }

    @JsonbProperty("build")
    public Build getBuild() {
        return build;
    }

    public void setBuild(Build build) {
        this.build = build;
    }
    
    @JsonbProperty("license")
    public License getLicense() {
        return license;
    }

    public void setLicense(License license) {
        this.license = license;
    }

    @JsonbProperty("documentation")
    public Documentation getDocumentation() {
        return documentation;
    }

    public void setDocumentation(Documentation documentation) {
        this.documentation = documentation;
    }

    @JsonbProperty("description")
    public Boolean getDescription() {
        return description;
    }

    public void setDescription(Boolean description) {
        this.description = description;
    }

    @JsonbProperty("concept")
    public Boolean getConcept() {
        return concept;
    }

    public void setConcept(Boolean concept) {
        this.concept = concept;
    }

    @JsonbProperty("rationale")
    public Boolean getRationale() {
        return rationale;
    }

    public void setRationale(Boolean rationale) {
        this.rationale = rationale;
    }

    @JsonbProperty("architecture")
    public Boolean getArchitecture() {
        return architecture;
    }

    public void setArchitecture(Boolean architecture) {
        this.architecture = architecture;
    }

    @JsonbProperty("publications")
    public Boolean getPublications() {
        return publications;
    }

    public void setPublications(Boolean publications) {
        this.publications = publications;
    }

    @JsonbProperty("readme")
    public Boolean getReadme() {
        return readme;
    }

    public void setReadme(Boolean readme) {
        this.readme = readme;
    }
}