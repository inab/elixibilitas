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
    private List<URI> binary_packages;

    private List<URI> sourcecode;
    private List<URI> source_packages;

    private List<Container> containers;
    
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
        if (binary_packages == null) {
            binary_packages = new ArrayList<>();
        }
        return binary_packages;
    }

    @JsonbProperty("binary_packages")
    public void setBinaryPackagesDistributions(List<URI> binary_packages) {
        this.binary_packages = binary_packages;
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
    
    @JsonbProperty("source_packages")
    public List<URI> getSourcePackagesDistributions() {
        if (source_packages == null) {
            source_packages = new ArrayList<>();
        }
        return source_packages;
    }

    @JsonbProperty("source_packages")
    public void setSourcePackagesDistributions(List<URI> source_packages) {
        this.source_packages = source_packages;
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
}
