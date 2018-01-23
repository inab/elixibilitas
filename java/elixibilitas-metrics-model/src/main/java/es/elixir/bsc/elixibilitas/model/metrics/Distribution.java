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

public class Distribution {

    private Boolean readme;
    private Binaries binaries;
    private Sourcecode sourcecode;
    private Boolean vre;
    private Boolean packages;

    @JsonbProperty("readme")
    public Boolean getReadme() {
        return readme;
    }

    @JsonbProperty("readme")
    public void setReadme(Boolean readme) {
        this.readme = readme;
    }

    @JsonbProperty("binaries")
    public Binaries getBinaries() {
        return binaries;
    }

    @JsonbProperty("binaries")
    public void setBinaries(Binaries binaries) {
        this.binaries = binaries;
    }

    @JsonbProperty("sourcecode")
    public Sourcecode getSourcecode() {
        return sourcecode;
    }

    @JsonbProperty("sourcecode")
    public void setSourcecode(Sourcecode sourcecode) {
        this.sourcecode = sourcecode;
    }
    
    @JsonbProperty("vre")
    public Boolean getVRE() {
        return vre;
    }

    @JsonbProperty("vre")
    public void setVRE(Boolean vre) {
        this.vre = vre;
    }
    
    @JsonbProperty("packages")
    public Boolean getBinaryPackages() {
        return packages;
    }

    @JsonbProperty("packages")
    public void setBinaryPackages(Boolean packages) {
        this.packages = packages;
    }
}
