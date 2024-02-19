package de.telekom.usp.proto.msg

import de.telekom.usp.Error
import de.telekom.usp.NoError

fun GetResp.debugMessage(): String {
    return buildString {
        req_path_results.forEach { requestedResult ->
            if (requestedResult.err_code != NoError.code) {
                append("------------- ${Error.from(requestedResult.err_code)} -------------\n")
            } else {
                requestedResult.resolved_path_results.forEach { pathResult ->
                    append("------------- ${pathResult.resolved_path} -------------\n")
                    pathResult.result_params.forEach { param ->
                        append("${param.key}=${param.value}\n")
                    }
                }
            }
        }
    }
}