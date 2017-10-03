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
import javax.json.bind.annotation.JsonbProperty;

/**
 * @author Dmitry Repchevsky
 */

public class Semantics {
    private List<URI> topics;
    private List<URI> operations;
    private List<Datatype> inputs;
    private List<Datatype> outputs;
    
    @JsonbProperty("topics")
    public List<URI> getTopics() {
        if (topics == null) {
            topics = new ArrayList<>();
        }
        return topics;
    }

    @JsonbProperty("topics")
    public void setTopics(List<URI> topics) {
        this.topics = topics;
    }

    @JsonbProperty("operations")
    public List<URI> getOperations() {
        if (operations == null) {
            operations = new ArrayList<>();
        }
        return operations;
    }

    @JsonbProperty("operations")
    public void setOperations(List<URI> operations) {
        this.operations = operations;
    }

    @JsonbProperty("inputs")
    public List<Datatype> getInputs() {
        if (inputs == null) {
            inputs = new ArrayList<>();
        }
        return inputs;
    }

    @JsonbProperty("inputs")
    public void setInputs(List<Datatype> inputs) {
        this.inputs = inputs;
    }

    @JsonbProperty("outputs")
    public List<Datatype> getOutputs() {
        if (outputs == null) {
            outputs = new ArrayList<>();
        }
        return outputs;
    }
    
    @JsonbProperty("outputs")
    public void setOutputs(List<Datatype> outputs) {
        this.outputs = outputs;
    }
}
