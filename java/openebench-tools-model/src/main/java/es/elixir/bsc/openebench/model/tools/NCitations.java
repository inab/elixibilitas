package es.elixir.bsc.openebench.model.tools;

import javax.json.bind.annotation.JsonbProperty;

/**
 * @author Dmitry Repchevsky
 */

public class NCitations {
    
    private String year;
    private int cited;
    
    @JsonbProperty("year")
    public String getYear() {
        return year;
    }
    
    @JsonbProperty("year")
    public void setYear(String year) {
        this.year = year;
    }
    
    @JsonbProperty("cited")
    public int getCited() {
        return cited;
    }
    
    @JsonbProperty("cited")
    public void setCited(int cited) {
        this.cited = cited;
    }
}
