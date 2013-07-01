/*
 * Copyright (c) 2012, Metron, Inc.
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
package com.metsci.glimpse.charts.vector.parser;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.metsci.glimpse.charts.vector.parser.objects.DNCObject;
import com.metsci.glimpse.charts.vector.parser.objects.GenericObject;

/**
 *
 */
public class OGRToDNCParser  {

    private static Logger logger = Logger.getLogger(OGRToDNCParser.class.toString());

    private DNCObjectInterpreter objectInterpreter;
    private DNCAttributeInterpreter attributeInterpreter;

    public OGRToDNCParser() throws IOException {
        attributeInterpreter = new DNCAttributeInterpreter();
        objectInterpreter = new DNCObjectInterpreter(attributeInterpreter);
    }

    public void parseDNC(String dncSourceDataDir, List<GenericObject> objList, List<DNCObject> dncList) {
        // Parse the contents
        try {
            writeDNCObjects(objList, dncList, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeDNCObjects(List<GenericObject> objList, List<DNCObject> dncList, boolean deleteGeneric) throws IOException {
        Iterator<GenericObject> _iterator = objList.iterator();
        while (_iterator.hasNext()) {
            GenericObject _currentObject = _iterator.next();
            try {
                DNCObject dncObject = objectInterpreter.convertObject(_currentObject);
                if (dncObject != null) {
                    dncList.add(dncObject);
                }
                if (deleteGeneric)
                    _iterator.remove();
            } catch (Exception e) {
                // Conversion failed somewhere so we just drop the entry
                logger.log(Level.SEVERE, "Conversion failed -- Generic Object Entry:" + _currentObject.toString(), e);
                System.exit(0);
            }
        }
    }
}
