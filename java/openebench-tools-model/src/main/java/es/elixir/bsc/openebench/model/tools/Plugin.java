package es.elixir.bsc.openebench.model.tools;

import java.net.URI;
import javax.json.bind.annotation.JsonbCreator;
import javax.json.bind.annotation.JsonbProperty;

/**
 * @author Dmitry Repchevsky
 */

public class Plugin extends Tool {

    @JsonbCreator
    public Plugin(@JsonbProperty("@id") URI id) {
        super(id, "plugin");
    }
}