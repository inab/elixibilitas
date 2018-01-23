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

public class Identity {

    private Boolean domain;
    private Boolean logo;
    private Boolean recognizability;
    private Boolean uniqueness;
    private Boolean trademark;

    @JsonbProperty("domain")
    public Boolean getDomain() {
        return domain;
    }

    @JsonbProperty("domain")
    public void setDomain(Boolean domain) {
        this.domain = domain;
    }

    @JsonbProperty("logo")
    public Boolean getLogo() {
        return logo;
    }

    @JsonbProperty("logo")
    public void setLogo(Boolean logo) {
        this.logo = logo;
    }

    @JsonbProperty("recognizability")
    public Boolean getRecognizability() {
        return recognizability;
    }

    @JsonbProperty("recognizability")
    public void setRecognizability(Boolean recognizability) {
        this.recognizability = recognizability;
    }

    @JsonbProperty("uniqueness")
    public Boolean getUniqueness() {
        return uniqueness;
    }

    @JsonbProperty("uniqueness")
    public void setUniqueness(Boolean uniqueness) {
        this.uniqueness = uniqueness;
    }

    @JsonbProperty("trademark")
    public Boolean getTrademark() {
        return trademark;
    }

    @JsonbProperty("trademark")
    public void setTrademark(Boolean trademark) {
        this.trademark = trademark;
    }
}
