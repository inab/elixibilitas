package es.elixir.bsc.openebench.checker.biotools;

import es.elixir.bsc.elixibilitas.model.metrics.Distribution;
import es.elixir.bsc.elixibilitas.model.metrics.Metrics;
import es.elixir.bsc.elixibilitas.model.metrics.Sourcecode;
import es.elixir.bsc.openebench.checker.MetricsChecker;
import es.elixir.bsc.openebench.model.tools.CommandLineTool;
import es.elixir.bsc.openebench.model.tools.DesktopApplication;
import es.elixir.bsc.openebench.model.tools.Library;
import es.elixir.bsc.openebench.model.tools.Plugin;
import es.elixir.bsc.openebench.model.tools.Script;
import es.elixir.bsc.openebench.model.tools.Suite;
import es.elixir.bsc.openebench.model.tools.Tool;
import java.util.List;

/**
 * @author Dmitry Repchevsky
 */

public class LanguageChecker implements MetricsChecker {

    @Override
    public String getToolPath() {
        return "/languages";
    }
    
    @Override
    public String getMetricsPath() {
        return "/distribution/sourcecode/interpreted";
    }

    @Override
    public Boolean check(Tool tool, Metrics metrics) {
        Boolean bool = check(tool);
        if (bool != null) {
            Distribution distribution = metrics.getDistribution();
            if (distribution == null) {
                metrics.setDistribution(distribution = new Distribution());
            }
            Sourcecode sourcecode = distribution.getSourcecode();
            if (sourcecode == null) {
                distribution.setSourcecode(sourcecode = new Sourcecode());
            }
            sourcecode.setInterpreted(bool);
        }
        return bool;
    }
    
    private static Boolean check(Tool tool) {
        final List<String> languages;
        if (tool instanceof CommandLineTool) {
            final CommandLineTool cmd = (CommandLineTool)tool;
            languages = cmd.getLanguages();
        } else if (tool instanceof DesktopApplication) {
            final DesktopApplication desktop = (DesktopApplication)tool;
            languages = desktop.getLanguages();
        } else if (tool instanceof Library) {
            final Library library = (Library)tool;
            languages = library.getLanguages();
        } else if (tool instanceof Script) {
            final Script script = (Script)tool;
            languages = script.getLanguages();
        } else if (tool instanceof Plugin) {
            final Plugin plugin = (Plugin)tool;
            languages = plugin.getLanguages();
        } else if (tool instanceof Suite) {
            final Suite suite = (Suite)tool;
            languages = suite.getLanguages();
        } else {
            return null;
        }
        
        if (languages.isEmpty()) {
            return null;
        }

        return !(languages.contains("C++") || 
                 languages.contains("C") || 
                 languages.contains("Java") ||
                 languages.contains("C#"));
    }
}
