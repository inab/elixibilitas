/**
 * *****************************************************************************
 * Copyright (C) 2017 ELIXIR ES, Spanish National Bioinformatics Institute (INB)
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
import java.util.ArrayList;
import java.util.List;
import javax.json.bind.annotation.JsonbProperty;

/**
 * @author Dmitry Repchevsky
 */

public class Documentation {
    
    private List<URI> docLinks;
    private URI help;
    private URI faq;
    private URI general;
    private URI build;
    private URI manual;
    private URI tutorial;
    private URI api;
    private URI termsOfUse;
    private URI trainingMaterial;
    private URI citationInstructions;
    
    @JsonbProperty("doc_links")
    public List<URI> getDocumentationLinks() {
        if (docLinks == null) {
            docLinks = new ArrayList<>();
        }
        return docLinks;
    }

    @JsonbProperty("doc_links")
    public void setDocumentationLinks(List<URI> docLinks) {
        this.docLinks = docLinks;
    }

    @JsonbProperty("faq")
    public URI getFaq() {
        return faq;
    }

    @JsonbProperty("faq")
    public void setFaq(URI faq) {
        this.faq = faq;
    }

    @JsonbProperty("help")
    public URI getHelp() {
        return help;
    }

    @JsonbProperty("help")
    public void setHelp(URI help) {
        this.help = help;
    }
    
    @JsonbProperty("general")
    public URI getGeneralDocumentation() {
        return general;
    }

    @JsonbProperty("general")
    public void setGeneralDocumentation(URI general) {
        this.general = general;
    }

    @JsonbProperty("tutorial")
    public URI getTutorial() {
        return tutorial;
    }

    @JsonbProperty("build_instructions")
    public void setBuildInstructions(URI build) {
        this.build = build;
    }

    @JsonbProperty("build_instructions")
    public URI getBuildInstructions() {
        return build;
    }

    @JsonbProperty("tutorial")
    public void setTutorial(URI tutorial) {
        this.tutorial = tutorial;
    }

    @JsonbProperty("manual")
    public URI getManual() {
        return manual;
    }

    @JsonbProperty("manual")
    public void setManual(URI manual) {
        this.manual = manual;
    }
    
    @JsonbProperty("api")
    public URI getAPIDocumentation() {
        return api;
    }

    @JsonbProperty("api")
    public void setAPIDocumentation(URI api) {
        this.api = api;
    }

    @JsonbProperty("terms_of_use")
    public URI getTermsOfUse() {
        return termsOfUse;
    }

    @JsonbProperty("terms_of_use")
    public void setTermsOfUse(URI termsOfUse) {
        this.termsOfUse = termsOfUse;
    }

    @JsonbProperty("training_material")
    public URI getTrainingMaterial() {
        return trainingMaterial;
    }

    @JsonbProperty("training_material")
    public void setTrainingMaterial(URI trainingMaterial) {
        this.trainingMaterial = trainingMaterial;
    }
    
    @JsonbProperty("citation_instructions")
    public URI getCitationInstructions() {
        return citationInstructions;
    }

    @JsonbProperty("citation_instructions")
    public void setCitationInstructions(URI citationInstructions) {
        this.citationInstructions = citationInstructions;
    }
}
