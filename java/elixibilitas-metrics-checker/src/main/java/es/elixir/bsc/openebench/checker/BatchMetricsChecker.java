package es.elixir.bsc.openebench.checker;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import es.elixir.bsc.elixibilitas.dao.MetricsDAO;
import es.elixir.bsc.elixibilitas.dao.ToolsDAO;
import es.elixir.bsc.elixibilitas.model.metrics.Metrics;
import es.elixir.bsc.openebench.model.tools.Tool;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Dmitry Repchevsky
 */

public class BatchMetricsChecker {
    
    private final static String HELP = "java -jar metrics_checker.jar -uri uri\n\n" +
                                       "parameters:\n\n" +
                                       "-uri - mongodb url\n";

    private final ExecutorService executor;
    
    public BatchMetricsChecker(ExecutorService executor) {
        this.executor = executor;
    }
    
    public static void main(String[] args) {
        Map<String, List<String>> params = parameters(args);

        //final ExecutorService executor = Executors.newFixedThreadPool(32);
        final ExecutorService executor = new ThreadPoolExecutor(32, 32, 0L, TimeUnit.MILLISECONDS, 
                                          new ArrayBlockingQueue<>(1), 
                                          new RejectedExecutionHandler() {
            @Override
            public void rejectedExecution(Runnable runnable, ThreadPoolExecutor executor) {
                if (!executor.isShutdown()) {
                    try {
                        executor.getQueue().put(runnable);
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                        Logger.getLogger(BatchMetricsChecker.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        });

        
        try {
            if (params.isEmpty()) {
                new BatchMetricsChecker(executor).check(new MongoClient("localhost"));
            } else {
                final List<String> uris = params.get("-uri");
                if (uris == null || uris.isEmpty()) {
                    System.out.println("missed 'url' parameter");
                    System.out.println(HELP);
                    System.exit(1);
                }

                final MongoClient mc = new MongoClient(new MongoClientURI(uris.get(0)));
                new BatchMetricsChecker(executor).check(mc);
            } 
        } finally {
            System.out.println("shutting down...");
            executor.shutdown();
        }
        
        System.out.println("finished...");
        System.exit(0);
    }

    public void check(MongoClient mc) {
        
        final ToolsDAO toolsDAO = new ToolsDAO(mc.getDatabase("elixibilitas"), "https://dev-openebench.bsc.es/monitor/tool/");
        final MetricsDAO metricsDAO = new MetricsDAO(mc.getDatabase("elixibilitas"), "https://dev-openebench.bsc.es/monitor/metrics/");
        
        final List<Tool> tools = toolsDAO.get();
        final CountDownLatch latch = new CountDownLatch(tools.size());

        tools.forEach(tool -> {
            
            try {
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
                            metricsDAO.merge("biotools", id, future.get());
                        } catch (Throwable th) {
                            Logger.getLogger(BatchMetricsChecker.class.getName()).log(Level.SEVERE, "update failed", th);
                        }
                        latch.countDown();
                    }
                });
            } catch(Throwable th) {
                Logger.getLogger(BatchMetricsChecker.class.getName()).log(Level.SEVERE, "submit failed", th);
                latch.countDown();
            }
        });

        // ensure that all taksks executed.
        try {
            latch.await(8, TimeUnit.HOURS);
        } catch (InterruptedException ex) {}
        
        executor.shutdownNow();
    }
    
    private static Map<String, List<String>> parameters(String[] args) {
        TreeMap<String, List<String>> parameters = new TreeMap();        
        List<String> values = null;
        for (String arg : args) {
            switch(arg) {
                case "-uri":  values = parameters.get(arg);
                              if (values == null) {
                                  values = new ArrayList(); 
                                  parameters.put(arg, values);
                              }
                              break;
                default: if (values != null) {
                    values.add(arg);
                }
            }
        }
        return parameters;
    }
}
