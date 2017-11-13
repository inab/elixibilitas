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

package es.elixir.bsc.elixibilitas.biotoolz.importer;

import com.mongodb.MongoClient;
import es.elixir.bsc.biotools.parser.model.ContainerFormatType;
import es.elixir.bsc.biotools.parser.model.CostType;
import es.elixir.bsc.biotools.parser.model.DiskFormatType;
import es.elixir.bsc.biotools.parser.model.DocumentationType;
import es.elixir.bsc.biotools.parser.model.EntityType;
import es.elixir.bsc.biotools.parser.model.RoleType;
import es.elixir.bsc.biotools.parser.model.ToolType;
import es.elixir.bsc.biotools.parser.model.DownloadType;
import es.elixir.bsc.biotools.parser.model.LicenseType;
import es.elixir.bsc.biotools.parser.model.MaturityType;
import es.elixir.bsc.biotools.parser.model.OperatingSystemType;
import es.elixir.bsc.biotools.parser.model.ToolLinkType;
import es.elixir.bsc.elixibilitas.tools.dao.ToolDAO;
import es.elixir.bsc.openebench.model.tools.Contact;
import es.elixir.bsc.openebench.model.tools.Credit;
import es.elixir.bsc.openebench.model.tools.DatabasePortal;
import es.elixir.bsc.openebench.model.tools.Datatype;
import es.elixir.bsc.openebench.model.tools.DesktopApplication;
import es.elixir.bsc.openebench.model.tools.Documentation;
import es.elixir.bsc.openebench.model.tools.Library;
import es.elixir.bsc.openebench.model.tools.Publication;
import es.elixir.bsc.openebench.model.tools.Semantics;
import es.elixir.bsc.openebench.model.tools.Tool;
import es.elixir.bsc.openebench.model.tools.WebApplication;
import es.elixir.bsc.openebench.model.tools.CommandLineTool;
import es.elixir.bsc.openebench.model.tools.Community;
import es.elixir.bsc.openebench.model.tools.Container;
import es.elixir.bsc.openebench.model.tools.Distributions;
import es.elixir.bsc.openebench.model.tools.Ontology;
import es.elixir.bsc.openebench.model.tools.Plugin;
import es.elixir.bsc.openebench.model.tools.SOAPServices;
import es.elixir.bsc.openebench.model.tools.SPARQLEndpoint;
import es.elixir.bsc.openebench.model.tools.Script;
import es.elixir.bsc.openebench.model.tools.Suite;
import es.elixir.bsc.openebench.model.tools.Support;
import es.elixir.bsc.openebench.model.tools.VMImage;
import es.elixir.bsc.openebench.model.tools.WebAPI;
import es.elixir.bsc.openebench.model.tools.Workbench;
import es.elixir.bsc.openebench.model.tools.Workflow;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonString;

/**
 * The bio.tools data model importer.
 * 
 * @author Dmitry Repchevsky
 */

public class BiotoolzContentImporter {
    
    public static void main(String[] args) {
        new BiotoolzContentImporter().load(new MongoClient("localhost"));
    }
    
    public void load(MongoClient mc) {
        int page = 1;
        do {
            ArrayList<Tool> tools = new ArrayList<>();
            page = next(tools, page);
            
            tools.forEach((tool) -> {
                ToolDAO.put(mc, "biotools", tool);
            });
        } while (page > 0);
    }
    
    /**
     * Get a next chunk of the tools from bio.tools registry
     * 
     * @param tools
     * @param page
     * @return 
     */
    public int next(List<Tool> tools, int page) {

        URL url;
        try {
            url = new URL("https://bio.tools/api/tool/?page=" + page);
        } catch(MalformedURLException ex) {
            return Integer.MIN_VALUE;
        }

        HttpURLConnection con = null;
        try {
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Accept", "application/json");
            try (InputStream in = con.getInputStream()) {
                JsonReader reader = Json.createReader(in);
                JsonObject jo = reader.readObject();
                JsonArray jtools = jo.getJsonArray("list");
                for (int i = 0, n = jtools.size(); i < n; i++) {
                    addTool(tools, jtools.getJsonObject(i));
                                        
//                    addOperatingSystems(tool, jtool);
//                    addToolTypes(tool, jtool);
//                    addAccessibility(tool, jtool);
//                    addLanguages(tool, jtool);
//                    addCollectionIDs(tool, jtool);
                }
                String next = jo.getString("next", null);
                return next == null || !next.startsWith("?page=") ? Integer.MIN_VALUE : Integer.parseInt(next.substring(6));
            }
        } catch(Exception ex) {
            Logger.getLogger(BiotoolzContentImporter.class.getName()).log(Level.WARNING, "error tools parsing, page " + page, ex);
            return Integer.MIN_VALUE;
        } finally {
            if (con != null) {
                con.disconnect();
            }
        }     
    }
   
    private void addTool(List<Tool> tools, JsonObject jtool) {
        
        String id = jtool.getString("id", null);

        StringBuilder idTemplate = new StringBuilder(ToolDAO.AUTHORITY).append("bio.tools:").append(id).append("/%s");
        final String jhomepage = jtool.getString("homepage", null);
        URI homepage = null;
        if (jhomepage != null) {
            try {
                homepage = URI.create(jhomepage);
                idTemplate.append('/').append(homepage.getHost());
            } catch (IllegalArgumentException ex) {
                Logger.getLogger(BiotoolzContentImporter.class.getName()).log(Level.INFO, "invalid homepage: {0}", jhomepage);
            }
        }

        final String name = jtool.getString("name", null);
        final String version = jtool.getString("version", null);
        
        JsonArray jtoolTypes = jtool.getJsonArray("toolType");
        for (int i = 0, n = jtoolTypes.size(); i < n; i++) {
            JsonString jtoolType = jtoolTypes.getJsonString(i);
            try {
                final Tool tool;

                final ToolType toolType = ToolType.fromValue(jtoolType.getString());

                switch(toolType) {
                    case COMMAND_LINE: tool = addCommandLineTool(new CommandLineTool(URI.create(
                                                    String.format(idTemplate.toString(), CommandLineTool.TYPE))), jtool);
                                       break;
                    case WEB_APPLICATION: tool = addWebApplication(new WebApplication(URI.create(
                                                    String.format(idTemplate.toString(), WebApplication.TYPE))), jtool);
                                       break;
                    case DESKTOP_APPLICATION: tool = addDesktopApplication(new DesktopApplication(URI.create(
                                                    String.format(idTemplate.toString(), DesktopApplication.TYPE))), jtool);
                                       break;
                    case DATABASE_PORTAL: tool = addDatabasePortal(new DatabasePortal(URI.create(
                                                    String.format(idTemplate.toString(), DatabasePortal.TYPE))), jtool);
                                       break;
                    case LIBRARY: tool = addLibrary(new Library(URI.create(
                                                    String.format(idTemplate.toString(), Library.TYPE))), jtool);
                                       break;
                    case WEB_SERVICE: tool = addSOAPServices(new SOAPServices(URI.create(
                                                    String.format(idTemplate.toString(), SOAPServices.TYPE))), jtool);
                                       break;
                    case WEB_API: tool = addWebAPI(new WebAPI(URI.create(
                                                    String.format(idTemplate.toString(), WebAPI.TYPE))), jtool);
                                       break;
                    case SPARQL_ENDPOINT: tool = addSPARQLEndpoint(new SPARQLEndpoint(URI.create(
                                                    String.format(idTemplate.toString(), SPARQLEndpoint.TYPE))), jtool);
                                       break;
                    case ONTOLOGY: tool = addOntology(new Ontology(URI.create(
                                                    String.format(idTemplate.toString(), Ontology.TYPE))), jtool);
                                       break;
                    case WORKFLOW: tool = addWorkflow(new Workflow(URI.create(
                                                    String.format(idTemplate.toString(), Workflow.TYPE))), jtool);
                                       break;
                    case SCRIPT: tool = addScript(new Script(URI.create(
                                                    String.format(idTemplate.toString(), Script.TYPE))), jtool);
                                       break;
                    case PLUGIN: tool = addPlugin(new Plugin(URI.create(
                                                    String.format(idTemplate.toString(), Plugin.TYPE))), jtool);
                                       break;
                    case SUITE: tool = addSuite(new Suite(URI.create(
                                                    String.format(idTemplate.toString(), Suite.TYPE))), jtool);
                                       break;
                    case WORKBENCH: tool = addWorkbench(new Workbench(URI.create(
                                                    String.format(idTemplate.toString(), Workbench.TYPE))), jtool);
                                       break;

                    default: continue;
                }

                tool.setName(name);
                tool.setVersion(version);

                tool.setHomepage(homepage);
                
                tool.setDescription(jtool.getString("description", null));
                
                addDocumentation(tool, jtool);
                addPublications(tool, jtool);
                addContacts(tool, jtool);
                addCredits(tool, jtool);
                addSemantics(tool, jtool);
                addDownloads(tool, jtool);
                addLinks(tool, jtool);
                setLicense(tool, jtool);
                setMaturity(tool, jtool);
                setCost(tool, jtool);
                
                tools.add(tool);
            } catch(IllegalArgumentException ex) {
                Logger.getLogger(BiotoolzContentImporter.class.getName()).log(Level.INFO, "unrecognized toolType: {0}", jtoolType.getString());
            }
        }
    }
    
    private CommandLineTool addCommandLineTool(CommandLineTool tool, JsonObject jtool) {
        setOperatingSystems(tool.getOperatingSystems(), jtool);
        return tool;
    }
    
    private WebApplication addWebApplication(WebApplication tool, JsonObject jtool) {
        return tool;
    }

    private DesktopApplication addDesktopApplication(DesktopApplication tool, JsonObject jtool) {
        return tool;
    }

    private DatabasePortal addDatabasePortal(DatabasePortal tool, JsonObject jtool) {
        return tool;
    }

    private Library addLibrary(Library tool, JsonObject jtool) {
        return tool;
    }
    
    private SOAPServices addSOAPServices(SOAPServices tool, JsonObject jtool) {
        return tool;
    }
    
    private WebAPI addWebAPI(WebAPI tool, JsonObject jtool) {
        return tool;
    }
    
    private SPARQLEndpoint addSPARQLEndpoint(SPARQLEndpoint tool, JsonObject jtool) {
        return tool;
    }
    
    private Ontology addOntology(Ontology tool, JsonObject jtool) {
        return tool;
    }
    
    private Workflow addWorkflow(Workflow tool, JsonObject jtool) {
        return tool;
    }
    
    private Script addScript(Script tool, JsonObject jtool) {
        return tool;
    }

    private Plugin addPlugin(Plugin tool, JsonObject jtool) {
        return tool;
    }

    private Suite addSuite(Suite tool, JsonObject jtool) {
        return tool;
    }
    
    private Workbench addWorkbench(Workbench tool, JsonObject jtool) {
        return tool;
    }
    
    private void addDocumentation(Tool tool, JsonObject jtool) {
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
                                case GENERAL: documentation.setGeneralDocumentation(uri); break;
                                case MANUAL: documentation.setManual(uri); break;
                                case API_DOCUMENTATION: documentation.setAPIDocumentation(uri); break;
                                case CITATION_INSTRUCTIONS: documentation.setCitationInstructions(uri); break;
                                case TERMS_OF_USE: documentation.setTermsOfUse(uri); break;
                                case TRAINING_MATERIAL: documentation.setTrainingMaterial(uri); break;
                                case OTHER: documentation.getDocumentationLinks().add(uri); break;
                            }

                         } catch(IllegalArgumentException ex) {
                             Logger.getLogger(BiotoolzContentImporter.class.getName()).log(Level.INFO, "unrecognized type: {0}", type);
                         }
                    }
                }
            }
        }
    }
    
    private void addPublications(Tool tool, JsonObject jtool) {
        JsonArray jpublications = jtool.getJsonArray("publication");
        for (int i = 0, n = jpublications.size(); i < n; i++) {
            Publication publication = new Publication();
            JsonObject jpublication = jpublications.getJsonObject(i);
            
//            try {
//                publication.setType(PublicationType.fromValue(jpublication.getString("type", "")));
//            } catch(IllegalArgumentException ex) {}

            publication.setDOI(jpublication.getString("doi", null));
            publication.setPMID(jpublication.getString("pmid", null));
            publication.setPMCID(jpublication.getString("pmcid", null));
            
            tool.getPublications().add(publication);
        }
    }
    
    private void addContacts(Tool tool, JsonObject jtool) {
        JsonArray jcontacts = jtool.getJsonArray("contact");
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
                    Logger.getLogger(BiotoolzContentImporter.class.getName()).log(Level.INFO, "incorrect contact url: {0}", url);
                }
            }

            tool.getContacts().add(contact);
        }     
    }

    private void addCredits(Tool tool, JsonObject jtool) {
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
                    Logger.getLogger(BiotoolzContentImporter.class.getName()).log(Level.INFO, "unrecognized credit type: {0}", type);
                }
            }
            
            final String role = jcredit.getString("typeRole", null);
            if (role != null) {
                try {
                    RoleType.fromValue(role);
                    credit.setRole(role);
                } catch(IllegalArgumentException ex) {
                    Logger.getLogger(BiotoolzContentImporter.class.getName()).log(Level.INFO, "unrecognized credit role: {0}", type);
                }
            }
            
            final String url = jcredit.getString("url", null);
            if (url != null) {
                try {
                    credit.setUrl(URI.create(url));
                } catch(IllegalArgumentException ex) {
                    Logger.getLogger(BiotoolzContentImporter.class.getName()).log(Level.INFO, "incorrect credit url: {0}", url);
                }
            }
            
            tool.getCredits().add(credit);
        }
    }

    private void addSemantics(Tool tool, JsonObject jtool) {
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
    
    private void addTopics(Semantics semantics, JsonObject jtool) {
        final JsonArray jtopics = jtool.getJsonArray("topic");
        for (int i = 0, n = jtopics.size(); i < n; i++) {
            final JsonObject jtopic = jtopics.getJsonObject(i);
            final String topic = jtopic.getString("uri", null);
            if (topic != null) {
                try {
                    final URI uri = URI.create(topic);
                    semantics.getTopics().add(uri);
                } catch(IllegalArgumentException ex) {
                    Logger.getLogger(BiotoolzContentImporter.class.getName()).log(Level.INFO, "incorrect topic uri: {0}", topic);
                }
            }
        }
    }

    private void addOperations(Semantics semantics, JsonObject jfunction) {
        final JsonArray joperations = jfunction.getJsonArray("operation");
        for (int i = 0, n = joperations.size(); i < n; i++) {
            final JsonObject joperation = joperations.getJsonObject(i);
            final String operation = joperation.getString("uri", null);
            if (operation != null) {
                try {
                    final URI uri = URI.create(operation);
                    semantics.getOperations().add(uri);
                } catch(IllegalArgumentException ex) {
                    Logger.getLogger(BiotoolzContentImporter.class.getName()).log(Level.INFO, "incorrect operation uri: {0}", operation);
                }
            }
        }
    }
    
    private void addDatatype(Datatype datatype, JsonObject jdatatype) {
        
        final JsonObject jdata = jdatatype.getJsonObject("data");
        if (jdata != null) {
            final String data = jdata.getString("uri", null);
            if (data != null) {
                try {
                    datatype.setDatatype(URI.create(data));
                } catch(IllegalArgumentException ex) {
                    Logger.getLogger(BiotoolzContentImporter.class.getName()).log(Level.INFO, "incorrect data uri: {0}", data);
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
                    Logger.getLogger(BiotoolzContentImporter.class.getName()).log(Level.INFO, "incorrect datatype format uri: {0}", format);
                }
            }
        }
    }

    
    private void addDownloads(Tool tool, JsonObject jtool) {
        JsonArray jdownloads = jtool.getJsonArray("download");
        
        for (int i = 0, n = jdownloads.size(); i < n; i++) {
            final JsonObject jdownload = jdownloads.getJsonObject(i);
            
            final String url = jdownload.getString("url", null);
            if (url == null || url.isEmpty()) {
                Logger.getLogger(BiotoolzContentImporter.class.getName()).log(Level.INFO, "empty download url");
                continue;                
            }

            final URI uri;
            try {
                uri = URI.create(url);
            } catch(IllegalArgumentException ex) {
                Logger.getLogger(BiotoolzContentImporter.class.getName()).log(Level.INFO, "incorrect download url: {0}", url);
                continue;
            }
            
            final String type = jdownload.getString("type", null);
            if (type == null || type.isEmpty()) {
                Logger.getLogger(BiotoolzContentImporter.class.getName()).log(Level.INFO, "no download type set");
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
                    case BINARY_PACKAGE: distributions.getBinaryPackagesDistributions().add(uri);
                                         break;
                    case SOURCE_CODE:    distributions.getSourcecodeDistributions().add(uri);
                                         break;
                    case SOURCE_PACKAGE: distributions.getSourcePackagesDistributions().add(uri);
                                         break;
                    case CONTAINER_FILE: final String cformat = jdownload.getString("containerFormat", null);
                                         Container container;
                                         if (cformat == null) {
                                             Logger.getLogger(BiotoolzContentImporter.class.getName()).log(Level.INFO, "no container format set");
                                             container = new Container("unknown");
                                         } else {
                                             try {
                                                ContainerFormatType.fromValue(cformat);
                                                container = new Container(cformat);
                                             } catch(IllegalArgumentException ex) {
                                                Logger.getLogger(BiotoolzContentImporter.class.getName()).log(Level.INFO, "unrecognized container format: {0}", cformat);
                                                container = new Container("unknown");
                                             }
                                         }
                                         container.setURI(uri);
                                         distributions.getContainers().add(container);
                                         break;
                    case VM_IMAGE:       final String dformat = jdownload.getString("diskFormat", null);
                                         VMImage vmImage;
                                         if (dformat == null) {
                                             Logger.getLogger(BiotoolzContentImporter.class.getName()).log(Level.INFO, "no vm image format set");
                                             vmImage = new VMImage("unknown");
                                         } else {
                                             try {
                                                DiskFormatType.fromValue(dformat);
                                                vmImage = new VMImage(dformat);
                                             } catch(IllegalArgumentException ex) {
                                                Logger.getLogger(BiotoolzContentImporter.class.getName()).log(Level.INFO, "unrecognized vm image  format: {0}", dformat);
                                                vmImage = new VMImage("unknown");
                                             }
                                         }
                                         vmImage.setURI(uri);
                                         distributions.getVirtualMachineImages().add(vmImage);
                                         break;

                    default: continue;
                }
                
                tool.setDistributions(distributions);
            } catch(IllegalArgumentException ex) {
                Logger.getLogger(BiotoolzContentImporter.class.getName()).log(Level.INFO, "unrecognized download type: {0}", type);
            }
        }
    }

    private void addLinks(Tool tool, JsonObject jtool) {
        final JsonArray jlinks = jtool.getJsonArray("link");
        for (int i = 0, n = jlinks.size(); i < n; i++) {
            final JsonObject jlink = jlinks.getJsonObject(i);
            
            final String url = jlink.getString("url", null);
            if (url == null || url.isEmpty()) {
                Logger.getLogger(BiotoolzContentImporter.class.getName()).log(Level.INFO, "empty link url");
                continue;                
            }
            
            final URI uri;
            try {
                uri = URI.create(url);
            } catch(IllegalArgumentException ex) {
                Logger.getLogger(BiotoolzContentImporter.class.getName()).log(Level.INFO, "incorrect link url: {0}", url);
                continue;
            }
            
            final String type = jlink.getString("type", null);
            if (type == null || type.isEmpty()) {
                Logger.getLogger(BiotoolzContentImporter.class.getName()).log(Level.INFO, "no download type set");
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
                Logger.getLogger(BiotoolzContentImporter.class.getName()).log(Level.INFO, "unrecognized link type: {0}", type);
            }
        }
    }
    
    private void setLicense(Tool tool, JsonObject jtool) {
        final String license = jtool.getString("license", null);
        if (license != null) {
            try {
                LicenseType.fromValue(license); // just to check if valid.
                tool.setLicense(license);
            } catch(IllegalArgumentException ex) {
                Logger.getLogger(BiotoolzContentImporter.class.getName()).log(Level.INFO, "unrecognized license: {0}", license);
            }
        }
    }
    
    private void setMaturity(Tool tool, JsonObject jtool) {
        final String maturity = jtool.getString("maturity", null);
        if (maturity != null) {
            try {
                MaturityType.fromValue(maturity);
                tool.setMaturity(maturity);
            } catch(IllegalArgumentException ex) {
                Logger.getLogger(BiotoolzContentImporter.class.getName()).log(Level.INFO, "unrecognized maturity: {0}", maturity);
            }
        }
    }

    private void setCost(Tool tool, JsonObject jtool) {
        final String cost = jtool.getString("cost", null);
        if (cost != null) {
            try {
                CostType.fromValue(cost);
                tool.setCost(cost);
            } catch(IllegalArgumentException ex) {
                Logger.getLogger(BiotoolzContentImporter.class.getName()).log(Level.INFO, "unrecognized cost: {0}", cost);
            }
        }
    }
    
    private void setOperatingSystems(List<String> operatingSystems, JsonObject jtool) {
        final JsonArray jOperatingSystems = jtool.getJsonArray("operatingSystem");
        if (jOperatingSystems != null) {
            for (int i = 0, n = jOperatingSystems.size(); i < n; i++) {
                final String operatingSystem = jOperatingSystems.getString(i, null);
                if (operatingSystem == null || operatingSystem.isEmpty()) {
                    Logger.getLogger(BiotoolzContentImporter.class.getName()).log(Level.INFO, "empty operating system in tool : {0}", jtool.getString("@id", ""));
                } else {
                    try {
                        OperatingSystemType.fromValue(operatingSystem);
                        operatingSystems.add(operatingSystem);
                    } catch(IllegalArgumentException ex) {
                        Logger.getLogger(BiotoolzContentImporter.class.getName()).log(Level.INFO, "unrecognized operating system : {0}", operatingSystem);
                    }
                }
            }
        }
    }
}
