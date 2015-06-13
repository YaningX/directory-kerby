/**
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *  
 *    http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License. 
 *  
 */
package org.apache.kerby.kerberos.kerb.client.impl.event;

import org.apache.kerby.event.AbstractEventHandler;
import org.apache.kerby.event.Event;
import org.apache.kerby.event.EventType;
import org.apache.kerby.kerberos.kerb.client.KrbContext;
import org.apache.kerby.kerberos.kerb.client.KrbHandler;
import org.apache.kerby.kerberos.kerb.client.request.AsRequest;
import org.apache.kerby.kerberos.kerb.client.request.KdcRequest;
import org.apache.kerby.kerberos.kerb.client.request.TgsRequest;
import org.apache.kerby.transport.Transport;
import org.apache.kerby.transport.event.MessageEvent;
import org.apache.kerby.transport.event.TransportEventType;

import java.io.IOException;
import java.nio.ByteBuffer;

public class EventKrbHandler extends AbstractEventHandler {

    private KrbHandler innerHandler;


    public void init(KrbContext context) {
        this.innerHandler = new KrbHandler() {
            @Override
            protected void sendMessage(KdcRequest kdcRequest,
                                       ByteBuffer requestMessage) throws IOException {
                Transport transport = (Transport) kdcRequest.getSessionData();
                transport.sendMessage(requestMessage);
            }
        };
        innerHandler.init(context);
    }

    @Override
    public EventType[] getInterestedEvents() {
        return new EventType[] {
                TransportEventType.INBOUND_MESSAGE,
                KrbClientEventType.TGT_INTENT,
                KrbClientEventType.TKT_INTENT
        };
    }

    @Override
    protected void doHandle(Event event) throws Exception {
        EventType eventType = event.getEventType();

        if (eventType == KrbClientEventType.TGT_INTENT ||
                eventType == KrbClientEventType.TKT_INTENT) {
            KdcRequest kdcRequest = (KdcRequest) event.getEventData();
            innerHandler.handleRequest(kdcRequest);
        } else if (event.getEventType() == TransportEventType.INBOUND_MESSAGE) {
            handleMessage((MessageEvent) event);
        }
    }

    protected void handleMessage(MessageEvent event) throws Exception {
        ByteBuffer receivedMessage = event.getMessage();

        KdcRequest kdcRequest = (KdcRequest) event.getTransport().getAttachment();
        innerHandler.onResponseMessage(kdcRequest, receivedMessage);
        if (AsRequest.class.isAssignableFrom(kdcRequest.getClass())) {
            dispatch(KrbClientEvent.createTgtResultEvent((AsRequest) kdcRequest));
        } else if (TgsRequest.class.isAssignableFrom(kdcRequest.getClass())) {
            dispatch(KrbClientEvent.createTktResultEvent((TgsRequest) kdcRequest));
        }
    }
}