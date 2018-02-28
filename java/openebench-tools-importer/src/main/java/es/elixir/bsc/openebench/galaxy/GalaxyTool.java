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

package es.elixir.bsc.openebench.galaxy;

import es.elixir.bsc.openebench.bioconda.BiocondaPackage;
import es.elixir.bsc.openebench.bioconda.BiocondaRepository;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * @author Dmitry Repchevsky
 */

public class GalaxyTool {
    
    public final static String GITHUB = "https://raw.githubusercontent.com/galaxyproject/tools-iuc/master/tools/%s/%s.xml";
    
    public final String id;
    public final String name;
    public final String description;
    private String repository;
    private List<String> categories;
    private List<BiocondaPackage> requirements;
    private List<String> citations;

    private volatile Metadata metadata;
    
    public GalaxyTool(final String id, 
                      final String name,
                      final String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }
    
    public String getRepository() {
        return repository;
    }
    
    public void setRepository(String repository) {
        this.repository = repository;
    }
    
    public List<String> getCatgories() {
        return categories != null ? categories : Collections.EMPTY_LIST;
    }
    
    public List<String> getCitations() {
        return citations != null ? citations : Collections.EMPTY_LIST;
    }
    
    public List<BiocondaPackage> getRequirements() {
        return requirements != null ? requirements : Collections.EMPTY_LIST;
    }

    public String getId() {
        if (id == null) {
            return null;
        }
        
        final String[] nodes = id.split("/");
        
        return nodes.length >= 3 ? nodes[nodes.length - 3] : null;
    }
    
    public String getName() {
        if (id == null) {
            return null;
        }
        
        final String[] nodes = id.split("/");
        
        return nodes.length >= 2 ? nodes[nodes.length - 2] : null;
    }
    
    public String getVersion() {
        if (id == null) {
            return null;
        }

        final String[] nodes = id.split("/");
        return nodes.length >= 1 ? nodes[nodes.length - 1] : null;
    }
    
    public Metadata getMetadata() throws IOException {
        if (metadata == null) {
            synchronized(this) {
                if (metadata == null) {
                    metadata = load();
                }
            }
        }
        return metadata;
    }
    
    private Metadata load() throws IOException {
        final String name = getName();
        final URI uri = URI.create(String.format(GITHUB, name, name));
        
        XMLStreamReader reader = null;
        try (InputStream in = uri.toURL().openStream()) {
            reader = XMLInputFactory.newFactory().createXMLStreamReader(in);
            while (reader.hasNext()) {
                reader.next();
            }
        } catch (XMLStreamException ex) {
            Logger.getLogger(GalaxyTool.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (XMLStreamException ex) {
                    Logger.getLogger(GalaxyTool.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        
        return new Metadata();
    }
    
    public final static GalaxyTool read(JsonObject obj) {
        final String id = obj.getString("id", null);
        final String name = obj.getString("name", null);
        final String description = obj.getString("description", null);
        
        final GalaxyTool tool = new GalaxyTool(id, name, description);
        
        final JsonArray categories = obj.getJsonArray("category_ids");
        if (categories != null) {
            tool.categories = Collections.unmodifiableList(categories.stream()
                                             .filter(JsonString.class::isInstance)
                                             .map(JsonString.class::cast)
                                             .map(n -> n.getString())
                                             .collect(Collectors.toList()));
            
        }
        
        final JsonArray citations = obj.getJsonArray("citations");
        if (citations != null) {
            tool.citations = Collections.unmodifiableList(citations.stream()
                                             .filter(JsonString.class::isInstance)
                                             .map(JsonString.class::cast)
                                             .map(n -> n.getString())
                                             .collect(Collectors.toList()));
            
        }
        
        final JsonArray requirements = obj.getJsonArray("requirements");
        if (requirements != null) {
            tool.requirements = new ArrayList<>();
            final Iterator<JsonValue> iter = requirements.iterator();
            while (iter.hasNext()) {
                final JsonValue value = iter.next();
                if (value.getValueType() == ValueType.OBJECT) {
                    final JsonObject requirement = value.asJsonObject();
                    final String rname = requirement.getString("name", null);
                    final String rversion = requirement.getString("version", null);
                    if (rname != null && !rname.isEmpty() &&
                        rversion != null && !rversion.isEmpty()) {
                        final BiocondaPackage pack = BiocondaRepository.getPackage(rname, rversion);
                        if (pack != null) {
                            tool.requirements.add(pack);
                        } else {
                            //Logger.getLogger(GalaxyTool.class.getName()).info("No conda package found: " + rname + ":" + rversion);
                        }
                    }
                }
            }
        }
        
        return tool;
    }
    
    public static class Metadata {
        
    }
}
