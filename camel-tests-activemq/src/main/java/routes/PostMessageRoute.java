package routes;

import javax.jms.ConnectionFactory;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jms.JmsComponent;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.model.rest.RestBindingMode;

public class PostMessageRoute {

    public static void main(String[] args) throws Exception {

        CamelContext context = new DefaultCamelContext();

        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("admin", "admin", "tcp://localhost:61616");

        context.addComponent("activemq", JmsComponent.jmsComponent(connectionFactory));
        context.addRoutes(new RouteBuilder() {

            @Override
            public void configure() throws Exception {
                restConfiguration().host("localhost").component("undertow").port(8080).bindingMode(RestBindingMode.off);

                rest("/messages")
                    .post("/message")
                    .route()
                    .routeId("post2")
                    .autoStartup(true)
                    .convertBodyTo(String.class, "UTF-8")
                    .inOnly("activemq:TAMANHO.MENSAGEM")
                    .endRest()
                    .responseMessage()
                    .message("OK");
            }
        });
        context.start();
        Thread.sleep(2000000);
        context.stop();
    }

}
