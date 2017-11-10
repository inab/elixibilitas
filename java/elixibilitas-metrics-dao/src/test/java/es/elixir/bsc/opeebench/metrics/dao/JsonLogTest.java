package es.elixir.bsc.opeebench.metrics.dao;

import es.elixir.bsc.openebench.metrics.dao.JsonLog;
import javax.json.JsonPatch;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Dmitry Repchevsky
 */

public class JsonLogTest {
    
    public final static String TEST01_SRC = "{}";
    public final static String TEST01_TGT = "{ \"project\": {\"web\" : true}}";
    public final static String TEST01_RES = "[{\"op\":\"add\",\"path\":\"/project\",\"value\":\"{}\"},{\"op\":\"add\",\"path\":\"/project/web\",\"value\":true}]";
    
    public final static String TEST02_SRC = "{ \"project\": {\"web\" : true}}";
    public final static String TEST02_TGT = "{ \"project\": {\"home\" : \"www.bsc.es\"}}";
    public final static String TEST02_RES = "[{\"op\":\"remove\",\"path\":\"/project/web\"},{\"op\":\"add\",\"path\":\"/project/home\",\"value\":\"www.bsc.es\"}]";
    
    public final static String TEST03_SRC = "{ \"project\": {\"web\" : true}}";
    public final static String TEST03_TGT = "{}";
    public final static String TEST03_RES = "[{\"op\":\"remove\",\"path\":\"/project/web\"},{\"op\":\"remove\",\"path\":\"/project\"}]";
    
    public final static String TEST04_SRC = "{ \"project\": {\"web\" : true}}";
    public final static String TEST04_TGT = "{ \"project\": {\"web\" : false}}";
    public final static String TEST04_RES = "[{\"op\":\"replace\",\"path\":\"/project/web\",\"value\":false}]";

    public final static String TEST05_SRC = "{ \"project\": {\"web\": {\"homepage\": \"www.bsc.es\"}}}";
    public final static String TEST05_TGT = "{ \"project\": {\"web\": {\"home_page\": \"www.inb.bsc.es\"}}}";
    public final static String TEST05_RES = "";
    
    @Test
    public void test01() {
        JsonPatch patch = JsonLog.createJsonPatch(TEST01_SRC, TEST01_TGT);
        Assert.assertEquals(TEST01_RES, patch.toJsonArray().toString());
    }
    
    @Test
    public void test02() {
        JsonPatch patch = JsonLog.createJsonPatch(TEST02_SRC, TEST02_TGT);
        Assert.assertEquals(TEST02_RES, patch.toJsonArray().toString());
    }

    @Test
    public void test03() {
        JsonPatch patch = JsonLog.createJsonPatch(TEST03_SRC, TEST03_TGT);
        Assert.assertEquals(TEST03_RES, patch.toJsonArray().toString());
    }
    
    @Test
    public void test04() {
        JsonPatch patch = JsonLog.createJsonPatch(TEST04_SRC, TEST04_TGT);
        Assert.assertEquals(TEST04_RES, patch.toJsonArray().toString());
    }
    
    @Test
    public void test05() {
        JsonPatch patch = JsonLog.createJsonPatch(TEST05_SRC, TEST05_TGT);
    }

}
