/*
 * This file is part of TechReborn, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2024 TechReborn
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package techreborn.items.armor;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public class QuantumSuitEventHandler {

	public static void register() {
		ServerTickEvents.END_WORLD_TICK.register(world -> {
			// Iterate over all players in the world
			for (PlayerEntity player : world.getPlayers()) {
				// We will hook into the vanilla damage system
				if (player.hurtTime > 0) { // hurtTime > 0 indicates the player has taken damage recently
					boolean energyConsumed = false; // Keep track if energy was consumed

					// Iterate over all equipped slots (armor slots)
					for (EquipmentSlot slot : EquipmentSlot.values()) {
						ItemStack stack = player.getEquippedStack(slot);
						if (stack.getItem() instanceof QuantumSuitItem) {
							QuantumSuitItem quantumArmor = (QuantumSuitItem) stack.getItem();
							long damageCost = quantumArmor.damageCost * player.hurtTime;

							if (quantumArmor.getStoredEnergy(stack) > quantumArmor.getEnergyCapacity()) {
								quantumArmor.setStoredEnergy(stack, 0);
							}

							if (quantumArmor.getStoredEnergy(stack) >= damageCost) {
								quantumArmor.tryUseEnergy(stack, damageCost);
								energyConsumed = true;
							} else {
								quantumArmor.setStoredEnergy(stack, 0);
							}
						}
					}

					if (energyConsumed) {
						player.hurtTime = 0;
					}
				}
			}
		});
	}
}
