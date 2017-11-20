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

package es.elixir.bsc.openebench.rest.validator;

import es.elixir.bsc.json.schema.JsonSchemaException;
import es.elixir.bsc.json.schema.JsonSchemaReader;
import es.elixir.bsc.json.schema.ValidationError;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonStructure;
import javax.servlet.ServletContext;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * JSON message validator
 * 
 * @author Dmitry Repchevsky
 */

public class JsonMessageValidator implements ConstraintValidator<JsonSchema, String> {

    @Inject
    private ServletContext ctx;

    private es.elixir.bsc.json.schema.model.JsonSchema schema;
    
    public JsonMessageValidator() {}
    
    @Override
    public void initialize(JsonSchema annotation) {
        initialize(annotation.location());
    }

    public void initialize(String location) {
        try {
            URL url = ctx.getResource("/META-INF/resources/" + location);
            schema = JsonSchemaReader.getReader().read(url);
        } catch (IOException | JsonSchemaException ex) {
            Logger.getLogger(JsonMessageValidator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public boolean isValid(String json, ConstraintValidatorContext context) {
        JsonStructure data = Json.createReader(new StringReader(json)).read();
        List<ValidationError> errors = new ArrayList<>();
        schema.validate(data, errors);
        return errors.isEmpty();
    }
}
