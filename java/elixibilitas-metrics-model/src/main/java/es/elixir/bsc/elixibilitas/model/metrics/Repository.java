package es.elixir.bsc.elixibilitas.model.metrics;

import javax.json.bind.annotation.JsonbProperty;

/**
 * @author Dmitry Repchevsky
 */

public class Repository {

    private Boolean anonymous;
    private Boolean online;
     
    @JsonbProperty("anonymous")
    public Boolean getAnonymous() {
        return anonymous;
    }

    public void setAnonymous(Boolean anonymous) {
        this.anonymous = anonymous;
    }

    @JsonbProperty("online")
    public Boolean getOnline() {
        return online;
    }

    public void setOnline(Boolean online) {
        this.online = online;
    }

}

