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

package com.hazelcast.internal.partition.operation;

import com.hazelcast.cluster.Address;
import com.hazelcast.internal.cluster.impl.operations.DemoteDataMemberOp;
import com.hazelcast.internal.partition.IPartitionService;
import com.hazelcast.internal.partition.MigrationCycleOperation;
import com.hazelcast.internal.partition.impl.InternalPartitionServiceImpl;
import com.hazelcast.internal.partition.impl.PartitionDataSerializerHook;
import com.hazelcast.internal.util.UUIDSerializationUtil;
import com.hazelcast.logging.ILogger;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.spi.impl.NodeEngine;

import java.io.IOException;
import java.util.UUID;

/**
 * Signal that the given member no longer owns any replicas (and its state may be changed to lite member),
 * sent from master member to the demoted member.
 *
 * @see DemoteDataMemberOp
 *
 * @since 5.4
 */
public class DemoteResponseOperation
        extends AbstractPartitionOperation implements MigrationCycleOperation {

    private UUID uuid;

    public DemoteResponseOperation() {
    }

    public DemoteResponseOperation(UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public void run() {
        InternalPartitionServiceImpl partitionService = getService();
        ILogger logger = getLogger();
        Address caller = getCallerAddress();

        NodeEngine nodeEngine = getNodeEngine();

        if (partitionService.isMemberMaster(caller)) {
            if (logger.isFinestEnabled()) {
                logger.finest("Received demote response from %s", caller);
            }

            if (nodeEngine.getLocalMember().getUuid().equals(uuid)) {
                partitionService.onDemoteResponse();
            } else {
                logger.warning("Ignoring demote response for " + uuid + " since it's not the expected member");
            }
        } else {
            logger.warning("Received demote response from " + caller + " but it's not the known master");
        }
    }

    @Override
    public boolean returnsResponse() {
        return false;
    }

    @Override
    public String getServiceName() {
        return IPartitionService.SERVICE_NAME;
    }

    @Override
    public int getClassId() {
        return PartitionDataSerializerHook.DEMOTE_RESPONSE;
    }

    @Override
    protected void writeInternal(ObjectDataOutput out) throws IOException {
        super.writeInternal(out);

        UUIDSerializationUtil.writeUUID(out, uuid);
    }

    @Override
    protected void readInternal(ObjectDataInput in) throws IOException {
        super.readInternal(in);

        uuid = UUIDSerializationUtil.readUUID(in);
    }
}
