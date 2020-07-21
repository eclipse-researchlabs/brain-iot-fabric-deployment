# fabric-deployment-repos

Builds zip file containing fabric repos

## Contents

### core.system

Builds "export jar" containing BrainIoT "core" components: EventBus and Behaviour Management System

### ui.system

Builds "export jar" containing BrainIoT "UI" components.

### brain-iot-repos

Builds zip file containing above components (and their dependencies).

```
Archive:  brain-iot-repos/target/brain-iot-repos-0.0.1-SNAPSHOT.zip
  Length      Date    Time    Name
---------  ---------- -----   ----
        0  07-21-2020 15:33   brain-iot-repos/
        0  07-21-2020 15:33   brain-iot-repos/ui/
        0  07-21-2020 15:33   brain-iot-repos/core/
```



## Deployment

The zip file should be unzipped on the marketplace server and referenced from the fabric system.xml `repopath`:

```
repopath="brain-iot-repos/core/index.xml,brain-iot-repos/ui/index.xml"
```


### end