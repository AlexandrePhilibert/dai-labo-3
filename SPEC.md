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

The SSP uses multicast UDP, with the reserved multicast address 239.0.192.5 with the port 25591.

The sending party (the service) does not expect a response. This is indented as a way to check for the outbound
networking status.

### SQP

The SQP uses unicast UDP, with the reserved port 25591.

The protocol follows the Request Response messaging pattern. It is described more in details in the next section

# Messages

### SSP

The SSP is voluntarily simple, in order to facilitate implementation.

The majority of the difficulty resides in the server implementation.

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