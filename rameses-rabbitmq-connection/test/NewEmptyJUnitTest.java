/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import java.math.BigDecimal;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import junit.framework.TestCase;
import net.sf.json.JSON;
import net.sf.json.JSONSerializer;
import net.sf.json.JsonConfig;
import net.sf.json.processors.JsonValueProcessor;

/**
 *
 * @author ramesesinc
 */
public class NewEmptyJUnitTest extends TestCase {
    
    public NewEmptyJUnitTest(String testName) {
        super(testName);
    }

    public void test1() throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd mm:hh:ss"); 
        Date date = new java.sql.Date( System.currentTimeMillis()); 
        
        Map m = new HashMap();
        m.put("name", "wlfores");
        m.put("age", 100);
        m.put("double", 9999.99);
        m.put("date", sdf.format(date)); 
        m.put("date2", date); 
        m.put("empty", null);
        m.put("list", new ArrayList());
        m.put("map", new HashMap());
        m.put("array", new Object[]{});
        m.put("decimal", new BigDecimal("999999999.99"));
        JsonConfig c = new JsonConfig();
        c.registerJsonValueProcessor(java.util.Date.class, new JsonDateValueProcessor());
        c.registerJsonValueProcessor(java.sql.Date.class, new JsonDateValueProcessor());
        c.registerJsonValueProcessor(java.sql.Timestamp.class, new JsonDateValueProcessor());
        JSON o = JSONSerializer.toJSON(m, c); 
        System.out.println( o );
    }
    
    private class JsonDateValueProcessor implements JsonValueProcessor {

        private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd mm:hh:ss"); 
        
        public Object processArrayValue(Object value, JsonConfig jc) { 
            if ( value instanceof java.util.Date ) {
                return sdf.format((java.util.Date) value); 
            }
            return "";
        }

        public Object processObjectValue(String name, Object value, JsonConfig jc) { 
            if ( value instanceof java.util.Date ) {
                return sdf.format((java.util.Date) value); 
            }
            return "";
        }
    }
}
