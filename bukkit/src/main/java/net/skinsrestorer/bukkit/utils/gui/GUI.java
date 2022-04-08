/*
 * SkinsRestorer
 *
 * Copyright (C) 2022 SkinsRestorer
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 */
package net.skinsrestorer.bukkit.utils.gui;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GUI {
    private final Inventory inv;
    private final JavaPlugin plugin;
    private final UUID player;
    private final List<GUIItem> guiItems = new ArrayList<>();

    public GUI(String name, int lines, JavaPlugin plugin, Player player) {
        inv = Bukkit.createInventory(player, lines * 9, name);
        this.plugin = plugin;
        this.player = player.getUniqueId();
    }

    public GUIItem addItem(int slot, ItemStack item) {
        inv.setItem(slot, item);

        GUIItem guiitem = new GUIItem(this, slot, inv, player, plugin);

        guiItems.add(guiitem);

        return guiitem;
    }

    public void openGUI() {
        Player p = Bukkit.getPlayer(player);

        if (p != null) {
            p.openInventory(inv);
        }
    }

    public void unregisterAllItems() {
        for (GUIItem guiItem : guiItems) {
            guiItem.unregisterAllListeners();
        }
    }

    public int getSize() {
        return inv.getSize();
    }
}
