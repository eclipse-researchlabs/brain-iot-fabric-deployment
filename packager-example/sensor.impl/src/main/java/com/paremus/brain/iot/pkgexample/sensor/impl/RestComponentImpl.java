/* Copyright 2019 Paremus, Ltd - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */
package com.paremus.brain.iot.pkgexample.sensor.impl;

import com.paremus.brain.iot.pkgexample.behaviour.api.SensorMessages;
import com.paremus.brain.iot.pkgexample.sensor.api.SensorReadingDTO;
import eu.brain.iot.eventing.annotation.SmartBehaviourDefinition;
import eu.brain.iot.eventing.api.EventBus;
import org.osgi.dto.DTO;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.whiteboard.propertytypes.HttpWhiteboardResource;
import org.osgi.service.jaxrs.whiteboard.propertytypes.JSONRequired;
import org.osgi.service.jaxrs.whiteboard.propertytypes.JaxrsResource;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * This component triggers sensor readings based on web clicks
 */
@Component(service = RestComponentImpl.class)
@JaxrsResource
@HttpWhiteboardResource(pattern = "/sensor-ui/*", prefix = "/static")
@JSONRequired
@Produces(MediaType.APPLICATION_JSON)
// SmartBehaviourDefinition is just so example sensor is added to repository
@SmartBehaviourDefinition(consumed = {}, // this component does not consume events
        author = "Paremus", name = "Example Packager Smart Sensor",
        description = "Implements a Packager Smart Sensor and UI to display it.")
public class RestComponentImpl {

    @Reference
    private EventBus eventBus;

    @Reference
    private SensorMessages messages;

    @Path("sensor")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public String send(SendDTO body) {
        SensorReadingDTO sensorReadingDTO = new SensorReadingDTO();
        sensorReadingDTO.message = body.message;
        eventBus.deliver(sensorReadingDTO);
        return "\"OK\"";
    }

    @Path("messages")
    @GET
    public List<String> getMessages() {
        return messages.getMessages();
    }

}

