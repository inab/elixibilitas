/**
 * *****************************************************************************
 * Copyright (C) 2017 ELIXIR ES, Spanish National Bioinformatics Institute (INB)
 * and Barcelona Supercomputing Center (BSC)
 *
 * Modifications to the initial code base are copyright of their respective
 * authors, or their employers as appropriate.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 *****************************************************************************
 */

package es.elixir.bsc.elixibilitas.metrics.rest;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import es.elixir.bsc.openebench.checker.BatchMetricsChecker;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

/**
 * @author Dmitry Repchevsky
 */

@WebListener
public class BiotoolsMetricsScheduler implements ServletContextListener {

    private ExecutorService executor;

    @Resource
    private ManagedScheduledExecutorService scheduler;
    
    @Override
    public void contextInitialized(ServletContextEvent evnt) {
        final String mongo_url = evnt.getServletContext().getInitParameter("mongodb.url");

        executor = Executors.newFixedThreadPool(8);
        scheduler.scheduleAtFixedRate(new MetricsImporter(executor, mongo_url), 0, 720, TimeUnit.MINUTES);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        executor.shutdown();
        scheduler.shutdownNow();
    }
    
    public static class MetricsImporter implements Runnable {

        private final ExecutorService executor;
        private final String uri;
        
        public MetricsImporter(ExecutorService executor, String uri) {
            this.executor = executor;
            this.uri = uri;
        }
        
        @Override
        public void run() {
            new BatchMetricsChecker(executor).check(new MongoClient(new MongoClientURI(uri)));
        }
    }
}
