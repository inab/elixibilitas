package es.elixir.bsc.openebench.openminted;

import es.elixir.bsc.openebench.model.tools.Tool;
import es.elixir.bsc.openebench.tools.OpenEBenchEndpoint;
import es.elixir.bsc.openebench.tools.OpenEBenchRepository;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Dmitry Repchevsky
 */

public class OMTDRepositoryImporter {
    
    private final static String ID_TEMPLATE = OpenEBenchEndpoint.URI_BASE + "omtd:%s:%s/%s/%s";

    private final URI server;
    private OpenEBenchRepository repository;
    
    public OMTDRepositoryImporter(URI server) {
        this.server = server;
    }
    
    public OMTDRepositoryImporter(URI server, String username, String password) {
        this.server = server;
        this.repository = new OpenEBenchRepository(username, password);
    }
    
    public void load() {

        List<OMTDComponent> omtd_tools = new ArrayList<>();
        
        try (OMTDRegistryIterator iter = new OMTDRegistryIterator(server)) {
            while(iter.hasNext()) {
                final OMTDComponent component = iter.next();
                System.out.println(component);
            }
        } catch (IOException ex) {
            Logger.getLogger(OMTDRepositoryImporter.class.getName()).log(Level.SEVERE, null, ex);
        }
        
//        for (OMTDComponent omtd_tool : omtd_tools) {
//            final String[] types = omtd_tool.getTypes();
//            if (types != null) {
//                
//                for (String type : types) {
//                    Tool tool = new Tool(URI.create(String.format(ID_TEMPLATE, omtd_tool.getId(), omtd_tool.getVersion(), type, omtd_tool.getAuthority())), type);
//                }
//            }
//            
//        }
    }
    
    public static void main(String[] args) {
        try (OMTDRegistryIterator iter = new OMTDRegistryIterator(URI.create("https://test.openminted.eu/api/request/application/all"))) {
            while(iter.hasNext()) {
                final OMTDComponent component = iter.next();
                System.out.println(component);
            }
        } catch (IOException ex) {
            Logger.getLogger(OMTDRepositoryImporter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
