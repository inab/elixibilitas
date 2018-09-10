package es.elixir.bsc.openebench.checker.biotools;

import es.elixir.bsc.elixibilitas.model.metrics.Metrics;
import es.elixir.bsc.elixibilitas.model.metrics.Project;
import es.elixir.bsc.elixibilitas.model.metrics.Website;
import es.elixir.bsc.openebench.checker.MetricsChecker;
import es.elixir.bsc.openebench.model.tools.Tool;
import es.elixir.bsc.openebench.model.tools.Web;

/**
 * @author Dmitry Repchevsky
 */

public class HomepageCopyrightChecker implements MetricsChecker {

    @Override
    public String getToolPath() {
        return "/web/copyright";
    }
    
    @Override
    public String getMetricsPath() {
        return "/project/website/copyright";
    }
    
    @Override
    public Boolean check(Tool tool, Metrics metrics) {
        Boolean bool = check(tool);
        Project project = metrics.getProject();
        if (Boolean.TRUE.equals(bool)) {
            Website website;
            if (project == null) {
                metrics.setProject(project = new Project());
                project.setWebsite(website = new Website());
            } else {
                website = project.getWebsite();
                if (website == null) {
                    project.setWebsite(website = new Website());
                }
            }
            website.setCopyright(true);
        } else if (project != null && project.getWebsite() != null) {
            project.getWebsite().setCopyright(bool);
        }
        return bool;

    }
    
    private static Boolean check(Tool tool) {
        final Web web = tool.getWeb();
        return web != null && web.getCopyright() != null && !web.getCopyright().isEmpty();
    }
}
