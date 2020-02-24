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

import es.bsc.inb.elixir.openebench.model.metrics.Deployment;
import es.bsc.inb.elixir.openebench.model.metrics.Metrics;
import es.bsc.inb.elixir.openebench.model.metrics.Project;
import es.bsc.inb.elixir.openebench.model.tools.Dependencies;
import es.bsc.inb.elixir.openebench.model.tools.Tool;
import es.elixir.bsc.elixibilitas.dao.MetricsDAO;
import es.elixir.bsc.elixibilitas.dao.ToolsDAO;
import es.elixir.bsc.openebench.checker.MetricsChecker;
import java.net.URI;
import java.util.List;

/**
 * @author Dmitry Repchevsky
 */

public class RuntimeDependenciesChecker implements MetricsChecker {
    
    @Override
    public Boolean check(ToolsDAO toolsDAO, MetricsDAO metricsDAO, Tool tool, Metrics metrics) {
        Boolean bool = check(tool);
        Project project = metrics.getProject();
        Deployment deployment;
        if (project == null) {
            project = new Project();
            project.setDeployment(deployment = new Deployment());
            metrics.setProject(project);
        } else {
            deployment = project.getDeployment();
            if (deployment == null) {
                project.setDeployment(deployment = new Deployment());
            }
        }
        deployment.setDependencies(bool);
        return bool;
    }
    
    private static Boolean check(Tool tool) {
        Dependencies dependencies = tool.getDependencies();
        if (dependencies == null) {
            return null;
        }
        
        final List<URI> list = dependencies.getRuntimeDependencies();
        return list != null && list.size() > 0;
    }

}
