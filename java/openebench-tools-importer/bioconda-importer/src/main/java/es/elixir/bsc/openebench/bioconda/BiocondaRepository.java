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

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Dmitry Repchevsky
 */

public class BiocondaRepository {
    
    public final static String LINUX64_REPO = "https://conda.anaconda.org/bioconda/linux-64/repodata.json.bz2";
    public final static String OSX64_REPO = "https://conda.anaconda.org/bioconda/osx-64/repodata.json.bz2";
    
    
    private static volatile Map<BiocondaPackage, BiocondaPackage> packages;
        
    public static BiocondaPackage getPackage(String name, String version) {
        Map<BiocondaPackage, BiocondaPackage> packages = BiocondaRepository.packages;
        if (packages == null) {
            synchronized(BiocondaRepository.class) {
                packages = BiocondaRepository.packages;
                if (packages == null) {
                    BiocondaRepository.packages = packages = load();
                }
            }
        }
        
        return packages.get(new BiocondaPackage(name, version));
    }
    
    public static Collection<BiocondaPackage> getPackages() {
        if (packages == null) {
            synchronized(BiocondaRepository.class) {
                if (packages == null) {
                    packages = load();
                }
            }
        }
        
        return packages.values();
    }
    
    private static Map<BiocondaPackage, BiocondaPackage> load() {
        final Map<BiocondaPackage, BiocondaPackage> map = new ConcurrentHashMap<>();
        
        load(LINUX64_REPO, map);
        load(OSX64_REPO, map);

        return map;
    }
    
    private static void load(final String repo, final Map<BiocondaPackage, BiocondaPackage> map) {
        try (BiocondaRepositoryIterator iter = new BiocondaRepositoryIterator(repo)) {
            while(iter.hasNext()) {
                final BiocondaPackage pack = iter.next();
                if (pack != null) {
                    map.put(pack, pack);
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(BiocondaRepository.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
