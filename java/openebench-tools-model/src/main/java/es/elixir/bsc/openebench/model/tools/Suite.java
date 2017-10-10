package es.elixir.bsc.openebench.model.tools;

import java.net.URI;
import javax.json.bind.annotation.JsonbCreator;
import javax.json.bind.annotation.JsonbProperty;

/**
 * @author Dmitry Repchevsky
 */

public class Suite extends Tool {

    @JsonbCreator
    public Suite(@JsonbProperty("@id") URI id) {
        super(id, "suite");
    }
}