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

public class Credit {

    private String name;
    private String email;
    private String type;
    private String role;
    private String orcid;
    private URI url;
    private String comment;

    @JsonbProperty("name")
    public String getName() {
        return name;
    }
    
    @JsonbProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    @JsonbProperty("email")
    public String getEmail() {
        return email;
    }
    
    @JsonbProperty("email")
    public void setEmail(String email) {
        this.email = email;
    }

    @JsonbProperty("type")
    public String getType() {
        return type;
    }
    
    @JsonbProperty("type")
    public void setType(String type) {
        this.type = type;
    }

    @JsonbProperty("role")
    public String getRole() {
        return role;
    }
    
    @JsonbProperty("role")
    public void setRole(String role) {
        this.role = role;
    }
    
    @JsonbProperty("orcid")
    public String getOrcid() {
        return orcid;
    }
    
    @JsonbProperty("orcid")
    public void setOrcid(String orcid) {
        this.orcid = orcid;
    }
    
    @JsonbProperty("url")
    public URI getUrl() {
        return url;
    }
    
    @JsonbProperty("url")
    public void setUrl(URI url) {
        this.url = url;
    }
    
    @JsonbProperty("comment")
    public String getComment() {
        return comment;
    }
    
    @JsonbProperty("comment")
    public void setComment(String comment) {
        this.comment = comment;
    }
}
