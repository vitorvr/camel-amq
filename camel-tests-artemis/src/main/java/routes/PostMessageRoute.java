package routes;

import java.net.URI;

import javax.jms.ConnectionFactory;

import org.apache.activemq.artemis.api.jms.ActiveMQJMSClient;
import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jms.JmsComponent;
import org.apache.camel.component.jms.JmsConfiguration;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.model.rest.RestBindingMode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class PostMessageRoute {

    private static final Logger LOG = LoggerFactory.getLogger(PostMessageRoute.class);

    public static void main(String[] args) throws Exception {
        
        CamelContext context = new DefaultCamelContext();

        JmsComponent component = createArtemisComponent();
        context.addComponent("amq", component);
        
        context.addRoutes(new RouteBuilder() {
            
            @Override
            public void configure() throws Exception {
                restConfiguration()
                .host("localhost")
                .component("undertow").port(8080)
                .bindingMode(RestBindingMode.off);
                
                rest("/messages")
                    .post("/message")
                        .route()
                        .routeId("post")
                        .autoStartup(true)
                        .convertBodyTo(String.class, "UTF-8")
                        .inOnly("amq:queue:TAMANHO.MENSAGEM")
                        .endRest().responseMessage().message("OK");
            }
        });
        context.start();
        Thread.sleep(2000000);
    }
    
    private static JmsComponent createArtemisComponent() {

        JmsComponent component = new JmsComponent();
        try {
            ConnectionFactory factory = createFactory("localhost", 61616, "Vitor", "admin", "admin");

            JmsConfiguration configuration = new JmsConfiguration();
            configuration.setConnectionFactory(factory);

            component = new JmsComponent(configuration);
        } catch (Exception e) {
            LOG.error("Error when creating Factory: {}.", e.getMessage(), e);
        }
        return component;
    }

    private static ConnectionFactory createFactory(final String host, final Integer port, final String name, final String username, final String password)
            throws Exception {

        URI uri = new URI("tcp", null, host, port, null, "httpEnabled=true", null);

        ActiveMQConnectionFactory factory = ActiveMQJMSClient.createConnectionFactory(uri.toString(), name);
        factory.setUser(username);
        factory.setPassword(password);

        return factory;
    }
}