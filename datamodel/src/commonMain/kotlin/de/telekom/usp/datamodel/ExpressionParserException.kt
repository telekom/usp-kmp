package de.telekom.usp.datamodel

import de.telekom.usp.PathElement

internal class ExpressionParserException(
    expression: PathElement.Expression,
    expressionIndex: Int,
    message: String
) : Exception("Error parsing ${expressionIndex + 1}. component of '$expression': $message")