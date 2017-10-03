package es.elixir.bsc.openebench.checker;

import com.mongodb.MongoClient;
import es.elixir.bsc.elixibilitas.model.metrics.Metrics;
import es.elixir.bsc.elixibilitas.tools.dao.ToolDAO;
import es.elixir.bsc.openebench.metrics.dao.MetricsDAO;
import es.elixir.bsc.openebench.model.tools.Tool;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Dmitry Repchevsky
 */

public class BatchMetricsChecker {
    
    private final ExecutorService executor;
    
    public BatchMetricsChecker(ExecutorService executor) {
        this.executor = executor;
    }
    
    public static void main(String[] args) {
        new BatchMetricsChecker(Executors.newCachedThreadPool()).check(new MongoClient("localhost"));
    }

    public void check(MongoClient mc) {
        List<Tool> tools = ToolDAO.get(mc);

        tools.forEach(tool -> {
            
            final String id = tool.id.getPath().substring(6); // "/tool/"
            
            Metrics metrics = MetricsDAO.get(mc, id);
            if (metrics == null) {
                metrics = new Metrics();
            }
            
            final Future<Metrics> future = executor.submit(new MetricsCheckTask(tool, metrics));
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        MetricsDAO.put(mc, id, future.get());
                    } catch (InterruptedException | ExecutionException ex) {
                        Logger.getLogger(BatchMetricsChecker.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    
                }
            });
        });
    }
}
