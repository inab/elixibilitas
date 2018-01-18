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

public class Sourcecode {

    private Boolean copyright;
    private Boolean license;
    private Boolean headers;
    private Boolean readme;
    private Boolean free;
    private Boolean publicAccess;
    private Repository repository;
    
    @JsonbProperty("copyright")
    public Boolean getCopyright() {
        return copyright;
    }

    public void setCopyright(Boolean copyright) {
        this.copyright = copyright;
    }
    
    @JsonbProperty("license")
    public Boolean getLicense() {
        return license;
    }

    public void setLicense(Boolean license) {
        this.license = license;
    }

    @JsonbProperty("license_headers")
    public Boolean getLicenseHeaders() {
        return headers;
    }

    @JsonbProperty("license_headers")
    public void setLicenseHeaders(Boolean headers) {
        this.headers = headers;
    }
    
    @JsonbProperty("readme")
    public Boolean getReadme() {
        return readme;
    }

    public void setReadme(Boolean readme) {
        this.readme = readme;
    }

    @JsonbProperty("free")
    public Boolean getFree() {
        return free;
    }

    public void setFree(Boolean free) {
        this.free = free;
    }

    @JsonbProperty("public")
    public Boolean getPublicAccess() {
        return publicAccess;
    }

    @JsonbProperty("public")
    public void setPublicAccess(Boolean publicAccess) {
        this.publicAccess = publicAccess;
    }
    
    @JsonbProperty("repository")
    public Repository getRepository() {
        return repository;
    }

    public void setRepository(Repository repository) {
        this.repository = repository;
    }
}
