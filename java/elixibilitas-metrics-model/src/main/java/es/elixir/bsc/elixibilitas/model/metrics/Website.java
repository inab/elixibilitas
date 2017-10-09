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
import java.util.ArrayList;
import java.util.List;
import javax.json.bind.annotation.JsonbProperty;

/**
 * @author Dmitry Repchevsky
 */

public class Website {

    private Boolean operational;
    private ZonedDateTime lastSeen;
    private List<ZonedDateTime> history;
    private Copyright copyright;
    private Boolean license;
    private Boolean resources;
    
    @JsonbProperty("operational")
    public Boolean getOperational() {
        return operational;
    }

    public void setOperational(Boolean operational) {
        this.operational = operational;
    }

    @JsonbProperty("last_seen")
    public ZonedDateTime getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(ZonedDateTime lastSeen) {
        this.lastSeen = lastSeen;
    }
    
    @JsonbProperty("history")
    public List<ZonedDateTime> getHistory() {
        if (history == null) {
            history = new ArrayList<>();
        }
        return history;
    }

    @JsonbProperty("history")
    public void setHistory(List<ZonedDateTime> history) {
        this.history = history;
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
