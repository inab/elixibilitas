package es.elixir.bsc.openebench.checker.biotools;

import es.bsc.inb.elixir.openebench.model.metrics.Metrics;
import es.bsc.inb.elixir.openebench.model.metrics.Project;
import es.bsc.inb.elixir.openebench.model.metrics.Website;
import es.bsc.inb.elixir.openebench.model.tools.Tool;
import es.bsc.inb.elixir.openebench.model.tools.Web;
import es.elixir.bsc.openebench.checker.MetricsChecker;

/**
 * @author Dmitry Repchevsky
 */

public class HomepageLicenseChecker implements MetricsChecker {

    @Override
    public String getToolPath() {
        return "/web/license";
    }
    
    @Override
    public String getMetricsPath() {
        return "/project/website/license";
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
            website.setLicense(true);
        } else if (project != null && project.getWebsite() != null) {
            project.getWebsite().setLicense(bool);
        }
        return bool;

    }
    
    private static Boolean check(Tool tool) {
        final Web web = tool.getWeb();
        return web != null && web.getLicense() != null && !web.getLicense().isEmpty();
    }
}
