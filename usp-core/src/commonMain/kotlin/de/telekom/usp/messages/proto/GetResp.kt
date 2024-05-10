package de.telekom.usp.messages.proto

import de.telekom.usp.Error
import de.telekom.usp.Path
import de.telekom.usp.ResolvedPath
import de.telekom.usp.datamodel.InstanceObject
import de.telekom.usp.toResolvedPath

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

/**
 * Finds all direct child instances of the specified path in this `GetResp`. For example if `path`
 * is `Device.IP.Interface.` the list may contain `Device.IP.Interface.1.` and `Device.IP.Interface.2.`
 * (but not `Device.IP.Interface.1.IPv4Address.1.` as it is not a direct child).
 */
fun GetResp.allInstancesOf(path: Path): List<InstanceObject> {
    return req_path_results.flatMap { requestedPathResult ->
        requestedPathResult.resolved_path_results.mapNotNull { result ->
            val resolvedPath = result.resolved_path.toResolvedPath()
            if (path.isChildInstance(resolvedPath)) {
                InstanceObject(resolvedPath, result.result_params)
            } else {
                null
            }
        }
    }
}