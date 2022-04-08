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
package net.skinsrestorer.bukkit;

import com.cryptomorin.xseries.XMaterial;
import com.mojang.authlib.properties.Property;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.skinsrestorer.api.bukkit.BukkitHeadAPI;
import net.skinsrestorer.api.property.IProperty;
import net.skinsrestorer.bukkit.utils.gui.GUI;
import net.skinsrestorer.shared.storage.Locale;
import net.skinsrestorer.shared.utils.C;
import net.skinsrestorer.shared.utils.log.SRLogger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

@RequiredArgsConstructor
public class SkinsGUI implements Listener {
    @Getter
    private static final Map<String, Integer> menus = new ConcurrentHashMap<>();
    private final SkinsRestorer plugin;
    private final SRLogger log;

    public GUI getGUI(Player player, int page, Map<String, IProperty> skinsList) {
        GUI gui = new GUI(C.c(Locale.SKINSMENU_TITLE_NEW).replace("%page", String.valueOf(page)), 6, plugin, player);

        Runnable noneAction = () -> {
            getGUI(player, page, skinsList).openGUI();
        };
        Runnable nextAction = () -> {
            if (plugin.isBungeeEnabled()) {
                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                    int currentPageG = getMenus().get(player.getName());
                    getMenus().put(player.getName(), currentPageG + 1);
                    plugin.requestSkinsFromBungeeCord(player, currentPageG + 1);
                });
            } else {
                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                    final int currentPageA = getMenus().get(player.getName());
                    getMenus().put(player.getName(), currentPageA + 1);
                    GUI newGUI = getGUI((player).getPlayer(), currentPageA + 1);

                    Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () ->
                            newGUI.openGUI());
                });
            }
        };
        Runnable previousAction = () -> {
            if (plugin.isBungeeEnabled()) {
                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                    int currentPageY = getMenus().get(player.getName());
                    getMenus().put(player.getName(), currentPageY - 1);
                    plugin.requestSkinsFromBungeeCord(player, currentPageY - 1);
                });
            } else {
                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                    final int currentPageB = getMenus().get(player.getName());
                    getMenus().put(player.getName(), currentPageB - 1);
                    GUI newGUI = getGUI((player).getPlayer(), currentPageB - 1);

                    Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () ->
                            newGUI.openGUI());
                });
            }
        };
        Runnable deleteAction = () -> {
            if (plugin.isBungeeEnabled()) {
                Bukkit.getScheduler().runTaskAsynchronously(plugin, () ->
                        plugin.requestSkinClearFromBungeeCord(player));
                player.closeInventory();
            } else {
                plugin.getSkinCommand().onSkinClear(player);
                player.closeInventory();
            }
        };

        ItemStack none = new GuiGlass(GlassType.NONE).getItemStack();
        ItemStack delete = new GuiGlass(GlassType.DELETE).getItemStack();
        ItemStack prev = new GuiGlass(GlassType.PREV).getItemStack();
        ItemStack next = new GuiGlass(GlassType.NEXT).getItemStack();

        // White Glass line
        IntStream.rangeClosed(36, 44).forEach(i -> gui.addItem(i, none).addDefaultEvent(noneAction));

        // If page is above 1, adding Previous Page button.
        if (page > 1) {
            gui.addItem(45, prev).addDefaultEvent(previousAction);
            gui.addItem(46, prev).addDefaultEvent(previousAction);
            gui.addItem(47, prev).addDefaultEvent(previousAction);
        } else {
            // Empty place previous
            gui.addItem(45, none).addDefaultEvent(noneAction);
            gui.addItem(46, none).addDefaultEvent(noneAction);
            gui.addItem(47, none).addDefaultEvent(noneAction);
        }

        // Middle button //remove skin
        gui.addItem(48, delete).addDefaultEvent(deleteAction);
        gui.addItem(49, delete).addDefaultEvent(deleteAction);
        gui.addItem(50, delete).addDefaultEvent(deleteAction);

        int i = 0;
        for (Map.Entry<String, IProperty> entry : skinsList.entrySet()) {
            if (i == 36) {
                break;
            }

            if (entry.getKey().chars().anyMatch(c -> Character.isLetter(c) && Character.isUpperCase(c))) {
                log.info("ERROR: skin " + entry.getKey() + ".skin contains a Upper case!");
                log.info("Please rename the file name to a lower case!.");
                continue;
            }

            gui.addItem(i, createSkull(entry.getKey(), entry.getValue()))
                    .addDefaultEvent(() -> {
                        if (plugin.isBungeeEnabled()) {
                            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                                plugin.requestSkinSetFromBungeeCord(player, entry.getKey());
                            });
                            player.closeInventory();
                        } else {
                            // TODO: use #setSkin() function from SkinCommand.class
                            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                                plugin.getSkinCommand().onSkinSetShort(player, entry.getKey());
                            });
                            player.closeInventory();
                        }
                    });
            i++;
        }

        // If the page is not empty, adding Next Page button.
        if (i == 36 && page < 999) {
            gui.addItem(53, next).addDefaultEvent(nextAction);
            gui.addItem(52, next).addDefaultEvent(nextAction);
            gui.addItem(51, next).addDefaultEvent(nextAction);
        } else {
            // Empty place next
            gui.addItem(53, none).addDefaultEvent(noneAction);
            gui.addItem(52, none).addDefaultEvent(noneAction);
            gui.addItem(51, none).addDefaultEvent(noneAction);
        }

        return gui;
    }

    public GUI getGUI(Player player, int page) {
        if (page > 999)
            page = 999;
        int skinNumber = 36 * page;

        Map<String, IProperty> skinsList = plugin.getSkinStorage().getSkins(skinNumber);
        ++page; // start counting from 1
        return getGUI(player, page, skinsList);
    }

    private ItemStack createSkull(String name, IProperty property) {
        ItemStack is = XMaterial.PLAYER_HEAD.parseItem();
        SkullMeta sm = (SkullMeta) Objects.requireNonNull(is).getItemMeta();

        List<String> lore = new ArrayList<>();
        lore.add(C.c(Locale.SKINSMENU_SELECT_SKIN));
        Objects.requireNonNull(sm).setDisplayName(name);
        sm.setLore(lore);
        is.setItemMeta(sm);

        try {
            BukkitHeadAPI.setSkull(is, ((Property) property).getValue());
        } catch (Exception e) {
            log.info("ERROR: could not add '" + name + "' to SkinsGUI, skin might be corrupted or invalid!");
            e.printStackTrace();
        }

        return is;
    }

    private enum GlassType {
        NONE, PREV, NEXT, DELETE
    }

    public static class GuiGlass {
        @Getter
        private ItemStack itemStack;
        @Getter
        private String text;

        public GuiGlass(GlassType glassType) {
            switch (glassType) {
                case NONE:
                    itemStack = XMaterial.WHITE_STAINED_GLASS_PANE.parseItem();
                    text = " ";
                    break;
                case PREV:
                    itemStack = XMaterial.YELLOW_STAINED_GLASS_PANE.parseItem();
                    text = C.c(Locale.SKINSMENU_PREVIOUS_PAGE);
                    break;
                case NEXT:
                    itemStack = XMaterial.GREEN_STAINED_GLASS_PANE.parseItem();
                    text = C.c(Locale.SKINSMENU_NEXT_PAGE);
                    break;
                case DELETE:
                    itemStack = XMaterial.RED_STAINED_GLASS_PANE.parseItem();
                    text = C.c(Locale.SKINSMENU_CLEAR_SKIN);
                    break;
            }

            ItemMeta itemMeta = Objects.requireNonNull(itemStack).getItemMeta();
            Objects.requireNonNull(itemMeta).setDisplayName(text);
            itemStack.setItemMeta(itemMeta);
        }
    }
}