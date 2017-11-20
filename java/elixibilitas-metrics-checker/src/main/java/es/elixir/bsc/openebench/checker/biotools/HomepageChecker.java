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

package es.elixir.bsc.openebench.checker.biotools;

import es.elixir.bsc.elixibilitas.model.metrics.Metrics;
import es.elixir.bsc.elixibilitas.model.metrics.Project;
import es.elixir.bsc.elixibilitas.model.metrics.Website;
import es.elixir.bsc.openebench.model.tools.Tool;
import es.elixir.bsc.openebench.checker.MetricsChecker;
import java.net.HttpURLConnection;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author Dmitry Repchevsky
 */

public class HomepageChecker implements MetricsChecker {
    
    private final static ClientBuilder CB;

    static {
        TrustManager[] trustAllManager = new TrustManager[] {new X509TrustManager() {
                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
                @Override
                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }
                @Override
                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            }
        };

        HostnameVerifier allVerifier = (String h, SSLSession s) -> true;

        ClientBuilder _cb;
        try {
            SSLContext sc = SSLContext.getInstance("TLSv1.2");
            sc.init(null, trustAllManager, new SecureRandom());

            _cb = ClientBuilder.newBuilder().sslContext(sc)
                                        .hostnameVerifier(allVerifier);
        } catch (NoSuchAlgorithmException | KeyManagementException ex) {
            _cb = ClientBuilder.newBuilder();
        }
        CB = _cb;
    }

    @Override
    public Boolean check(Tool tool, Metrics metrics) {
        final Integer[] result = check(tool);
        final boolean operational = isOperational(result[0]);
        
        Project project = metrics.getProject();
        if (project != null) {
            Website website = project.getWebsite();
            if (result[0] != null) {
                if (website == null) {
                    project.setWebsite(website = new Website());
                }
                website.setOperational(result[0]);
                website.setAccessTime(result[1]);
                if (operational) {
                    website.setLastSeen(ZonedDateTime.now(ZoneId.of("Z")));
                }
            } else if (website != null) {
                website.setOperational(null);
                website.setAccessTime(null);
            }
        } else if (result[0] != null) {
            Website website = new Website();
            website.setOperational(result[0]);
            website.setAccessTime(result[1]);
            project = new Project();
            project.setWebsite(website);
            
            metrics.setProject(project);
        }
        
        return operational;
    }
    
    private boolean isOperational(Integer code) {
        return code != null && code == HttpURLConnection.HTTP_OK ||
                code == HttpURLConnection.HTTP_NOT_MODIFIED;
    }
    
    private static Integer[] check(Tool tool) {
        
        URI homepage = tool.getHomepage();
        if(homepage == null) {
            return null;
        }

        final long time = System.currentTimeMillis();
        try {

            int code;
            for (int i = 0; i < 10; i++) {
                final Client client = CB.build();
                try {
                    final Response response = client.target(homepage).request(MediaType.WILDCARD).header("User-Agent", "Mozilla/5.0 Gecko/20100101 Firefox/54.0").get();
                    try {
                        code = response.getStatus();

                        switch (code) {
                            case HttpURLConnection.HTTP_OK: 
                            case HttpURLConnection.HTTP_NOT_MODIFIED: return new Integer[] {code, (int)(System.currentTimeMillis() - time)};
                            case HttpURLConnection.HTTP_MOVED_PERM:
                            case HttpURLConnection.HTTP_MOVED_TEMP:
                            case 307: // Temporary Redirect
                            case 308: // Permanent Redirect
                            case HttpURLConnection.HTTP_SEE_OTHER:
                                URI redirect = response.getLocation();
                                if (redirect == null) {
                                    Logger.getLogger(HomepageChecker.class.getName()).log(Level.INFO, String.format("\n-----> %1$s %2$s redirect to null", tool.id.toString(), homepage));
                                    return new Integer[] {code, null};
                                }

                                homepage = redirect.isAbsolute() ? redirect : homepage.resolve(redirect);

                                continue;
                            default: Logger.getLogger(HomepageChecker.class.getName()).log(Level.INFO, String.format("\n-----> %1$s %2$s %3$s", tool.id.toString(), homepage, code));
                                     return new Integer[] {code, null};
                        }
                    } finally {
                        response.close();
                    }
                } finally {
                    client.close();
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(HomepageChecker.class.getName()).log(Level.INFO, String.format("\n-----> %1$s %2$s error loading home page: %3$s", tool.id.toString(), homepage, ex.getMessage()));
        }

        return new Integer[] {HttpURLConnection.HTTP_CLIENT_TIMEOUT, (int)(System.currentTimeMillis() - time)};
    }
}
