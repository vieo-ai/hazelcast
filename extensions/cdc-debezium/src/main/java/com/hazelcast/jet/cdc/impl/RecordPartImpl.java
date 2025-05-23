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

package com.hazelcast.jet.cdc.impl;

import com.hazelcast.jet.cdc.ParsingException;
import com.hazelcast.jet.cdc.RecordPart;
import com.hazelcast.jet.json.JsonUtil;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

class RecordPartImpl implements RecordPart {

    private String json;

    private final transient Supplier<String> jsonSupplier;

    private Map<String, Object> content;

    RecordPartImpl(@Nonnull Supplier<String> json) {
        this.jsonSupplier = requireNonNull(json);
    }
    RecordPartImpl(@Nonnull String json) {
        this.json = requireNonNull(json);
        this.jsonSupplier = null;
    }

    @Override
    @Nonnull
    public <T> T toObject(@Nonnull Class<T> clazz) throws ParsingException {
        requireNonNull(clazz, "class");
        try {
            T t = JsonUtil.beanFrom(toJson(), clazz);
            if (t == null) {
                throw new ParsingException(String.format("Mapping %s as %s didn't yield a result", json, clazz.getName()));
            }
            return t;
        } catch (IOException e) {
            throw new ParsingException(e.getMessage(), e);
        }
    }

    @Override
    @Nonnull
    public Map<String, Object> toMap() throws ParsingException {
        if (content == null) {
            try {
                content = JsonUtil.mapFrom(toJson());
                if (content == null) {
                    throw new ParsingException(String.format("Parsing %s didn't yield a result", json));
                }
            } catch (IOException e) {
                throw new ParsingException(e.getMessage(), e);
            }
        }
        return content;
    }

    @Override
    @Nonnull
    public String toJson() {
        if (json == null && jsonSupplier != null) {
            json = jsonSupplier.get();
        }
        return requireNonNull(json, "RecordPart.json must not be null");
    }

    @Override
    public int hashCode() {
        return toJson().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        RecordPartImpl other = (RecordPartImpl) obj;
        return Objects.equals(toJson(), other.toJson());
    }

    @Override
    public String toString() {
        return toJson();
    }

}
