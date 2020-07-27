/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package appeng.parts.reporting;

import java.util.List;

import alexiil.mc.lib.attributes.item.FixedItemInv;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Hand;

import appeng.api.config.Settings;
import appeng.api.config.SortDir;
import appeng.api.config.SortOrder;
import appeng.api.config.ViewItems;
import appeng.api.implementations.tiles.IViewCellStorage;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.ITerminalHost;
import appeng.api.storage.data.IAEStack;
import appeng.api.util.IConfigManager;
import appeng.container.ContainerLocator;
import appeng.container.ContainerOpener;
import appeng.container.implementations.MEMonitorableContainer;
import appeng.me.GridAccessException;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.util.ConfigManager;
import appeng.util.IConfigManagerHost;
import appeng.util.inv.IAEAppEngInventory;
import appeng.util.inv.InvOperation;
import net.minecraft.util.math.Vec3d;

/**
 * Anything resembling an network terminal with view cells can reuse this.
 *
 * Note this applies only to terminals like the ME Terminal. It does not apply
 * for more specialized terminals like the Interface Terminal.
 *
 * @author AlgorithmX2
 * @author yueh
 * @version rv3
 * @since rv3
 */
public abstract class AbstractTerminalPart extends AbstractDisplayPart
        implements ITerminalHost, IConfigManagerHost, IViewCellStorage, IAEAppEngInventory {

    private final IConfigManager cm = new ConfigManager(this);
    private final AppEngInternalInventory viewCell = new AppEngInternalInventory(this, 5);

    public AbstractTerminalPart(final ItemStack is) {
        super(is);

        this.cm.registerSetting(Settings.SORT_BY, SortOrder.NAME);
        this.cm.registerSetting(Settings.VIEW_MODE, ViewItems.ALL);
        this.cm.registerSetting(Settings.SORT_DIRECTION, SortDir.ASCENDING);
    }

    @Override
    public void getDrops(final List<ItemStack> drops, final boolean wrenched) {
        super.getDrops(drops, wrenched);

        for (final ItemStack is : this.viewCell) {
            if (!is.isEmpty()) {
                drops.add(is);
            }
        }
    }

    @Override
    public IConfigManager getConfigManager() {
        return this.cm;
    }

    @Override
    public void readFromNBT(final CompoundTag data) {
        super.readFromNBT(data);
        this.cm.readFromNBT(data);
        this.viewCell.readFromNBT(data, "viewCell");
    }

    @Override
    public void writeToNBT(final CompoundTag data) {
        super.writeToNBT(data);
        this.cm.writeToNBT(data);
        this.viewCell.writeToNBT(data, "viewCell");
    }

    @Override
    public boolean onPartActivate(final PlayerEntity player, final Hand hand, final Vec3d pos) {
        if (!super.onPartActivate(player, hand, pos)) {
            if (!player.world.isClient) {
                ContainerOpener.openContainer(getContainerType(player), player, ContainerLocator.forPart(this));
            }
        }
        return true;
    }

    public ScreenHandlerType<?> getContainerType(final PlayerEntity player) {
        return MEMonitorableContainer.TYPE;
    }

    @Override
    public <T extends IAEStack<T>> IMEMonitor<T> getInventory(IStorageChannel<T> channel) {
        try {
            return this.getProxy().getStorage().getInventory(channel);
        } catch (final GridAccessException e) {
            // err nope?
        }
        return null;
    }

    @Override
    public void updateSetting(final IConfigManager manager, final Settings settingName, final Enum<?> newValue) {

    }

    @Override
    public FixedItemInv getViewCellStorage() {
        return this.viewCell;
    }

    @Override
    public void onChangeInventory(final FixedItemInv inv, final int slot, final InvOperation mc,
                                  final ItemStack removedStack, final ItemStack newStack) {
        this.getHost().markForSave();
    }
}
