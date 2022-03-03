<p align="center">
  <img src="https://setl.io/build/images/logo.png" style="align:right, float:left" alt="Setl Cordapp" width="300">
</p>

# SETL Corda Network

Welcome to the deployment project for the Setl Corda network.

# Network Participants

1. **Notary0** node:

        * RPC Address: ``notary0.setl.net:10009``
        * Host Public IP: ``34.250.64.111``

2. **Setl** node:

        * RPC Address: ``master.setl.net:10003``
        * Host Public IP: ``34.250.64.111``

3. **ClientA** node:

        * RPC Address: ``clienta.setl.net:10015``
        * Host Public IP: ``34.250.64.111``

4. **ClientB** node:

        *RPC Address: ``clientb.setl.net:10021``
        * Host Public IP: ``34.250.64.111``


5. **ClientC** node:

        * RPC Address: ``clientc.setl.net:10027``
        * Host Public IP: ``34.250.64.111``

**NOTE**: Node ``RPC Addresses`` are only valid on the docker virtual network.

# Setup/Deployment

## Server Requirements

* 1 EC2 instance running **Ubuntu 18.04 or 20.04**

## Software Requirements

* Docker-CE version 1.18.3 and above
* Docker-compose version 1.22.*
* Python 2.7 and above
* Git
* Download the Corda network bootstrapper ``corda-tools-network-bootstrapper-4.3.jar`` from [here](https://software.r3.com/artifactory/corda-releases/net/corda/corda-tools-network-bootstrapper/4.3/corda-tools-network-bootstrapper-4.3.jar)


# Packaging the nodes for the test network

## Generate the network map

1. Make sure you have **Corda Network Bootstrapper** [corda-tools-network-bootstrapper-4.3.jar](https://software.r3.com/artifactory/corda-releases/net/corda/corda-tools-network-bootstrapper/4.3/corda-tools-network-bootstrapper-4.3.jar) download in the directory.
    - ``curl -o corda-tools-network-bootstrapper-4.3.jar https://software.r3.com/artifactory/corda-releases/net/corda/corda-tools-network-bootstrapper/4.3/corda-tools-network-bootstrapper-4.3.jar ``
2. Ensure the node configurations are already defined in the ``participants`` directory. See the section [participants directory](#Participants Directory)
3. Copy the Cordapp jar files ``contracts-0.3.jar`` and ``workflows-0.3.jar`` to the ``participants`` dir
4. Run ``java -jar corda-tools-network-bootstrapper-4.3.jar --dir ./participants``
5. Copy the generated ``corda.jar`` file from ``./participants/setl`` to the parent directory of ``./participants`` directory.
   - ``cd ./participants .``
   - ``cp setl/corda.jar .``

# Participants Directory

    ├── notary0
    │   └── node.conf
    ├── setl
    │   └── node.conf
    └── clientA
        └── node.conf


## Build Docker images

1. ``cd`` to the participant directory *./participants*
2. Run:

    * ``docker build -t iobc/corda:4.3 .`` from ``./participants`` on the host
    

## Deploy Corda Nodes

1. While in the project ``corda-docker``, (parent directory of ./participants) directory, run ``./corda-network-start.sh`` script. This should start up the corda nodes on the host.

