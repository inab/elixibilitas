package es.elixir.bsc.openebench.checker.biotools;

import es.elixir.bsc.elixibilitas.model.metrics.Distribution;
import es.elixir.bsc.elixibilitas.model.metrics.Metrics;
import es.elixir.bsc.elixibilitas.model.metrics.Repository;
import es.elixir.bsc.elixibilitas.model.metrics.Sourcecode;
import es.elixir.bsc.openebench.model.tools.Tool;
import es.elixir.bsc.openebench.checker.MetricsChecker;
import java.net.URI;
import java.util.List;

/**
 * @author Dmitry Repchevsky
 */

public class RepositoryChecker implements MetricsChecker {

    @Override
    public Boolean check(Tool tool, Metrics metrics) {
        
        Boolean bool = check(tool);
        Distribution distribution = metrics.getDistribution();
        if (Boolean.TRUE.equals(bool)) {
            if (distribution == null) {
                metrics.setDistribution(distribution = new Distribution());
            }
            Sourcecode sourcecode = distribution.getSourcecode();
            if (sourcecode == null) {
                distribution.setSourcecode(sourcecode = new Sourcecode());
            }
            Repository repository = sourcecode.getRepository();
            if (repository == null) {
                sourcecode.setRepository(new Repository());
            }
        } else if (distribution != null) {
            final Sourcecode sourcecode = distribution.getSourcecode();
            if (sourcecode != null) {
                sourcecode.setRepository(null);
            }
        }
        return bool;
    }
    
    private static Boolean check(Tool tool) {
        final List<URI> repositories = tool.getRepositories();
        return !repositories.isEmpty();
    }
}
