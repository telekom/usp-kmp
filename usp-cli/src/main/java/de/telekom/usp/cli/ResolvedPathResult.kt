/*
 * SPDX-FileCopyrightText: 2026 Deutsche Telekom AG
 *
 * SPDX-License-Identifier: APACHE-2.0
 */

package de.telekom.usp.cli

import de.telekom.usp.messages.proto.AddResp
import de.telekom.usp.messages.proto.GetInstancesResp
import de.telekom.usp.messages.proto.GetResp
import de.telekom.usp.messages.proto.SetResp

/**
 * Helper class for printing a requested path, its resolved paths and their data in a uniform
 * manner for get, set, add etc. request responses.
 */
data class ResolvedPathResult(
    val requestedPath: String,
    val results: List<PathResult> = emptyList(),
    val errorCode: Int = 0,
    val errorMessage: String? = null
) {
    override fun toString(): String {
        return buildString {
            for (result in results) {
                append("Requested path '")
                append(requestedPath)
                append("' actual path: '")
                appendLine(result.path)

                for (row in result.rows) {
                    append(result.path)
                    append(row.key)
                    append(" = ")
                    appendLine(row.value)
                }
                if (result.errorCode != 0) {
                    appendLine("Error message: '${result.errorMessage}' (${result.errorCode})")
                }
            }

            if (errorCode != 0) {
                append("Requested path '")
                append(requestedPath)
                append("' returned error: '")
                append(errorMessage)
                append("' (")
                append(errorCode)
                appendLine(")")
            }
        }
    }
}

data class PathResult(
    val path: String,
    val rows: Map<String, String> = emptyMap(),
    val errorCode: Int = 0,
    val errorMessage: String? = null
)

fun List<GetResp.RequestedPathResult>.toGetResult() = map {
    ResolvedPathResult(
        requestedPath = it.requested_path,
        results = it.resolved_path_results.map { result ->
            PathResult(result.resolved_path, result.result_params)
        },
        errorCode = it.err_code,
        errorMessage = it.err_msg
    )
}

fun List<GetInstancesResp.RequestedPathResult>.toGetInstancesResult() = map {
    ResolvedPathResult(
        requestedPath = it.requested_path,
        results = it.curr_insts.map { instance ->
            PathResult(instance.instantiated_obj_path, instance.unique_keys)
        },
        errorCode = it.err_code,
        errorMessage = it.err_msg
    )
}

fun List<SetResp.UpdatedObjectResult>.toSetResult() = map { updatedObject ->
    val failures =
        updatedObject.oper_status?.oper_failure?.updated_inst_failures?.flatMap { failure ->
            failure.param_errs.map {
                PathResult(
                    path = failure.affected_path,
                    errorCode = it.err_code,
                    errorMessage = it.err_msg
                )
            }
        } ?: emptyList()

    val successes =
        updatedObject.oper_status?.oper_success?.updated_inst_results?.flatMap { success ->
            success.updated_params.map {
                PathResult(
                    path = success.affected_path,
                    rows = success.updated_params
                )
            }
        } ?: emptyList()

    ResolvedPathResult(updatedObject.requested_path, failures + successes)
}

fun List<AddResp.CreatedObjectResult>.toAddResult() = map { createdObject ->
    if (createdObject.oper_status?.oper_failure != null) {
        val failure = createdObject.oper_status?.oper_failure!!
        ResolvedPathResult(
            createdObject.requested_path,
            listOf(
                PathResult(
                    path = createdObject.requested_path,
                    errorCode = failure.err_code,
                    errorMessage = failure.err_msg
                )
            )
        )
    } else if (createdObject.oper_status?.oper_success != null) {
        val success = createdObject.oper_status?.oper_success!!
        ResolvedPathResult(
            createdObject.requested_path,
            buildList {
                add(PathResult(success.instantiated_path, success.unique_keys))
                success.param_errs.forEach { err ->
                    add(
                        PathResult(
                            path = success.instantiated_path,
                            errorCode = err.err_code,
                            errorMessage = err.err_msg
                        )
                    )
                }
            }
        )
    } else {
        throw IllegalArgumentException("Either failure or success must exist in $createdObject")
    }
}