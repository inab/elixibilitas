package es.elixir.bsc.openebench.model.tools;

import java.net.URI;
import javax.json.bind.annotation.JsonbCreator;
import javax.json.bind.annotation.JsonbProperty;

/**
 * @author Dmitry Repchevsky
 */

public class Workbench extends Tool {

    @JsonbCreator
    public Workbench(@JsonbProperty("@id") URI id) {
        super(id, "workbench");
    }
}