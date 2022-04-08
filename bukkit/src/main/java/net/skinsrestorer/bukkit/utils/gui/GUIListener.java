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

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@RequiredArgsConstructor
public class GUIListener implements Listener {
    private final GUI gui;
    private final Inventory inv;
    private final UUID player;
    private final int slot;
    @Getter
    private final Map<InventoryAction, Runnable> actions = new EnumMap<>(InventoryAction.class);
    @Setter
    private Runnable defaultTask;

    @EventHandler
    private void onClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            Player eventPlayer = (Player) event.getWhoClicked();

            if (eventPlayer.getUniqueId().equals(player) && Objects.equals(event.getClickedInventory(), inv) && event.getSlot() == slot) {
                boolean d = true;

                for (Map.Entry<InventoryAction, Runnable> entry : actions.entrySet()) {
                    if (event.getAction().equals(entry.getKey())) {
                        try {
                            entry.getValue().run();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        d = false;
                        break;
                    }
                }

                if (d && event.getAction() != InventoryAction.NOTHING && event.getAction() != InventoryAction.UNKNOWN) {
                    try {
                        defaultTask.run();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                event.setCancelled(true);

                gui.unregisterAllItems();
            }
        }
    }
}
