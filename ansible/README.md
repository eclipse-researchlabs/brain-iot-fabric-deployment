# Brain-IoT Fabric Management

The fabric is managed using `ansible` scripts. The ansible scripts are available from the Brain-IoT Git repository https://git.repository-pert.ismb.it/BRAIN-IoT/fabric-deployment/tree/master/ansible.

They are currently installed in `~fabric-n4/ansible` on `fabric-n4`, but could be installed on any host with access to the network.

## Deploy the initial Fabric

$ **ansible-playbook fabric.yml**

This only needs to be used to initially deploy the fabric code and Java to each node or to add new nodes.

It can also be used to update the config on all nodes, for example if you change the infrastructure node.

The nodes in the fabric are defined in the hosts file:

```
[infra]
192.168.2.130 ansible_user=fabric-n9

[simple]
192.168.2.101 ansible_user=fabric-n1
192.168.2.120 ansible_user=fabric-n4
192.168.2.118 ansible_user=fabric-n5
192.168.2.125 ansible_user=fabric-n6
192.168.2.109 ansible_user=fabric-n7
192.168.2.202 ansible_user=brainiot-ros
```

Note: hosts is a symbolic-link to one of many fabric configurations, for example

```
hosts-int - using internal network addresses
hosts-vpn - using external VPN addresses
```

Currently all nodes in the fabric are Pi 4s running Debian 10, except **fabric-n9** is an intel NUC running Ubuntu 18.

The fabric master "infrastructure" node is **fabric-n9**, which benefits from its larger cpu and memory.

## Control Brain-IoT Fabric

$ **./fabric.sh**
Usage: ./fabric.sh: start | stop | erase | status | bootlog | start1 | reinstall

- start | stop  - controls the systemd fabric.service on all fabric nodes.
- status - shows java process status on all fabric nodes.
- erase - removes fabric state at next start (this will stop previously deployed systems from restarting)

To perform a full restart of fabric, it is suggested you do the following:

```
./fabric.sh stop
./fabric.sh status
./fabric.sh erase
./fabric.sh start
./fabric.sh status
```

The remaining options are mostly for debugging:

* bootlog - shows the initial fibre startup log on each node. This will show, for example, if Java is missing.
* start1 IP-address - this will restart the fibre service on the specified node
* reinstall - this will force re-install of the fabric archive, for example, if you receive an update.

## Fabric UI

The fabric admin UI runs on the infrastructure node (**fabric-n9**). It is available after the fabric is **started**.

[https://10.8.0.94:9106](https://10.8.0.94:9106/)
[https://192.168.2.130:9106](https://192.168.2.130:9106/)
User: AdminUser
Password: AdminUser

The Brain-IoT UI is usually configured to run on node **fabric-n9**. It is only available after a Brain-IoT system.xml document is imported and deployed (using the Fabric Admin UI):
[http://10.8.0.94:8888](http://10.8.0.94:8888/)
[http://192.168.2.130:8888](http://192.168.2.130:8888/)
User: admin
Password: admin

## Fabric Import

To run Brain-IoT systems on the fabric, you need to create a system.xml document that describes the system and "import" it using the **Systems** tab on the Fabric Admin UI. Note: when importing a system using the Fabric Admin UI the internal non-VPN address should be used.

The system.xml document (and the resources it references) need to be accessible to all nodes in the fabric. So they are installed on the **marketplace** node in `/var/www/html`. For example: http://10.8.0.98/security-light-system.xml.

If you look at this document, you will see it references the following resources:

- `repopath` - this contains the "core" Brain-IoT runtime - Event Bus, Behaviour Management Service and UI.

  - repopath="brain-iot-repos/core/index.xml,brain-iot-repos/ui/index.xml"
  - `brain-iot-repos` was created by the fabric-deployment project: https://git.repository-pert.ismb.it/BRAIN-IoT/fabric-deployment/tree/master/fabric-deploy-repos

- `indexes` - this contains the deployable smart behaviours. `com.paremus.brain.iot.marketplace` was created from the Brain-IoT marketplace project: https://git.repository-pert.ismb.it/BRAIN-IoT/marketplace/tree/master/security-light-marketplace

  ```
  <property name="indexes" value="http://192.168.2.88/com.paremus.brain.iot.marketplace/security-light-marketplace/0.0.1-SNAPSHOT/index.xml"/>
  ```

## Add node to fabric

To add a new node to the fabric, you must ensure it is accessible using the brainiot ssh-key (without a password). For example:

```
ssh fabric-n4
cd ~/ansible
ssh-copy-id -i brainiot.pub brainiot-ros@10.8.0.106
```

You should now be able to ssh to the new node without a password:
    `ssh -i brainiot brainiot-ros@10.8.0.106`

You also need to ensure the fabric-user can use sudo to become root without a password:

```
$ sudo visudo
[sudo] password for brainiot-ros:
# append this line to end of file
brainiot-ros ALL=(ALL) NOPASSWD:ALL
```

Now add the new node to the fabric by editing the hosts file:

```
[simple]
...
192.168.2.202 ansible_user=brainiot-ros
```

Next deploy the fabric to the new node:

```
ansible-playbook fabric.yml
```

## Installing Ansible scripts

To install the Ansible scripts on another host the following setup is required:

* create a new ssh key pair using `ssh-keygen` and save the keys in ~/ansible:

  ```
$ cd ~/ansible 
$ ssh-keygen
Generating public/private rsa key pair.
Enter file in which to save the key (/home/fabric-n8/.ssh/id_rsa): ./mykey
Enter passphrase (empty for no passphrase):
  ```

* edit `ansible.cfg` and change the name of the ssh key:

  ```
private_key_file = mykey
  ```

* allow the new node ssh access to all hosts in the fabric without password, for example:

  ```
$ ssh-copy-id -i mykey.pub fabric-n1@192.168.2.101
  ```

* copy the non-git files from the existing installation:
  * fabric-eval.zip - fabric installation archive
  
  * license.ini - fabric license file
  
* jdk-8u231-linux-arm32-vfp-hflt.tar.gz - Java for ARM (Raspberry π)
  
  * jdk-8u231-linux-x64.tar.gz - Java for x86
  
  If you want to change the Java version, change the reference to these archives in `fabric.yml`.
  
* check the `hosts` file contains all the nodes required in the fabric:

  You can have multiple different files and create a symbolic link to the current one, for example:
  
  ```
  $ ls -l hosts*
  lrwxrwxrwx 1 fabric-n4 pi   9 Jun 22 11:49 hosts -> hosts-int
  -rw-r--r-- 1 fabric-n4 pi 317 Aug  3 15:16 hosts-int
  -rw-r--r-- 1 fabric-n4 pi 261 Jun 17 21:16 hosts-vpn
  ```
  

## end

  