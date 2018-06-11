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

package es.elixir.bsc.openebench.biotools;

import es.elixir.bsc.openebench.model.tools.Tool;
import es.elixir.bsc.openebench.tools.OpenEBenchEndpoint;
import es.elixir.bsc.openebench.tools.OpenEBenchRepository;
import java.io.IOException;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The bio.tools data model importer.
 * 
 * @author Dmitry Repchevsky
 */

public class BiotoolsRepositoryImporter {
    
    private OpenEBenchRepository repository;
    
    public BiotoolsRepositoryImporter() {}

    public BiotoolsRepositoryImporter(String username, String password) {
        repository = new OpenEBenchRepository(username, password);
    }
    
    public void load() {
        
        boolean exception = false;
        
        final Set<URI> ids = new HashSet<>();
        
        BiotoolsRepositoryIterator iter = new BiotoolsRepositoryIterator();
        while (iter.hasNext()) {
            final Tool tool = iter.next();
            try {
                System.out.println("> PUT: " + tool.id);
                if (repository != null) {
                    repository.put(tool);
                }
                ids.add(tool.id);
            } catch (IOException ex) {
                exception = true;
                Logger.getLogger(BiotoolsRepositoryImporter.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        if (!exception) {
            for (Tool tool : OpenEBenchRepository.getTools().values()) {
                final Boolean deprecated = tool.getDepricated();

                final String id = tool.id.toString();
                if (!id.startsWith(OpenEBenchEndpoint.URI_BASE)) {
                    Logger.getLogger(BiotoolsRepositoryImporter.class.getName()).log(Level.WARNING, "dubious id: {0}", id);
                    continue;
                }
                if (id.regionMatches(OpenEBenchEndpoint.URI_BASE.length(), "biotools:", 0, 10)) {
                    try {
                        if (ids.contains(tool.id)) {
                            if (Boolean.TRUE.equals(deprecated)) {
                                tool.setDepricated(null);
                                if (repository != null) {
                                    repository.put(tool);
                                }
                            }
                        } else if (!Boolean.TRUE.equals(deprecated)) {
                            tool.setDepricated(true);

                            System.out.println("> DEPRICATE: " + tool.id);
                            if (repository != null) {
                                repository.patch(tool);
                            }
                        }
                    } catch (IOException ex) {
                        Logger.getLogger(BiotoolsRepositoryImporter.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
    }
}
