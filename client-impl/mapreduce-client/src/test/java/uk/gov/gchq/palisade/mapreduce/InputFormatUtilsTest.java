/*
 * Copyright 2018 Crown Copyright
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.gov.gchq.palisade.mapreduce;

import org.junit.BeforeClass;
import org.junit.Test;

import uk.gov.gchq.palisade.RequestId;
import uk.gov.gchq.palisade.resource.StubResource;
import uk.gov.gchq.palisade.service.request.DataRequestResponse;
import uk.gov.gchq.palisade.service.request.StubConnectionDetail;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PrimitiveIterator;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class InputFormatUtilsTest {

    private static DataRequestResponse reqResponse;

    @BeforeClass
    public static void setup() {
        reqResponse = new DataRequestResponse()
                .requestId(new RequestId().id("testID"))
                .resource(new StubResource("type1", "id1", "format1"), new StubConnectionDetail("con1"))
                .resource(new StubResource("type2", "id2", "format2"), new StubConnectionDetail("con2"))
                .resource(new StubResource("type3", "id3", "format3"), new StubConnectionDetail("con3"))
                .resource(new StubResource("type4", "id4", "format4"), new StubConnectionDetail("con4"))
                .resource(new StubResource("type5", "id5", "format5"), new StubConnectionDetail("con5"));
    }

    @Test
    public void testListToMapCollector() {
        //Given - a Map of numbers to their double
        Map<Integer, Integer> original = IntStream
                .range(1, 20)
                .boxed()
                .collect(Collectors.toMap(Function.identity(), x -> x * 2));
        //When - convert to a list of entry
        List<Map.Entry<Integer, Integer>> entries = original
                .entrySet()
                .stream()
                .collect(Collectors.toList());
        //Then - should equal original map
        assertEquals(original, entries
                        .stream()
                        .collect(InputFormatUtils.listToMapCollector())
        );
    }

    @Test
    public void testListToMapCollectorEmpty() {
        //Given - a Map of numbers to their double
        Map<Integer, Integer> original = Collections.emptyMap();
        //When - convert to a list of entry
        List<Map.Entry<Integer, Integer>> entries = original
                .entrySet()
                .stream()
                .collect(Collectors.toList());
        //Then - should equal original map
        assertEquals(original, entries
                        .stream()
                        .collect(InputFormatUtils.listToMapCollector())
        );
    }

    @Test (expected = NullPointerException.class)
    public void shouldReturnErrorAsThereAreNoResourcesSet() {
        //Given
        DataRequestResponse req = new DataRequestResponse();
        PrimitiveIterator.OfInt index = IntStream.range(1, 9999).iterator();
        //When
        InputFormatUtils.toInputSplits(req, index);
        //Then
        fail("Test should have thrown a NullPointerException.");
    }

    @Test
    public void shouldReturnSingleSplit() {
        //Given - ask for a single split
        PrimitiveIterator.OfInt index = IntStream.generate(() -> 1).iterator();
        //When
        List<PalisadeInputSplit> result = InputFormatUtils.toInputSplits(reqResponse, index);
        //Then
        assertEquals(1, result.size());
        assertEquals(5, result.get(0).getRequestResponse().getResources().size());
    }

    @Test
    public void shouldReturnMultipleSplit() {
        //Given - ask for 3 splits
        PrimitiveIterator.OfInt index = IntStream.of(0, 1, 2, 0, 1, 2, 0, 1, 2).iterator();
        //When
        List<PalisadeInputSplit> result = InputFormatUtils.toInputSplits(reqResponse, index);
        //Then
        assertEquals(3, result.size());
        //should be two in the first two splits, one in the last
        assertEquals(2, result.get(0).getRequestResponse().getResources().size());
        assertEquals(2, result.get(1).getRequestResponse().getResources().size());
        assertEquals(1, result.get(2).getRequestResponse().getResources().size());
        //now check we still got 5 distinct values
        //create set of the values from the map
        Set<String> values0 = result.get(0).getRequestResponse().getResources()
                .values()
                .stream()
                .map(x -> ((StubConnectionDetail) x).getCon())
                .collect(Collectors.toSet());
        Set<String> values1 = result.get(1).getRequestResponse().getResources()
                .values()
                .stream()
                .map(x -> ((StubConnectionDetail) x).getCon())
                .collect(Collectors.toSet());
        Set<String> values2 = result.get(2).getRequestResponse().getResources()
                .values()
                .stream()
                .map(x -> ((StubConnectionDetail) x).getCon())
                .collect(Collectors.toSet());
        Set<String> merged = new HashSet<>();
        merged.addAll(values0);
        merged.addAll(values1);
        merged.addAll(values2);
        assertEquals(5, merged.size());
    }
}
