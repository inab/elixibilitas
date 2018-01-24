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

import es.elixir.bsc.elixibilitas.model.metrics.IssueTracking;
import es.elixir.bsc.elixibilitas.model.metrics.Metrics;
import es.elixir.bsc.elixibilitas.model.metrics.Support;
import es.elixir.bsc.openebench.model.tools.Tool;
import es.elixir.bsc.openebench.checker.MetricsChecker;

/**
 * @author Dmitry Repchevsky
 */

public class IssueTrackerChecker implements MetricsChecker {

    @Override
    public String getToolPath() {
        return "/support/issue_tracker";
    }
    
    @Override
    public String getMetricsPath() {
        return "/support/issue_tracker";
    }

    @Override
    public Boolean check(Tool tool, Metrics metrics) {
        
        Boolean bool = check(tool);
        Support support = metrics.getSupport();
        if (Boolean.TRUE.equals(bool)) {
            if (support == null) {
                metrics.setSupport(support = new Support());
                support.setIssueTracking(new IssueTracking());
            } else if (support.getIssueTracking() == null) {
                support.setIssueTracking(new IssueTracking());
            }
        } else if (support != null) {
            support.setIssueTracking(null);
        }
        return bool;
    }
    
    private static Boolean check(Tool tool) {
        final es.elixir.bsc.openebench.model.tools.Support support = tool.getSupport();
        return support != null && support.getIssueTracker() != null;
    }
}
