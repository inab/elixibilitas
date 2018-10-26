/**
 * *****************************************************************************
 * Copyright (C) 2018 ELIXIR ES, Spanish National Bioinformatics Institute (INB)
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

package es.elixir.bsc.openebench.bioconda;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;

/**
 * @author Dmitry Repchevsky
 */

public class BiocondaPackage {
    
    private final static String SERVER = "https://anaconda.org/bioconda/%s/%s/download/%s/%s";
    
    private final static int[][] MATRIX = matrix(1000, 1000);
    
    public final String name;
    public final String version;
    public final String platform;
    public final String file;
    
    private volatile Metadata metadata;
    
    public BiocondaPackage(String name, String version) {
        this(name, version, null, null);
    }
    
    public BiocondaPackage(String name, String version, 
            String platform, String file) {
        this.name = name;
        this.version = version;
        this.platform = platform;
        this.file = file;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 19 * hash + Objects.hashCode(this.name);
        hash = 19 * hash + Objects.hashCode(this.version);
        return hash;
    }
    

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final BiocondaPackage other = (BiocondaPackage) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (!Objects.equals(this.version, other.version)) {
            return false;
        }
        return true;
    }
    
    public Metadata getMetadata() throws IOException {
        if (metadata == null) {
            synchronized(this) {
                if (metadata == null) {
                    metadata = load();
                }
            }
        }
        return metadata;
    }
    
    private Metadata load() throws IOException {
        
        String cookies = null;
        URI uri = URI.create(toString());
        
        for (int i = 0; i < 10; i++) {
            URL url = uri.toURL();
            HttpURLConnection con = (HttpURLConnection)url.openConnection();
            con.addRequestProperty("User-Agent", "Mozilla");
            if (cookies != null && !cookies.isEmpty()) {
                con.setRequestProperty("Cookie", cookies);
            }
            final int status = con.getResponseCode();
            switch(status) {
                case HttpURLConnection.HTTP_OK: 
                case HttpURLConnection.HTTP_NOT_MODIFIED:
                    try (BufferedInputStream in = new BufferedInputStream(con.getInputStream())) {
                        try (BZip2CompressorInputStream bzip = new BZip2CompressorInputStream(in);
                            TarArchiveInputStream tar = new TarArchiveInputStream(bzip)) {

                            TarArchiveEntry entry;
                            while((entry = tar.getNextTarEntry()) != null) {
                                if (entry.isFile() && "info/recipe/meta.yaml".equals(entry.getName())) {
                                    try {
                                        final Metadata meta = loadYaml(tar);
                                        if (meta != null) {
                                            return meta;
                                        }
                                    } catch(Exception ex) {
                                        System.out.println(toString() + "\n" + ex.getMessage());
                                    }
                                }
                            }
                        } catch (IOException ex) {
                            System.out.println(toString() + "\n" + ex.getMessage());
                            // tar can't detect the EOF - do nothing...
                        }
                    }
                    return null;
                    
                case HttpURLConnection.HTTP_MOVED_TEMP:
                case HttpURLConnection.HTTP_MOVED_PERM:
                case 307: // Temporary Redirect
                case 308: // Permanent Redirect
                case HttpURLConnection.HTTP_SEE_OTHER:
                    final String location = con.getHeaderField("Location");
                    if (location == null || location.isEmpty()) {
                        return null;
                    }
                    cookies = con.getHeaderField("Set-Cookie");
                    URI redirect = URI.create(location);
                    uri = redirect.isAbsolute() ? uri : uri.resolve(redirect);
            }
        }
        
        return null;
    }
            
    private Metadata loadYaml(TarArchiveInputStream tar) throws YAMLException {
        Yaml yaml = new Yaml();
        for (Object object : yaml.loadAll(tar)) {
            if (object instanceof Map) {
                Map<String, Object> map = (Map<String, Object>) object;
                
                String home = null;
                String license = null;
                String summary = null;
                String git = null;
                String src_urls[] = null;
                String identifiers[] = null;
                
                final Object about = map.get("about");
                if (about instanceof Map) {
                    final Map<String, Object> about_map = (Map<String, Object>)about;
                    home = about_map.getOrDefault("home", "").toString();
                    license = about_map.getOrDefault("license", "").toString();
                    summary = about_map.getOrDefault("summary", "").toString();
                }
                
                final Object source = map.get("source");
                if (source instanceof Map) {
                    final Map<String, Object> source_map = (Map<String, Object>) source;
                    git = source_map.getOrDefault("git_url", "").toString();
                    
                    final Object obj = source_map.get("url");
                    if (obj != null) {
                        if (obj instanceof String) {
                            src_urls = new String[]{obj.toString()};
                        } else if (obj instanceof List) {
                            final List list = (List)obj;
                            src_urls = new String[list.size()];
                            for (int i = 0; i < src_urls.length; i++) {
                                src_urls[i] = list.get(i).toString();
                            }
                        }
                    }
                }
                
                final Object extra = map.get("extra");
                if (extra instanceof Map) {
                    final Map<String, Object> extra_map = (Map<String, Object>) extra;
                    final Object obj = extra_map.get("identifiers");
                    if (obj != null) {
                        if (obj instanceof String) {
                            identifiers = new String[]{obj.toString()};
                        } else if (obj instanceof List) {
                            final List list = (List)obj;
                            identifiers = new String[list.size()];
                            for (int i = 0; i < identifiers.length; i++) {
                                identifiers[i] = list.get(i).toString();
                            }
                        }
                    }
                }

                return new Metadata(home, license, summary, git, src_urls, identifiers);
            }
        }
        return null;
    }
    
    @Override
    public String toString() {
        return String.format(SERVER, name, version, platform, file);
    }
    
    public float cmpSummary(String description) {
        if (metadata == null || metadata.summary == null || metadata.summary.isEmpty() ||
            description == null || description.isEmpty()) {
            return 0;
        }
        final int[][] matrix = metadata.summary.length() > 1000 && description.length() > 1000 ?
                                matrix(metadata.summary.length(), description.length()) : 
                                MATRIX;
        final int score = score(matrix, description);
        
        return score <= 0 ? 0 : (float)(score * score)/(metadata.summary.length() * description.length());
    }
    
    private final static int GAP_PENALTY = -1;
    
    private static int[][] matrix(final int x, final int y) {
        final int[][] m = new int[x][y];
        
        for (int i = 0; i < y; i++) {
            m[0][i] = GAP_PENALTY * i;
        }
        for (int i = 0; i < x; i++) {
            m[i][0] = GAP_PENALTY * i;
        }
        
        return m;
    }
    
    private int score(int[][] m, String description) {
        int score = 0;
        for (int i = 1; i < metadata.summary.length(); i++) {
            for (int j = 1, n = description.length(); j < n; j++) {
                score = Math.max(m[i - 1][j - 1] + subst(metadata.summary.charAt(i), description.charAt(j - 1)), 
                        Math.max(m[i - 1][j] + GAP_PENALTY, m[i][j - 1] + GAP_PENALTY));
                m[i][j] = score;
            }
        }

        return score;
    }
    
    private static int subst(final char a, final char b) {
        return b == a ? 1 : -1;
    }


    public static class Metadata {
        
        public final String home;
        public final String license;
        public final String summary;
        public final String git;
        public final String[] src_urls;
        public final String[] identifiers;
        
        public Metadata(String home, String license, String summary, String git, 
                String[] src_urls, String[] identifiers) {
            this.home = home;
            this.license = license;
            this.summary = summary;
            this.git = git;
            this.src_urls = src_urls;
            this.identifiers = identifiers;
        }
        
        @Override
        public String toString() {
            return home;
        }
    }
}
