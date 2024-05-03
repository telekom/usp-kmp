package de.telekom.usp.messages.proto

import de.telekom.usp.Error
import de.telekom.usp.ResolvedPath
import de.telekom.usp.datamodel.InstanceObject

fun GetResp.allResolvedPaths(): List<InstanceObject> {
    return req_path_results.flatMap { requestedPathResult ->
        requestedPathResult.resolved_path_results.map { result ->
            InstanceObject(ResolvedPath(result.resolved_path), result.result_params)
        }
    }
}

fun GetResp.allErrors(): List<Error> {
    return req_path_results.mapNotNull { it.errorOrNull() }
}

fun GetResp.RequestedPathResult.errorOrNull(): Error? {
    return if (err_code != 0) {
        Error(err_code, err_msg)
    } else {
        null
    }
}