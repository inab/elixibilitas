/**
 * *****************************************************************************
 * Copyright (C) 2020 ELIXIR ES, Spanish National Bioinformatics Institute (INB)
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

package es.elixir.bsc.openebench.checker.biotools;

import es.bsc.inb.elixir.openebench.model.metrics.HomepageAccess;
import es.bsc.inb.elixir.openebench.model.metrics.Metrics;
import es.bsc.inb.elixir.openebench.model.metrics.Project;
import es.bsc.inb.elixir.openebench.model.metrics.Website;
import es.bsc.inb.elixir.openebench.model.tools.Tool;
import es.bsc.inb.elixir.openebench.repository.OpenEBenchEndpoint;
import es.elixir.bsc.elixibilitas.dao.MetricsDAO;
import es.elixir.bsc.elixibilitas.dao.ToolsDAO;
import es.elixir.bsc.openebench.checker.MetricsChecker;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import static java.time.temporal.ChronoUnit.DAYS;
import javax.json.JsonArray;
import javax.json.JsonObject;

/**
 * @author Dmitry Repchevsky
 */

public class HomePageStatChecker implements MetricsChecker {

    @Override
    public Boolean check(ToolsDAO toolsDAO, MetricsDAO metricsDAO, Tool tool, Metrics metrics) {
        
        final String id = tool.id.toString().substring(OpenEBenchEndpoint.TOOL_URI_BASE.length());
        
        final HomepageAccess homepage_access = getHomepageAccess(metricsDAO, id);
        if (homepage_access == null) {
            return false;
        }
        
        Website website;
        Project project = metrics.getProject();
        if (project == null) {
            website = new Website();
            project = new Project();
            project.setWebsite(website);
            metrics.setProject(project);
        } else {            
            website = project.getWebsite();
            if (website == null) {
                project.setWebsite(website = new Website());
            }
        }

        website.setHomepageAccess(homepage_access);
        
        return true;
    }
    
    private HomepageAccess getHomepageAccess(MetricsDAO metricsDAO, String id) {
        final HomepageAccess homepage_access = new HomepageAccess();
        
        final String month_ago_time = LocalDate.now().minusMonths(1).plusDays(1).toString();
        
        final JsonArray access_time = metricsDAO.findLog(id, "/project/website/access_time", month_ago_time, null, null);
        if(access_time != null && !access_time.isEmpty()) {
            int total_access_time = 0;
            int access_time_measures = 0;
            for (int i = 0, n = access_time.size(); i < n; i++) {
                final JsonObject obj = access_time.getJsonObject(i);
                final String time = obj.getString("value", "0");
                try {
                    final int t = Integer.parseInt(time);
                    if (t > 0) {
                        total_access_time += t;
                        access_time_measures++;
                    }
                } catch(NumberFormatException ex) {}
            }
            
            if (access_time_measures > 0) {
                homepage_access.setAverageAccessTime(total_access_time / access_time_measures);
            }
        }
        
        final JsonArray last_check = metricsDAO.findLog(id, "/project/website/last_check", month_ago_time, null, null);
        if (last_check == null || last_check.isEmpty()) {
            return homepage_access;
        }
        
        final JsonArray operational = metricsDAO.findLog(id, "/project/website/operational", null, null, null);        
        if (operational == null || operational.isEmpty()) {
            return homepage_access;
        }

        int operational_days = 0;
        int unoperational_days = 0;
        
        JsonObject obj = operational.getJsonObject(0);
        String code = obj.getString("value", "0");
        String date = obj.getString("date", null);

        ZonedDateTime last_date = null;
        for (int i = 0, j = 0, m = last_check.size(), n = operational.size(); i < m; i++) {
            final JsonObject o = last_check.getJsonObject(i);
            final String adate = o.getString("date", null);
            if (adate == null) {
                continue;
            }

            if (last_date == null) {
                last_date = ZonedDateTime.parse(adate);
            } else {
                final ZonedDateTime current_date = ZonedDateTime.parse(adate);
                
                // do not consider hh:mm:ss, so 23:00 - 01:00 (2h) is a 1 (next) day.
                long days = DAYS.between(last_date.toLocalDate(), current_date.toLocalDate());

                try {
                    final int c = Integer.parseInt(code);
                    while (--days > 0) {
                        if (c >= 200 && c < 300) {
                            operational_days++;
                        } else {
                            unoperational_days++;
                        }
                    }
                } catch(NumberFormatException ex) {}

                last_date = current_date;
            }

            while(j <= n && adate.compareTo(date) >= 0) {
                code = obj.getString("value", "0");
                if (++j < n) {
                    obj = operational.getJsonObject(j);
                    date = obj.getString("date", null);
                }
            }

            try {
                final int c = Integer.parseInt(code);
                if (c >= 200 && c < 300) {
                    operational_days++;
                } else {
                    unoperational_days++;
                }
            } catch(NumberFormatException ex) {}
        }
        
        homepage_access.setUptimeDays(operational_days);
        homepage_access.setDowntimeDays(unoperational_days);
        
        return homepage_access;

    }
}
