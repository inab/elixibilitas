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

package es.elixir.bsc.openebench.tools;

import es.elixir.bsc.openebench.model.tools.Distributions;
import es.elixir.bsc.openebench.model.tools.Tool;
import java.net.URI;
import java.util.Map;


/**
 * @author Dmitry Repchevsky
 */

public class ToolsComparator {
    
    private final static int[][] matrix = matrix(1000, 1000);
        
    public static double compare(Tool t1, Tool t2) {
        final double[] scores = new double[] {
                                    cmpHomepages(t1, t2),
                                    cmpDescriptions(t1, t2),
                                    cmpSources(t1, t2),
                                    cmpBinaries(t1, t2)
                                };
        
        double score = 0;
        for (int i = 0; i < scores.length; i++) {
            score += (1 - score) * scores[i];
        }
        
        return score;
    }
    
    private static double cmpSources(Tool t1, Tool t2) {
        
        final Distributions dist1 = t1.getDistributions();
        final Distributions dist2 = t2.getDistributions();
        if (dist1 == null || dist2 == null) {
            return 0;
        }
        double score = 0;
        for (URI uri1 : dist1.getSourcecodeDistributions()) {
            for (URI uri2 : dist2.getSourcecodeDistributions()) {
                score = Math.max(score, cmpURIs(uri1, uri2));
            }
        }
        
        for (URI uri1 : dist1.getSourcePackagesDistributions()) {
            for (URI uri2 : dist2.getSourcePackagesDistributions()) {
                score = Math.max(score, cmpURIs(uri1, uri2));
            }
        }
        
        return score * 0.9;
    }

    private static double cmpBinaries(Tool t1, Tool t2) {
        
        final Distributions dist1 = t1.getDistributions();
        final Distributions dist2 = t2.getDistributions();
        if (dist1 == null || dist2 == null) {
            return 0;
        }
        double score = 0;
        for (URI uri1 : dist1.getBinaryDistributions()) {
            for (URI uri2 : dist2.getBinaryDistributions()) {
                score = Math.max(score, cmpURIs(uri1, uri2));
            }
        }
        
        for (URI uri1 : dist1.getBinaryPackagesDistributions()) {
            for (URI uri2 : dist2.getBinaryPackagesDistributions()) {
                score = Math.max(score, cmpURIs(uri1, uri2));
            }
        }
        
        return score * 0.9;
    }
    
    private static double cmpHomepages(Tool t1, Tool t2) {
        final URI home1 = t1.getHomepage();
        final URI home2 = t2.getHomepage();
        
        return cmpURIs(home1, home2);
    }
    
    private static double cmpURIs(URI uri1, URI uri2) {
        if (uri1 == null || uri2 == null) {
            return 0;
        }

        if (uri1.equals(uri2)) {
            return 1;
        }

        final String host1 = uri1.getHost();
        final String host2 = uri2.getHost();

        if (host1 == null || host2 == null) {
            return 0;
        }
        
        String path1 = uri1.getPath();
        String path2 = uri2.getPath();
        
        if (path1 == null || path1.length() <= 1 || path2 == null || path2.length() <= 1) {
            return host1.equals(host2) ? 0.05 : 0;
        }
        
        final String query1 = uri1.getQuery();
        if (query1 != null) {
            path1 += query1;
        }
        
        final String query2 = uri2.getQuery();
        if (query2 != null) {
            path2 += query2;
        }
        
        int i = -1;
        final int n = Math.min(path1.length(), path2.length());
        while (++i < n && path1.charAt(i) == path2.charAt(i)) {}
        
        final double score = (i * i) / (path1.length() * path2.length());
        
        return host1.equals(host2) ? score * 0.9 : score * 0.05;        
    }
    
    private static double cmpNames(Tool t1, Tool t2) {
        final String name1 = t1.getName();
        final String name2 = t2.getName();
        if (name1 == null || name1.isEmpty() ||
            name2 == null || name2.isEmpty()) {
            return 0;
        }
        final int[][] m = name1.length() > 1000 && name2.length() > 1000 ?
                                matrix(name1.length(), name2.length()) :
                                matrix;
        final int score = score(m, name1, name2);
        
        return score <= 0 ? 0 : (float)(score * score)/(name1.length() * name2.length());
    }
    
    private static double cmpDescriptions(Tool t1, Tool t2) {
    
        final String descr1 = t1.getDescription();
        final String descr2 = t2.getDescription();
        if (descr1 == null || descr1.isEmpty() ||
            descr2 == null || descr2.isEmpty()) {
            return 0;
        }
        
        final int[][] m = descr1.length() > 1000 || descr2.length() > 1000 ?
                                matrix(descr1.length(), descr2.length()) :
                                matrix;
        final int score = score(m, descr1, descr2);
        
        return score <= 0 ? 0 : (float)(score * score)/(descr1.length() * descr2.length());
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
    
    private static int score(int[][] m, String descr1, String descr2) {
        int score = 0;
        for (int i = 1; i < descr1.length(); i++) {
            for (int j = 1, n = descr2.length(); j < n; j++) {
                score = Math.max(m[i - 1][j - 1] + subst(descr1.charAt(i), descr2.charAt(j - 1)),
                        Math.max(m[i - 1][j] + GAP_PENALTY, m[i][j - 1] + GAP_PENALTY));
                m[i][j] = score;
            }
        }

        return score;
    }
    
    private static int subst(final char a, final char b) {
        return b == a ? 1 : -1;
    }
}
