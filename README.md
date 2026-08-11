<!--
SPDX-FileCopyrightText: 2026 Deutsche Telekom AG

SPDX-License-Identifier: Apache-2.0    
-->

# USP Kotlin Multiplatform Client Library

This is a Kotlin Multiplatform client library for the [User Services Platform](https://usp.technology/) (USP).

[![REUSE Compliance Check](../../actions/workflows/reuse-compliance.yml/badge.svg)](../../actions/workflows/reuse-compliance.yml)

## Overview of components

### Message Transfer

The package `de.telekom.usp.mtp` contains classes implementing the USP message transfer protocols.
Currently only web socket and MQTT are supported. The core interface `MessageTransfer` is contained
in the **usp-core** project, while implementations are available in **usp-mtp**.

### USP Record Parsing

USP records are only used during transport and can be neglected once a USP message is derived from one
or more records. Hence, record parsing is encapsulated in the `MessageConverter` interface.
Its implementation is contained in the **usp-records** project. This is also the only project with
references to the USP record proto buffer classes.

### End to End Message Exchange

The package `de.telekom.usp.e2e` contains the `MessageConverter` class, which uses `MessageTransfer`
and `MessageConverter` to provide an easy-to-use interface for sending USP messages.

### Data Model

The data model implementation of this library is mainly used for storing USP agent data retrieved
via the network. Hence it contains no data validation, but also provides a `PathResolver` for
conversion of an unresolved path into resolved paths.

### Core Project

The core project (**usp-core**) provides the interface definitions of the aforementioned classes and
some additional core classes like `Path`, `ResolvedPath`, `EndpointIdentifier` etc.

It also contains the auto-generated proto buffer classes for message exchange in package
`de.telekom.usp.messages.proto`. Additionally `de.telekom.usp.messages.dsl` provides a Kotlin DSL
for creation of USP `Msg` instances.

### Command Line Interface

The project **usp-cli** contains a simple command line interface, mainly aimed at testing and basic
agent manipulation.

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
