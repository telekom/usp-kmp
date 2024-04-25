# A USP Multiplatform Library

A Kotlin Multiplatform library for exchanging USP messages between two USP endpoints.

```plantuml
package "Package mtp" {
  [MessageTransfer]
  [MessageTransferFactory]
}

package "Package messages" {
  [MessageConverter]
  [MessageConversionResult]
  package "Package proto" {
    [ProtoBuffer classes]
  }
  package "Package dsl" {
    [Kotlin DSL]
  }
}

package "Package e2e" {
  [MessageExchange]
}

package "Package datamodel" {
  [DataModel]
}

MessageTransferFactory --|> MessageTransfer
MessageConversionResult -- MessageConverter
```

## References
- [The User Services Platform Specification](https://usp.technology/specification/index.htm)
- [TR-369.org](https://tr369.org/)
- [Understanding TR-369 USP Message Types](https://tr369.org/tr-369-usp-message-types/)
