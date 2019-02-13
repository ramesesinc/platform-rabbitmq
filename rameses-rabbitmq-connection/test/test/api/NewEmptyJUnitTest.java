/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package test.api;

import com.rameses.rabbitmq.JSON;
import com.rameses.io.IOStream;
import com.rameses.util.Base64Cipher;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import junit.framework.TestCase;

/**
 *
 * @author ramesesinc
 */
public class NewEmptyJUnitTest extends TestCase {
    
    public NewEmptyJUnitTest(String testName) {
        super(testName);
    }
    
    public void test0() throws Exception {
        String auth = "Basic "+ new String(new Base64Cipher().encode("ramesesinc:!ramesesinc#".getBytes())); 
//        URL u = new URL("http://localhost:15672/api/exchanges"); 
        URL u = new URL("http://localhost:15672/api/exchanges/%2F/test"); 
        HttpURLConnection urlc = null; 
        byte[] bytes = null; 
        try {
            urlc = (HttpURLConnection) u.openConnection();
            urlc.setRequestProperty("Authorization", auth);
            
            InputStream inp = urlc.getInputStream(); 
            bytes = IOStream.toByteArray(inp); 
            
        } finally {
            try { urlc.disconnect(); } catch(Throwable t){;}  
        }

//        System.out.println(new String(bytes));
        JSON js = new JSON(); 
        Object res = js.decode(bytes);
        if ( res instanceof List ) {
            List list = (List) res; 
            for (Object o : list) {
                System.out.println("> " + o );
            }
        } else {
            System.out.println(">> " + res );
        }
    }
    
    
}
