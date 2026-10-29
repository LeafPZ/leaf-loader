/*
 * Copyright (C) 2026 aoqia
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General
 * Public License as published by the Free Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program. If not,
 * see <https://www.gnu.org/licenses/>.
 *
 * This license and file are additionally subjected to the exceptions described in LICENSE_EXCEPTION.md.
 *
 * The exceptions permit independent software to use this project as a dependency under terms of its own choosing, but
 * does not permit the copying or incorporation of covered source code into another work outside the terms of the AGPL.
 */

package dev.aoqia.leaf.loader.impl.game.zomboid;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import dev.aoqia.leaf.loader.impl.FormattedException;
import dev.aoqia.leaf.loader.impl.util.log.LogCategory;
import dev.aoqia.leaf.loader.impl.util.log.LogHandler;
import dev.aoqia.leaf.loader.impl.util.log.LogLevel;

/**
 * A log handler to use the built-in game logging system. This one is specifically for >=B42. If
 * something in a future unstable release breaks this class, it should be updated. This class should
 * be merged into {@link LegacyZomboidLogHandler} when unstable becomes stable.
 */
public final class ZomboidLogHandler implements LogHandler {
    private static final Class<?> DEBUG_LOG;
    private static final Class<?> DEBUG_LOG_STREAM;
    private static final Method DEBUG_LOG_STREAM_isLogEnabled;
    private static final Class<?> DEBUG_TYPE;
    private static final Object DEBUG_TYPE_Leaf;
    private static final Object DEBUG_TYPE_Leaf_logStream;
    private static final Method DEBUG_TYPE_trace;
    private static final Method DEBUG_TYPE_debugln;
    private static final Method DEBUG_TYPE_println;
    private static final Method DEBUG_TYPE_warn;
    private static final Method DEBUG_TYPE_error;
    private static final Method DEBUG_TYPE_printException;
    private static final Class<?> LOG_SEVERITY;
    private static final Object LOG_SEVERITY_Trace;
    private static final Object LOG_SEVERITY_Debug;
    private static final Object LOG_SEVERITY_General;
    private static final Object LOG_SEVERITY_Warning;
    private static final Object LOG_SEVERITY_Error;

    static {
        try {
            LOG_SEVERITY = Class.forName("zombie.debug.LogSeverity");
            DEBUG_LOG = Class.forName("zombie.debug.DebugLog");

            final Method logSeverityValueOf = LOG_SEVERITY.getMethod("valueOf", String.class);
            LOG_SEVERITY_Trace = logSeverityValueOf.invoke(null, "Trace");
            LOG_SEVERITY_Debug = logSeverityValueOf.invoke(null, "Debug");
            LOG_SEVERITY_General = logSeverityValueOf.invoke(null, "General");
            LOG_SEVERITY_Warning = logSeverityValueOf.invoke(null, "Warning");
            LOG_SEVERITY_Error = logSeverityValueOf.invoke(null, "Error");

            DEBUG_TYPE = Class.forName("zombie.debug.DebugType");
            DEBUG_TYPE_Leaf = DEBUG_TYPE.getMethod("valueOf", String.class).invoke(null, "Leaf");
            DEBUG_TYPE_Leaf_logStream = DEBUG_TYPE.getMethod("getLogStream")
                .invoke(DEBUG_TYPE_Leaf);
            DEBUG_TYPE_trace = DEBUG_TYPE.getMethod("trace", Object.class);
            DEBUG_TYPE_debugln = DEBUG_TYPE.getMethod("debugln", Object.class);
            DEBUG_TYPE_println = DEBUG_TYPE.getMethod("println", String.class);
            DEBUG_TYPE_warn = DEBUG_TYPE.getMethod("warn", Object.class);
            DEBUG_TYPE_error = DEBUG_TYPE.getMethod("error", Object.class);
            DEBUG_TYPE_printException = DEBUG_TYPE.getMethod("printException", Throwable.class, String.class,
                LOG_SEVERITY);

            // Set log stream as enabled just in case.
            DEBUG_TYPE.getMethod("setLogSeverity", LOG_SEVERITY).invoke(DEBUG_TYPE_Leaf, LOG_SEVERITY_Trace);
            DEBUG_LOG.getMethod("setLogEnabled", DEBUG_TYPE, boolean.class).invoke(null, DEBUG_TYPE_Leaf, true);

            DEBUG_LOG_STREAM = Class.forName("zombie.debug.DebugLogStream");
            DEBUG_LOG_STREAM_isLogEnabled = DEBUG_LOG_STREAM.getMethod("isLogEnabled",
                LOG_SEVERITY);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException |
                 InvocationTargetException e) {
            throw new FormattedException("Failed to handle reflection for new log handler", e);
        }
    }

    @Override
    public void log(long time, LogLevel level, LogCategory category, String msg, Throwable exc,
        boolean fromReplay, boolean wasSuppressed) {
        if (DEBUG_TYPE_Leaf == null) {
            return;
        }

        if (msg == null) {
            if (exc == null) {
                return;
            }

            msg = "Exception";
        }

        try {
            switch (level) {
                case ERROR:
                    if (exc == null) {
                        DEBUG_TYPE_error.invoke(DEBUG_TYPE_Leaf, msg);
                    } else {
                        DEBUG_TYPE_printException.invoke(DEBUG_TYPE_Leaf, exc, msg,
                            LOG_SEVERITY_Error);
                    }

                    break;
                case WARN:
                    if (exc == null) {
                        DEBUG_TYPE_warn.invoke(DEBUG_TYPE_Leaf, msg);
                    } else {
                        DEBUG_TYPE_printException.invoke(DEBUG_TYPE_Leaf, exc, msg,
                            LOG_SEVERITY_Warning);
                    }

                    break;
                case INFO:
                    if (exc == null) {
                        DEBUG_TYPE_println.invoke(DEBUG_TYPE_Leaf, msg);
                    } else {
                        DEBUG_TYPE_printException.invoke(DEBUG_TYPE_Leaf, exc, msg,
                            LOG_SEVERITY_General);
                    }

                    break;
                case DEBUG:
                    if (exc == null) {
                        DEBUG_TYPE_debugln.invoke(DEBUG_TYPE_Leaf, msg);
                    } else {
                        DEBUG_TYPE_printException.invoke(DEBUG_TYPE_Leaf, exc, msg,
                            LOG_SEVERITY_Debug);
                    }

                    break;
                case TRACE:
                    if (exc == null) {
                        DEBUG_TYPE_trace.invoke(DEBUG_TYPE_Leaf, msg);
                    } else {
                        DEBUG_TYPE_printException.invoke(DEBUG_TYPE_Leaf, exc, msg,
                            LOG_SEVERITY_Trace);
                    }

                    break;
                default:
                    throw new IllegalArgumentException("Unknown level: " + level);
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new FormattedException(
                "Failed to invoke logging functions for log() in NewZomboidLogHandler!", e);
        }
    }

    @Override
    public boolean shouldLog(LogLevel level, LogCategory category) {
        if (DEBUG_TYPE_Leaf == null) {
            return true;
        }

        try {
            switch (level) {
                case ERROR:
                    return (boolean) DEBUG_LOG_STREAM_isLogEnabled.invoke(DEBUG_TYPE_Leaf_logStream,
                        LOG_SEVERITY_Error);
                case WARN:
                    return (boolean) DEBUG_LOG_STREAM_isLogEnabled.invoke(DEBUG_TYPE_Leaf_logStream,
                        LOG_SEVERITY_Warning);
                case INFO:
                    return (boolean) DEBUG_LOG_STREAM_isLogEnabled.invoke(DEBUG_TYPE_Leaf_logStream,
                        LOG_SEVERITY_General);
                case DEBUG:
                    return (boolean) DEBUG_LOG_STREAM_isLogEnabled.invoke(DEBUG_TYPE_Leaf_logStream,
                        LOG_SEVERITY_Debug);
                case TRACE:
                    return (boolean) DEBUG_LOG_STREAM_isLogEnabled.invoke(DEBUG_TYPE_Leaf_logStream,
                        LOG_SEVERITY_Trace);
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new FormattedException("Failed to invoke isLogEnabled in shouldLog!", e);
        }

        throw new IllegalArgumentException("Unknown level: " + level);
    }

    @Override
    public void close() {}
}
