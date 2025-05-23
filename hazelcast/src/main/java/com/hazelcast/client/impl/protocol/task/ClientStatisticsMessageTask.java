/*
 * Copyright (c) 2008-2025, Hazelcast, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hazelcast.client.impl.protocol.task;

import com.hazelcast.client.impl.statistics.ClientStatistics;
import com.hazelcast.client.impl.protocol.ClientMessage;
import com.hazelcast.client.impl.protocol.codec.ClientStatisticsCodec;
import com.hazelcast.instance.impl.Node;
import com.hazelcast.internal.nio.Connection;

import java.security.Permission;

public class ClientStatisticsMessageTask
        extends AbstractCallableMessageTask<ClientStatisticsCodec.RequestParameters> {

    public ClientStatisticsMessageTask(ClientMessage clientMessage, Node node, Connection connection) {
        super(clientMessage, node, connection);
    }

    @Override
    protected ClientStatisticsCodec.RequestParameters decodeClientMessage(ClientMessage clientMessage) {
        return ClientStatisticsCodec.decodeRequest(clientMessage);
    }

    @Override
    protected ClientMessage encodeResponse(Object response) {
        return ClientStatisticsCodec.encodeResponse();
    }

    @Override
    protected Object call() throws Exception {
        ClientStatistics clientStatistics = new ClientStatistics(parameters.timestamp, parameters.clientAttributes,
                parameters.metricsBlob);
        endpoint.setClientStatistics(clientStatistics);
        return null;
    }

    @Override
    public String getServiceName() {
        return null;
    }

    @Override
    public Permission getRequiredPermission() {
        return null;
    }

    @Override
    public String getDistributedObjectName() {
        return null;
    }

    @Override
    public String getMethodName() {
        return null;
    }

    @Override
    public Object[] getParameters() {
        return null;
    }

}
