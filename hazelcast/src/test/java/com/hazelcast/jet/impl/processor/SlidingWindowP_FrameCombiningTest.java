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

package com.hazelcast.jet.impl.processor;

import com.hazelcast.jet.aggregate.AggregateOperations;
import com.hazelcast.jet.core.test.TestSupport;
import com.hazelcast.jet.datamodel.KeyedWindowResult;
import com.hazelcast.test.HazelcastParallelClassRunner;
import com.hazelcast.test.annotation.ParallelJVMTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import static com.hazelcast.jet.core.SlidingWindowPolicy.slidingWinPolicy;
import static com.hazelcast.jet.core.processor.Processors.combineToSlidingWindowP;

@Category(ParallelJVMTest.class)
@RunWith(HazelcastParallelClassRunner.class)
public class SlidingWindowP_FrameCombiningTest {

    private static final Long KEY = 77L;

    @Test
    public void when_multipleFrames_then_combine() {
        TestSupport
                .verifyProcessor(
                        combineToSlidingWindowP(slidingWinPolicy(8, 4), AggregateOperations.<String>toSet(),
                                (start, end, key, result, isEarly) -> result(end, result)))
                .input(List.of(
                        frame(2, Set.of("a")),
                        frame(4, Set.of("b")),
                        frame(6, Set.of("c"))
                ))
                .expectOutput(List.of(
                        result(4, List.of("a", "b")),
                        result(8, List.of("a", "b", "c")),
                        result(12, List.of("c"))
                ));
    }

    private static <V> KeyedWindowResult<Long, V> frame(long ts, V value) {
        return new KeyedWindowResult<>(0, ts, KEY, value);
    }

    private static String result(long winEnd, Collection<String> result) {
        return String.format("(%03d, %s: %s)", winEnd, KEY, result);
    }
}
