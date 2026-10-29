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

package dev.aoqia.leaf.loader.impl.gui;

import javax.swing.JOptionPane;

import dev.aoqia.leaf.loader.impl.discovery.ModCandidateImpl;
import dev.aoqia.leaf.loader.impl.discovery.ModSource;

public class VerifyModDialog {
    public static boolean show(ModCandidateImpl mod) {
        ModSource source = mod.getSource();

        String s = "A leaf mod was discovered and is about to be loaded."
            + "\nDo you want to load the mod?"
            + "\nIf you do not recognise or trust this mod, you should click no."
            + "\n\nDiscovery source: " + source;

        if (source == ModSource.WORKSHOP || source == ModSource.UNKNOWN) {
            s += "\nWorkshop ID: " + mod.getWorkshopId();
        }

        s += "\nGame Mod ID" + (mod.getModInfo() == null ? " (?)" : "") + ": " + mod.getGameId();
        s += "\nLeaf Mod ID: " + mod.getId();

        if (source == ModSource.UNKNOWN) {
            s += "\n\nJAR path: " + mod.getLocalPath();
        }

        int result = JOptionPane.showConfirmDialog(null, s, "Mod Verification", JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);

        return result == JOptionPane.YES_OPTION;
    }
}
