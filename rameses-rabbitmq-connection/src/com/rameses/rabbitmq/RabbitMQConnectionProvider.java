/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rameses.rabbitmq;

import com.rameses.osiris3.xconnection.XConnection;
import com.rameses.osiris3.xconnection.XConnectionProvider;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author wflores 
 */
public class RabbitMQConnectionProvider extends XConnectionProvider {

    private final static String PROVIDER_NAME = "rabbitmq";
    
    public String getProviderName() {
        return PROVIDER_NAME; 
    }

    public XConnection createConnection(String name, Map conf) { 
        return new RabbitMQConnectionPool(conf, context, name);
    }

}
