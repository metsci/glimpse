/*
 * Copyright (c) 2016, Metron, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of Metron, Inc. nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL METRON, INC. BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.metsci.glimpse.util.primitives.sorted;

import java.util.Arrays;

import com.metsci.glimpse.util.primitives.Doubles;

/**
 * @author hogye
 */
public interface SortedDoubles extends Doubles
{

    /**
     * Follows the convention of {@link Arrays#binarySearch(double[], double)}:
     * <ul>
     * <li>If {@code x} is contained in this sequence, returns the index of {@code x}
     * <li>Otherwise, returns <tt>(-insertionPoint - 1)</tt>
     * </ul>
     *
     * @see Arrays#binarySearch(double[], double)
     */
    int indexOf( double x );

    /**
     * Index of the value closest to x. If the two closest values are x-C
     * and x+C, returns the index of x+C. If doubles is empty, returns -1.
     */
    int indexNearest( double x );

    /**
     * Largest index whose value is less than x
     */
    int indexBefore( double x );

    /**
     * Smallest index whose value is greater than x
     */
    int indexAfter( double x );

    /**
     * Largest index whose value is less than or equal to x
     */
    int indexAtOrBefore( double x );

    /**
     * Smallest index whose value is greater than or equal to x
     */
    int indexAtOrAfter( double x );

    /**
     * The continuous index at which x falls in this sequence.
     *
     * @see com.metsci.glimpse.util.primitives.sorted.ContinuousIndex
     */
    void continuousIndexOf( double x, ContinuousIndex result );

    /**
     * The continuous index at which x falls in this sequence.
     *
     * @see com.metsci.glimpse.util.primitives.sorted.ContinuousIndex
     */
    ContinuousIndex continuousIndexOf( double x );

    /**
     * For each x in xs, the continuous index at which x falls in this
     * sequence.
     *
     * @see com.metsci.glimpse.util.primitives.sorted.ContinuousIndexArray
     */
    void continuousIndicesOf( Doubles xs, ContinuousIndexArray result );

    /**
     * For each x in xs, the continuous index at which x falls in this
     * sequence.
     *
     * @see com.metsci.tracks.ContinuousIndexArray
     */
    ContinuousIndexArray continuousIndicesOf( Doubles xs );

    /**
     * For each x in xs, the continuous index at which x falls in this
     * sequence.
     *
     * Since xs is sorted, this method may be faster than {@link SortedDoubles#continuousIndicesOf(Doubles, ContinuousIndexArray)}
     * in some implementations.
     *
     * @see com.metsci.glimpse.util.primitives.sorted.ContinuousIndexArray
     */
    void continuousIndicesOf( SortedDoubles xs, ContinuousIndexArray result );

    /**
     * For each x in xs, the continuous index at which x falls in this
     * sequence.
     *
     * Since xs is sorted, this method may be faster than {@link SortedDoubles#continuousIndicesOf(Doubles)}
     * in some implementations.
     *
     * @see com.metsci.glimpse.util.primitives.sorted.ContinuousIndexArray
     */
    ContinuousIndexArray continuousIndicesOf( SortedDoubles xs );

}
