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

package uk.gov.gchq.palisade.service;

import org.junit.Test;

import static org.junit.Assert.*;

public class MetricsProviderUtilTest {

    @Test(expected = NullPointerException.class)
    public void throwOnNull() {
        MetricsProviderUtil.validateFilter(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwOnEmpty() {
        MetricsProviderUtil.validateFilter("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwOnSpace() {
        MetricsProviderUtil.validateFilter("    ");
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwOnEmptyLines() {
        MetricsProviderUtil.validateFilter("\n\n");
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwDoubleBegin() {
        MetricsProviderUtil.validateFilter("**testtesttest");
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwDoubleBeginDot() {
        MetricsProviderUtil.validateFilter("**test.test.test");
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwDoubleEnd() {
        MetricsProviderUtil.validateFilter("testtesttest**");
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwDoubleEndDot() {
        MetricsProviderUtil.validateFilter("test.test.test**");
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwBeginAndEnd() {
        MetricsProviderUtil.validateFilter("*testtesttest*");
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwBeginAndEndDot() {
        MetricsProviderUtil.validateFilter("*test.test.test*");
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwBeginMiddle() {
        MetricsProviderUtil.validateFilter("*test.test.*test");
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwMiddleEnd() {
        MetricsProviderUtil.validateFilter("test.*test.test*");
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwMiddle() {
        MetricsProviderUtil.validateFilter("test*testtest");
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwTwoMiddle() {
        MetricsProviderUtil.validateFilter("test*test*test");
    }

    @Test
    public void shouldPassNoStar() {
        MetricsProviderUtil.validateFilter("testtesttest");
    }

    @Test
    public void shouldPassNoStarDot() {
        MetricsProviderUtil.validateFilter("test.test.test");
    }

    @Test
    public void shouldPassBegin() {
        MetricsProviderUtil.validateFilter("*test.test.test");
    }

    @Test
    public void shouldPassSingleChar() {
        MetricsProviderUtil.validateFilter("*t");
    }

    @Test
    public void shouldPassSingleCharEnd() {
        MetricsProviderUtil.validateFilter("*t");
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwOnlyStar() {
        MetricsProviderUtil.validateFilter("*");
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwOnlyDoubleStar() {
        MetricsProviderUtil.validateFilter("**");
    }

    @Test
    public void shouldValidateTrueNoStar() {
        assertTrue(MetricsProviderUtil.filterMatches("test_key", "test_key"));
    }

    @Test
    public void shouldValidateTrueEndStar() {
        assertTrue(MetricsProviderUtil.filterMatches("t*", "test_key"));
    }

    @Test
    public void shouldValidateTrueBeginStar() {
        assertTrue(MetricsProviderUtil.filterMatches("*y", "test_key"));
    }

    @Test
    public void shouldValidateTrueBeginStarMulti() {
        assertTrue(MetricsProviderUtil.filterMatches("test*", "test_key"));
    }

    @Test
    public void shouldValidateTrueEndStarMulti() {
        assertTrue(MetricsProviderUtil.filterMatches("*_key", "test_key"));
    }

    @Test
    public void shouldFailMismatch() {
        assertFalse(MetricsProviderUtil.filterMatches("nothing","test_key"));
    }

    @Test
    public void shouldFailEndStar() {
        assertFalse(MetricsProviderUtil.filterMatches("a*","test_key"));
    }

    @Test
    public void shouldFailBeginStar() {
        assertFalse(MetricsProviderUtil.filterMatches("z*","test_key"));
    }

    @Test
    public void shouldPassLiteralStar() {
        assertTrue(MetricsProviderUtil.filterMatches("test*","test*test"));
    }

    @Test
    public void shouldValidateEndLiteralStar() {
        assertTrue(MetricsProviderUtil.filterMatches("*test","test*test"));
    }

    @Test
    public void shouldValidateEndPerfect() {
        assertTrue(MetricsProviderUtil.filterMatches("*something","something"));
    }

    @Test
    public void shouldValidateBeginPerfect() {
        assertTrue(MetricsProviderUtil.filterMatches("something*","something"));
    }

    @Test
    public void shouldFailOneExtraCharEnd() {
        assertFalse(MetricsProviderUtil.filterMatches("*tests","test"));
    }

    @Test
    public void shouldFailOneExtraChar() {
        assertFalse(MetricsProviderUtil.filterMatches("tests*","test"));
    }
}
