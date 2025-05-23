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

package com.hazelcast.scheduledexecutor.impl;

import com.hazelcast.spi.impl.executionservice.impl.DelegatingTaskScheduler;
import com.hazelcast.test.HazelcastParallelClassRunner;
import com.hazelcast.test.annotation.ParallelJVMTest;
import com.hazelcast.test.annotation.QuickTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(HazelcastParallelClassRunner.class)
@Category({QuickTest.class, ParallelJVMTest.class})
public class DelegatingScheduledFutureStripperTest {

    private ScheduledExecutorService scheduler;
    private DelegatingTaskScheduler taskScheduler;
    private ExecutorService executor;

    @Before
    public void setup() {
        executor = Executors.newSingleThreadExecutor();
        scheduler = Executors.newSingleThreadScheduledExecutor();
        taskScheduler = new DelegatingTaskScheduler(scheduler, executor);
    }

    @After
    public void teardown() throws Exception {
        scheduler.shutdownNow();
        scheduler.awaitTermination(10, TimeUnit.SECONDS);
        executor.shutdownNow();
    }

    @Test(expected = NullPointerException.class)
    public void constructWithNull() {
        new DelegatingScheduledFutureStripper<>(null);
    }

    @Test(expected = UnsupportedOperationException.class)
    @SuppressWarnings("ConstantConditions")
    public void compareTo() {
        ScheduledFuture<Integer> future = new DelegatingScheduledFutureStripper<>(
                scheduler.schedule(new SimpleCallableTestTask(), 0, TimeUnit.SECONDS));
        future.compareTo(null);
    }

    @Test
    public void getDelay() {
        ScheduledFuture<Integer> future = new DelegatingScheduledFutureStripper<>(
                scheduler.schedule(new SimpleCallableTestTask(), 0, TimeUnit.SECONDS));
        //getDelay returns the remaining delay; zero or negative values indicate that the delay has already elapsed
        //If JVM pauses for GC we may get a negative value
        assertTrue(future.getDelay(TimeUnit.SECONDS) <= 0);

        future = new DelegatingScheduledFutureStripper<>(
                scheduler.schedule(new SimpleCallableTestTask(), 10, TimeUnit.SECONDS));
        assertTrue(future.getDelay(TimeUnit.SECONDS) <= 10);
    }

    @Test
    public void cancel() throws Exception {
        ScheduledFuture<Object> outer = createScheduledFutureMock();
        ScheduledFuture<Object> inner = createScheduledFutureMock();
        when(outer.get()).thenReturn(inner);

        new DelegatingScheduledFutureStripper<>(outer).cancel(true);

        verify(inner).cancel(true);
    }

    @Test
    public void cancel_twice() {
        ScheduledFuture<Future<Integer>> original = taskScheduler.schedule(new SimpleCallableTestTask(), 10, TimeUnit.SECONDS);
        ScheduledFuture stripper = new DelegatingScheduledFutureStripper<>(original);

        stripper.cancel(true);
        stripper.cancel(true);
    }

    @Test
    public void isDone() throws Exception {
        ScheduledFuture<Object> outer = createScheduledFutureMock();
        ScheduledFuture<Object> inner = createScheduledFutureMock();
        when(outer.get()).thenReturn(inner);

        when(outer.isDone()).thenReturn(true);
        when(inner.isDone()).thenReturn(false);
        assertFalse(new DelegatingScheduledFutureStripper<>(outer).isDone());

        when(outer.isDone()).thenReturn(true);
        when(inner.isDone()).thenReturn(true);
        assertTrue(new DelegatingScheduledFutureStripper<>(outer).isDone());
    }

    @Test
    public void isCancelled() throws Exception {
        ScheduledFuture<Object> outer = createScheduledFutureMock();
        ScheduledFuture<Object> inner = createScheduledFutureMock();
        when(outer.get()).thenReturn(inner);

        when(outer.isCancelled()).thenReturn(false);
        when(inner.isCancelled()).thenReturn(false);
        assertFalse(new DelegatingScheduledFutureStripper<>(outer).isCancelled());

        when(outer.isCancelled()).thenReturn(true);
        when(inner.isCancelled()).thenReturn(false);
        assertTrue(new DelegatingScheduledFutureStripper<>(outer).isCancelled());

        when(outer.isCancelled()).thenReturn(false);
        when(inner.isCancelled()).thenReturn(true);
        assertTrue(new DelegatingScheduledFutureStripper<>(outer).isCancelled());
    }

    @Test
    public void get() throws Exception {
        ScheduledFuture<Future<Integer>> original = taskScheduler.schedule(new SimpleCallableTestTask(), 0, TimeUnit.SECONDS);
        ScheduledFuture stripper = new DelegatingScheduledFutureStripper<>(original);

        assertNotNull(original.get());
        assertEquals(5, stripper.get());
    }

    @Test(expected = InterruptedException.class)
    public void get_interrupted() throws Exception {
        ScheduledFuture<Object> outer = createScheduledFutureMock();
        ScheduledFuture<Object> inner = createScheduledFutureMock();
        when(outer.get()).thenThrow(new InterruptedException());
        when(inner.get()).thenReturn(2);

        new DelegatingScheduledFutureStripper<>(outer).get();
    }

    @Test(expected = ExecutionException.class)
    public void get_executionExc() throws Exception {
        ScheduledFuture<Object> outer = createScheduledFutureMock();
        ScheduledFuture<Object> inner = createScheduledFutureMock();
        when(outer.get()).thenThrow(new ExecutionException(new NullPointerException()));
        when(inner.get()).thenReturn(2);

        new DelegatingScheduledFutureStripper<>(outer).get();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void get_unsupported() throws Exception {
        ScheduledFuture<Integer> future = scheduler.schedule(new SimpleCallableTestTask(), 0, TimeUnit.SECONDS);
        new DelegatingScheduledFutureStripper<>(future).get(1, TimeUnit.SECONDS);
    }

    @Test
    public void equals() {
        ScheduledFuture<Future<Integer>> original = taskScheduler.schedule(new SimpleCallableTestTask(), 0, TimeUnit.SECONDS);
        ScheduledFuture<Future<Integer>> joker = taskScheduler.schedule(new SimpleCallableTestTask(), 1, TimeUnit.SECONDS);

        ScheduledFuture testA = new DelegatingScheduledFutureStripper<>(original);
        ScheduledFuture testB = new DelegatingScheduledFutureStripper<>(original);
        ScheduledFuture testC = new DelegatingScheduledFutureStripper<>(joker);

        assertNotNull(testA);
        assertEquals(testA, testA);
        assertEquals(testA, testB);
        assertNotEquals(testA, testC);
    }

    @SuppressWarnings("unchecked")
    private static ScheduledFuture<Object> createScheduledFutureMock() {
        return mock(ScheduledFuture.class);
    }

    private static class SimpleCallableTestTask implements Callable<Integer> {
        @Override
        public Integer call() throws Exception {
            return 5;
        }
    }

}
