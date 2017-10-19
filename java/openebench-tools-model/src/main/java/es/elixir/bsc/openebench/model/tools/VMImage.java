package es.elixir.bsc.openebench.model.tools;

import java.net.URI;
import javax.json.bind.annotation.JsonbCreator;
import javax.json.bind.annotation.JsonbProperty;

/**
 * @author Dmitry Repchevsky
 */

public class VMImage {

    @JsonbProperty("type")
    public final String type;
    private URI uri;
    
    @JsonbCreator
    public VMImage(@JsonbProperty("type") String type) {
        this.type = type;
    }
    
    @JsonbProperty("uri")
    public URI getURI() {
        return uri;
    }

    public void setURI(URI uri) {
        this.uri = uri;
    }

}
