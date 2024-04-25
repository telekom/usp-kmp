# A USP Multiplatform Library

A Kotlin Multiplatform library for exchanging USP messages between two USP endpoints.

## Architecture

The following diagram provides an overview of the main package structure:

```plantuml
package "Package e2e" {
  [MessageExchange]
}

package "Package mtp" {
  [MessageTransfer]
  [MessageTransferFactory]
}

package "Package messages" {
  [MessageConverter]
  [MessageConversionResult]
}

package "Package datamodel" {
  [DataModel]
  [PathResolver]
}

MessageTransfer <|-- MessageTransferFactory
MessageConverter -- MessageConversionResult
MessageExchange -> MessageTransfer
MessageExchange --> MessageConverter
DataModel <-- PathResolver
```

### Message Transfer

The package `de.telekom.usp.mtp` contains classes to implement the USP message transfer protocols.
Currently only web socket and MQTT are supported. The core interface `MessageTransfer` is contained
the **usp-core** project, while implementations are available in **usp-mtp**.

### USP Record Parsing

USP records are only used during transport and can be neglected once a USP message is derived from
one or several records. Hence record parsing is encapsulated in the `MessageConverter` interface.
Its implementation is contained in the **usp-records** project. This is also the only project with
references to the USP record proto buffer classes.

### End to End Message Exchange

The package `de.telekom.usp.e2e` contains the `MessageConverter` class, which uses `MessageTransfer`
and `MessageConverter` to provide an easy to use interface for sending USP messages.

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

## References

- [The User Services Platform Specification](https://usp.technology/specification/index.htm)
- [TR-369.org](https://tr369.org/)
- [Understanding TR-369 USP Message Types](https://tr369.org/tr-369-usp-message-types/)
