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

package es.elixir.bsc.openebench.bioconda;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.util.Iterator;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.stream.JsonParser;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;

/**
 * @author Dmitry Repchevsky
 */

public class BiocondaRepositoryIterator implements Iterator<BiocondaPackage> , Closeable, AutoCloseable {
    
    public final static String SERVER = "https://conda.anaconda.org/bioconda/linux-64/repodata.json.bz2";
    
    private final BZip2CompressorInputStream in;
    private final JsonParser parser;
    
    private JsonParser.Event event;
    
    public BiocondaRepositoryIterator() throws IOException {
        in = new BZip2CompressorInputStream (URI.create(SERVER).toURL().openStream());
        parser = Json.createParser(in);
        if (parser.hasNext() &&
            parser.next() == JsonParser.Event.START_OBJECT) {
            while (parser.hasNext()) {
                if (parser.next() == JsonParser.Event.KEY_NAME && 
                    "packages".equals(parser.getString()) && 
                    parser.hasNext() &&
                    parser.next() == JsonParser.Event.START_OBJECT) {
                    return;
                }
            }
        }
        
        throw new IOException();
    }
    
    @Override
    public boolean hasNext() {
        if (parser.hasNext()) {
            if (event == null) {
                event = parser.next();
            }
            return JsonParser.Event.KEY_NAME == event;
        }
        return false;
    }

    @Override
    public BiocondaPackage next() {
        if (event != null || hasNext()) {
            event = null;
            final String file = parser.getString();
            if (parser.hasNext() &&
                parser.next() == JsonParser.Event.START_OBJECT) {
                final JsonObject object = parser.getObject();
                return new BiocondaPackage(object.getString("name", null),
                                           object.getString("version", null),
                                           object.getString("subdir", null),
                                           file);
            }
        }

        return null;
    }

    @Override
    public void close() throws IOException {
        parser.close();
        in.close();
    }
    
    public static void main(String[] args) throws IOException {
        BiocondaRepositoryIterator iter = new BiocondaRepositoryIterator();
        while(iter.hasNext()) {
            System.out.println(iter.next().getMetadata());

        }
        
    }
}
