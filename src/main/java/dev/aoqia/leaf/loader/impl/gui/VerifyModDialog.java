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
