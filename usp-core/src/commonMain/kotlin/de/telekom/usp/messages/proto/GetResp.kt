package de.telekom.usp.messages.proto

import de.telekom.usp.ResolvedPath
import de.telekom.usp.datamodel.InstanceObject

fun GetResp.allResolvedPaths(): List<InstanceObject> {
    return req_path_results.flatMap { requestedPathResult ->
        requestedPathResult.resolved_path_results.map { result ->
            InstanceObject(ResolvedPath(result.resolved_path), result.result_params)
        }
    }
}
