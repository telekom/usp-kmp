package de.telekom.usp.messages.proto

import de.telekom.usp.Error
import de.telekom.usp.NoError

fun GetResp.debugMessage(): String {
    return buildString {
        req_path_results.forEach { requestedResult ->
            if (requestedResult.err_code != NoError.code) {
                append("------------- ${Error.from(requestedResult.err_code)} -------------")
            } else {
                if (requestedResult.resolved_path_results.isEmpty()) {
                    append("------------- ${requestedResult.requested_path} -------------\n[]")
                } else {
                    requestedResult.resolved_path_results.forEach { pathResult ->
                        append("\n")
                        append(pathResult.debugMessage())
                    }
                }
            }
        }
    }
}

fun GetResp.ResolvedPathResult.debugMessage(): String {
    return result_params.entries.joinToString(
        separator = "\n",
        prefix = "------------- ${this.resolved_path} -------------\n"
    ) {
        "${it.key}=${it.value}"
    }
}