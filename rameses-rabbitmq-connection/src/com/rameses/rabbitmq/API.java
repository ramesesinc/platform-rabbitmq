/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rameses.rabbitmq;

import com.rameses.io.IOStream;
import com.rameses.util.Base64Cipher;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author wflores
 */
public class API {
    
    private String username;
    private String password;
    private String host;
    private int port; 
    private boolean secured;
    
    public void setUsername( String username ) {
        this.username = username; 
    }
    public void setPassword( String password ) {
        this.password = password; 
    }
    public void setHost( String host ) {
        this.host = host; 
    }
    public void setPort( int port ) {
        this.port = port; 
    }
    public void setSecured( boolean secured ) {
        this.secured = secured; 
    }
    
    public Map getExchange( String name ) {
        try {
            if ( name == null || name.trim().length() == 0 ) {
                return new HashMap();
            }
            return (Map) get("/exchanges/%2F/"+ name);
        } catch( FileNotFoundException fnfe ) {
            throw new RuntimeException("'"+name+"' exchange not found"); 
        } catch(RuntimeException re) {
            throw re; 
        } catch(Throwable t) {
            throw new RuntimeException(t.getMessage(), t); 
        } 
    }
    
    public Map getQueue( String name ) {
        try {
            if ( name == null || name.trim().length() == 0 ) {
                return new HashMap();
            }
            return (Map) get("/queues/%2F/"+ name);
        } catch( FileNotFoundException fnfe ) {
            throw new RuntimeException("'"+name+"' queue not found"); 
        } catch(RuntimeException re) {
            throw re; 
        } catch(Throwable t) {
            throw new RuntimeException(t.getMessage(), t); 
        } 
    }
    
    private Object get( String path ) throws Exception {
        StringBuilder sb = new StringBuilder(); 
        sb.append( buildBaseURLPath() ).append( path ); 
        
        String authstr = (username +":"+ password); 
        String authenc = "Basic "+ new String(new Base64Cipher().encode(authstr.getBytes()));
        
        byte[] bytes = null; 
        HttpURLConnection urlc = null; 
        try {
            URL u = new URL( sb.toString()); 
            urlc = (HttpURLConnection) u.openConnection();
            urlc.setRequestProperty("Authorization", authenc);
            
            InputStream inp = urlc.getInputStream(); 
            bytes = IOStream.toByteArray(inp); 
            
        } finally {
            try { urlc.disconnect(); } catch(Throwable t){;}  
        }
        
        return new JSON().decode(bytes); 
    }
    
    private String buildBaseURLPath() {
        StringBuilder sb = new StringBuilder(); 
        sb.append( secured ? "https" : "http").append("://"); 
        sb.append( host == null ? "localhost" : host ).append(":");
        sb.append( port <= 0 ? "15672" : (port+"")).append("/api");
        return sb.toString(); 
    }
}
