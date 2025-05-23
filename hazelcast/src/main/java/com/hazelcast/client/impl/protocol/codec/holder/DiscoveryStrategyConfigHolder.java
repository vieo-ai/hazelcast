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
package com.hazelcast.client.impl.protocol.codec.holder;

import com.hazelcast.internal.serialization.Data;

import java.util.Map;
import java.util.Objects;

public final class DiscoveryStrategyConfigHolder {
    private final String className;
    private final Map<String, Data> properties;

    public DiscoveryStrategyConfigHolder(String className, Map<String, Data> properties) {
        this.className = className;
        this.properties = properties;
    }

    public String getClassName() {
        return className;
    }

    public Map<String, Data> getProperties() {
        return properties;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DiscoveryStrategyConfigHolder that = (DiscoveryStrategyConfigHolder) o;
        return Objects.equals(className, that.className) && Objects.equals(properties, that.properties);
    }

    @Override
    public int hashCode() {
        return Objects.hash(className, properties);
    }
}
