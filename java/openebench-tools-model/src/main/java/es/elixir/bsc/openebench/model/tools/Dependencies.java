/**
 * *****************************************************************************
 * Copyright (C) 2018 ELIXIR ES, Spanish National Bioinformatics Institute (INB)
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
import java.util.ArrayList;
import java.util.List;
import javax.json.bind.annotation.JsonbProperty;

/**
 * @author Dmitry Repchevsky
 */

public class Dependencies {
    private String buildSystem;
    private List<URI> build;
    private List<URI> runtime;
    
    @JsonbProperty("build_system")
    public String getBuildSystem() {
        return buildSystem;
    }
    
    @JsonbProperty("build_system")
    public void setBuildSystem(String buildSystem) {
        this.buildSystem = buildSystem;
    }
    
    @JsonbProperty("build")
    public List<URI> getBuildDependencies() {
        if (build == null) {
            build = new ArrayList<>();
        }
        return build;
    }
    
    @JsonbProperty("build")
    public void setBuildDependencies(List<URI> build) {
        this.build = build;
    }
    
    @JsonbProperty("runtime")
    public List<URI> getRuntimeDependencies() {
        if (runtime == null) {
            runtime = new ArrayList<>();
        }
        return runtime;
    }
    
    @JsonbProperty("runtime")
    public void setRuntimeDependencies(List<URI> runtime) {
        this.runtime = runtime;
    }

}