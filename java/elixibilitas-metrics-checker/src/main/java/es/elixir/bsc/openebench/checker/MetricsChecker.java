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

package es.elixir.bsc.openebench.checker;

import es.bsc.inb.elixir.openebench.model.metrics.Metrics;
import es.bsc.inb.elixir.openebench.model.tools.Tool;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Dmitry Repchevsky
 */

public interface MetricsChecker {
    /**
     * Sets the metrics value for the tool.
     * 
     * @param tool the tool for which metrics is calculated.
     * @param metrics metrics object to set the metrics
     * 
     * @return calculated metrics value
     */
    Boolean check(Tool tool, Metrics metrics);
    
    String getToolPath();
    String getMetricsPath();

    public static Map<String, MetricsChecker> checkers() {
        Map<String, MetricsChecker> checkers = new ConcurrentHashMap<>();
        
        ServiceLoader<MetricsChecker> loader = ServiceLoader.load(MetricsChecker.class);
        Iterator<MetricsChecker> iterator = loader.iterator();
        while(iterator.hasNext()) {
            MetricsChecker checker = iterator.next();
            checkers.put(checker.getMetricsPath(), checker);
        }
        
        return checkers;
    }
    
    public static void checkAll(Tool tool, Metrics metrics) {
        ServiceLoader<MetricsChecker> loader = ServiceLoader.load(MetricsChecker.class);
        Iterator<MetricsChecker> iterator = loader.iterator();
        while(iterator.hasNext()) {
            MetricsChecker checker = iterator.next();
            checker.check(tool, metrics);
        }
    }
}
