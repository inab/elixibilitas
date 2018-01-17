/**
 * *****************************************************************************
 * Copyright (C) 2018 ELIXIR ES, Spanish National Bioinformatics Institute (INB)
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
