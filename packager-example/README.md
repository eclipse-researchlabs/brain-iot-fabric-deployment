# Paremus Packager Example

This example shows how to discover and use Docker services from a Smart Behaviour.

Paremus Packager is used to deploy the Docker service as part of a Brain-IoT fabric.

This example is copied and modified from the EventBus security light example:

* Browse to example web page: http://localhost:8080/sensor-ui/index.html
* Enter a message and send it by clicking the button.
  * This just sends an event on the `EventBus`, like the original example.
* When an message is received, by the `BehaviourImpl`, it is saved using an api on a Python REST service.
  * This remote Docker service is discovered using `@Reference` (OSGi Declarative Services).
* The web page also shows all messages sent using a direct API on the `BehaviourImpl` (not using the EventBus).

## Python service

We are using this example Python service: https://github.com/paremus/python_rest_tutorial.

It was originally configured to use `docker-compose` to deploy both the web and mongodb components.

Packager doesn't support `docker-compose` directly, but it can connect docker services, even if they are deployed on different nodes.

To facilitate this, `web/app.py` was changed to use an environment variable to specify the location of mongo:

```python
mongoUrl = os.getenv('MONGO_URL', "mongodb://my_db:27017")
```

The web component needs to be published to a Docker repository. In this example, I'm using my personal Docker hub account *db82407*.

```sh
$ cd ./web
$ docker build -t python_rest:1.0 .
Successfully tagged python_rest:1.0

$ docker tag python_rest:1.0 db82407/pkgtest:1.0
$ docker push db82407/pkgtest:1.0
```

*The mongodb component doesn't need to be published, as it just references an existing mongodb image.*

## Discover service in Java

Use OSGi Declarative Services to discover the Python service in Java.

The `@Reference` target is looking for a service with properties:

* name=python-rest (see next section).
* uri=http://*

`baseuri` is set to the Python service endpoint (possibly on a remote host with a dynamically allocated port).  

```java
private static final String PACKAGER_SERVICE_NAME = "python-rest";    

@Reference(target = "(&(uri=http://*)(name=" + PACKAGER_SERVICE_NAME + "))")
void setEndpoint(Endpoint endpoint, Map<String, Object> props) {
    Object uri = props.get("uri");
    if (uri instanceof String) {
        baseUri = URI.create((String) uri);
    } else if (uri instanceof String[]) {
        List<String> uris = Arrays.asList((String[]) uri);
        baseUri = URI.create(uris.get(0));
    }
    System.out.println("PYTHON REST API discovered at: " + baseUri);
}
```



## Deployment

### Packager configuration

`system.xml` is the Paremus Service Fabric deployment document.

The part that configures Packager to deploy the example Python web service is:

```xml
<config factory="true" pid="com.paremus.packager.pkg.generic">
    <property name="imageRef" value="db82407/pkgtest:1.0"/>
    <property name="packageType" value="python-rest"/>
    <property name="port" value="${port#any}"/>
    <property name="containerPort" value="5000"/>
    <property name="environment" value="MONGO_URL; endpoint:='(&amp;(uri=mongodb://*)(name=mongo-for-pyrest))' "/>
</config>
```

* **imageRef** specifies the Docker image reference (in my personal Docker hub account).
  * If you are using a private Docker repository, just specify the full image reference, including the repository host, for example: `docker-repo.brainiot.org:1234/pkgtest:1.0`
* **packageType** is referenced by `PACKAGER_SERVICE_NAME` in Java (above) to discover the service.
* **port** and **containerPort** map the Python container port (5000) to a dynamic host port
* **environment** sets `MONGO_URL` to the discovered (possibily remote) location of mongodb.



The part that configures Packager to deploy the example Mongo db service is:

```xml
<config factory="true" pid="com.paremus.packager.pkg.generic">
    <property name="imageRef" value="mongo:3.6.4"/>
    <property name="packageType" value="mongo-for-pyrest"/>
    <property name="port" value="${port#any}"/>
    <property name="containerPort" value="27017"/>
    <property name="advertiseScheme" value="mongodb"/>
</config>
```

* **imageRef** specifies the Docker image reference.
* **packageType** is referenced by **environment** in the Python packager config (above), to ensure it only finds this mongo instance.
* **advertiseScheme** uses `mongodb:` instead of `http:` as the published URL scheme.



### Fabric deployment

**Note**: *The fabric user needs permission to run Docker commands. This can be usually achieved by adding group read/write permission to the Docker socket and adding the fabric user to that group:*

```
$ sudo chmod g+rw /var/run/docker.sock
$ sudo usermod -a -G docker fabric
```

The full `system.xml` for the example is here: [brain-iot-pkgeg.xml](brain-iot-pkgeg.xml)

**Note**: *If all nodes in the Fabric are not running Docker, then you should label those that are with `Docker=true` and add a contract to limit deployment to those nodes, as is done for the UI:*

```xml
<contract features="(Docker=true)" cancelationCost="-1"/>
```

`system.xml` references the Brain-IoT `core` and `ui` bundles and Paremus Packager bundles in `repopath`:

```xml
repopath="brain-iot-repos/core/index.xml,brain-iot-repos/ui/index.xml, https://nexus2.paremus.com/content/repositories/releases/com/paremus/packager/_index/3.2.16/_index-3.2.16.xml"
```

**Note** *the Brain-IoT `core` and `ui` bundles are **not** deployed to Nexus, so must be provided locally.*

`packager-example-marketplace` is referenced via `http://localhost:8000`: 

```
    <config pid="eu.brain.iot.BehaviourManagementService">
      <!-- indexes must be accessible to all fabric nodes -->
      <property name="indexes"
                value="http://localhost:8000/packager-example-marketplace/target/marketplace/index.xml" />
    </config>
```

You can test locally using a simple http server to serve `brain-iot-pkgeg.xml` and its referenced resources.

First unpack the core brain-iot-repos in the `brain-iot-repos` directory:

```shell
$ unzip $BRAIN_IOT_REPOS/target/brain-iot-repos-0.0.1-SNAPSHOT.zip
```

Then start a simple http server

```shell
$ python3 -m http.server
```

Now import http://localhost:8000/brain-iot-pkgeg.xml into the Fabric, and deploy it.

You can now use the Brain-IoT UI at http://localhost:8888/behaviours to install the example sensor.

That will create the sensor UI at http://localhost:9000/sensor-ui/index.html, where you can send and list messages using the Python service.



## End

