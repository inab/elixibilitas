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

package es.elixir.bsc.openebench.rest.ext;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Dmitry Repchevsky
 */

public class ContentRange {

    private final static Pattern PATTERN = Pattern.compile("^([a-zA-Z]+) (\\d+)-(\\d+)/(\\*|\\d+)$");
    
    private String unit;
    private long firstPos;
    private long lastPos;
    private long length;
    
    protected ContentRange() {}

    public ContentRange(Long firstPos, Long lastPos) {
        this(null, firstPos, lastPos, Long.MIN_VALUE);
    }

    public ContentRange(String unit, Long firstPos, Long lastPos) {
        this(unit, firstPos, lastPos, Long.MIN_VALUE);
    }

    public ContentRange(String unit, Long firstPos, Long lastPos, Long length) {
        this.unit = unit;
        this.firstPos = firstPos == null || firstPos < 0 ? Long.MIN_VALUE : firstPos;
        this.lastPos = lastPos == null || lastPos < 0 ? Long.MIN_VALUE : lastPos;
        this.length = length == null || length < 0 ? Long.MIN_VALUE : length;
    }

    public ContentRange(String contentRange) {
        Matcher m = PATTERN.matcher(contentRange);
        if (m.find()) {
            final String g1 = m.group(1);
            final String g2 = m.group(2);
            final String g3 = m.group(3);
            final String g4 = m.group(4);
            
            unit = g1 == null || g1.isEmpty() ? "bytes" : g1;
            firstPos = g2 == null || g2.isEmpty() ? Long.MIN_VALUE : Long.parseLong(g2);
            lastPos = g3 == null || g3.isEmpty() ? Long.MIN_VALUE : Long.parseLong(g3);
            length = g4 == null || g4.isEmpty() || "*".equals(g4) ? Long.MIN_VALUE : Long.parseLong(g4);
        } else {
            firstPos = Long.MIN_VALUE;
            lastPos = Long.MIN_VALUE;
            length = Long.MIN_VALUE;
        }
    }
    
    public String getUnit() {
        return unit;
    }
    
    public void setUnit(String unit) {
        this.unit = unit;
    }

    public Long getFirstPos() {
        return firstPos >= 0 ? firstPos : null;
    }
    
    public Long getLastPos() {
        return lastPos >= 0 ? lastPos : null;
    }

    public Long getLength() {
        return length >= 0 ? length : null;
    }
    
    @Override
    public String toString() {
        return build(unit, firstPos, lastPos, length);
    }
    
    public static String build(String unit, long firstPos, long lastPos, long length) {
        final StringBuilder sb = new StringBuilder();
        
        sb.append(unit == null ? "bytes" : unit).append(' ');
        sb.append(firstPos < 0 ? 0 : firstPos).append('-');
        sb.append(lastPos < 0 ? '*' : Long.toString(lastPos)).append("/");
        sb.append(length < 0 ? '*' : Long.toString(length));
        
        return sb.toString();
    }
}
