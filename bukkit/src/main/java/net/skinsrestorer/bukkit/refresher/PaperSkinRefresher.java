/*
 * SkinsRestorer
 * Copyright (C) 2024  SkinsRestorer Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.skinsrestorer.bukkit.refresher;

import lombok.SneakyThrows;
import net.skinsrestorer.bukkit.utils.BukkitReflection;
import net.skinsrestorer.bukkit.utils.HandleReflection;
import net.skinsrestorer.shared.utils.ReflectionUtil;
import org.bukkit.entity.Player;

import javax.inject.Inject;
import java.lang.reflect.Method;
import java.util.function.Consumer;

// TODO: Rethink how necessary this class this since we already have native API support for this
public final class PaperSkinRefresher implements SkinRefresher {
    private final Method refreshPlayerMethod;
    private final Consumer<Player> triggerHealthUpdate;

    @SuppressWarnings("deprecation")
    @Inject
    public PaperSkinRefresher() {
        try {
            refreshPlayerMethod = BukkitReflection.getBukkitClass("entity.CraftPlayer").getDeclaredMethod("refreshPlayer");
            refreshPlayerMethod.setAccessible(true);

            // XP won't get updated on unsupported Paper builds
            this.triggerHealthUpdate = player -> {
                try {
                    Object entityPlayer = HandleReflection.getHandle(player, Object.class);

                    ReflectionUtil.invokeObjectMethod(entityPlayer, "triggerHealthUpdate");
                } catch (ReflectiveOperationException e) {
                    player.resetMaxHealth();
                }
            };
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @SneakyThrows
    public void refresh(Player player) {
        refreshPlayerMethod.invoke(player);
        triggerHealthUpdate.accept(player);
    }
}
