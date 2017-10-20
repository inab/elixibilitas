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
import java.util.ArrayList;
import java.util.List;
import javax.json.bind.annotation.JsonbCreator;
import javax.json.bind.annotation.JsonbProperty;

/**
 * @author Dmitry Repchevsky
 */

public class CommandLineTool extends Tool {
    
    public final static String TYPE = "cmd";
    
    private String executable;
    private List<String> operatingSystems;

    @JsonbCreator
    public CommandLineTool(@JsonbProperty("@id") URI id) {
        super(id, TYPE);
    }
    
    @JsonbProperty("os")
    public List<String> getOperatingSystems() {
        if (operatingSystems == null) {
            operatingSystems = new ArrayList<>();
        }
        return operatingSystems;
    }

    @JsonbProperty("os")
    public void setOperatingSystems(List<String> operatingSystems) {
        this.operatingSystems = operatingSystems;
    }

    @JsonbProperty("executable")
    public String getExecutable() {
        return executable;
    }

    public void setExecutable(String executable) {
        this.executable = executable;
    }
}
