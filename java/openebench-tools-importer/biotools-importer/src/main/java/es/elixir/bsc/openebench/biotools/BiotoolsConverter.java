/**
 * *****************************************************************************
 * Copyright (C) 2020 ELIXIR ES, Spanish National Bioinformatics Institute (INB)
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

package es.elixir.bsc.openebench.biotools;

import es.bsc.inb.elixir.biotools.model.CostType;
import es.bsc.inb.elixir.biotools.model.DocumentationType;
import es.bsc.inb.elixir.biotools.model.DownloadType;
import es.bsc.inb.elixir.biotools.model.EntityType;
import es.bsc.inb.elixir.biotools.model.LicenseType;
import es.bsc.inb.elixir.biotools.model.MaturityType;
import es.bsc.inb.elixir.biotools.model.OperatingSystemType;
import es.bsc.inb.elixir.biotools.model.RoleType;
import es.bsc.inb.elixir.biotools.model.ToolLinkType;
import es.bsc.inb.elixir.openebench.model.tools.Community;
import es.bsc.inb.elixir.openebench.model.tools.Contact;
import es.bsc.inb.elixir.openebench.model.tools.Container;
import es.bsc.inb.elixir.openebench.model.tools.Credit;
import es.bsc.inb.elixir.openebench.model.tools.Datatype;
import es.bsc.inb.elixir.openebench.model.tools.Distributions;
import es.bsc.inb.elixir.openebench.model.tools.Documentation;
import es.bsc.inb.elixir.openebench.model.tools.Publication;
import es.bsc.inb.elixir.openebench.model.tools.Semantics;
import es.bsc.inb.elixir.openebench.model.tools.Support;
import es.bsc.inb.elixir.openebench.model.tools.Tool;
import es.bsc.inb.elixir.openebench.model.tools.ToolType;
import es.bsc.inb.elixir.openebench.model.tools.VMImage;
import es.bsc.inb.elixir.openebench.model.tools.Web;
import es.bsc.inb.elixir.openebench.repository.OpenEBenchEndpoint;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonString;

/**
 * @author Dmitry Repchevsky
 */

public class BiotoolsConverter {
   
    public static List<Tool> convert(JsonObject jtool) {
        
        final String id = jtool.getString("biotoolsID", null);
        if (id == null || id.isEmpty()) {
            return Collections.EMPTY_LIST;
        }

        final List<Tool> tools = new ArrayList<>();

        final String _id = id.toLowerCase().trim();
        
        final String name = jtool.getString("name", null);
        final String jhomepage = jtool.getString("homepage", null);
        
        /* inset 'generic' tool with @type: null*/
        Tool generic_tool = new Tool(URI.create(OpenEBenchEndpoint.TOOL_URI_BASE + _id), null);
        generic_tool.setName(name);
        if (jhomepage != null) {
            try {
                Web web = new Web();
                web.setHomepage(new URI(jhomepage));
                generic_tool.setWeb(web);
            } catch(URISyntaxException ex) {
                Logger.getLogger(BiotoolsConverter.class.getName()).log(Level.INFO, "invalid homepage: {0}", jhomepage);
            }
        }

        generic_tool.setDescription(jtool.getString("description", null));
        tools.add(generic_tool);

        final Set<String> tags = new HashSet<>();

        final JsonArray versions = jtool.getJsonArray("version");
        for (int j = 0, m = (versions == null || versions.isEmpty()) ? 1 : versions.size(); j < m; j++) {
            StringBuilder idTemplate = new StringBuilder(OpenEBenchEndpoint.TOOL_URI_BASE).append("biotools:").append(_id);

            final String version = (versions != null && versions.size() > 0) ? versions.getJsonString(j).getString() : null;
            if (version != null) {
                idTemplate.append(':').append(version.replace(' ', '_'));
            }

            idTemplate.append("/%s");
            
            URI homepage = null;
            if (jhomepage != null) {
                try {
                    homepage = URI.create(jhomepage);
                    idTemplate.append('/').append(homepage.getHost());
                } catch (IllegalArgumentException ex) {
                    Logger.getLogger(BiotoolsConverter.class.getName()).log(Level.INFO, "invalid homepage: {0}", jhomepage);
                }
            }

            JsonArray jtoolTypes = jtool.getJsonArray("toolType");
            for (int i = 0, n = jtoolTypes.size(); i < n; i++) {
                final String tool_type;
                final JsonString jtoolType = jtoolTypes.getJsonString(i);
                try {
                    switch(es.bsc.inb.elixir.biotools.model.ToolType.fromValue(jtoolType.getString())) {
                        case COMMAND_LINE: tool_type = ToolType.COMMAND_LINE.type; break;
                        case WEB_APPLICATION: tool_type = ToolType.WEB_APPLICATION.type; break;
                        case DESKTOP_APPLICATION: tool_type = ToolType.DESKTOP_APPLICATION.type; break;
                        case DATABASE_PORTAL: tool_type = ToolType.DATABASE_PORTAL.type; break;
                        case LIBRARY: tool_type = ToolType.LIBRARY.type; break;
                        case WEB_SERVICE: tool_type = ToolType.WEB_SERVICE.type; break;
                        case WEB_API: tool_type = ToolType.WEB_API.type; break;
                        case SPARQL_ENDPOINT: tool_type = ToolType.SPARQL_ENDPOINT.type; break;
                        case ONTOLOGY: tool_type = ToolType.ONTOLOGY.type; break;
                        case WORKFLOW: tool_type = ToolType.WORKFLOW.type; break;
                        case SCRIPT: tool_type = ToolType.SCRIPT.type; break;
                        case PLUGIN: tool_type = ToolType.PLUGIN.type; break;
                        case SUITE: tool_type = ToolType.SUITE.type; break;
                        case WORKBENCH: tool_type = ToolType.WORKBENCH.type; break;

                        default: continue;
                    }
                } catch(IllegalArgumentException ex) {
                    Logger.getLogger(BiotoolsConverter.class.getName()).log(Level.INFO, "unrecognized toolType: {0} for the {1}", new String[] {jtoolType.getString(), name});
                    continue;
                }
                final Tool tool = new Tool(URI.create(String.format(idTemplate.toString(), tool_type)), tool_type);
                
                if (!id.equals(_id)) {
                    tool.setExternalId(version == null || version.isEmpty() ? id : id + ":" + version);
                }

                tool.setName(name);
                if (homepage != null) {
                    Web web = new Web();
                    web.setHomepage(homepage);
                    tool.setWeb(web);
                }

                tool.setDescription(jtool.getString("description", null));

                setLanguages(tool.getLanguages(), jtool);
                setOperatingSystems(tool.getOperatingSystems(), jtool);

                addTags(tool, jtool);
                tags.addAll(tool.getTags());
                
                addDocumentation(tool, jtool);
                addPublications(tool, jtool);
//                addContacts(tool, jtool);
                addCredits(tool, jtool);
                addSemantics(tool, jtool);
                addDownloads(tool, jtool);
                addLinks(tool, jtool);
                setLicense(tool, jtool);
                setMaturity(tool, jtool);
                setCost(tool, jtool);

    //                    addOperatingSystems(tool, jtool);
    //                    addAccessibility(tool, jtool);
    //                    addCollectionIDs(tool, jtool);

                tools.add(tool);
            }
        }
        
        if (!tags.isEmpty()) {
            generic_tool.getTags().addAll(tags);
        }
        
        return tools;
    }
    
    private static void addTags(Tool tool, JsonObject jtool) {
        final JsonArray jcollections = jtool.getJsonArray("collectionID");
        if (jcollections.size() > 0) {
            for (int i = 0, n = jcollections.size(); i < n; i++) {
                final JsonString collectionID = jcollections.getJsonString(i);
                tool.getTags().add(collectionID.getString());
            }
        }
    }
    
    private static void addDocumentation(Tool tool, JsonObject jtool) {
        final JsonArray jdocumentations = jtool.getJsonArray("documentation");
        if (jdocumentations.size() > 0) {
            final Documentation documentation = new Documentation();
            tool.setDocumentation(documentation);
            for (int i = 0, n = jdocumentations.size(); i < n; i++) {
                final JsonObject jdocumentation = jdocumentations.getJsonObject(i);
                final String url = jdocumentation.getString("url", null);
                if (url != null) {
                    final String type = jdocumentation.getString("type", null);
                    if (type != null) {
                        try {
                            final URI uri = URI.create(url);
                            final DocumentationType documentationType = DocumentationType.fromValue(type);
                            switch (documentationType) {
                                case API_DOCUMENTATION: documentation.setAPIDocumentation(uri); break;
                                case CITATION_INSTRUCTIONS: documentation.setCitationInstructions(uri); break;
                                case CODE_OF_CONDUCT: documentation.setCodeOfConduct(uri); break;
                                case COMMANDLINE_OPTIONS: documentation.setCommandlineOptions(uri); break;
                                case CONTRIBUTIONS_POLICY: documentation.setContributionsPolicy(uri); break;
                                case FAQ: documentation.setFaq(uri); break;
                                case GENERAL: documentation.setGeneral(uri); break;
                                case GOVERNANCE: documentation.setGovernance(uri); break;
                                case INSTALLATION_INSTRUCTIONS: documentation.setInstallationInstructions(uri); break;
                                case USER_MANUAL: documentation.setManual(uri); break;
                                case RELEASE_NOTES: documentation.setReleaseNotes(uri); break;
                                case TERMS_OF_USE: documentation.setTermsOfUse(uri); break;
                                case TRAINING_MATERIAL: documentation.setTrainingMaterial(uri); break;
                                case OTHER: documentation.getDocumentationLinks().add(uri); break;
                            }

                         } catch(IllegalArgumentException ex) {
                             Logger.getLogger(BiotoolsConverter.class.getName()).log(Level.INFO, "unrecognized type: {0}", type);
                         }
                    }
                }
            }
        }
    }
    
    private static void addPublications(Tool tool, JsonObject jtool) {
        JsonArray jpublications = jtool.getJsonArray("publication");
        for (int i = 0, n = jpublications.size(); i < n; i++) {
            Publication publication = new Publication();
            JsonObject jpublication = jpublications.getJsonObject(i);
            
//            try {
//                publication.setType(PublicationType.fromValue(jpublication.getString("type", "")));
//            } catch(IllegalArgumentException ex) {}

            publication.setDOI(jpublication.getString("doi", null));
            publication.setPmid(jpublication.getString("pmid", null));
            publication.setPMCID(jpublication.getString("pmcid", null));
            
            tool.getPublications().add(publication);
        }
    }
    
    private static void addContacts(Tool tool, JsonObject jtool) {
        JsonArray jcontacts = jtool.getJsonArray("contact");
        if (jcontacts != null) {
            for (int i = 0, n = jcontacts.size(); i < n; i++) {
                JsonObject jcontact = jcontacts.getJsonObject(i);
                Contact contact = new Contact();

                contact.setName(jcontact.getString("name", null));
                contact.setEmail(jcontact.getString("email", null));
                contact.setPhone(jcontact.getString("tel", null));

                final String url = jcontact.getString("url", null);
                if (url != null) {
                    try {
                        contact.setUrl(URI.create(url));
                    } catch(IllegalArgumentException ex) {
                        Logger.getLogger(BiotoolsConverter.class.getName()).log(Level.INFO, "incorrect contact url: {0}", url);
                    }
                }

                tool.getContacts().add(contact);
            }
        }
    }

    private static void addCredits(Tool tool, JsonObject jtool) {
        JsonArray jcredits= jtool.getJsonArray("credit");
        for (int i = 0, n = jcredits.size(); i < n; i++) {
            JsonObject jcredit = jcredits.getJsonObject(i);
            Credit credit = new Credit();
            
            credit.setName(jcredit.getString("name", null));
            
            credit.setEmail(jcredit.getString("email", null));
//            credit.setGridId(jcredit.getString("gridId", null));
            credit.setOrcid(jcredit.getString("orcidId", null));
            credit.setComment(jcredit.getString("comment", null));
            
            final String type = jcredit.getString("typeEntity", null);
            if (type != null) {
                try {
                    EntityType.fromValue(type);
                    credit.setType(type);
                } catch(IllegalArgumentException ex) {
                    Logger.getLogger(BiotoolsConverter.class.getName()).log(Level.INFO, "unrecognized credit type: {0}", type);
                }
            }
            
            final String role = jcredit.getString("typeRole", null);
            if (role != null) {
                try {
                    RoleType.fromValue(role);
                    credit.setRole(role);
                } catch(IllegalArgumentException ex) {
                    Logger.getLogger(BiotoolsConverter.class.getName()).log(Level.INFO, "unrecognized credit role: {0}", type);
                }
            }
            
            final String url = jcredit.getString("url", null);
            if (url != null) {
                try {
                    credit.setUrl(URI.create(url));
                } catch(IllegalArgumentException ex) {
                    Logger.getLogger(BiotoolsConverter.class.getName()).log(Level.INFO, "incorrect credit url: {0}", url);
                }
            }
            
            tool.getCredits().add(credit);
        }
    }

    private static void addSemantics(Tool tool, JsonObject jtool) {
        Semantics semantics = new Semantics();
        
        addTopics(semantics, jtool);
        
        JsonArray jfunctions = jtool.getJsonArray("function");
        for (int i = 0, n = jfunctions.size(); i < n; i++) {
            JsonObject jfunction = jfunctions.getJsonObject(i);
            addOperations(semantics, jfunction);
            
            JsonArray jinputs = jfunction.getJsonArray("input");
            for (int j = 0, m = jinputs.size(); j < m; j++) {
                Datatype input = new Datatype();
                addDatatype(input, jinputs.getJsonObject(j));
                semantics.getInputs().add(input);
            }

            JsonArray joutputs = jfunction.getJsonArray("output");
            for (int j = 0, m = joutputs.size(); j < m; j++) {
                Datatype output = new Datatype();
                addDatatype(output, joutputs.getJsonObject(j));
                semantics.getOutputs().add(output);
            }
        }

        tool.setSemantics(semantics);
    }
    
    private static void addTopics(Semantics semantics, JsonObject jtool) {
        final JsonArray jtopics = jtool.getJsonArray("topic");
        for (int i = 0, n = jtopics.size(); i < n; i++) {
            final JsonObject jtopic = jtopics.getJsonObject(i);
            final String topic = jtopic.getString("uri", null);
            if (topic != null) {
                try {
                    final URI uri = URI.create(topic);
                    semantics.getTopics().add(uri);
                } catch(IllegalArgumentException ex) {
                    Logger.getLogger(BiotoolsConverter.class.getName()).log(Level.INFO, "incorrect topic uri: {0}", topic);
                }
            }
        }
    }

    private static void addOperations(Semantics semantics, JsonObject jfunction) {
        final JsonArray joperations = jfunction.getJsonArray("operation");
        for (int i = 0, n = joperations.size(); i < n; i++) {
            final JsonObject joperation = joperations.getJsonObject(i);
            final String operation = joperation.getString("uri", null);
            if (operation != null) {
                try {
                    final URI uri = URI.create(operation);
                    semantics.getOperations().add(uri);
                } catch(IllegalArgumentException ex) {
                    Logger.getLogger(BiotoolsConverter.class.getName()).log(Level.INFO, "incorrect operation uri: {0}", operation);
                }
            }
        }
    }

    private static void addDatatype(Datatype datatype, JsonObject jdatatype) {
        
        final JsonObject jdata = jdatatype.getJsonObject("data");
        if (jdata != null) {
            final String data = jdata.getString("uri", null);
            if (data != null) {
                try {
                    datatype.setDatatype(URI.create(data));
                } catch(IllegalArgumentException ex) {
                    Logger.getLogger(BiotoolsConverter.class.getName()).log(Level.INFO, "incorrect data uri: {0}", data);
                }
            }
        }
            
        final JsonArray jformats = jdatatype.getJsonArray("format");
        for (int i = 0, n = jformats.size(); i < n; i++) {
            final JsonObject jformat = jformats.getJsonObject(i);
            final String format = jformat.getString("uri", null);
            if (format != null) {
                try {
                    final URI uri = URI.create(format);
                    datatype.getFormats().add(uri);
                } catch(IllegalArgumentException ex) {
                    Logger.getLogger(BiotoolsConverter.class.getName()).log(Level.INFO, "incorrect datatype format uri: {0}", format);
                }
            }
        }
    }

    
    private static void addDownloads(Tool tool, JsonObject jtool) {
        JsonArray jdownloads = jtool.getJsonArray("download");
        
        for (int i = 0, n = jdownloads.size(); i < n; i++) {
            final JsonObject jdownload = jdownloads.getJsonObject(i);
            
            final String url = jdownload.getString("url", null);
            if (url == null || url.isEmpty()) {
                Logger.getLogger(BiotoolsConverter.class.getName()).log(Level.INFO, "empty download url");
                continue;                
            }

            final URI uri;
            try {
                uri = URI.create(url);
            } catch(IllegalArgumentException ex) {
                Logger.getLogger(BiotoolsConverter.class.getName()).log(Level.INFO, "incorrect download url: {0}", url);
                continue;
            }
            
            final String type = jdownload.getString("type", null);
            if (type == null || type.isEmpty()) {
                Logger.getLogger(BiotoolsConverter.class.getName()).log(Level.INFO, "no download type set");
                continue;                
            }
            
            try {
                DownloadType downloadType = DownloadType.fromValue(type);
                
                Distributions distributions = tool.getDistributions();
                if (distributions == null) {
                    distributions = new Distributions();
                }
                
                switch(downloadType) {
                    case BINARIES:       distributions.getBinaryDistributions().add(uri);
                                         break;
                    case SOURCE_CODE:    distributions.getSourcecodeDistributions().add(uri);
                                         break;
                    case CONTAINER_FILE: final String cformat = jdownload.getString("containerFormat", null);
                                         Container container;
//                                         if (cformat == null) {
//                                             Logger.getLogger(BiotoolsRepositoryIterator.class.getName()).log(Level.INFO, "no container format set");
//                                             container = new Container("unknown");
//                                         } else {
//                                             try {
//                                                ContainerFormatType.fromValue(cformat);
//                                                container = new Container(cformat);
//                                             } catch(IllegalArgumentException ex) {
//                                                Logger.getLogger(BiotoolsRepositoryIterator.class.getName()).log(Level.INFO, "unrecognized container format: {0}", cformat);
//                                                container = new Container("unknown");
//                                             }
//                                         }
                                         container = new Container("unknown");
                                         container.setURI(uri);
                                         distributions.getContainers().add(container);
                                         break;
                    case VM_IMAGE:       final String dformat = jdownload.getString("diskFormat", null);
                                         VMImage vmImage;
//                                         if (dformat == null) {
//                                             Logger.getLogger(BiotoolsRepositoryIterator.class.getName()).log(Level.INFO, "no vm image format set");
//                                             vmImage = new VMImage("unknown");
//                                         } else {
//                                             try {
//                                                DiskFormatType.fromValue(dformat);
//                                                vmImage = new VMImage(dformat);
//                                             } catch(IllegalArgumentException ex) {
//                                                Logger.getLogger(BiotoolsRepositoryIterator.class.getName()).log(Level.INFO, "unrecognized vm image  format: {0}", dformat);
//                                                vmImage = new VMImage("unknown");
//                                             }
//                                         }
                                         vmImage = new VMImage("unknown");
                                         vmImage.setURI(uri);
                                         distributions.getVirtualMachineImages().add(vmImage);
                                         break;

                    default: continue;
                }
                
                tool.setDistributions(distributions);
            } catch(IllegalArgumentException ex) {
                Logger.getLogger(BiotoolsConverter.class.getName()).log(Level.INFO, "unrecognized download type: {0}", type);
            }
        }
    }

    private static void addLinks(Tool tool, JsonObject jtool) {
        final JsonArray jlinks = jtool.getJsonArray("link");
        for (int i = 0, n = jlinks.size(); i < n; i++) {
            final JsonObject jlink = jlinks.getJsonObject(i);
            
            final String url = jlink.getString("url", null);
            if (url == null || url.isEmpty()) {
                Logger.getLogger(BiotoolsConverter.class.getName()).log(Level.INFO, "empty link url");
                continue;                
            }
            
            final URI uri;
            try {
                uri = URI.create(url);
            } catch(IllegalArgumentException ex) {
                Logger.getLogger(BiotoolsConverter.class.getName()).log(Level.INFO, "incorrect link url: {0}", url);
                continue;
            }
            
            final String type = jlink.getString("type", null);
            if (type == null || type.isEmpty()) {
                Logger.getLogger(BiotoolsConverter.class.getName()).log(Level.INFO, "no download type set");
                continue;                
            }
            try {
                final ToolLinkType toolLinkType = ToolLinkType.fromValue(type);
                
                Support support = tool.getSupport();
                if (support == null) {
                    support = new Support();
                }
                
                Community community = tool.getCommunity();
                if (community == null) {
                    community = new Community();
                }
                
                switch(toolLinkType) {
                    case REPOSITORY:    tool.getRepositories().add(uri);
                                        break;
                    case HELPDESK:      tool.setSupport(support);
                                        support.setHelpdesk(uri);
                                        break;
                    case ISSUE_TRACKER: tool.setSupport(support);
                                        support.setIssueTracker(uri);
                                        break;
                    case MAILING_LIST:  tool.setSupport(support);
                                        support.setMailingList(uri);
                                        break;
                    case SOCIAL_MEDIA:  tool.setCommunity(community);
                                        community.getSocialMedia().add(uri);
                                        break;
                    
                }
            } catch(IllegalArgumentException ex) {
                Logger.getLogger(BiotoolsConverter.class.getName()).log(Level.INFO, "unrecognized link type: {0}", type);
            }
        }
    }

    private static void setLicense(Tool tool, JsonObject jtool) {
        final String license = jtool.getString("license", null);
        if (license != null) {
            try {
                LicenseType.fromValue(license); // just to check if valid.
                tool.setLicense(license);
            } catch(IllegalArgumentException ex) {
                Logger.getLogger(BiotoolsConverter.class.getName()).log(Level.INFO, "unrecognized license: {0}", license);
            }
        }
    }
    
    private static void setMaturity(Tool tool, JsonObject jtool) {
        final String maturity = jtool.getString("maturity", null);
        if (maturity != null) {
            try {
                MaturityType.fromValue(maturity);
                tool.setMaturity(maturity);
            } catch(IllegalArgumentException ex) {
                Logger.getLogger(BiotoolsConverter.class.getName()).log(Level.INFO, "unrecognized maturity: {0}", maturity);
            }
        }
    }

    private static void setCost(Tool tool, JsonObject jtool) {
        final String cost = jtool.getString("cost", null);
        if (cost != null) {
            try {
                CostType.fromValue(cost);
                tool.setCost(cost);
            } catch(IllegalArgumentException ex) {
                Logger.getLogger(BiotoolsConverter.class.getName()).log(Level.INFO, "unrecognized cost: {0}", cost);
            }
        }
    }
    
    private static void setOperatingSystems(List<String> operatingSystems, JsonObject jtool) {
        final JsonArray jOperatingSystems = jtool.getJsonArray("operatingSystem");
        if (jOperatingSystems != null) {
            for (int i = 0, n = jOperatingSystems.size(); i < n; i++) {
                final String operatingSystem = jOperatingSystems.getString(i, null);
                if (operatingSystem == null || operatingSystem.isEmpty()) {
                    Logger.getLogger(BiotoolsConverter.class.getName()).log(Level.INFO, "empty operating system in tool : {0}", jtool.getString("@id", ""));
                } else {
                    try {
                        OperatingSystemType.fromValue(operatingSystem);
                        operatingSystems.add(operatingSystem);
                    } catch(IllegalArgumentException ex) {
                        Logger.getLogger(BiotoolsConverter.class.getName()).log(Level.INFO, "unrecognized operating system : {0}", operatingSystem);
                    }
                }
            }
        }
    }
    
    private static void setLanguages(List<String> list, JsonObject jtool) {
        final JsonArray jlanguages = jtool.getJsonArray("language");
        if (jlanguages != null) {
            for (int i = 0, n = jlanguages.size(); i < n; i++) {
               final String language = jlanguages.getString(i, null);
               if (language != null) {
                   list.add(language);
               }
            }
        }
    }

}
