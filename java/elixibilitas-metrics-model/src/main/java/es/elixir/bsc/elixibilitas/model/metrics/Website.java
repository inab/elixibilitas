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

import java.time.ZonedDateTime;
import javax.json.bind.annotation.JsonbProperty;

/**
 * @author Dmitry Repchevsky
 */

public class Website {

    private Integer operational;
    private Integer accessTime;
    private ZonedDateTime lastCheck;
    private Copyright copyright;
    private Boolean license;
    private Boolean resources;
    
    @JsonbProperty("operational")
    public Integer getOperational() {
        return operational;
    }

    public void setOperational(Integer operational) {
        this.operational = operational;
    }

    @JsonbProperty("access_time")
    public Integer getAccessTime() {
        return accessTime;
    }

    public void setAccessTime(Integer accessTime) {
        this.accessTime = accessTime;
    }
    
    @JsonbProperty("last_check")
    public ZonedDateTime getLastCheck() {
        return lastCheck;
    }

    public void setLastCheck(ZonedDateTime lastCheck) {
        this.lastCheck = lastCheck;
    }
    
    @JsonbProperty("copyright")
    public Copyright getCopyright() {
        return copyright;
    }

    public void setCopyright(Copyright copyright) {
        this.copyright = copyright;
    }
    
    @JsonbProperty("license")
    public Boolean getLicense() {
        return license;
    }

    public void setLicense(Boolean license) {
        this.license = license;
    }

    @JsonbProperty("resources")
    public Boolean getResources() {
        return resources;
    }

    public void setResources(Boolean resources) {
        this.resources = resources;
    }
}
