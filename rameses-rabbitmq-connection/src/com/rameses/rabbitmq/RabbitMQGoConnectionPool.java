/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rameses.rabbitmq;

import com.rabbitmq.client.ConnectionFactory;
import com.rameses.http.HttpClient;
import com.rameses.osiris3.core.AbstractContext;
import com.rameses.osiris3.script.messaging.ScriptInvokerHandler;
import com.rameses.osiris3.script.messaging.ScriptResponseHandler;
import com.rameses.osiris3.xconnection.MessageConnection;
import com.rameses.osiris3.xconnection.MessageHandler;
import com.rameses.service.ScriptServiceContext;
import com.rameses.util.Base64Cipher;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import net.sf.json.JSONSerializer;
import net.sf.json.JsonConfig;
import net.sf.json.processors.JsonValueProcessor;

/**
 *
 * @author Toshiba
 */
public class RabbitMQGoConnectionPool extends MessageConnection
{
    private Map conf;
    private Map appConf;
    private AbstractContext context;
    private String name;
    private String queueName;
    private boolean started;

    private API api;
    private RabbitMQConnection rabbit;

    public RabbitMQGoConnectionPool(Map conf, AbstractContext context, String name){
        this.started = false;
        this.name = name;
        this.conf = conf;
        this.context = context;
        this.queueName = getProperty("queue");
        
        appConf = new HashMap();
        appConf.putAll(conf);
        
        api = new API(); 
        api.setUsername(getProperty("user"));
        api.setPassword(getProperty("pwd")); 
        api.setHost(getProperty("host")); 
        try {
            api.setPort(Integer.parseInt(getProperty("port"))); 
        } catch(Throwable t) {;}
        
        Map map = api.getExchange(getProperty("exchange")); 
        conf.put("exchange.auto_delete", getProperty("auto_delete", map)); 
        conf.put("exchange.durable", getProperty("durable", map)); 
        
        map = api.getQueue( this.queueName ); 
        conf.put("queue.auto_delete", getProperty("auto_delete", map)); 
        conf.put("queue.durable", getProperty("durable", map)); 
    }
            
    @Override
    public void start() {
        if ( started ) return;
        started = true;
        
        Object apphost = appConf.get("app.host");
        
        System.out.println("Initializing RabbitMQ Connection Factory (v2.0)...");
        ConnectionFactory factory = createConnectionFactory(); 
        
        rabbit = new RabbitMQConnection(name, context, conf); 
        if ( apphost == null ) {
            rabbit.addHandler( new MessageHandlerProxy());
        } else { 
            GoResponseHandler rrh = new GoResponseHandler( );
            ScriptInvokerHandler handler = new ScriptInvokerHandler(appConf, rrh);
            rabbit.addHandler(handler);
        } 

        rabbit.setAPI(api);
        rabbit.setFactory(factory); 
        rabbit.start();
    }

    private ConnectionFactory createConnectionFactory() {
        ConnectionFactory factory = new ConnectionFactory(); 
        factory.setHost( getProperty("host") ); 
        factory.setUsername( getProperty("user") ); 
        factory.setPassword( getProperty("pwd") ); 
        factory.setAutomaticRecoveryEnabled( true ); 

        int heartbeat = 30; 
        try { 
            heartbeat = Integer.parseInt(getProperty("heartbeat")); 
        } catch(Throwable t) {;} 

        int networkRecoveryInterval = 10000; 
        try { 
            networkRecoveryInterval = Integer.parseInt(getProperty("networkRecoveryInterval")); 
        } catch(Throwable t) {;} 

        factory.setRequestedHeartbeat( heartbeat );
        factory.setNetworkRecoveryInterval( networkRecoveryInterval ); 
        return factory; 
    }

    @Override
    public void stop() {
        try {
            rabbit.stop();
        } catch(Throwable e){;}
    }

    @Override
    public Map getConf() {
        return conf;
    }

    @Override
    public void send(Object data) {
        send(data, queueName);
    }

    @Override
    public void sendText(String data) {
        send(data, queueName);
    }

    @Override
    public void send(Object data, String queueName) {
        rabbit.send(data, queueName);
    }

    @Override
    public void addResponseHandler(String tokenid, MessageHandler handler) throws Exception {
        rabbit.addResponseHandler(tokenid, handler);
    }
    
    public void addQueue(String queueName, String exchange) throws Exception {
        if(exchange==null) {
            exchange = (String)conf.get("exchange");
        }
        rabbit.addQueue(queueName, exchange);
    }    
    
    public void removeQueue(String queueName, String exchange)  {
        if(exchange==null) {
            exchange = (String)conf.get("exchange");
        }
        rabbit.removeQueue(queueName, exchange);
    }    
    
    private class MessageHandlerProxy implements MessageHandler {

        public boolean accept(Object data) { 
            return true; 
        } 

        public void onMessage(Object data) { 
            RabbitMQGoConnectionPool.this.notifyHandlers( data ); 
        } 
    } 
    
    private String getProperty( String name ) {
        return getProperty(name, conf); 
    } 
    private String getProperty( String name, Map map ) {
        Object o = (map == null? null: map.get(name)); 
        return ( o == null ? null: o.toString()); 
    } 
    
    private class GoResponseHandler implements ScriptResponseHandler {

        GoResponseHandler() { 
        }
        
        public void send(Map map) {
            try {
                Object token = map.get("tokenid"); 
                if ( token == null ) {
                    System.out.println("Could not push result to cache server caused by tokenid not set."); 
                    return; 
                } 
                
                String tokenid = token.toString(); 
                Object result = map.get("result"); 
                Base64Cipher base64 = new Base64Cipher();       
                result = base64.decode(result.toString()); 

                Map reply = new HashMap(); 
                reply.put("status", "success"); 
                reply.put("message", "success"); 
                if ( result instanceof Throwable ) { 
                    Throwable t = ((Throwable) result); 
                    t.printStackTrace(); 
                    t = getCause(t); 
                    
                    reply.put("status", "error"); 
                    reply.put("message", t.getMessage()); 
                    result = reply; 
                } 
                
                JsonConfig jc = new JsonConfig(); 
                jc.registerJsonValueProcessor(java.util.Date.class, new JsonDateValueProcessor()); 
                jc.registerJsonValueProcessor(java.sql.Date.class, new JsonDateValueProcessor()); 
                jc.registerJsonValueProcessor(java.sql.Timestamp.class, new JsonDateValueProcessor()); 
                result = JSONSerializer.toJSON(result, jc); 
                
                reply.put("tokenid", tokenid);
                Object wsreply = JSONSerializer.toJSON(reply, jc); 
                
                pushToCache( tokenid, result.toString() ); 
                
                Object golangHost = appConf.get("golang.host");   
                HttpClient httpc = new HttpClient( golangHost.toString());
                httpc.setDebug( "true".equals(appConf.get("debug")+""));
                System.out.println("[golang] post result to "+ tokenid);
                httpc.post("gdx-notifier/publish/"+ tokenid, wsreply.toString()); 
            } catch(Throwable t) { 
                t.printStackTrace(); 
            } 
        }
        
        private Throwable getCause( Throwable t ) {
            Throwable p = t; 
            while (p != null) {
                if ( p.getMessage() != null ) return p; 
                
                p = p.getCause(); 
            }
            return t; 
        }
        
        private void pushToCache( String tokenid, Object result ) {
            Object cacheHost = appConf.get("cache.host");
            if ( cacheHost == null ) cacheHost = "localhost"; 
            
            Object cacheCluster = appConf.get("cache.cluster");
            Object cacheContext = appConf.get("cache.context");
            Object cacheTimeout = appConf.get("cache.timeout");
            if ( cacheTimeout == null ) cacheTimeout = 60000; 
            
            Map appenv = new HashMap();
            appenv.put("app.host", cacheHost);
            appenv.put("app.cluster", cacheCluster);
            appenv.put("app.context", cacheContext);     
            ScriptServiceContext ctx = new ScriptServiceContext(appenv);
            ICacheService svc = ctx.create("CacheService", ICacheService.class);
            
            Map param = new HashMap(); 
            param.put("key", tokenid);
            param.put("value", result); 
            param.put("timeout", cacheTimeout); 
            svc.put( param ); 
        }
    }
    
    public interface ICacheService {
        Object put( Object param ); 
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
