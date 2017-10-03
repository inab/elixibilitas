package es.elixir.bsc.openebench.model.tools;

import java.net.URI;
import javax.json.bind.annotation.JsonbProperty;

/**
 * @author Dmitry Repchevsky
 */

public class Support {

    private URI helpdesk;
    private URI issueTracker;
    private URI mailingList;
    
    @JsonbProperty("helpdesk")
    public URI getHelpdesk() {
        return helpdesk;
    }
    
    public void setHelpdesk(URI helpdesk) {
        this.helpdesk = helpdesk;
    }
   
    @JsonbProperty("issue_tracker")
    public URI getIssueTracker() {
        return issueTracker;
    }
    
    public void setIssueTracker(URI issueTracker) {
        this.issueTracker = issueTracker;
    }

    @JsonbProperty("mailing_list")
    public URI getMailingList() {
        return mailingList;
    }
    
    public void setMailingList(URI mailingList) {
        this.mailingList = mailingList;
    }

}
