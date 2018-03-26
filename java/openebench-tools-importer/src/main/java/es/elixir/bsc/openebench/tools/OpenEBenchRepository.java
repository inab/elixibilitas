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

package es.elixir.bsc.openebench.tools;

import es.elixir.bsc.openebench.model.tools.Tool;
import java.io.IOException;
import java.util.Map;
/**
 * @author Dmitry Repchevsky
 */

public class OpenEBenchRepository {
    
    private static volatile Map<String, Tool> tools;
    
    private OpenEBenchEndpoint endpoint;
    
    public OpenEBenchRepository() {
    }
    
    public OpenEBenchRepository(String name, String password) {
        endpoint = new OpenEBenchEndpoint(name, password);
    }

    public int put(Tool tool) throws IOException {
        if (endpoint == null) {
            return 403;
        }

        final int code = endpoint.put(tool);
        if (code == 200) {
            if (OpenEBenchRepository.tools != null) {
                tools.put(tool.id.toString(), tool);
            }
        }
        return code;
    }

    public int patch(Tool tool) throws IOException {
        if (endpoint == null) {
            return 403;
        }

        final int code = endpoint.patch(tool);
        if (code == 200) {
            if (OpenEBenchRepository.tools != null) {
                tools.put(tool.id.toString(), tool);
            }
        }
        return code;
    }

    public static Map<String, Tool> getTools() {
        Map<String, Tool> toolz = OpenEBenchRepository.tools;
        if (toolz == null) {
            synchronized(ToolsComparator.class) {
                toolz = OpenEBenchRepository.tools;
                if (toolz == null) {
                    OpenEBenchRepository.tools = toolz = OpenEBenchEndpoint.get();
                }
            }
        }
        return toolz;
    }
}