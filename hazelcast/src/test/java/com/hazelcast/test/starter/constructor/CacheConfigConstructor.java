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

package com.hazelcast.test.starter.constructor;

import com.hazelcast.test.starter.HazelcastStarterConstructor;

import static com.hazelcast.test.starter.ReflectionUtils.setFieldValueReflectively;

@HazelcastStarterConstructor(classNames = {"com.hazelcast.config.CacheConfig", "com.hazelcast.cache.impl.PreJoinCacheConfig"})
public class CacheConfigConstructor extends AbstractConfigConstructor {

    public CacheConfigConstructor(Class<?> targetClass) {
        super(targetClass);
    }

    @Override
    Object createNew0(Object delegate) throws Exception {
        ClassLoader classloader = targetClass.getClassLoader();
        Object otherConfig = cloneConfig(delegate, classloader);
        setFieldValueReflectively(otherConfig, "classLoader", classloader);
        return otherConfig;
    }
}
