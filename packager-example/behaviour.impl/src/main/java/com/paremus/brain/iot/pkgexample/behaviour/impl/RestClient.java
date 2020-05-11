package com.paremus.brain.iot.pkgexample.behaviour.impl;

import org.osgi.dto.DTO;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import java.net.URI;
import java.util.List;

/**
 * client for https://github.com/DewaldOosthuizen/python_rest_tutorial
 */
public class RestClient {

    private final CredsDTO creds;
    private WebTarget target;
    private boolean isRegistered;

    public RestClient(ClientBuilder builder, URI baseUri, String username, String password) {
        creds = new CredsDTO();
        creds.username = username;
        creds.password = password;

        Client client = builder.build();

        client.register(JsonpConvertingPlugin.class);

        this.target = client.target(baseUri);
    }

    private void register() {
        if (!isRegistered) {
            isRegistered = true;
            ResponseDTO resp = target.path("register").request()
                    .post(Entity.json(creds), ResponseDTO.class);
        }
    }

    public List<String> retrieve() {
        register();
        RetrieveDTO resp = target.path("retrieve").request()
                .post(Entity.json(creds), RetrieveDTO.class);
        return resp.obj;
    }

    public void save(String message) {
        SaveDTO save = new SaveDTO();
        save.username = creds.username;
        save.password = creds.password;
        save.message = message;

        register();
        ResponseDTO resp = target.path("save").request()
                .post(Entity.json(save), ResponseDTO.class);
    }

}

