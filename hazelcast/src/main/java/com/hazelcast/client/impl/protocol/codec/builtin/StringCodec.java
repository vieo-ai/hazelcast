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

package com.hazelcast.client.impl.protocol.codec.builtin;

import com.hazelcast.client.impl.protocol.ClientMessage;

import java.nio.charset.StandardCharsets;

public final class StringCodec {

    private StringCodec() {
    }

    public static void encode(ClientMessage clientMessage, String value) {
        clientMessage.add(new ClientMessage.Frame(value.getBytes(StandardCharsets.UTF_8)));
    }

    public static String decode(ClientMessage.ForwardFrameIterator iterator) {
        return new String(iterator.next().content, StandardCharsets.UTF_8);
    }
}
