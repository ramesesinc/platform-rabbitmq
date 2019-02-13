/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package test.api;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import junit.framework.TestCase;

/**
 *
 * @author ramesesinc
 */
public class MQSender extends TestCase {
    
    private String host; 
    private String user;
    private String pwd;
    private String type;
    private String exchange;
    
    public MQSender(String testName) {
        super(testName);
        
        this.host = "localhost"; 
        this.user = "ramesesinc";
        this.pwd  = "!ramesesinc#";
        this.type = "direct";
        this.exchange = "gdx";
    }
    
    public void testSend() throws Exception {
        
        String exc = (this.exchange == null ? "" : this.exchange); 
        String qName = "154x";
        Connection conn = null; 
        try {
            conn = createConnection(); 
            Channel channel = createChannel(conn, qName); 
            channel.basicPublish(exc, qName, null, "Hello World".getBytes()); 
        } finally {
            try { conn.close(); }catch(Throwable t){;} 
        }
    }
    
    private Connection createConnection() throws Exception {
        ConnectionFactory factory = new ConnectionFactory(); 
        factory.setHost( host ); 
        factory.setUsername( user ); 
        factory.setPassword( pwd ); 
        factory.setAutomaticRecoveryEnabled( true ); 

        int heartbeat = 30; 
        int networkRecoveryInterval = 10000; 

        factory.setRequestedHeartbeat( heartbeat );
        factory.setNetworkRecoveryInterval( networkRecoveryInterval ); 
        return factory.newConnection(); 
    }
    
    private Channel createChannel(Connection conn, String qName ) throws Exception {
        Channel channel = conn.createChannel();
        
        if (qName != null) {
            channel.queueDeclarePassive( qName );
            
//            if ( type == null ) type = "direct";  
//            if ( exchange != null ) {
//                channel.exchangeDeclare( exchange, type, true );
//                channel.queueBind( qName, exchange, qName);
//            } 
        }
        return channel; 
    }

}
