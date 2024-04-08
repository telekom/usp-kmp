package de.telekom.usp.datamodel

import de.telekom.usp.PathElement

class ExpressionParserException(
    expression: PathElement.Expression,
    expressionIndex: Int,
    message: String
) : Exception("Error parsing ${expressionIndex + 1}. component of '$expression': $message")