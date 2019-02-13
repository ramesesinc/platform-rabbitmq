/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rameses.rabbitmq;

import com.rameses.io.IOStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.sf.json.JSONArray;
import net.sf.json.JSONNull;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import net.sf.json.util.PropertyFilter;

/**
 *
 * @author wflores
 */
public class JSON {

    public Object decode(InputStream inp) {
        try {
            if (inp == null) {
                return null;
            }
            return decode(IOStream.toByteArray(inp));
        } catch (RuntimeException re) {
            throw re;
        } catch (Throwable t) {
            throw new RuntimeException(t.getMessage(), t);
        }
    }

    public Object decode(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        return decode(new String(bytes));
    }

    public Object decode(String text) {
        String str = (text == null ? "" : text.trim());
        if (str.length() == 0) {
            return null;
        }

        Object jo = null;
        if (str.startsWith("[")) {
            jo = JSONArray.fromObject(str);
        } else {
            jo = JSONObject.fromObject(str);
        }
        
        if ( jo instanceof JSONArray ) {
            return toList((JSONArray) jo);  
        } else {
            return toMap((JSONObject) jo); 
        } 
    }

    private Map toMap( JSONObject jo ) {
        JsonConfig conf = new JsonConfig();
        conf.setJavaPropertyFilter(new JsonFilter());
        return (Map) JSONObject.toBean(jo, new LinkedHashMap(), conf);
    }
    
    private List toList( JSONArray arr) {
        Iterator iter = arr.iterator();
        List list = new ArrayList();
        while(iter.hasNext()) {
            Object o = iter.next();
            if( o instanceof JSONObject ) {
                list.add( toMap((JSONObject) o ));
            } else if( o instanceof JSONArray ) {
                list.add( toList((JSONArray) o ));
            } else {
                list.add(o);
            }
        }
        return list;
    }    

    private class JsonFilter implements PropertyFilter {

        public boolean apply(Object object, String field, Object value) {
            Map map = (Map) object;  //the holder
            if (value == null) {
                map.put(field, null);
            } else if (value instanceof JSONNull) {
                map.put(field, null);
            } else if (value instanceof JSONArray) {
                List list = toList((JSONArray) value);
                map.put(field, list);
            } else if (value instanceof JSONObject) {
                JSONObject o = (JSONObject) value;
                Map bean = toMap(o);
                map.put(field, bean);
            } else if (value instanceof Double) {
                map.put(field, new BigDecimal(value + ""));
            } else {
                map.put(field, value);
            }
            return true;
        }
    }
}
