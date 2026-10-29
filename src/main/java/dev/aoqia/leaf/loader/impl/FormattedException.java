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

package dev.aoqia.leaf.loader.impl;

import dev.aoqia.leaf.loader.impl.util.Localization;

@SuppressWarnings("serial")
public final class FormattedException extends RuntimeException {
    private final String mainText;
    private String translatedText;

    public FormattedException(String mainText, String message) {
        super(message);

        this.mainText = mainText;
    }

    public FormattedException(String mainText, String format, Object... args) {
        super(String.format(format, args));

        this.mainText = mainText;
    }

    public FormattedException(String mainText, String message, Throwable cause) {
        super(message, cause);

        this.mainText = mainText;
    }

    public FormattedException(String mainText, Throwable cause) {
        super(cause);

        this.mainText = mainText;
    }

    public static FormattedException ofLocalized(String key, String message) {
        return new FormattedException(Localization.formatRoot(key), message).addTranslation(key);
    }

    public static FormattedException ofLocalized(String key, String format, Object... args) {
        return new FormattedException(Localization.formatRoot(key), format, args).addTranslation(key);
    }

    public static FormattedException ofLocalized(String key, String message, Throwable cause) {
        return new FormattedException(Localization.formatRoot(key), message, cause).addTranslation(key);
    }

    public static FormattedException ofLocalized(String key, Throwable cause) {
        return new FormattedException(Localization.formatRoot(key), cause).addTranslation(key);
    }

    public String getDisplayedText() {
        return translatedText == null || translatedText.equals(mainText) ? mainText : translatedText + " (" + mainText
            + ")";
    }

    public String getMainText() {
        return mainText;
    }

    private FormattedException addTranslation(String key) {
        this.translatedText = Localization.format(key);
        return this;
    }
}
