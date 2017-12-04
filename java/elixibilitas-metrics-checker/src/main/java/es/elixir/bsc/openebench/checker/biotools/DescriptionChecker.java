package es.elixir.bsc.openebench.checker.biotools;

import es.elixir.bsc.elixibilitas.model.metrics.Metrics;
import es.elixir.bsc.elixibilitas.model.metrics.Project;
import es.elixir.bsc.elixibilitas.model.metrics.Summary;
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
        Summary summary;
        if (project == null) {
            project = new Project();
            project.setSummary(summary = new Summary());
            metrics.setProject(project);
        } else {
            summary = project.getSummary();
            if (summary == null) {
                project.setSummary(summary = new Summary());
            }
        }
        summary.setDescription(bool);
        
        return bool;
    }
    
    private static Boolean check(Tool tool) {
        final String description = tool.getDescription();
        return description == null ? null : !description.isEmpty();
    }
}
