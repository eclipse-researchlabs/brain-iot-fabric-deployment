/* Copyright 2019 Paremus, Ltd - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */
package com.paremus.brain.iot.pkgexample.behaviour.impl;

import com.paremus.brain.iot.pkgexample.behaviour.api.SensorMessages;
import com.paremus.brain.iot.pkgexample.sensor.api.SensorReadingDTO;
import eu.brain.iot.eventing.annotation.SmartBehaviourDefinition;
import eu.brain.iot.eventing.api.SmartBehaviour;
import org.bndtools.service.endpoint.Endpoint;
import org.osgi.service.component.annotations.*;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

import javax.ws.rs.client.ClientBuilder;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * A Smart Behaviour using a REST API from a Docker packaged service
 */
@Component
@SmartBehaviourDefinition(consumed = SensorReadingDTO.class, filter = "(timestamp=*)",
        author = "Paremus", name = "Example Packager Smart Behaviour",
        description = "Uses REST API to send message to Docker packaged service.")
@Designate(ocd = ComponentImpl.Config.class)
public class ComponentImpl implements SmartBehaviour<SensorReadingDTO>, SensorMessages {
    /**
     * Service name must match the packageType in the system.xml deployment descriptor
     */
    private static final String PACKAGER_SERVICE_NAME = "python-rest";

    @ObjectClassDefinition(
            name = "Packager Example Behaviour",
            description = "Configuration for the Packager Example Behaviour."
    )
    @interface Config {
        @AttributeDefinition(type = AttributeType.STRING,
                name = "Username",
                description = "API username"
        )
        String username() default "brainiot";

        @AttributeDefinition(type = AttributeType.PASSWORD,
                name = "Password",
                description = "API password"
        )
        String password() default "brainiot";
    }

    @Reference(target = "(&(uri=http://*)(name=" + PACKAGER_SERVICE_NAME + "))",
    cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY)
    void setEndpoint(Endpoint endpoint, Map<String, Object> props) {
        Object uri = props.get("uri");
        if (uri instanceof String) {
            baseUri = URI.create((String) uri);
        } else if (uri instanceof String[]) {
            List<String> uris = Arrays.asList((String[]) uri);
            baseUri = URI.create(uris.get(0));
        }

        System.out.println("PYTHON REST API discovered at: " + baseUri);

        setClient();
    }

    void unsetEndpoint(Object endpoint, Map<String, Object> props) {
        client = null;
    }

    @Reference
    private ClientBuilder clientBuilder;

    private URI baseUri;

    private Config config;

    private RestClient client;

    private void setClient() {
        if (baseUri != null && config != null) {
            try {
                client = new RestClient(clientBuilder, baseUri, config.username(), config.password());
            }
            catch (Exception e) {
                System.err.println("XXX eek! failed to create client: " + e);
                e.printStackTrace();
            }
        }
    }

    @Activate
    void activate(Config config) {
        this.config = config;
        setClient();
    }

    @Modified
    void modify(Config config) {
        this.config = config;
        setClient();
    }

    @Override
    public void notify(SensorReadingDTO event) {
        String message = event.message;
        if (message == null || message.isEmpty()) {
            message = "hello world";
        }

        if (client != null) {
            client.save(message);
        }
        else {
            System.out.println("PYTHON REST API not yet discovered");
        }
    }

    @Override
    public List<String> getMessages() {
        if (client != null) {
            return client.retrieve();
        }
        else {
            return Collections.singletonList("-- behaviour.impl is not connected to python service --");
        }
    }

}
