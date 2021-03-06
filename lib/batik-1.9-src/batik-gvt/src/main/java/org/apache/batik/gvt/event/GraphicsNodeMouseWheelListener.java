/*

   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */
package org.apache.batik.gvt.event;

import java.util.EventListener;

/**
 * The listener interface for receiving graphics node mouse wheel events.
 *
 * @author <a href="mailto:cam%40mcc%2eid%2eau">Cameron McCormack</a>
 * @version $Id: GraphicsNodeMouseWheelListener.java 1733416 2016-03-03 07:07:13Z gadams $
 */
public interface GraphicsNodeMouseWheelListener extends EventListener {

    /**
     * Invoked when the mouse wheel has been moved.
     * @param evt the graphics node mouse event
     */
    void mouseWheelMoved(GraphicsNodeMouseWheelEvent evt);
}
