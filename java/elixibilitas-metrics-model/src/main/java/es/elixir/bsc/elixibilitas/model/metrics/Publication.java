package es.elixir.bsc.elixibilitas.model.metrics;

import javax.json.bind.annotation.JsonbProperty;

/**
 * @author Dmitry Repchevsky
 */

public class Publication {
    
    private String doi;
    
    @JsonbProperty("doi")
    public String getDOI() {
        return doi;
    }
    
    @JsonbProperty("doi")
    public void setDOI(String doi) {
        this.doi = doi;
    }
}
