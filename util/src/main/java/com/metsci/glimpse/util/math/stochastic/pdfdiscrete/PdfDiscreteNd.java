/*
 * Copyright (c) 2020, Metron, Inc.
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
package com.metsci.glimpse.util.math.stochastic.pdfdiscrete;

import com.metsci.glimpse.util.math.stochastic.Generator;

/**
 * A fixed-dimensional multivariate discrete distribution.
 *
 * @author lo
 */
public interface PdfDiscreteNd
{

    /**
     * Gets the dimension of this distribution.
     *
     * @return the dimension
     */
    int dimension( );


    /**
     * Returns a random integer k-tuple, where k is the dimension of this distribution.
     *
     * @param g source of randomness
     * @return the generated int vector
     */
    int[] draw( Generator g );

    /**
     * Writes a random integer k-tuple into dst[offset], dst[offset+1], ..., dst[offset+k-1], where
     * k is the dimension of this distribution.
     *
     * @param dst array into which to write the random sample
     * @param offset initial index into dst array
     * @param g source of randomness
     * @throws IndexOutOfBoundsException if dst has insufficient space or offset < 0
     * @throws NullPointerException if dst is null
     */
    void draw( int[] dst, int offset, Generator g );

}
