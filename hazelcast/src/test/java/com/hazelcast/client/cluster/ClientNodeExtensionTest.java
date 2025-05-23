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

package com.hazelcast.client.cluster;

import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.properties.ClientProperty;
import com.hazelcast.client.test.TestHazelcastFactory;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.hazelcast.core.OperationTimeoutException;
import com.hazelcast.instance.impl.DefaultNodeExtension;
import com.hazelcast.instance.impl.Node;
import com.hazelcast.cluster.Address;
import com.hazelcast.test.HazelcastParallelClassRunner;
import com.hazelcast.test.HazelcastTestSupport;
import com.hazelcast.test.annotation.ParallelJVMTest;
import com.hazelcast.test.annotation.QuickTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

@RunWith(HazelcastParallelClassRunner.class)
@Category({QuickTest.class, ParallelJVMTest.class})
public class ClientNodeExtensionTest extends HazelcastTestSupport {
    private TestHazelcastFactory factory;

    @Before
    public void before() {
        factory = new TestHazelcastFactory();
    }

    @After
    public void after() {
        factory.terminateAll();
    }

    @Test(expected = IllegalStateException.class)
    public void test_canNotConnect_whenNodeExtensionIsNotComplete() throws UnknownHostException {
        factory.withNodeExtensionCustomizer(node -> new MockNodeExtension(node, false))
               .newHazelcastInstance(new Address("127.0.0.1", 5555));

        ClientConfig clientConfig = new ClientConfig();
        clientConfig.getNetworkConfig().addAddress("127.0.0.1:5555");
        clientConfig.getConnectionStrategyConfig().getConnectionRetryConfig().setClusterConnectTimeoutMillis(2000);
        clientConfig.setProperty(ClientProperty.HEARTBEAT_TIMEOUT.getName(), "1000");
        factory.newHazelcastClient(clientConfig);
    }

    @Test(expected = OperationTimeoutException.class)
    public void test_canGetFromMap_whenNodeExtensionIsNotComplete() {
        IMap<Object, Object> map = null;
        AtomicReference<MockNodeExtension> nodeExtension = new AtomicReference<>();
        try {
            factory.withNodeExtensionCustomizer(node -> {
                nodeExtension.set(new MockNodeExtension(node, true));
                return nodeExtension.get();
            }).newHazelcastInstance(new Address("127.0.0.1", 5555));

            ClientConfig clientConfig = new ClientConfig();
            clientConfig.setProperty(ClientProperty.INVOCATION_TIMEOUT_SECONDS.getName(), "3");
            clientConfig.getNetworkConfig().addAddress("127.0.0.1:5555");

            HazelcastInstance hazelcastClient = factory.newHazelcastClient(clientConfig);

            map = hazelcastClient.getMap(randomMapName());

            assertNull(map.get("dummy"));
        } catch (Throwable t) {
            fail("Should not throw exception! Error:" + t);
        }

        nodeExtension.get().setStartupDone(false);

        map.get("dummy");
    }

    private static class MockNodeExtension extends DefaultNodeExtension {
        private final AtomicBoolean startupDone;

        MockNodeExtension(Node node, boolean isStarted) {
            super(node);
            startupDone = new AtomicBoolean(isStarted);
        }

        @Override
        public boolean isStartCompleted() {
            return startupDone.get() && super.isStartCompleted();
        }

        public void setStartupDone(boolean started) {
            this.startupDone.set(started);
        }
    }
}
