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

import es.elixir.bsc.elixibilitas.model.metrics.Metrics;
import es.elixir.bsc.elixibilitas.model.metrics.Project;
import es.elixir.bsc.openebench.checker.MetricsChecker;
import es.elixir.bsc.openebench.model.tools.Documentation;
import es.elixir.bsc.openebench.model.tools.Tool;

/**
 * @author Dmitry Repchevsky
 */

public class DocumentationGeneralChecker implements MetricsChecker {
    
    @Override
    public String getToolPath() {
        return "/documentation/general";
    }
    
    @Override
    public String getMetricsPath() {
        return "/project/documentation/general";
    }

    @Override
    public Boolean check(Tool tool, Metrics metrics) {
        Boolean bool = check(tool);
        Project project = metrics.getProject();
        if (Boolean.TRUE.equals(bool)) {
            es.elixir.bsc.elixibilitas.model.metrics.Documentation documentation;
            if (project == null) {
                metrics.setProject(project = new Project());
                project.setDocumentation(documentation = new es.elixir.bsc.elixibilitas.model.metrics.Documentation());
            } else {
                documentation = project.getDocumentation();
                if (documentation == null) {
                    project.setDocumentation(documentation = new es.elixir.bsc.elixibilitas.model.metrics.Documentation());
                }
            }
            documentation.setGeneral(true);
        } else if (project != null && project.getDocumentation() != null) {
            project.getDocumentation().setGeneral(bool);
        }
        return bool;
    }
    
    private static Boolean check(Tool tool) {
        Documentation documentation = tool.getDocumentation();
        if (documentation == null) {
            return null;
        }
        return documentation.getGeneral() == null ? null : true;
    }
}
