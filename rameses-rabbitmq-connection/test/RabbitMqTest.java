/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import junit.framework.TestCase;


/**
 *
 * @author dell
 */
public class RabbitMqTest extends TestCase {
    
    public RabbitMqTest(String testName) {
        super(testName);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }
    
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    // TODO add test methods here. The name must begin with 'test'. For example:
    public void testHello() throws Exception {
        ConnectionFactory factory = new ConnectionFactory(); 
        factory.setHost( "localhost" ); 
        factory.setUsername( "test" ); 
        factory.setPassword( "test" ); 
        factory.setRequestedHeartbeat(10); 
        Connection connection = factory.newConnection(); 
        Channel channel = connection.createChannel();   
        channel.queueDeclare("etracsddn", true, true, true, null);
        String message = "Hello World New Message 1";
        channel.basicPublish("etracsddn", "063.100", null, message.getBytes());
        
        javax.swing.JOptionPane.showMessageDialog(null, "Pause");
        
        message = "Hello World New Message 100";
        channel.basicPublish("etracsddn", "063.100", null, message.getBytes());
        javax.swing.JOptionPane.showMessageDialog(null, "Pause");

        message = "Hello World New Message 3";
        channel.basicPublish("etracsddn", "063.100", null, message.getBytes());
        
        channel.close();
        connection.close();
        javax.swing.JOptionPane.showMessageDialog(null, "Yeah finished");
    }
}
