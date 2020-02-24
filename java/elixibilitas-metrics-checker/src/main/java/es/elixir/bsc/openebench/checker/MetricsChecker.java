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

package es.elixir.bsc.openebench.checker;

import es.bsc.inb.elixir.openebench.model.metrics.Metrics;
import es.bsc.inb.elixir.openebench.model.tools.Tool;
import es.bsc.inb.elixir.openebench.repository.OpenEBenchEndpoint;
import es.elixir.bsc.elixibilitas.dao.MetricsDAO;
import es.elixir.bsc.elixibilitas.dao.ToolsDAO;
import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Dmitry Repchevsky
 */

public interface MetricsChecker {

    /**
     * Sets the metrics value for the tool.
     * 
     * @param toolsDAO
     * @param metricsDAO
     * @param tool the tool for which metrics is calculated.
     * @param metrics metrics object to set the metrics
     * 
     * @return calculated metrics value
     */
    Boolean check(ToolsDAO toolsDAO, MetricsDAO metricsDAO, Tool tool, Metrics metrics);
    
    public static void checkAll(ToolsDAO toolsDAO, MetricsDAO metricsDAO, Tool tool, Metrics metrics) {
            
        ServiceLoader<MetricsChecker> loader = ServiceLoader.load(MetricsChecker.class);
        Iterator<MetricsChecker> iterator = loader.iterator();
        while(iterator.hasNext()) {
            MetricsChecker checker = iterator.next();
            try {
                checker.check(toolsDAO, metricsDAO, tool, metrics);
            } catch (Exception ex) {
                Logger.getLogger(MetricsChecker.class.getName()).log(Level.SEVERE, "error in metrics: " + tool.id.toString(), ex);
            }
        }
    }
}
