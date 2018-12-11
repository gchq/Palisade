/*
 * Copyright 2018 Crown Copyright
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.gchq.palisade.example.hrdatagenerator;

import java.util.Random;

public enum Ethnicity {
    WHITE_ENGLISH,
    WHITE_WELSH,
    WHITE_SCOTTISH,
    WHITE_NORTHERN_IRISH,
    WHITE_BRITISH,
    WHITE_IRISH,
    WHITE_GYPSY,
    WHITE_IRISH_TRAVELLER,
    MIXED_CARIBBEAN,
    MIXED_AFRICAN,
    WHITE_AND_ASIAN,
    ASIAN_INDIAN,
    ASIAN_PAKISTANI,
    ASIAN_BANGLADESHI,
    ASIAN_CHINESE,
    BLACK_AFRICAN,
    BLACK_CARIBBEAN,
    OTHER_BLACK,
    OTHER_AFRICAN,
    OTHER_CARIBBEAN,
    ARAB;

    public static Ethnicity generate(Random random) {
        return Ethnicity.values()[random.nextInt(21)];
    }
}


