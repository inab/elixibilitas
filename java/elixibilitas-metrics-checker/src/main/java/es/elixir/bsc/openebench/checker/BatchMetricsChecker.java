package es.elixir.bsc.openebench.checker;

import com.mongodb.MongoClient;
import es.elixir.bsc.elixibilitas.dao.MetricsDAO;
import es.elixir.bsc.elixibilitas.dao.ToolsDAO;
import es.elixir.bsc.elixibilitas.model.metrics.Metrics;
import es.elixir.bsc.openebench.model.tools.Tool;
import java.util.List;
import java.util.concurrent.CountDownLatch;
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
        
        final ToolsDAO toolsDAO = new ToolsDAO(mc.getDatabase("elixibilitas"), "https://openebench.bsc.es/monitor/tool/");
        final MetricsDAO metricsDAO = new MetricsDAO(mc.getDatabase("elixibilitas"), "https://openebench.bsc.es/monitor/metrics/");
        
        final List<Tool> tools = toolsDAO.get();
        final CountDownLatch latch = new CountDownLatch(tools.size());

        tools.forEach(tool -> {
            
            final String id = tool.id.toString().substring(toolsDAO.baseURI.length());
            
            Metrics metrics = metricsDAO.get(id);
            if (metrics == null) {
                metrics = new Metrics();
            }
            
            final Future<Metrics> future = executor.submit(new MetricsCheckTask(tool, metrics));
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        metricsDAO.update("biotools", id, future.get());
                    } catch (InterruptedException | ExecutionException ex) {
                        Logger.getLogger(BatchMetricsChecker.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    latch.countDown();
                }
            });
        });
        
        // ensure that all taksks executed.
        try {
            latch.await();
        } catch (InterruptedException ex) {}
    }
}
