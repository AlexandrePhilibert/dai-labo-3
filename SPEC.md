# Overview

The Simple Monitoring Tool Protocol (or SMTP), is a protocol used by the SMT tool to both:

- Communicate to a central server the status of services, including custom labels
- Allow for clients to access the server, and view the current status of the service, in addition to uptime statistics,
  and custom labels

# Transport Protocol

The SMTP protocol is composed of the two following sections:

- The Status Signaling Protocol (or SSP) is implemented by both the monitored services and the server
- The Status Querying Protocol (or SQP) is implemented by both the client and the server.

Both the SSP and SQP uses the UDP protocol. The protocol itself is textual, and all textual data
MUST be encoded in the UTF-8 format for transmission.

The maximum packet length is 512. No additional characters will be treated in either protocols.
This allows for remote access to the SQP protocol, as a safe maximum for UDP packets are a 512 byte max payload,
and to simplify the implementation.

### SSP

The SSP uses multicast UDP, with the reserved multicast addresses 239.0.192.5 & 239.0.192.6 with the port 25591.

The sending party (the service) does not expect a response. This is indented as a way to check for the outbound
networking status.

### SQP

The SQP uses unicast UDP, with the reserved port 25592.

The protocol follows the Request Response messaging pattern. It is described more in details in the next section

# Messages

### SSP

The SSP is voluntarily simple, in order to facilitate implementation.

The majority of the difficulty resides in the server implementation.

The multicast address depends on the wanted visibility:

| IP Address  | Visibility |
|-------------|------------|
| 239.0.192.5 | Public     |
| 239.0.192.6 | Private    |

Other ports MAY be used by the implementation.

The protocol relies on the following simple datagram, sent at regular intervals (at most 5s)

```
name=Test Service
up=true
m_cpu=500/2000
mb_ram=1024/2048
```

The datagram uses the following format:

```
[<unit>_]<metric>=<value>
```

Currently, only the following metrics must be supported:

- `name`, a string, represents the name of the service being monitored
- `up`, a boolean value, represents whether the service considers itself healthy.
- `cpu`, an integer value, represents the cpu usage of the service / machine, in milli-cpu.
- `ram`, an integer value, represents the current ram usage of the service / machine, in megabytes.

As this protocol is Fire & Forget, no response is to be expected from the server.

### SQP

The SQP protocol is a Request & Response protocol, and every message should expect a corresponding response.

The client MAY send the command multiple times, until a response is received.

#### `LIST`

Allows to list status of different clients.

The query is of the following format:

```
LIST [status=(UP|DOWN)]
```

Where `status` allows for the client to filter the clients by their status:
> [!INFO]
> It is important to note that the interpretation of `alive` and `dead` is left to the implementer,
> but a service cannot be both at any given time.

- The value `UP` means that only services that are currently **alive** will be returned by the server
- The value `DOWN` means that only services that are currently **dead** will be returned by the server

The server MUST respond to the source ip and port with a message of the following format:

```
<service1_name> UP
<service2_name> DOWN
...
```

The implementer MAY support listings above the maximal length of the packet,
as defined by this specification.

#### `GET`

The `GET` command allows to get the details of a given service.
The command is of the following format:

```
GET <service_name> [type=(last|avg)]
```

Where `type` indicates the data that will be returned by the server (case-insensitive):

- `last` will only get the last value
- `avg` will extract absolute averages (since the server started)

The response of the server should look like the following:

```
name=<service_name>
up=true
m_cpu=500/2000
mb_ram=1024/2048
```

The format is the same as the one in the SSP protocol, with a major difference:

In the event that the server considers the other service dead (the method of determining the deadness of a service is
left to the implementer, but can be based upon a PING, or the non-retrieval of a status datagram since a fixed amount of
time)

In that case, the output may look like this:

```
name=<service_name>
up=false
```

In the case of the client selecting the `avg` type in the request, the following changes to the SSP MUST be considered:
- The integer metrics are transformed to floating point averages, for both the current, and maximum values.
- The following additional metrics MUST be present:
  - `p_uptime`, the uptime in percents (with AT LEAST 3 decimal places of precision)
