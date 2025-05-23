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

package com.hazelcast.internal.serialization.impl.defaultserializers;

import com.hazelcast.internal.serialization.impl.SerializationUtil;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;

import java.io.IOException;
import java.util.Map;

/**
 * The {@link Map} serializer
 */
abstract class AbstractMapStreamSerializer<MapType extends Map> implements StreamSerializer<MapType> {

    @Override
    public void write(ObjectDataOutput out, MapType map) throws IOException {
        SerializationUtil.writeMap(map, out);
    }

    MapType deserializeEntries(ObjectDataInput in, int size, MapType result) throws IOException {
        for (int i = 0; i < size; i++) {
            result.put(in.readObject(), in.readObject());
        }
        return result;
    }

}
