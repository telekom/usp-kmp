package de.telekom.usp

import de.telekom.usp.util.isHexDigit
import de.telekom.usp.util.isUnreserved
import kotlin.jvm.JvmInline

@JvmInline
value class InstanceId(val instance: String) {

    companion object {

        fun isValidId(instance: String) : Boolean {
            var i = 0
            while (i < instance.length) {
                if (instance[i].isUnreserved()) {
                    i += 1
                    continue
                } else if (instance[i] == '%' && i < instance.length - 2) {
                    if (instance[i + 1].isHexDigit() && instance[i + 2].isHexDigit()) {
                        i += 3
                        continue
                    }
                }
                return false
            }
            return true
        }
    }
}