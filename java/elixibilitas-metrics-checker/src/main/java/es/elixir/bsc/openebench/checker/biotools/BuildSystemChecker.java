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

package es.elixir.bsc.openebench.checker.biotools;

import es.bsc.inb.elixir.openebench.model.metrics.Build;
import es.bsc.inb.elixir.openebench.model.metrics.Metrics;
import es.bsc.inb.elixir.openebench.model.metrics.Project;
import es.bsc.inb.elixir.openebench.model.tools.Dependencies;
import es.bsc.inb.elixir.openebench.model.tools.Tool;
import es.elixir.bsc.openebench.checker.MetricsChecker;

/**
 * @author Dmitry Repchevsky
 */

public class BuildSystemChecker implements MetricsChecker {
    
    @Override
    public String getToolPath() {
        return "/dependencies/build_system";
    }
    
    @Override
    public String getMetricsPath() {
        return "/project/build/automated";
    }

    @Override
    public Boolean check(Tool tool, Metrics metrics) {
        Boolean bool = check(tool);
        Project project = metrics.getProject();
        Build build;
        if (project == null) {
            metrics.setProject(project = new Project());
            project.setBuild(build = new Build());
        } else {
            build = project.getBuild();
            if (build == null) {
                project.setBuild(build = new Build());
            }
        }
        build.setAutomated(bool);
        return bool;
    }
    
    private static Boolean check(Tool tool) {
        Dependencies dependencies = tool.getDependencies();
        if (dependencies == null) {
            return null;
        }
        
        final String buildSystem = dependencies.getBuildSystem();
        return buildSystem != null && buildSystem.length() > 0;
    }
}
