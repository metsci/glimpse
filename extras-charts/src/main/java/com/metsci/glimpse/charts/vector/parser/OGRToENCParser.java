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

import com.metsci.glimpse.charts.vector.parser.objects.ENCObject;
import com.metsci.glimpse.charts.vector.parser.objects.GenericObject;

public class OGRToENCParser  {

    private static Logger logger = Logger.getLogger(OGRToENCParser.class.toString());

    private ENCObjectInterpreter objectInterpreter;
    private ENCUnitInterpreter unitInterpreter;
    private ENCAttributeInterpreter attributeInterpreter;
    private ENCAbstractBinder binder;

    public OGRToENCParser() throws IOException {
        unitInterpreter = new ENCUnitInterpreter();
        attributeInterpreter = new ENCAttributeInterpreter(unitInterpreter);
        objectInterpreter = new ENCObjectInterpreter(attributeInterpreter);

        binder = new ENCAbstractBinder();
    }

    public void parseENC(String encSourceDataDir, List<GenericObject> objList, List<ENCObject> encList) {
        // Parse the contents
        try {
            binder.resolveDependency(objList, encSourceDataDir);
            GenericObject.clearMetaObjects();
            binder.resolveMetaObjects(objList);
            //List<ENCMetaObject> metaObjects = binder.resolveMetaObjects2(_objList);

            //List<ENCObject> encList = new LinkedList<ENCObject>();
            writeENCObjects(objList, encList, true);

            // Remove the meta objects for the file we just loaded
            GenericObject.clearMetaObjects();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeENCObjects(List<GenericObject> objList, List<ENCObject> encList, boolean deleteGeneric) throws IOException {
        Iterator<GenericObject> _iterator = objList.iterator();
        while (_iterator.hasNext()) {
            GenericObject _currentObject = _iterator.next();
            try {
                ENCObject encObject = objectInterpreter.convertObject(_currentObject);
                encList.add(encObject);
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
