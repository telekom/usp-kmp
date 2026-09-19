<!--
SPDX-FileCopyrightText: 2026 Deutsche Telekom AG

SPDX-License-Identifier: Apache-2.0    
-->

# USP Kotlin Multiplatform Client Library

A Kotlin Multiplatform client library for [TR-369](https://usp.technology/specification/index.html),
the [User Services Platform](https://usp.technology/) (USP) specified by the Broad Band Forum.

[![REUSE Compliance Check](../../actions/workflows/reuse-compliance.yml/badge.svg)](../../actions/workflows/reuse-compliance.yml)

## Overview of components

Packages and subprojects are aligned with the chapters of the TR-369 specification:

### Core

The core project (**usp-core**) provides some base classes like `Path`, `ResolvedPath`,
`EndpointIdentifier` etc. It also contains Kotlin value classes which represent all types
defined by the USP specification (like `Alias`, `MACAddress` etc. pp.).

It additionally defines interfaces for the main USP processing logic like `MessageTransfer`,
`MessageConverter` and `DataModel`.

The core project also contains the auto-generated Kotlin classes derived from the proto buffer message
definitions, like for example `Get` and `GetResp` (we use [wire](https://square.github.io/wire/)
for code generation). The `de.telekom.usp.messages.dsl` package also provides exhaustive DSL support
for generating messages. Using the DSL, creating a `Get` message is as simple as:

```kotlin
val get = Get {
    maxDepth = 2
    paths(Device, DeviceInfo)
}
```

(Note that this example also uses the Kotlin objects `Device` and `DeviceInfo`. There is a Kotlin
object for every root path specified by USP.)

Finally, the core package also contains the `MessageExchange` class, which is responsible for handling
the end-to-end message exchange, replying on the `MessageTransfer` and `MessageConverter` interfaces.

### MTP

The **usp-mtp** subproject provides an implementation of the `MessageTransfer` interface, responsible
for encapsulation of the message transfer details. Currently supported transfer protocols are
**Websockets** and **MQTT**.

### Records

The **usp-records** subproject provides an implementation of the `MessageConverter` interface, responsible
for converting raw bytes from and to USP messages.

It also contains the auto-generated Kotlin classes derived from the proto buffer record definitions.

### Data Model

The **usp-datamodel** subproject provides simple data model implementation (im-memory or file based)
for storing USP agent data retrieved via the network. It is a light-weight data model, not meant for
full-featured data model storage. It also provides a `PathResolver` for conversion of an unresolved
path into resolved paths.

### Exchange

Finally, the **usp-exchange** subproject provides the `MessageExchange` class for exchanging actual data
between USP endpoints. It also provides a builder class for gluing together all configuration parts of
the message exchange.

### CLI

Additionally, the **usp-cli** is a JVM only subproject, which provides a simple command line interface,
aimed at interactive testing and basic agent manipulation.

## Code of Conduct

This project has adopted the [Contributor Covenant](https://www.contributor-covenant.org/) in version 2.1 as our code of
conduct. Please see the details in our [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md). All contributors must abide by the code
of conduct.

By participating in this project, you agree to abide by its [Code of Conduct](./CODE_OF_CONDUCT.md) at all times.

## Licensing

Copyright (c) 2026 Deutsche Telekom AG

All content in this repository is licensed under at least one of the licenses found in [./LICENSES](./LICENSES); you may
not use this file, or any other file in this repository, except in compliance with the Licenses. You may obtain a copy
of
the Licenses by reviewing the files found in the [./LICENSES](./LICENSES) folder.

Unless required by applicable law or agreed to in writing, software distributed under the Licenses is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See in
the [./LICENSES](./LICENSES)
folder for the specific language governing permissions and limitations under the Licenses.

This project follows the [REUSE standard for software licensing](https://reuse.software/).
Each file contains copyright and license information, and license texts can be found in the [./LICENSES](./LICENSES)
folder. For more information visit https://reuse.software/. You can find a guide for developers at
https://telekom.github.io/reuse-template/.

## References

- [The User Services Platform Specification](https://usp.technology/specification/index.htm)
- [TR-369.org](https://tr369.org/)
- [Understanding TR-369 USP Message Types](https://tr369.org/tr-369-usp-message-types/)
