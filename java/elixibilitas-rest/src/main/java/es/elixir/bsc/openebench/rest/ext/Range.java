package es.elixir.bsc.openebench.rest.ext;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Dmitry Repchevsky
 */

public class Range {
    private final static Pattern PATTERN = Pattern.compile("^([a-zA-Z]+)=(\\d+)-(\\d+)$");
    
    private String unit;
    private long firstPos;
    private long lastPos;
    
    protected Range() {}
    
    public Range(String range) {
        Matcher m = PATTERN.matcher(range);
        if (m.find()) {
            final String g1 = m.group(1);
            final String g2 = m.group(2);
            final String g3 = m.group(3);
            
            unit = g1 == null || g1.isEmpty() ? null : g1;
            firstPos = g2 == null || g2.isEmpty() ? Long.MIN_VALUE : Long.parseLong(g2);
            lastPos = g3 == null || g3.isEmpty() ? Long.MIN_VALUE : Long.parseLong(g3);
        } else {
            firstPos = Long.MIN_VALUE;
            lastPos = Long.MIN_VALUE;
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
    
    @Override
    public String toString() {
        return build(unit, firstPos, lastPos);
    }
    
    public static String build(String unit, long firstPos, long lastPos) {
        StringBuilder sb = new StringBuilder();
        
        sb.append(unit == null ? "bytes" : unit).append('=');
        sb.append(firstPos < 0 ? 0 : firstPos).append('-');
        sb.append(lastPos < 0 ? '*' : Long.toString(lastPos));
        
        return sb.toString();
    }
}
