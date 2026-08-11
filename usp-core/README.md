<!--
SPDX-FileCopyrightText: 2026 Deutsche Telekom AG

SPDX-License-Identifier: Apache-2.0    
-->

# Project Base

The base project defines some basic interface and classes to work with USP:

- `de.telekom.usp.messages.MessageConverter` which allows the conversion of byte strings into USP
  message. This completely encapsulates the handling of USP records. The conversion of records to
  message to vice versa is implemented in the **records subproject**.
- `de.telekom.usp.mtp.MessageTransfer`, which is an abstraction for the message transfer protocol
  (i.e. web sockets, MQTT, Stomp etc.). Implementations of the message transfer protocol can be
  found in the **mtp subproject**.
- The package `de.telekom.usp.messages.dsl` contains a comprehensive set of DSL builders to help the
  creation of instances of USP messages (instances of `Msg`), which can be very clumsy and error
  prone otherwise.
- The package `de.telekom.usp.messages.proto` contains all proto buffer classes and some useful
  extension functions.
- Additionally the project contains some independent base classes like `EndpointIdentifier`, `Path`,
  `Error` etc.

## Protocol Buffers

We use [Wire](https://square.github.io/wire/) for marshalling and unmarshalling bytes into objects
via Protocol Buffers. Unlike `kotlinx.serialization` Wire provides a Kotlin class generator to
convert `.proto` files into class hierarchies and is also multiplatform capable. With Wire
comes [Okio](https://square.github.io/okio/), which is a great tool for mangling bytes strings in
network I/O. Hence we use it also in depending projects.

**Important**: dependent projects must include the Wire runtime dependency:

```kotlin
api("com.squareup.wire:wire-runtime:$wireVersion")
```