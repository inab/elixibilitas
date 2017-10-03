package es.elixir.bsc.openebench.checker.biotools;

import es.elixir.bsc.elixibilitas.model.metrics.Metrics;
import es.elixir.bsc.elixibilitas.model.metrics.Project;
import es.elixir.bsc.openebench.model.tools.Tool;
import es.elixir.bsc.openebench.checker.MetricsChecker;

/**
 * @author Dmitry Repchevsky
 */

public class DescriptionChecker implements MetricsChecker {

    @Override
    public Boolean check(Tool tool, Metrics metrics) {
        Boolean bool = check(tool);
        
        Project project = metrics.getProject();
        if (project != null) {
            project.setDescription(bool);
        } else if (bool != null) {
            project = new Project();
            project.setDescription(bool);
            metrics.setProject(project);
        }
        
        return bool;
    }
    
    private static Boolean check(Tool tool) {
        final String description = tool.getDescription();
        return description == null ? null : !description.isEmpty();
    }
}
