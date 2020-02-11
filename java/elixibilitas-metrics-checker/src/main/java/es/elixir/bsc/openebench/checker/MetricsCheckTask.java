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
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Dmitry Repchevsky
 */

public class MetricsCheckTask implements Callable<Metrics> {

    private final Tool tool;
    private final Metrics metrics;
    
    public MetricsCheckTask(Tool tool, Metrics metrics) {
        this.tool = tool;
        this.metrics = metrics;
    }

    @Override
    public Metrics call() throws Exception {
        try {
            MetricsChecker.checkAll(tool, metrics);
        } catch(Exception ex) {
            Logger.getLogger(MetricsCheckTask.class.getName()).log(Level.WARNING, "error in metrics: " + tool.id.toString(), ex);
        }
        return metrics;
    }
}
