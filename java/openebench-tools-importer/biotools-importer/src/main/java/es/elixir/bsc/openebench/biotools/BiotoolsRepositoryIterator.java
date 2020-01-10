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

package es.elixir.bsc.openebench.biotools;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;

/**
 * @author Dmitry Repchevsky
 */

public class BiotoolsRepositoryIterator implements Iterator<JsonObject> {

    private int page;
    private Iterator<JsonObject> iterator;

    public BiotoolsRepositoryIterator() {
        page = 1;
    }
    
    @Override
    public boolean hasNext() {
        if (iterator != null && iterator.hasNext()) {
            return true;
        }
        if (page > 0) {
            final List<JsonObject> tools = new ArrayList<>();
            page = next(tools, page);
            iterator = tools.iterator();
            return iterator.hasNext();
        }

        return false;
    }

    @Override
    public JsonObject next() {
        if (hasNext()) {
            return iterator.next();
        }
        
        return null;
    }
    
    /**
     * Get a next chunk of the tools from bio.tools registry
     * 
     * @param tools
     * @param page
     * @return 
     */
    private int next(List<JsonObject> tools, int page) {

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
                    tools.add(jtools.getJsonObject(i));
                }
                String next = jo.getString("next", null);
                return next == null || !next.startsWith("?page=") ? Integer.MIN_VALUE : Integer.parseInt(next.substring(6));
            }
        } catch(Exception ex) {
            Logger.getLogger(BiotoolsConverter.class.getName()).log(Level.WARNING, "error tools parsing, page " + page, ex);
            return Integer.MIN_VALUE;
        } finally {
            if (con != null) {
                con.disconnect();
            }
        }     
    }
}
