//
// Auto generated code - do not edit!
//
package de.telekom.usp.types

/**
 * Represents the MoCA 2.0 PQOS Ingress Classification Rule.
 */
@Generated
public enum class MocaFlowIngrClassRule(
    public val text: String,
    public val code: Int,
) : DataType {
    RULE_DAVLANTAG_4_OR_5("ruleDAVLANtag4or5", 0),
    RULE_DAONLY("ruleDAonly", 4),
    RULE_DAAND_DSCPNO_VLAN("ruleDAandDSCPnoVLAN", 5),
    RULE_DAAND_VLANIGNORE_DSCP("ruleDAandVLANignoreDSCP", 6),
    RULE_DAAND_VLANOR_DSCP("ruleDAandVLANorDSCP", 7),
    ;

    public companion object {
        public fun from(text: String): MocaFlowIngrClassRule? = entries.firstOrNull {
            it.text == text
        }

        public fun from(code: Int): MocaFlowIngrClassRule? = entries.firstOrNull { it.code == code }
    }
}
