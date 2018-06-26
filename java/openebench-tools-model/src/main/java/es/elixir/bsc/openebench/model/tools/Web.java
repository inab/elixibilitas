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
import javax.json.bind.annotation.JsonbProperty;

/**
 * @author Dmitry Repchevsky
 */

public class Web {
    private URI homepage;
    private String copyright;
    private String license;
    
    @JsonbProperty("homepage")
    public URI getHomepage() {
        return homepage;
    }

    @JsonbProperty("homepage")
    public void setHomepage(URI homepage) {
        this.homepage = homepage;
    }
    
    @JsonbProperty("copyright")
    public String getCopyright() {
        return copyright;
    }

    @JsonbProperty("copyright")
    public void setCopyright(String copyright) {
        this.copyright = copyright;
    }
    
    @JsonbProperty("license")
    public String getLicense() {
        return license;
    }

    @JsonbProperty("license")
    public void setLicense(String license) {
        this.license = license;
    }
}
