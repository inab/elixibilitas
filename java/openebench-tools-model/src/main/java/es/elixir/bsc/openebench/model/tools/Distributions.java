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

package es.elixir.bsc.openebench.model.tools;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import javax.json.bind.annotation.JsonbProperty;

/**
 * @author Dmitry Repchevsky
 */

public class Distributions {
    
    private List<URI> binaries;
    private List<URI> binaryPackages;

    private List<URI> sourcecode;
    private List<URI> sourcePackages;

    private List<URI> vre;
    
    private List<Container> containers;
    private List<VMImage> vmImages;
    
    @JsonbProperty("binaries")
    public List<URI> getBinaryDistributions() {
        if (binaries == null) {
            binaries = new ArrayList<>();
        }
        return binaries;
    }

    @JsonbProperty("binaries")
    public void setBinaryDistributions(List<URI> binaries) {
        this.binaries = binaries;
    }
    
    @JsonbProperty("binary_packages")
    public List<URI> getBinaryPackagesDistributions() {
        if (binaryPackages == null) {
            binaryPackages = new ArrayList<>();
        }
        return binaryPackages;
    }

    @JsonbProperty("binary_packages")
    public void setBinaryPackagesDistributions(List<URI> binaryPackages) {
        this.binaryPackages = binaryPackages;
    }

    @JsonbProperty("sourcecode")
    public List<URI> getSourcecodeDistributions() {
        if (sourcecode == null) {
            sourcecode = new ArrayList<>();
        }
        return sourcecode;
    }

    @JsonbProperty("sourcecode")
    public void setSourcecodeDistributions(List<URI> sourcecode) {
        this.sourcecode = sourcecode;
    }
    
    @JsonbProperty("vre")
    public void setVirtualResearchEnvironment(List<URI> vre) {
        this.vre = vre;
    }
    
    @JsonbProperty("vre")
    public List<URI> getVirtualResearchEnvironment() {
        if (vre == null) {
            vre = new ArrayList<>();
        }
        return vre;
    }
    
    @JsonbProperty("source_packages")
    public List<URI> getSourcePackagesDistributions() {
        if (sourcePackages == null) {
            sourcePackages = new ArrayList<>();
        }
        return sourcePackages;
    }

    @JsonbProperty("source_packages")
    public void setSourcePackagesDistributions(List<URI> sourcePackages) {
        this.sourcePackages = sourcePackages;
    }
    
    @JsonbProperty("containers")
    public List<Container> getContainers() {
        if (containers == null) {
            containers = new ArrayList<>();
        }
        return containers;
    }

    @JsonbProperty("containers")
    public void setContainers(List<Container> containers) {
        this.containers = containers;
    }
    
    @JsonbProperty("vm_images")
    public List<VMImage> getVirtualMachineImages() {
        if (vmImages == null) {
            vmImages = new ArrayList<>();
        }
        return vmImages;
    }

    @JsonbProperty("vm_images")
    public void setVirtualMachineImages(List<VMImage> vmImages) {
        this.vmImages = vmImages;
    }
}
