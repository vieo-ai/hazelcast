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

package com.hazelcast.map.impl.operation;

import com.hazelcast.internal.nio.IOUtil;
import com.hazelcast.map.impl.MapDataSerializerHook;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.internal.serialization.Data;
import com.hazelcast.spi.impl.operationservice.Operation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Operation factory for load all operations.
 */
public class MapLoadAllOperationFactory extends AbstractMapOperationFactory {

    private List<Data> keys;
    private boolean replaceExistingValues;

    public MapLoadAllOperationFactory() {
        keys = Collections.emptyList();
    }

    public MapLoadAllOperationFactory(String name, List<Data> keys, boolean replaceExistingValues) {
        super(name);
        this.keys = keys;
        this.replaceExistingValues = replaceExistingValues;
    }

    @Override
    public Operation createOperation() {
        return new LoadAllOperation(name, keys, replaceExistingValues);
    }

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        out.writeString(name);
        final int size = keys.size();
        out.writeInt(size);
        for (Data key : keys) {
            IOUtil.writeData(out, key);
        }
        out.writeBoolean(replaceExistingValues);
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        name = in.readString();
        final int size = in.readInt();
        if (size > 0) {
            keys = new ArrayList<>(size);
        }
        for (int i = 0; i < size; i++) {
            Data data = IOUtil.readData(in);
            keys.add(data);
        }
        replaceExistingValues = in.readBoolean();
    }

    @Override
    public int getClassId() {
        return MapDataSerializerHook.LOAD_ALL_FACTORY;
    }
}
