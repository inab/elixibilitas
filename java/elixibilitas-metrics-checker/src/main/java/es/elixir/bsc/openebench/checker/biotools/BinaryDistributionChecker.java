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

package es.elixir.bsc.openebench.checker.biotools;

import es.elixir.bsc.elixibilitas.model.metrics.Binaries;
import es.elixir.bsc.elixibilitas.model.metrics.Distribution;
import es.elixir.bsc.elixibilitas.model.metrics.Metrics;
import es.elixir.bsc.openebench.model.tools.Tool;
import es.elixir.bsc.openebench.checker.MetricsChecker;
import java.net.URI;
import java.util.List;

/**
 * @author Dmitry Repchevsky
 */

public class BinaryDistributionChecker implements MetricsChecker {

    @Override
    public Boolean check(Tool tool, Metrics metrics) {
        Boolean bool = check(tool);
        if (Boolean.TRUE.equals(bool)) {
            Distribution distribution = metrics.getDistribution();
            if (distribution == null) {
                distribution = new Distribution();
                metrics.setDistribution(distribution);
                distribution.setBinaries(new Binaries());
            } else if (distribution.getSourcecode() == null) {
                distribution.setBinaries(new Binaries());
            }
        }
        return bool;
    }
    
    private static Boolean check(Tool tool) {
        
        es.elixir.bsc.openebench.model.tools.Distributions distributions = tool.getDistributions();
        if (distributions != null) {
            List<URI> binarie = distributions.getBinaryDistributions();
            List<URI> packages = distributions.getBinaryPackagesDistributions();

            return !(binarie.isEmpty() && packages.isEmpty());
        }
        return false;
    }
}
