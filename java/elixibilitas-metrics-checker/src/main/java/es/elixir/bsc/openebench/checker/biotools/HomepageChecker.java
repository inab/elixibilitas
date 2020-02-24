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

import es.bsc.inb.elixir.openebench.model.metrics.Metrics;
import es.bsc.inb.elixir.openebench.model.metrics.Project;
import es.bsc.inb.elixir.openebench.model.metrics.Website;
import es.bsc.inb.elixir.openebench.model.tools.Tool;
import es.bsc.inb.elixir.openebench.model.tools.Web;
import es.elixir.bsc.elixibilitas.dao.MetricsDAO;
import es.elixir.bsc.elixibilitas.dao.ToolsDAO;
import es.elixir.bsc.openebench.checker.MetricsChecker;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * @author Dmitry Repchevsky
 */

public class HomepageChecker implements MetricsChecker {
    
    private final static X509TrustManager TM;

    static {
        X509TrustManager tmp = null;
        try {
            TrustManagerFactory tmf = TrustManagerFactory
                    .getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init((KeyStore)null);
            for (TrustManager tm : tmf.getTrustManagers()) {
                if (tm instanceof X509TrustManager) {
                    tmp = (X509TrustManager) tm;
                    break;
                }
            }
        } catch (NoSuchAlgorithmException | KeyStoreException ex) {
            Logger.getLogger(HomepageChecker.class.getName()).log(Level.SEVERE, null, ex);
        }
        TM = tmp;
    }

    @Override
    public Boolean check(ToolsDAO toolsDAO, MetricsDAO metricsDAO, Tool tool, Metrics metrics) {
        
        Web web = tool.getWeb();
        if (web == null) {
            return false;
        }
        
        URI homepage = web.getHomepage();
        if(homepage == null) {
            return false;
        }

        StringBuilder security = new StringBuilder();
        
        TrustManager[] trustAllManager = new TrustManager[] {new X509TrustManager() {
                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return TM.getAcceptedIssuers();
                }
                @Override
                public void checkClientTrusted(X509Certificate[] certs, String authType) {}

                @Override
                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                   try {
                        TM.checkServerTrusted(certs, authType);
                    } catch (CertificateException ex) {
                        security.append(ex.getMessage());
                    }
                }
            }
        };

        try {
            SSLContext sc = SSLContext.getInstance("TLSv1.2");
            sc.init(null, trustAllManager, new SecureRandom());
            SSLContext.setDefault(sc);
        } catch (NoSuchAlgorithmException | KeyManagementException ex) {
            Logger.getLogger(HomepageChecker.class.getName()).log(Level.INFO, ex.getMessage());
        }
        
        final HostnameVerifier allVerifier = (String h, SSLSession s) -> true;

        String cookies = null;
        int code = HttpURLConnection.HTTP_CLIENT_TIMEOUT;
        boolean bioschemas = false;
        
        final long time = System.currentTimeMillis();
        long timeout = 0;
        try {
            loop:
            for (int i = 0; i < 10; i++) {
                final URL url = homepage.toURL();
                
                final String protocol = url.getProtocol();
                if ("ftp".equals(protocol)) {
                    try {
                        url.openStream().close();
                        code = 205;
                    } catch (IOException ex) {
                        code = 404;
                    }
                }
                HttpURLConnection con = null;
                try {
                    con = (HttpURLConnection)url.openConnection();
                    if (con instanceof HttpsURLConnection) {
                        ((HttpsURLConnection)con).setHostnameVerifier(allVerifier);
                    }

                    con.setReadTimeout(120000);
                    con.setConnectTimeout(300000);
                    
                    // redirect manually to bypass SSL verifier.
                    con.setInstanceFollowRedirects(false);

                    con.addRequestProperty("User-Agent", "Mozilla/5.0 Gecko/20100101 Firefox/54.0");
                    if (cookies != null && !cookies.isEmpty()) {
                        con.setRequestProperty("Cookie", cookies);
                    }
                    
                    try (BufferedInputStream in = new BufferedInputStream(con.getInputStream())) {
                        code = con.getResponseCode();
                        switch(code) {
                            case HttpURLConnection.HTTP_OK:           timeout = (System.currentTimeMillis() - time);
                                                                      bioschemas = checkBioschemas(con, in);
                            case HttpURLConnection.HTTP_NOT_MODIFIED: break loop;
                            case HttpURLConnection.HTTP_MOVED_PERM:
                            case HttpURLConnection.HTTP_MOVED_TEMP:
                            case 307: // Temporary Redirect
                            case 308: // Permanent Redirect
                            case HttpURLConnection.HTTP_SEE_OTHER:
                                final String location = con.getHeaderField("Location");
                                if (location == null || location.isEmpty()) {
                                    Logger.getLogger(HomepageChecker.class.getName()).log(Level.INFO, String.format("\n-----> %1$s %2$s invalid redirect", tool.id.toString(), homepage));
                                    code = 418;
                                    break loop;
                                }
                                cookies = con.getHeaderField("Set-Cookie");
                                URI redirect = URI.create(location);
                                homepage = redirect.isAbsolute() ? redirect : homepage.resolve(redirect);
                                break;
                            default: Logger.getLogger(HomepageChecker.class.getName()).log(Level.INFO, String.format("\n-----> %1$s %2$s %3$s", tool.id.toString(), homepage, code));
                                timeout = (System.currentTimeMillis() - time);
                                break loop;
                        }
                    }
                } catch (IOException ex) {
                    Logger.getLogger(HomepageChecker.class.getName()).log(Level.INFO, String.format("\n-----> %1$s %2$s error loading home page: %3$s", tool.id.toString(), homepage, ex.getMessage()));
                    if (con != null) {
                        try (InputStream in = con.getErrorStream()) {}
                    }
                    break;
                }
            }

            if (security.length() > 0) {
                Logger.getLogger(HomepageChecker.class.getName()).log(Level.INFO, String.format("\n-----> %1$s %2$s ssl error: %3$s", tool.id.toString(), homepage, security.toString()));
            }
        } catch (MalformedURLException ex) {
            Logger.getLogger(HomepageChecker.class.getName()).log(Level.INFO, String.format("\n-----> %1$s %2$s error loading home page: %3$s", tool.id.toString(), homepage, ex.getMessage()));
        } catch (Exception ex) {}

        final boolean operational = isOperational(code);
        
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
        
        website.setHTTPS("https".equals(homepage.getScheme()));
        website.setSSL("https".equals(homepage.getScheme()) ? security.length() == 0 : null);
            
        website.setOperational(code);
        website.setAccessTime(operational ? timeout : null);
        website.setLastCheck(ZonedDateTime.now(ZoneId.of("Z")));
        
        website.setBioschemas(bioschemas);

        return operational;
    }
    
    private boolean isOperational(Integer code) {
        return code != null && code == HttpURLConnection.HTTP_OK ||
                code == HttpURLConnection.HTTP_NOT_MODIFIED;
    }
    
    private boolean checkBioschemas(final HttpURLConnection con, InputStream in) {
        String charset;
        final String contentType = con.getContentType();
        if (contentType != null) {
            final int idx = contentType.indexOf("charset=");
            if (idx >= 0) {
                charset = contentType.substring(idx + 8).split(";")[0].trim();
            } else {
                charset = "UTF-8";
            }
        } else {
            charset = "UTF-8";
        }

        try {
            final Document doc = Jsoup.parse(in, charset, con.getURL().toString());

            // json-ld
            for(Element script : doc.select("script")) {
                if ("application/ld+json".equals(script.attr("type"))) {
                    final String json = script.html();
                    try (JsonReader reader = Json.createReader(new StringReader(json))) {
                        final JsonValue value = reader.readValue();
                        if (value != null && JsonValue.ValueType.OBJECT == value.getValueType()) {
                            final JsonObject obj = value.asJsonObject();
                            final String context = obj.getString("@context", null);
                            if (context != null && context.equals("http://schema.org")) {
                                return true;
//                                final String type = obj.getString("@type", null);
//                                if (type != null && type.equals("SoftwareApplication")) {
//                                    return true;
//                                }
                            }
                        }
                    }
                }
            }

            // microdata
            for(Element elements : doc.getElementsByAttribute("itemscope")) {
                for (Element items : elements.getElementsByAttribute("itemtype")) {
                    final String itemtype = items.attr("itemtype");
                    if (itemtype != null &&
                        itemtype.startsWith("http://schema.org/")) {
//                       (itemtype.equals("http://schema.org/SoftwareApplication") ||
//                        itemtype.equals("http://schema.org/DataCatalog"))) {
                        return true;
                    }
                }
            }

            for(Element elements : doc.getElementsByAttribute("vocab")) {
                final String vocab = elements.attr("vocab");
                if (vocab != null && vocab.equals("http://schema.org")) {
                    return true;
//                    final String typeof = elements.attr("typeof");
//                    if (typeof != null && typeof.equals("SoftwareApplication")) {
//                        return true;
//                    }
                }
            }
        } catch (IOException ex) {
            // html parsing error
        }

        return false;
    }
}
