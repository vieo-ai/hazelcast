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

package com.hazelcast.internal.diagnostics;

/**
 * The diagnostics log output.
 */
public interface DiagnosticsLog {

    /**
     * Writes the output of the provided {@code plugin} to the diagnostics
     * output.
     *
     * @param plugin the plugin to log
     */
    void write(DiagnosticsPlugin plugin);

    /**
     * Sends closing signal to the logger.
     */
    void close();
}
