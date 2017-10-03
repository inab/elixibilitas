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

/**
 * @author Dmitry Repchevsky
 */

public class LicenseChecker implements MetricsChecker {

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
//        Labels labels = tool.getLabels();
//        if (labels != null) {
//            LicenseType licenseType = labels.getLicense();
//            if (licenseType != null) {
//                License license = new License();
//                switch(licenseType) {
//                    case PROPRIETARY: license.setOpenSource(false); break;
//                    case OTHER: break;
//                    default: license.setOpenSource(true);
//                }
//                return license;
//            }
//        }
        return null;
    }
}