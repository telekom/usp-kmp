package de.telekom.usp

data class Error(val code: Int, val name: String) {

    fun toPair() = code to name

    override fun toString(): String {
        return "$name ($code)"
    }

    companion object {
        fun from(code: Int): Error {
            return when (code) {
                0 -> NoError
                7000 -> MessageFailed
                7001 -> MessageNotSupported
                7002 -> RequestDenied
                7003 -> InternalError
                7004 -> InvalidArguments
                7005 -> ResourcesExceeded
                7006 -> PermissionDenied
                7007 -> InvalidConfiguration
                7008 -> InvalidPathSyntax
                7009 -> ParameterActionFailed
                7010 -> UnsupportedParameter
                7011 -> InvalidType
                7012 -> InvalidValue
                7013 -> AttemptToUpdateNonWriteableParameter
                7014 -> ValueConflict
                7015 -> OperationError
                7016 -> ObjectDoesNotExist
                7017 -> ObjectCouldNotBeCreated
                7018 -> ObjectIsNotTable
                7019 -> AttemptToCreateNonCreatableObject
                7020 -> ObjectCouldNotBeUpdated
                7021 -> RequiredParameterFailed
                7022 -> CommandFailure
                7023 -> CommandCanceled
                7024 -> DeleteFailure
                7025 -> ObjectExistsWithDuplicateKey
                7026 -> InvalidPath
                7027 -> InvalidCommandArguments
                7028 -> RegisterFailure
                7029 -> AlreadyInUse
                7030 -> DeregisterFailure
                7031 -> PathAlreadyRegistered
                7100 -> RecordCouldNotBeParsed
                7101 -> SecureSessionRequired
                7102 -> SecureSessionNotSupported
                7103 -> SegmentationAndReassemblyNotSupported
                7104 -> InvalidRecordValue
                7105 -> SessionContextTerminated
                7106 -> SessionContextNotAllowed
                7200 - 7299 -> Error(code, "Data model error")
                7800 - 7999 -> Error(code, "Vendor defined error")
                else -> Error(7003, "Unknown error code: $code")
            }
        }
    }
}

val NoError = Error(0, "No Error")
val MessageFailed = Error(7000, "Message failed")
val MessageNotSupported = Error(7001, "Message not supported")
val RequestDenied = Error(7002, "Request denied (no reason specified)")
val InternalError = Error(7003, "Internal error")
val InvalidArguments = Error(7004, "Invalid arguments")
val ResourcesExceeded = Error(7005, "Resources exceeded")
val PermissionDenied = Error(7006, "Permission denied")
val InvalidConfiguration = Error(7007, "Invalid configuration")
val InvalidPathSyntax = Error(7008, "Invalid path syntax")
val ParameterActionFailed = Error(7009, "Parameter action failed")
val UnsupportedParameter = Error(7010, "Unsupported parameter")
val InvalidType = Error(7011, "Invalid type")
val InvalidValue = Error(7012, "Invalid value")
val AttemptToUpdateNonWriteableParameter = Error(7013, "Attempt to update non-writeable parameter")
val ValueConflict = Error(7014, "Value conflict")
val OperationError = Error(7015, "Operation error")
val ObjectDoesNotExist = Error(7016, "Object does not exist")
val ObjectCouldNotBeCreated = Error(7017, "Object could not be created")
val ObjectIsNotTable = Error(7018, "Object is not a table")
val AttemptToCreateNonCreatableObject = Error(7019, "Attempt to create non-creatable object")
val ObjectCouldNotBeUpdated = Error(7020, "Object could not be updated")
val RequiredParameterFailed = Error(7021, "Required parameter failed")
val CommandFailure = Error(7022, "Command failure")
val CommandCanceled = Error(7023, "Command canceled")
val DeleteFailure = Error(7024, "Delete failure")
val ObjectExistsWithDuplicateKey = Error(7025, "Object exists with duplicate key")
val InvalidPath = Error(7026, "Invalid path")
val InvalidCommandArguments = Error(7027, "Invalid command arguments")
val RegisterFailure = Error(7028, "Register failure")
val AlreadyInUse = Error(7029, "Already in use")
val DeregisterFailure = Error(7030, "Deregister failure")
val PathAlreadyRegistered = Error(7031, "Path already registered")

// USP Record errors -------------------------------------------------------------------------------
val RecordCouldNotBeParsed = Error(7100, "Record could not be parsed")
val SecureSessionRequired = Error(7101, "Secure session required")
val SecureSessionNotSupported = Error(7102, "Secure session not supported")
val SegmentationAndReassemblyNotSupported = Error(7103, "Segmentation and reassembly not supported")
val InvalidRecordValue = Error(7104, "Invalid Record value")
val SessionContextTerminated = Error(7105, "Session Context terminated")
val SessionContextNotAllowed = Error(7106, "Session Context not allowed")