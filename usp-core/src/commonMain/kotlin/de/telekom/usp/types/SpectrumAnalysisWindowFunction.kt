/*
 * SPDX-FileCopyrightText: 2026 Deutsche Telekom AG
 *
 * SPDX-License-Identifier: Apache-2.0
 */

//
// Auto generated code - do not edit!
//
package de.telekom.usp.types

/**
 * This object controls the windowing function which will be used when performing the discrete
 * Fourier transform for the analysis. Note that all window functions may not be supported by all
 * devices. If an attempt is made to set the object to an unsupported window function, an error of
 * inconsistentValue will be returned.
 */
@Generated
public enum class SpectrumAnalysisWindowFunction(
    public val text: String,
    public val code: Int,
) : DataType {
    OTHER("Other", 0),
    HANN("Hann", 1),
    BLACKMAN_HARRIS("BlackmanHarris", 2),
    RECTANGULAR("Rectangular", 3),
    HAMMING("Hamming", 4),
    FLAT_TOP("FlatTop", 5),
    GAUSSIAN("Gaussian", 6),
    CHEBYSHEV("Chebyshev", 7),
    ;

    public companion object {
        public fun from(text: String): SpectrumAnalysisWindowFunction? = entries.firstOrNull {
            it.text == text
        }

        public fun from(code: Int): SpectrumAnalysisWindowFunction? = entries.firstOrNull {
            it.code == code
        }
    }
}
