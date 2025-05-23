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

package com.hazelcast.client.executor.durable;

import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.test.TestHazelcastFactory;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.durableexecutor.DurableExecutorService;
import com.hazelcast.test.HazelcastParallelClassRunner;
import com.hazelcast.test.HazelcastTestSupport;
import com.hazelcast.test.annotation.NightlyTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertTrue;

@RunWith(HazelcastParallelClassRunner.class)
@Category(NightlyTest.class)
public class ClientDurableExecutionDelayTest extends HazelcastTestSupport {

    private static final int CLUSTER_SIZE = 3;
    private static final AtomicInteger COUNTER = new AtomicInteger();

    private final List<HazelcastInstance> instances = new ArrayList<>(CLUSTER_SIZE);

    private TestHazelcastFactory hazelcastFactory;

    @Before
    public void setup() {
        hazelcastFactory = new TestHazelcastFactory();
        for (int i = 0; i < CLUSTER_SIZE; i++) {
            instances.add(hazelcastFactory.newHazelcastInstance());
        }
    }

    @After
    public void tearDown() {
        hazelcastFactory.shutdownAll();
    }

    @Test
    public void testExecutorRetriesTask_whenOneNodeTerminates() throws Exception {
        final int taskCount = 20;
        ScheduledExecutorService ex = Executors.newSingleThreadScheduledExecutor();
        try {
            ex.schedule(() -> instances.get(1).getLifecycleService().terminate(), 1000, TimeUnit.MILLISECONDS);

            Task task = new Task();
            runClient(task, taskCount);

            assertTrueEventually(() -> {
                final int taskExecutions = COUNTER.get();
                assertTrue(taskExecutions >= taskCount);
            });
        } finally {
            ex.shutdown();
        }
    }

    @Test
    public void testExecutorRetriesTask_whenOneNodeShutdowns() throws Exception {
        final int taskCount = 20;
        ScheduledExecutorService ex = Executors.newSingleThreadScheduledExecutor();
        try {
            ex.schedule(() -> instances.get(1).shutdown(), 1000, TimeUnit.MILLISECONDS);

            Task task = new Task();
            runClient(task, taskCount);

            assertTrueEventually(() -> {
                final int taskExecutions = COUNTER.get();
                assertTrue(taskExecutions >= taskCount);
            });
        } finally {
            ex.shutdown();
        }
    }

    private void runClient(Task task, int executions) throws Exception {
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.getNetworkConfig().setRedoOperation(true);
        HazelcastInstance client = hazelcastFactory.newHazelcastClient(clientConfig);
        DurableExecutorService executor = client.getDurableExecutorService("executor");
        for (int i = 0; i < executions; i++) {
            Future<Object> future = executor.submitToKeyOwner(task, i);
            future.get();
            Thread.sleep(100);
        }
    }

    private static class Task implements Callable<Object>, Serializable {


        @Override
        public Object call() {
            COUNTER.incrementAndGet();
            return null;
        }

        @Override
        public String toString() {
            return "Task{}";
        }
    }
}
