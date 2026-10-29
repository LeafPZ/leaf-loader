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

package dev.aoqia.leaf.loader.impl.util.log;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;

public class ConsoleLogHandler implements LogHandler {
    private static final LogLevel MIN_STDERR_LEVEL = LogLevel.ERROR;
    private static final LogLevel MIN_STDOUT_LEVEL = LogLevel.getDefault();

    public static final PrintStream originalOut = new PrintStream(new FileOutputStream(FileDescriptor.out));
    public static final PrintStream originalErr = new PrintStream(new FileOutputStream(FileDescriptor.err));

    protected static String formatLog(long time, LogLevel level, LogCategory category, String msg, Throwable exc) {
        String ret = String.format("[%tT] [%s] [%s/%s]: %s%n", time, level.name(), category.context, category.name,
            msg);

        if (exc != null) {
            StringWriter writer = new StringWriter(ret.length() + 500);

            try (PrintWriter pw = new PrintWriter(writer, false)) {
                pw.print(ret);
                exc.printStackTrace(pw);
            }

            ret = writer.toString();
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
        String formatted = formatLog(time, level, category, msg, exc);

        // Call original streams instead because the game hijacks stdout and stderr
        if (level.isLessThan(MIN_STDERR_LEVEL)) {
            originalOut.print(formatted);
        } else {
            originalErr.print(formatted);
        }
    }

    @Override
    public boolean shouldLog(LogLevel level, LogCategory category) {
        return !level.isLessThan(MIN_STDOUT_LEVEL);
    }
}
