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

import es.elixir.bsc.elixibilitas.model.metrics.License;
import es.elixir.bsc.elixibilitas.model.metrics.Metrics;
import es.elixir.bsc.elixibilitas.model.metrics.Project;
import es.elixir.bsc.openebench.model.tools.Tool;
import es.elixir.bsc.openebench.checker.MetricsChecker;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Dmitry Repchevsky
 */

public class LicenseChecker implements MetricsChecker {

    private final static Set<String> OPEN = Collections.unmodifiableSet(new HashSet<String>(
            Arrays.asList("0BSD", "AAL", "ADSL", "AFL-1.1", "AFL-1.2", "AFL-2.0",
                    "AFL-2.1", "AFL-3.0", "AGPL-1.0", "AGPL-3.0", "AMDPLPA", "AML", 
                    "AMPAS", "ANTLR-PD", "APAFML", "APL-1.0", "APSL-1.0", "APSL-1.1",
                    "APSL-1.2", "APSL-2.0", "Abstyles", "Adobe-2006", "Adobe-Glyph",
                    "Afmparse", "Aladdin", "Apache-1.0", "Apache-1.1", "Apache-2.0",
                    "Artistic-1.0", "Artistic-1.0-Perl", "Artistic-1.0-cl8",
                    "Artistic-2.0", "BSD-2-Clause", "BSD-2-Clause-FreeBSD",
                    "BSD-2-Clause-NetBSD", "BSD-3-Clause", "BSD-3-Clause-Attribution",
                    "BSD-3-Clause-Clear", "BSD-3-Clause-LBNL", "BSD-3-Clause-No-Nuclear-License",
                    "BSD-3-Clause-No-Nuclear-License-2014", "BSD-3-Clause-No-Nuclear-Warranty",
                    "BSD-4-Clause", "BSD-4-Clause-UC", "BSD-Protection", "BSD-Source-Code",
                    "BSL-1.0", "Bahyph", "Barr", "Beerware", "BitTorrent-1.0", "BitTorrent-1.1",
                    "Borceux", "CATOSL-1.1", "CC-BY-1.0", "CC-BY-2.0", "CC-BY-2.5",
                    "CC-BY-3.0", "CC-BY-4.0", "CC-BY-NC-1.0", "CC-BY-NC-2.0", "CC-BY-NC-2.5",
                    "CC-BY-NC-3.0", "CC-BY-NC-4.0", "CC-BY-NC-ND-1.0", "CC-BY-NC-ND-2.0",
                    "CC-BY-NC-ND-2.5", "CC-BY-NC-ND-3.0", "CC-BY-NC-ND-4.0", "CC-BY-NC-SA-1.0",
                    "CC-BY-NC-SA-2.0", "CC-BY-NC-SA-2.5", "CC-BY-NC-SA-3.0", "CC-BY-NC-SA-4.0",
                    "CC-BY-ND-1.0", "CC-BY-ND-2.0", "CC-BY-ND-2.5", "CC-BY-ND-3.0", "CC-BY-ND-4.0",
                    "CC-BY-SA-1.0", "CC-BY-SA-2.0", "CC-BY-SA-2.5", "CC-BY-SA-3.0", "CC-BY-SA-4.0",
                    "CC0-1.0", "CDDL-1.0", "CDDL-1.1", "CECILL-1.0", "CECILL-1.1", "CECILL-2.0",
                    "CECILL-2.1", "CECILL-B", "CECILL-C", "CNRI-Jython", "CNRI-Python",
                    "CNRI-Python-GPL-Compatible", "CPAL-1.0", "CPL-1.0", "CPOL-1.02",
                    "CUA-OPL-1.0", "Caldera", "ClArtistic", "Condor-1.1", "Crossword",
                    "CrystalStacker", "Cube", "D-FSL-1.0", "DOC", "DSDP", "Dotseqn",
                    "ECL-1.0", "ECL-2.0", "EFL-1.0", "EFL-2.0", "EPL-1.0", "EPL-2.0", "EUDatagrid",
                    "EUPL-1.0", "EUPL-1.1", "Entessa", "ErlPL-1.1", "Eurosym", "FSFAP",
                    "FSFUL", "FSFULLR", "FTL", "Fair", "Frameworx-1.0", "FreeImage",
                    "GFDL-1.1", "GFDL-1.2", "GFDL-1.3", "GL2PS", "GPL-1.0", "GPL-2.0",
                    "GPL-3.0", "Giftware", "Glide", "Glulxe", "HPND", "HaskellReport",
                    "IBM-pibs", "ICU", "IJG", "IPA", "IPL-1.0", "ISC", "ImageMagick",
                    "Imlib2", "Info-ZIP", "Intel", "Intel-ACPI", "Interbase-1.0",
                    "JSON", "JasPer-2.0", "LAL-1.2", "LAL-1.3", "LGPL-2.0", "LGPL-2.1",
                    "LGPL-3.0", "LGPLLR", "LPL-1.0", "LPL-1.02", "LPPL-1.0", "LPPL-1.1",
                    "LPPL-1.2", "LPPL-1.3a", "LPPL-1.3c", "Latex2e", "Leptonica",
                    "LiLiQ-P-1.1", "LiLiQ-R-1.1", "LiLiQ-Rplus-1.1", "Libpng", "MIT",
                    "MIT-CMU", "MIT-advertising", "MIT-enna", "MIT-feh",
                    "MITNFA", "MPL-1.0", "MPL-1.1", "MPL-2.0", "MPL-2.0-no-copyleft-exception", 
                    "MS-PL", "MS-RL", "MTLL", "MakeIndex", "MirOS", "Motosoto",
                    "Multics", "Mup", "NASA-1.3", "NBPL-1.0", "NCSA", "NGPL", "NLOD-1.0",
                    "NLPL", "NOSL", "NPL-1.0", "NPL-1.1", "NPOSL-3.0", "NRL", "NTP",
                    "Naumen", "NetCDF", "Newsletr", "Nokia", "Noweb", "Nunit", "OCCT-PL", 
                    "OCLC-2.0", "ODbL-1.0", "OFL-1.0", "OFL-1.1", "OGTSL", "OLDAP-1.1", 
                    "OLDAP-1.2", "OLDAP-1.3", "OLDAP-1.4", "OLDAP-2.0", "OLDAP-2.0.1", 
                    "OLDAP-2.1", "OLDAP-2.2", "OLDAP-2.2.1", "OLDAP-2.2.2", "OLDAP-2.3", 
                    "OLDAP-2.4", "OLDAP-2.5", "OLDAP-2.6", "OLDAP-2.7", "OLDAP-2.8",
                    "OML", "OPL-1.0", "OSET-PL-2.1", "OSL-1.0", "OSL-1.1", "OSL-2.0", 
                    "OSL-2.1", "OSL-3.0", "OpenSSL", "PDDL-1.0", "PHP-3.0", "PHP-3.01",
                    "Plexus", "PostgreSQL", "Python-2.0", "QPL-1.0", "Qhull", "RHeCos-1.1",
                    "RPL-1.1", "RPL-1.5", "RPSL-1.0", "RSA-MD", "RSCPL", "Rdisc", "Ruby",
                    "SAX-PD", "SCEA", "SGI-B-1.0", "SGI-B-1.1", "SGI-B-2.0", "SISSL",
                    "SISSL-1.2", "SMLNJ", "SMPPL", "SNIA", "SPL-1.0", "SWL", "Saxpath",
                    "Sendmail", "SimPL-2.0", "Sleepycat", "Spencer-86", "Spencer-94",
                    "Spencer-99", "SugarCRM-1.1.3", "TCL", "TMate", "TORQUE-1.1",
                    "TOSL", "UPL-1.0", "Unicode-TOU", "Unlicense", "VOSTROM", "VSL-1.0",
                    "Vim", "W3C", "W3C-19980720", "WTFPL", "Watcom-1.0", "Wsuipa",
                    "X11", "XFree86-1.1", "XSkat", "Xerox", "Xnet", "YPL-1.0", "YPL-1.1",
                    "ZPL-1.1", "ZPL-2.0", "ZPL-2.1", "Zed", "Zend-2.0", "Zimbra-1.3", 
                    "Zimbra-1.4", "Zlib", "bzip2-1.0.5", "bzip2-1.0.6", "curl", "diffmark",
                    "dvipdfm", "eGenix", "gSOAP-1.3b", "gnuplot", "iMatix", "libtiff",
                    "mpich2", "psfrag", "psutils", "xinetd", "xpp", "zlib-acknowledgement")));

    private final static Set<String> OSI = Collections.unmodifiableSet(new HashSet<String>(
            Arrays.asList("0BSD", "AAL", "AFL-3.0", "AGPL-3.0", "APL-1.0", "APSL-2.0", 
                    "Apache-2.0", "Artistic-2.0", "BSD-2-Clause", "BSD-2-Clause-FreeBSD",
                    "BSD-2-Clause-NetBSD", "BSD-3-Clause", "BSD-3-Clause-Attribution",
                    "BSD-3-Clause-Clear", "BSD-3-Clause-LBNL", "BSD-3-Clause-No-Nuclear-License",
                    "BSD-3-Clause-No-Nuclear-License-2014", "BSD-3-Clause-No-Nuclear-Warranty",
                    "BSD-4-Clause", "BSD-4-Clause-UC", "BSD-Protection", "BSD-Source-Code",
                    "BSL-1.0", "CATOSL-1.1", "CECILL-2.1", "CNRI-Python", "CPAL-1.0", 
                    "CUA-OPL-1.0", "ECL-2.0", "EFL-2.0", "EPL-1.0", "EPL-2.0", "EUDatagrid",
                    "EUPL-1.1", "Entessa", "Fair", "Frameworx-1.0", "GPL-2.0", "GPL-3.0", 
                    "HPND", "IPA", "IPL-1.0", "ISC", "LGPL-2.1", "LGPL-3.0", "LPL-1.02", 
                    "LPPL-1.3c", "LiLiQ-P-1.1", "LiLiQ-R-1.1", "LiLiQ-Rplus-1.1", "Libpng", 
                    "MIT", "MPL-1.0", "MPL-1.1", "MPL-2.0", "MS-PL", "MS-RL", "MirOS", "Motosoto",
                    "Multics", "Mup", "NASA-1.3", "NCSA", "NGPL", "NPOSL-3.0", "NTP",
                    "Naumen", "Nokia", "OCLC-2.0", "OFL-1.1", "OGTSL", "OSET-PL-2.1", "OSL-3.0", 
                    "PHP-3.0", "PostgreSQL", "Python-2.0", "QPL-1.0", "RPL-1.5", "RPSL-1.0", "RSCPL",
                    "SPL-1.0", "SimPL-2.0", "Sleepycat", "VSL-1.0", "W3C", "Watcom-1.0", "Xnet",
                    "ZPL-2.0", "Zlib")));

    @Override
    public Boolean check(Tool tool, Metrics metrics) {
        License license = check(tool);
        
        Project project = metrics.getProject();
        if (license != null) {
            if (project == null) {
                metrics.setProject(project = new Project());
            }
            project.setLicense(license);
        } else if (project != null) {
            project.setLicense(null);
        }
        
        return license != null;
    }
    
    private static License check(Tool tool) {
        final String lic = tool.getLicense();
        
        if (lic == null || lic.isEmpty()) {
            return null;
        }
        
        final License license = new License();
        license.setOsi(OSI.contains(lic));
        license.setOpenSource(OPEN.contains(lic));
        
        return license;
    }
}