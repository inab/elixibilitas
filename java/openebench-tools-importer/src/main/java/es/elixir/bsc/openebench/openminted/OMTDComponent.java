package es.elixir.bsc.openebench.openminted;

import java.util.ArrayList;
import java.util.List;
import javax.json.JsonArray;
import javax.json.JsonObject;

/**
 * @author Dmitry Repchevsky
 */

public class OMTDComponent {

    private String id;          // OMTD 'componentInfo.resourceIdentifiers.value'
    private String name;         // OMTD 'componentInfo.resourceShortName'
    private String version;      // OMTD 'componentInfo.versionInfo.version'
    private String authority;    // OMTD 'organizationNames[].value'
    private String types[];     // OMTD 'distributionInfos.componentDistributionForm'
    private String description;  // OMTD 'componentInfo.descriptions.value'
    private String licences[];   // OMTD 'componentInfo.rightsInfo.licenceInfos.licence'
    private String homepage;     // OMTD 'landingPage'
    private String organization;
    
    public String getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public String getVersion() {
        return version;
    }

    public String getAuthority() {
        return authority;
    }

    public String[] getTypes() {
        return types;
    }
    
    public String getDescription() {
        return description;
    }
    
    public String[] getLicences() {
        return licences;
    }
    
    public String getHomepage() {
        return homepage;
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("id: ").append(id).append('\n');
        sb.append("name: ").append(name).append('\n');
        sb.append("version: ").append(version).append('\n');
        sb.append("authority: ").append(authority).append('\n');
        if (homepage != null) {
            sb.append("homepage: ").append(homepage).append('\n');
        }
        if (organization != null) {
            sb.append("organization: ").append(organization).append('\n');
        }
        sb.append("description: ").append(description).append('\n');
        
        if (types != null && types.length > 0) {
            sb.append("types: (").append(String.join(",", types)).append(")").append('\n');
        }
        
        if (licences != null && licences.length > 0) {
            sb.append("licences: (").append(String.join(",", licences)).append(")").append('\n');
        }
        
        return sb.toString();
    }
    public static OMTDComponent load(JsonObject object) {
        
        final OMTDComponent component = new OMTDComponent();
        
        final JsonObject metadata_info = object.getJsonObject("metadataHeaderInfo");
        if (metadata_info != null) {
            final JsonArray creators = metadata_info.getJsonArray("metadataCreators");
            if (creators != null) {
                for (int i = 0; i < creators.size(); i++) {
                    final JsonObject creator = creators.getJsonObject(i);
                    if (creator != null) {
                        String email = null;
                        final JsonObject communication_info = creator.getJsonObject("communicationInfo");
                        if (communication_info != null) {
                            final JsonArray emails = communication_info.getJsonArray("emails");
                            if (emails != null) {
                                for (int j = 0; j < emails.size(); j++) {
                                    email = emails.getString(j);
                                    if (email != null && email.indexOf('@') > 0) {
                                        break;
                                    }
                                }
                            }
                        }
                        
                        final JsonObject affiliation = creator.getJsonObject("affiliation");
                        if (affiliation != null) {
                            final JsonObject organization = affiliation.getJsonObject("affiliatedOrganization");
                            if (organization != null) {
                                final JsonArray names = organization.getJsonArray("organizationNames");
                                if (names != null) {
                                    for (int j = 0; j < names.size(); j++) {
                                        final JsonObject name = names.getJsonObject(j);
                                        if (name != null) {
                                            component.organization = name.getString("value", null);
                                            if (component.organization != null && !component.organization.isEmpty()) {
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        
                        if (email != null && component.organization != null) {
                            final int idx = email.indexOf('@');
                            if (idx >= 0) {
                                component.authority = email.substring(idx + 1);
                                break;
                            }
                        }
                    }
                }
            }
        }
        final JsonObject component_info = object.getJsonObject("componentInfo");
        if (component_info != null) {
            final JsonObject identification = component_info.getJsonObject("identificationInfo");
            if (identification != null) {
                final JsonArray identifiers = identification.getJsonArray("resourceIdentifiers");
                if (identifiers != null) {
                    for (int i = 0; i < identifiers.size(); i++) {
                        final JsonObject identifier = identifiers.getJsonObject(i);
                        if (identifier != null) {
                            final String value = identifier.getString("value", null);
                            if (value != null && !value.isEmpty()) {
                                component.id = value;
                                break;
                            }
                        }
                    }
                }

                component.name = identification.getString("resourceShortName", null);
                if (component.name == null || component.name.isEmpty()) {
                    final JsonArray rnames = identification.getJsonArray("resourceNames");
                    if (rnames != null) {
                        for (int i = 0; i < rnames.size(); i++) {
                            final JsonObject rname = rnames.getJsonObject(i);
                            if (rname != null) {
                                final String name = rname.getString("value");
                                if (name != null && !name.isEmpty()) {
                                    component.name = name;
                                    break;
                                }
                            }
                        }
                    } 
                }

                final JsonArray descriptions = identification.getJsonArray("descriptions");
                if (descriptions != null) {
                    for (int i = 0; i < descriptions.size(); i++) {
                        final JsonObject description = descriptions.getJsonObject(i);
                        if (description != null) {
                            final String value = description.getString("value", null);
                            if (value != null && !value.isEmpty()) {
                                component.description = value;
                                break;
                            }
                        }
                    }
                }
            }            

            final JsonObject version_info = component_info.getJsonObject("versionInfo");
            if (version_info != null) {
                component.version = version_info.getString("version", null);

            }
            
            final JsonObject contact_info = component_info.getJsonObject("contactInfo");
            if (contact_info != null) {
                final String contact_type = contact_info.getString("contactType", null);
                if (contact_type != null) {
                    final String contact_point = contact_info.getString("contactPoint", null);
                    switch(contact_type) {
                        case "landingPage": component.homepage = contact_point; break;
                    }
                }
            }

            final JsonArray distributions = component_info.getJsonArray("distributionInfos");
            if (distributions != null) {
                List<String> types = new ArrayList<>();
                for (int i = 0; i < distributions.size(); i++) {
                    final JsonObject distribution = distributions.getJsonObject(i);
                    if (distribution != null) {
                        final String type = distribution.getString("componentDistributionForm", null);
                        if (type != null && !type.isEmpty()) {
                            switch(type) {
                                case "webService":
                                case "WEB_SERVICE":
                                    final String ws_type = distribution.getString("webServiceType", null);
                                    if (ws_type != null) {
                                        switch(ws_type) {
                                            case "REST": types.add("rest");break;
                                            case "SOAP": types.add("soap");break;
                                        }
                                    }
                                    break;
                                case "sourceCode":
                                case "executableCode": types.add("cmd");break;
                                case "sourceAndExecutableCode":
                                case "dockerImage": break;
                                case "galaxyWorkflow": types.add("workflow");break;
                                case "workflowFile": types.add("workflow");break;
                            }
                        }
                    }
                }
                component.types = types.toArray(new String[types.size()]);
            }
            
            final JsonObject rights = component_info.getJsonObject("rightsInfo");
            if (rights != null) {
                final JsonArray licence_infos = rights.getJsonArray("licenceInfos");
                if (licence_infos != null) {
                    List<String> licences = new ArrayList<>();
                    for (int i = 0; i < licence_infos.size(); i++) {
                        final JsonObject licence_info = licence_infos.getJsonObject(i);
                        if (licence_info != null) {
                            final String licence = licence_info.getString("licence", null);
                            if (licence != null && !licence.isEmpty()) {
                                licences.add(licence);
                            }
                        }
                    }
                    component.licences = licences.toArray(new String[licences.size()]);
                }
            }
        }
        
        return component;
    }
}
