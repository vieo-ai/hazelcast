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

package com.hazelcast.cluster;

import com.hazelcast.spi.annotation.NamespacesSupported;

import java.util.EventListener;

/**
 * Cluster membership listener.
 *
 * The MembershipListener will never be called concurrently and all MembershipListeners will receive the events
 * in the same order.
 *
 * @see InitialMembershipListener
 * @see Cluster#addMembershipListener(MembershipListener)
 */
@NamespacesSupported
public interface MembershipListener extends EventListener {

    /**
     * Invoked when a new member is added to the cluster.
     *
     * @param membershipEvent membership event
     */
    void memberAdded(MembershipEvent membershipEvent);

    /**
     * Invoked when an existing member leaves the cluster.
     *
     * @param membershipEvent membership event when an existing member leaves the cluster
     */
    void memberRemoved(MembershipEvent membershipEvent);
}
