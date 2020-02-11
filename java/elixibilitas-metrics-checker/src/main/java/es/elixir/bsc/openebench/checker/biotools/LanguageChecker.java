package es.elixir.bsc.openebench.checker.biotools;

import es.bsc.inb.elixir.openebench.model.metrics.Distribution;
import es.bsc.inb.elixir.openebench.model.metrics.Metrics;
import es.bsc.inb.elixir.openebench.model.metrics.Sourcecode;
import es.bsc.inb.elixir.openebench.model.tools.Tool;
import es.elixir.bsc.openebench.checker.MetricsChecker;

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
        final List<String> languages = tool.getLanguages();
        
        if (languages.isEmpty()) {
            return null;
        }

        return !(languages.contains("C++") || 
                 languages.contains("C") || 
                 languages.contains("Java") ||
                 languages.contains("C#"));
    }
}
