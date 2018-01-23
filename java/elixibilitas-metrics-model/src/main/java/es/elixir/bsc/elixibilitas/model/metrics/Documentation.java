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

public class Documentation {
    
    private Boolean api;
    private Boolean apiVersioned;
    private Boolean howto;
    private Boolean tutorial;
    private Boolean citation;
    
    @JsonbProperty("api")
    public Boolean getAPI() {
        return api;
    }

    @JsonbProperty("api")
    public void setAPI(Boolean api) {
        this.api = api;
    }
    
    @JsonbProperty("api_versioned")
    public Boolean getAPIVersioned() {
        return apiVersioned;
    }

    @JsonbProperty("api_versioned")
    public void setAPIVersioned(Boolean apiVersioned) {
        this.apiVersioned = apiVersioned;
    }

    @JsonbProperty("howto")
    public Boolean getHowTo() {
        return howto;
    }

    @JsonbProperty("howto")
    public void setHowTo(Boolean howto) {
        this.howto = howto;
    }
    
    @JsonbProperty("tutorial")
    public Boolean getTutorial() {
        return tutorial;
    }

    @JsonbProperty("tutorial")
    public void setTutorial(Boolean tutorial) {
        this.tutorial = tutorial;
    }
    
    @JsonbProperty("citation")
    public Boolean getCitationInstructions() {
        return citation;
    }

    @JsonbProperty("citation")
    public void setCitationInstructions(Boolean citation) {
        this.citation = citation;
    }
}
