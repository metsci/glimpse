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
package com.metsci.glimpse.docking;

public enum ViewCloseOption
{

    /**
     * Do not provide UI elements (e.g. an "x" button) which would allow the user to
     * request that the view be closed.
     */
    VIEW_NOT_CLOSEABLE,

    /**
     * Provide UI elements (e.g. an "x" button) which allow the user to request that
     * the view be closed, and automatically call {@link DockingGroup#closeView(View)}
     * in response to such requests.
     */
    VIEW_AUTO_CLOSEABLE,

    /**
     * Provide UI elements (e.g. an "x" button) which allow the user to request that
     * the view be closed -- but don't automatically call {@link DockingGroup#closeView(View)}.
     * <p>
     * This allows applications to provide custom view-close behavior, by implementing
     * {@link DockingGroupListener#userRequestingCloseView(DockingGroup, View)}, and
     * calling {@link DockingGroup#closeView(View)} as appropriate.
     */
    VIEW_CUSTOM_CLOSEABLE;

}
