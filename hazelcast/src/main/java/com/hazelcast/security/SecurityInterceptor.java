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

package com.hazelcast.security;

import java.security.AccessControlException;

/**
 * This interface allows to intercept requests which were produced by an API call like map.put, queue.offer...
 */
public interface SecurityInterceptor {

    /**
     *
     * @throws AccessControlException if access is denied
     */
    void before(Credentials credentials, String objectType, String objectName, String methodName,
                Parameters parameters) throws AccessControlException;

    void after(Credentials credentials, String objectType, String objectName, String methodName, Parameters parameters);
}
