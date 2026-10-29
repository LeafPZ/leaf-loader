// spotless:off

/*
 * Copyright 2026 aoqia
 *
 * This file was originally released under LICENSE_UPSTREAM (see below for the original header) and has been relicensed
 * to GNU's AGPLv3 license, with additional terms in the LICENSE_EXCEPTION.md file.
 *
 * All modifications made to this file after the original version were created separate to the original copyright.
 * These modifications are provided under the AGPLv3 license (including exception terms). The portions of this file that
 * were not modified remain subject to the original copyright notice and license, and the original LICENSE is kept in
 * LICENSE_UPSTREAM located alongside LICENSE.
 *
 * The complete record of changes to this file is publicly available in the project's Git history, which anyone may view
 * at any time for any reason.
 *
 * Copyright 2016 FabricMC
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

// spotless:on

package dev.aoqia.leaf.loader.api.metadata;

import java.util.Map;

/**
 * Represents a custom value in the {@code leaf.mod.json}.
 */
public interface CustomValue {
    /**
     * Returns this value as an {@link CvType#ARRAY}.
     *
     * @return this value
     * @throws ClassCastException if this value is not an array
     */
    CvArray getAsArray();

    /**
     * Returns this value as a {@link CvType#BOOLEAN}.
     *
     * @return this value
     * @throws ClassCastException if this value is not a boolean
     */
    boolean getAsBoolean();

    /**
     * Returns this value as a {@link CvType#NUMBER}.
     *
     * @return this value
     * @throws ClassCastException if this value is not a number
     */
    Number getAsNumber();

    /**
     * Returns this value as an {@link CvType#OBJECT}.
     *
     * @return this value
     * @throws ClassCastException if this value is not an object
     */
    CvObject getAsObject();

    /**
     * Returns this value as a {@link CvType#STRING}.
     *
     * @return this value
     * @throws ClassCastException if this value is not a string
     */
    String getAsString();

    /**
     * Returns the type of the value.
     */
    CvType getType();

    /**
     * Represents an {@link CvType#ARRAY} value.
     */
    interface CvArray extends CustomValue, Iterable<CustomValue> {
        /**
         * Gets the value at {@code index} within this array value.
         *
         * @param index the index of the value
         * @return the value associated
         * @throws IndexOutOfBoundsException if the index is not within {{@link #size()}}
         */
        CustomValue get(int index);

        /**
         * Returns the number of values within this array value.
         */
        int size();
    }

    /**
     * Represents an {@link CvType#OBJECT} value.
     */
    interface CvObject extends CustomValue, Iterable<Map.Entry<String, CustomValue>> {
        /**
         * Returns whether a {@code key} is present within this object value.
         *
         * @param key the key to check
         * @return whether the key is present
         */
        boolean containsKey(String key);

        /**
         * Gets the value associated with a {@code key} within this object value.
         *
         * @param key the key to check
         * @return the value associated, or {@code null} if no such value is present
         */
        CustomValue get(String key);

        /**
         * Returns the number of key-value pairs within this object value.
         */
        int size();
    }

    /**
     * The possible types of a custom value.
     */
    enum CvType {
        OBJECT, ARRAY, STRING, NUMBER, BOOLEAN, NULL;
    }
}
