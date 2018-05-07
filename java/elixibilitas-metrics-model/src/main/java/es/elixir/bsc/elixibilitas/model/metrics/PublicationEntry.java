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

package es.elixir.bsc.elixibilitas.model.metrics;

import java.util.ArrayList;
import java.util.List;
import javax.json.bind.annotation.JsonbProperty;

/**
 * @author Dmitry Repchevsky
 */

public class PublicationEntry {
    
    private String title;
    private int year;
    
    private int ref_count;
    private int cit_count;
    
    private List<NReferences> refs;
    private List<NCitations> citations;
    
    @JsonbProperty("title")
    public String getTitle() {
        return title;
    }

    @JsonbProperty("title")
    public void setTitle(String title) {
        this.title = title;
    }

    @JsonbProperty("year")
    public Integer getYear() {
        return year;
    }

    @JsonbProperty("year")
    public void setYear(Integer year) {
        this.year = year;
    }
    
    @JsonbProperty("ref_count")
    public Integer getReferencesCount() {
        return ref_count;
    }

    @JsonbProperty("ref_count")
    public void setReferencesCount(Integer ref_count) {
        this.ref_count = ref_count;
    }

    @JsonbProperty("cit_count")
    public Integer getCitationsCount() {
        return cit_count;
    }

    @JsonbProperty("cit_count")
    public void setCitationsCount(Integer cit_count) {
        this.cit_count = cit_count;
    }

    @JsonbProperty("refs")
    public List<NReferences> getReferences() {
        if (refs == null) {
            refs = new ArrayList<>();
        }
        return refs;
    }

    @JsonbProperty("refs")
    public void setReferences(List<NReferences> refs) {
        this.refs = refs;
    }
    
    @JsonbProperty("citations")
    public List<NCitations> getCitations() {
        if (citations == null) {
            citations = new ArrayList<>();
        }
        return citations;
    }

    @JsonbProperty("citations")
    public void setCitations(List<NCitations> citations) {
        this.citations = citations;
    }
}
