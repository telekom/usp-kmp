# Project MTP

Handles the processing of the Message Transfer Protocols. This package is agnostic of the actual
underlying protocol (WebSocket, MQTT etc.). It handles session creation and sending/receiving of 
USP records.

## Protocol Buffers
We use [Wire](https://square.github.io/wire/) for marshalling and unmarshalling bytes into objects
via Protocol Buffers. Wire is fast and easy to integrate into a multiplatform project. With Wire 
comes [Okio](https://square.github.io/okio/), which is a great tool for mangling with bytes in
network I/O. Hence we use it also in depending projects.