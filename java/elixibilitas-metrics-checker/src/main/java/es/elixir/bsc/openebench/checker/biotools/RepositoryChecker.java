/**
 * *****************************************************************************
 * Copyright (C) 2020 ELIXIR ES, Spanish National Bioinformatics Institute (INB)
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

package es.elixir.bsc.openebench.checker.biotools;

import es.bsc.inb.elixir.openebench.model.metrics.Distribution;
import es.bsc.inb.elixir.openebench.model.metrics.Metrics;
import es.bsc.inb.elixir.openebench.model.metrics.Repository;
import es.bsc.inb.elixir.openebench.model.metrics.Sourcecode;
import es.bsc.inb.elixir.openebench.model.tools.Tool;
import es.elixir.bsc.openebench.checker.MetricsChecker;
import java.net.URI;
import java.util.List;

/**
 * @author Dmitry Repchevsky
 */

public class RepositoryChecker implements MetricsChecker {

    @Override
    public Boolean check(Tool tool, Metrics metrics) {
        
        Boolean bool = check(tool);
        Distribution distribution = metrics.getDistribution();
        if (Boolean.TRUE.equals(bool)) {
            if (distribution == null) {
                metrics.setDistribution(distribution = new Distribution());
            }
            Sourcecode sourcecode = distribution.getSourcecode();
            if (sourcecode == null) {
                distribution.setSourcecode(sourcecode = new Sourcecode());
            }
            Repository repository = sourcecode.getRepository();
            if (repository == null) {
                sourcecode.setRepository(new Repository());
            }
        } else if (distribution != null) {
            final Sourcecode sourcecode = distribution.getSourcecode();
            if (sourcecode != null) {
                sourcecode.setRepository(null);
            }
        }
        return bool;
    }
    
    private static Boolean check(Tool tool) {
        final List<URI> repositories = tool.getRepositories();
        return repositories != null && !repositories.isEmpty();
    }
}
