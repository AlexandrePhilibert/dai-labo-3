# Simple Monitoring Tool

This repository contains the sources for the Simple Monitoring Tool Protocol and its implementation.

## Protocol

You can find more informations about the protocol being used by checking the [SPEC.md](./SPEC.md) file.

## Build

## Building the JAR

The project uses `mvnw`` in order to build and package the app.

```
./mvnw dependency:resolve clean compile package
```

### Building the docker image

The Simple Monitoring Tool is distributed as a Docker image.

Build the docker image:

```sh
docker build -t ghcr.io/alexandrephilibert/smt:latest
``` 

You can publish the image to ghcr using the following command:

```sh
docker push ghcr.io/alexandrephilibert/smt:latest
```

You must have the `package:write` permissions in order to push the image.

Here is the [link](https://github.com/AlexandrePhilibert/dai-labo-3/pkgs/container/smt) to the docker image.

## Running the app

The CLI is split in multiple subcommands:

- client
- emitter
- server

You can get more informations using the the following command:

```sh
java -jar ./target/dai-labo-3-1.0-SNAPSHOT.jar --help  
```

### Server example

For example, you can run the server with the following command:

```sh
java -jar ./target/dai-labo-3-1.0-SNAPSHOT.jar server --interface=<interface>
```

Where `<interface>` is one of your network interfaces.

You can find more examples in the [docker-compose.yaml](./docker-compose.yaml) file.

If no errors are output by the CLI, the app should be running 

### Client example

You can list the services using the following command:

```sh
java -jar ./target/dai-labo-3-1.0-SNAPSHOT.jar client            
```

You can then list the status of a service using:

```sh
java -jar ./target/dai-labo-3-1.0-SNAPSHOT.jar client -n <service>
```

Where `<service>` is the name of the service displayed in the list of services.