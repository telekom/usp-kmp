# Project Messages

Handles the processing of the USP records and messages. This package is agnostic of the actual
underlying message transfer protocol (WebSocket, MQTT etc.).

## Protocol Buffers
We use [Wire](https://square.github.io/wire/) for marshalling and unmarshalling bytes into objects
via Protocol Buffers. Unlike `kotlinx.serialization` Wire provides a Kotlin class generator to 
convert `.proto` files into class hierarchies and is also multiplatform capable. With Wire 
comes [Okio](https://square.github.io/okio/), which is a great tool for mangling with bytes in
network I/O. Hence we use it also in depending projects.

Important: dependent projects must include the Wire runtime dependency: 
`api("com.squareup.wire:wire-runtime:$wireVersion")`