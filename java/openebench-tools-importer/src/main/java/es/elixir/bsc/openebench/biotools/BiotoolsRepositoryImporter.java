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

import com.mongodb.MongoClient;

import es.elixir.bsc.elixibilitas.dao.ToolsDAO;
import es.elixir.bsc.openebench.model.tools.Tool;
import es.elixir.bsc.openebench.tools.OpenEBenchRepository;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;

/**
 * The bio.tools data model importer.
 * 
 * @author Dmitry Repchevsky
 */

public class BiotoolsRepositoryImporter {
    
    public static void main(String[] args) {
        new BiotoolsRepositoryImporter().load(new MongoClient("localhost"));
    }
    
    public void load(MongoClient mc) {
        
        final ToolsDAO dao = new ToolsDAO(mc.getDatabase("elixibilitas"), OpenEBenchRepository.URI_BASE);
        final Set<URI> ids = new HashSet<>();
        
        BiotoolsRepositoryIterator iter = new BiotoolsRepositoryIterator();
        while (iter.hasNext()) {
            final Tool tool = iter.next();
            dao.put("bio.tools", tool);
            ids.add(tool.id);
        }

        for (Tool tool : dao.get()) {
            final Boolean deprecated = tool.getDepricated();
            if (ids.contains(tool.id)) {
                if (Boolean.TRUE.equals(deprecated)) {
                    tool.setDepricated(null);
                    dao.put ("bio.tools", tool);
                }
            } else if (!Boolean.TRUE.equals(deprecated)) {
                tool.setDepricated(true);

                System.out.println("----> " + tool.id);
                dao.update("bio.tools", tool, tool.id.toString().substring(dao.baseURI.length()));
            }
        }
        
    }
}
