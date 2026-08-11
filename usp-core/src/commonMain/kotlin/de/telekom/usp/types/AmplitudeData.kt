/*
 * SPDX-FileCopyrightText: 2026 Deutsche Telekom AG
 *
 * SPDX-License-Identifier: Apache-2.0
 */

//
// Auto generated code - do not edit!
//
package de.telekom.usp.types

import okio.ByteString
import okio.ByteString.Companion.decodeHex
import kotlin.jvm.JvmInline

/**
 * This data type represents a sequence of spectral amplitudes. Each spectral amplitude value
 * corresponds to a bin. The format of the bin measurement is as follows.  Sequence of:  : 4 bytes:
 * ChCenterFreq  :: The center frequency of the upstream channel.  : 4 bytes: FreqSpan  :: The width in
 * Hz of the band across which the spectral amplitudes    characterizing the channel are measured.  : 4
 * bytes: NumberOfBins  :: The number of data points or bins that compose the spectral data. The   
 * leftmost bin corresponds to the lower band edge, the rightmost bin    corresponds to the upper band
 * edge, and the middle bin center is    aligned with the center frequency of the analysis span.  : 4
 * bytes:  :: BinSpacing The frequency separation between adjacent bin centers. It    is derived from
 * the frequency span and the number of bins or data    points. The bin spacing is computed as:  :::
 * BinSpacing = FrequencySpan/(NumberOfBins -1)  ::The larger the number of bins the finer the
 * resolution.  : 4 bytes: ResolutionBW  :: The resolution bandwidth or equivalent noise bandwidth of
 * each bin. If    spectral windowing is used (based on vendor implementation), the bin    spacing and
 * resolution bandwidth would not generally be the same.  : n bytes: Amplitude (2 bytes * NumberOfBins)
 *  :: A sequence of two byte elements. Each element represents the spectral    amplitudes in relation
 * to the expected received signal power of a bin,    in units of 0.01dB. That is, a test CMTS input
 * signal with square-root    raised-cosine spectrum, bandwidth equal to the expected received   
 * signal bandwidth, and power equal to the expected received signal    power, which is present for the
 * entire spectrum sampling period, will    exhibit a spectrum measurement of 0 dB average power in
 * each bin of    the signal passband. Each bin element amplitude value format is 2's    complement
 * which provides a range of -327.68 dB to 327.67 dB amplitude    value for the bin measurement.
 */
@JvmInline
@Generated
public value class AmplitudeData(
    public val wrapped: ByteString,
) : DataType {
    public constructor(text: String) : this(text.decodeHex())

    override fun toString(): String = wrapped.hex()
}
