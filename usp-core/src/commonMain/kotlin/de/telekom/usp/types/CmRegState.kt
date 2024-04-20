//
// Auto generated code - do not edit!
//
package de.telekom.usp.types

/**
 * This data type defines the CM connectivity state as reported by the CM.  {{enum}}  See
 * {{bibref|CM-SP-MULPIv3.0}}, Cable Modem - CMTS Interaction.
 */
@Generated
public enum class CmRegState(
    public val text: String,
    public val code: Int,
) : DataType {
    /**
     * indicates any state not described below
     */
    OTHER("Other", 1),

    /**
     * indicates that the CM has not started the registration process yet
     */
    NOT_READY("NotReady", 2),

    /**
     * indicates that the CM has not initiated or completed the synchronization of the downstream
     * physical layer
     */
    NOT_SYNCHRONIZED("NotSynchronized", 3),

    /**
     * indicates that the CM has completed the synchronization of the downstream physical layer
     */
    PHY_SYNCHRONIZED("PhySynchronized", 4),

    /**
     * indicates that the CM has completed the upstream parameters acquisition or have completed the
     * downstream and upstream service groups resolution, whether the CM is registering in a pre-3.0 or
     * a 3.0 CMTS
     */
    US_PARAMETERS_ACQUIRED("UsParametersAcquired", 5),

    /**
     * indicates that the CM has completed initial ranging and received a Ranging Status of success
     * from the CMTS in the RNG-RSP message
     */
    RANGING_COMPLETE("RangingComplete", 6),

    /**
     * indicates that the CM has received a DHCPv4 ACK message from the CMTS
     */
    DHCPV_4_COMPLETE("DHCPv4Complete", 7),

    /**
     * indicates that the CM has successfully acquired time of day. If the ToD is acquired after the
     * CM is operational, this value should not be reported
     */
    TO_DESTABLISHED("ToDEstablished", 8),

    /**
     * indicates that the CM has successfully completed the BPI initialization process
     */
    SECURITY_ESTABLISHED("SecurityEstablished", 9),

    /**
     * indicates that the CM has completed the config file download process
     */
    CONFIG_FILE_DOWNLOAD_COMPLETE("ConfigFileDownloadComplete", 10),

    /**
     * indicates that the CM has successfully completed the Registration process with the CMTS
     */
    REGISTRATION_COMPLETE("RegistrationComplete", 11),

    /**
     * indicates that the CM has completed all necessary initialization steps and is operational
     */
    OPERATIONAL("Operational", 12),

    /**
     * indicates that the CM has received a registration aborted notification from the CMTS
     */
    ACCESS_DENIED("AccessDenied", 13),

    /**
     * indicates that the CM has sent an Auth Info message for EAE
     */
    EAEIN_PROGRESS("EAEInProgress", 14),

    /**
     * indicates that the CM has sent a DHCPv4 DISCOVER to gain IP connectivity
     */
    DHCPV_4_IN_PROGRESS("DHCPv4InProgress", 15),

    /**
     * indicates that the CM has sent an DHCPv6 Solicit message
     */
    DHCPV_6_IN_PROGRESS("DHCPv6InProgress", 16),

    /**
     * indicates that the CM has received a DHCPv6 Reply message from the CMTS
     */
    DHCPV_6_COMPLETE("DHCPv6Complete", 17),

    /**
     * indicates that the CM has sent a Registration Request (REG-REQ or REG-REQ-MP)
     */
    REGISTRATION_IN_PROGRESS("RegistrationInProgress", 18),

    /**
     * indicates that the CM has started the BPI initialization process as indicated in the CM
     * config file. If the CM already performed EAE, this state is skipped by the CM
     */
    BPIINIT("BPIInit", 19),

    /**
     * indicates that the registration process was completed, but the network access option in the
     * received configuration file prohibits forwarding
     */
    FORWARDING_DISABLED("ForwardingDisabled", 20),

    /**
     * indicates that the CM is attempting to determine its MD-DS-SG
     */
    DS_TOPOLOGY_RESOLUTION_IN_PROGRESS("DsTopologyResolutionInProgress", 21),

    /**
     * indicates that the CM has initiated the ranging process
     */
    RANGING_IN_PROGRESS("RangingInProgress", 22),

    /**
     * indicates that the CM is instructed to mute all channels in the CM-CTRL-REQ message from CMTS
     */
    RFMUTE_ALL("RFMuteAll", 23),
    ;

    public companion object {
        public fun from(text: String): CmRegState? = entries.firstOrNull { it.text == text }

        public fun from(code: Int): CmRegState? = entries.firstOrNull { it.code == code }
    }
}
