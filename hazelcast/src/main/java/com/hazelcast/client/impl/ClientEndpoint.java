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

package com.hazelcast.client.impl;

import com.hazelcast.client.Client;
import com.hazelcast.client.config.RoutingMode;
import com.hazelcast.client.impl.statistics.ClientStatistics;
import com.hazelcast.internal.metrics.DynamicMetricsProvider;
import com.hazelcast.internal.server.ServerConnection;
import com.hazelcast.security.Credentials;
import com.hazelcast.transaction.TransactionContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;

/**
 * Represents an endpoint to a client. So for each client connected to a member, a ClientEndpoint object is available.
 */
public interface ClientEndpoint extends Client, DynamicMetricsProvider {

    /**
     * Checks if the endpoint is alive.
     *
     * @return true if alive, false otherwise.
     */
    boolean isAlive();

    /**
     * Adds a remove callable to be called when endpoint is destroyed to clean related listener
     * Following line will be called when endpoint destroyed :
     * eventService.deregisterListener(service, topic, ID);
     * Note: removeDestroyAction should be called when there is no need to destroy action anymore.
     *
     * @param service name of the related service of listener
     * @param topic   topic name of listener(mostly distributed object name)
     * @param id      registration ID of remove action
     */
    void addListenerDestroyAction(String service, String topic, UUID id);

    /**
     * Adds a remove callable to be called when endpoint is destroyed
     * Note: removeDestroyAction should be called when there is no need to destroy action anymore.
     *
     * @param registrationId registration ID of destroy action
     * @param removeAction   callable that will be called when endpoint is destroyed
     */
    void addDestroyAction(UUID registrationId, Callable<Boolean> removeAction);

    /**
     * @param id registration ID of destroy action
     * @return true if remove is successful
     */
    boolean removeDestroyAction(UUID id);

    Credentials getCredentials();

    void setTransactionContext(TransactionContext context);

    TransactionContext getTransactionContext(UUID txnId);

    void removeTransactionContext(UUID txnId);

    Subject getSubject();

    ServerConnection getConnection();

    long getConnectionStartTime();

    void setLoginContext(LoginContext lc);

    void authenticated(UUID clientUuid, Credentials credentials, String clientVersion,
                       long authCorrelationId, String clientName, Set<String> labels, RoutingMode routingMode,
                       boolean cpDirectToLeaderRouting);

    /**
     * @return true if endpoint is authenticated with valid security credentials, returns false otherwise
     */
    boolean isAuthenticated();

    /**
     * @return the version string as obtained from the environment
     */
    String getClientVersion();

    /**
     * @param version the version string as obtained from the environment
     */
    void setClientVersion(String version);

    /**
     * @return true if the client uses the enterprise build of Hazelcast
     */
    boolean isEnterprise();

    /**
     * @param enterprise indicates whether the client uses the enterprise build or not
     */
    void setEnterprise(Boolean enterprise);

    /**
     * Updates to the latest client statistics.
     *
     * @param stats the latest statistics retrieved from the client
     */
    void setClientStatistics(ClientStatistics stats);

    /**
     * Returns the latest client statistics.
     *
     * @return the client statistics
     */
    ClientStatistics getClientStatistics();

    /**
     * @return client attributes string for the client
     */
    String getClientAttributes();

    /**
     * @return the time this endpoint is created
     */
    long getCreationTime();

    /**
     * Similar to {@link ClientEndpointImpl#toString()} but lacks some information due to security reasons.
     * Used when handling unauthenticated requests.
     */
    String toSecureString();

    /**
     * Sets the TPC token associated with this endpoint.
     */
    void setTpcToken(@Nonnull TpcToken tpcToken);

    /**
     * Returns the TPC token associated with this endpoint,
     * if TPC is enabled on the server-side and the client
     * is TPC-aware. If not, {@code null} is returned.
     */
    @Nullable
    TpcToken getTpcToken();

    /**
     * Returns the {@link RoutingMode} of this client.
     *
     * @return the {@link RoutingMode} of this client
     */
    RoutingMode getRoutingMode();

    /**
     * Returns whether this client has the `cp-direct-to-leader-routing`
     * option enabled or not.
     *
     * @return {@code true} if it is enabled, else {@code false}
     */
    boolean isCpDirectToLeaderEnabled();
}
