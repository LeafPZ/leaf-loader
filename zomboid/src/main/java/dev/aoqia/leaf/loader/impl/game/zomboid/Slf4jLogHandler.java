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

package dev.aoqia.leaf.loader.impl.game.zomboid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.aoqia.leaf.loader.impl.util.log.LogCategory;
import dev.aoqia.leaf.loader.impl.util.log.LogHandler;
import dev.aoqia.leaf.loader.impl.util.log.LogLevel;

public final class Slf4jLogHandler implements LogHandler {
    private static Logger getLogger(LogCategory category) {
        Logger ret = (Logger) category.data;

        if (ret == null) {
            category.data = ret = LoggerFactory.getLogger(category.toString());
        }

        return ret;
    }

    @Override
    public void close() {
    }

    @Override
    public void log(long time, LogLevel level, LogCategory category, String msg, Throwable exc, boolean fromReplay,
        boolean wasSuppressed)
    {
        Logger logger = getLogger(category);

        if (msg == null) {
            if (exc == null) {
                return;
            }
            msg = "Exception";
        }

        switch (level) {
            case ERROR:
                if (exc == null) {
                    logger.error(msg);
                } else {
                    logger.error(msg, exc);
                }

                break;
            case WARN:
                if (exc == null) {
                    logger.warn(msg);
                } else {
                    logger.warn(msg, exc);
                }

                break;
            case INFO:
                if (exc == null) {
                    logger.info(msg);
                } else {
                    logger.info(msg, exc);
                }

                break;
            case DEBUG:
                if (exc == null) {
                    logger.debug(msg);
                } else {
                    logger.debug(msg, exc);
                }

                break;
            case TRACE:
                if (exc == null) {
                    logger.trace(msg);
                } else {
                    logger.trace(msg, exc);
                }

                break;
            default:
                throw new IllegalArgumentException("unknown level: " + level);
        }
    }

    @Override
    public boolean shouldLog(LogLevel level, LogCategory category) {
        Logger logger = getLogger(category);

        switch (level) {
            case ERROR:
                return logger.isErrorEnabled();
            case WARN:
                return logger.isWarnEnabled();
            case INFO:
                return logger.isInfoEnabled();
            case DEBUG:
                return logger.isDebugEnabled();
            case TRACE:
                return logger.isTraceEnabled();
        }

        throw new IllegalArgumentException("unknown level: " + level);
    }
}
