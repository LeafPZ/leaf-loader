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

package dev.aoqia.leaf.loader.zomboid.test;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.jspecify.annotations.NonNull;

import dev.aoqia.leaf.loader.impl.game.zomboid.ZomboidVersion;
import dev.aoqia.leaf.loader.impl.game.zomboid.ZomboidVersionLookup;

public final class ZomboidVersionLookupTest {
    private static final Pattern pattern = Pattern.compile(
        // major
        "(0|[1-9]\\d*)"
            // minor
            + "\\.(0|[1-9]\\d*)"
            // patch
            + "(\\.(0|[1-9]\\d*))?"
            + "(-(pre-release)"
            // pre-release branch name
            + ")?");

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            throw new RuntimeException("usage: <file/dir-to-try>");
        }

        Path path = Paths.get(args[0]);
        List<String> invalid = new ArrayList<>();

        if (Files.isDirectory(path)) {
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                @Override
                @NonNull
                public FileVisitResult visitFile(@NonNull Path file, @NonNull BasicFileAttributes attrs) {
                    if (file.getFileName().toString().endsWith(".jar")) {
                        check(file, path.relativize(file).toString(), invalid);
                    }

                    return FileVisitResult.CONTINUE;
                }
            });
        } else {
            check(path, path.getFileName().toString(), invalid);
        }

        System.out.println();

        if (invalid.isEmpty()) {
            System.out.println("All passed!");
        } else {
            System.out.println("Invalid:");

            for (String s : invalid) {
                System.out.println(s);
            }

            System.out.printf("%d invalid results%n", invalid.size());
        }
    }

    private static void check(Path file, String name, List<String> invalid) {
        ZomboidVersion result = ZomboidVersionLookup.getVersionExceptClassVersion(file);
        String msg = String.format("%s: %s", name, result.getId());
        System.out.println(msg);

        if (!pattern.matcher(result.getId()).matches()) {
            System.out.println("** invalid!");
            invalid.add(msg);
        }
    }
}
