package techreborn.items.armor;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public class QuantumSuitEventHandler {

	public static void register() {
		ServerTickEvents.END_WORLD_TICK.register(world -> {
			// Iterate over all players in the world
			for (PlayerEntity player : world.getPlayers()) {
				// We will hook into the vanilla damage system
				if (player.hurtTime > 0) { // hurtTime > 0 indicates the player has taken damage recently
					for (EquipmentSlot slot : EquipmentSlot.values()) {
						ItemStack stack = player.getEquippedStack(slot);
						if (stack.getItem() instanceof QuantumSuitItem) {
							QuantumSuitItem quantumArmor = (QuantumSuitItem) stack.getItem();
							long damageCost = quantumArmor.damageCost * player.hurtTime;

							if (quantumArmor.getStoredEnergy(stack) >= damageCost) {
								quantumArmor.tryUseEnergy(stack, damageCost);
								// Cancel the damage by resetting hurtTime to 0
								player.hurtTime = 0;
							} else {
								quantumArmor.setStoredEnergy(stack, 0);
							}
						}
					}
				}
			}
		});
	}
}
