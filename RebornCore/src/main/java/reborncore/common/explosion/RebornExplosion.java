/*
 * This file is part of RebornCore, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2021 TeamReborn
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

package reborncore.common.explosion;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.apache.commons.lang3.time.StopWatch;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import reborncore.RebornCore;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by modmuss50 on 12/03/2016.
 */
public class RebornExplosion extends Explosion {

	@NotNull
	BlockPos center;

	@NotNull
	World world;

	@NotNull
	int radius;

	@Nullable
	LivingEntity livingBase;

	public RebornExplosion(
		@NotNull
			BlockPos center,
		@NotNull
			World world,
		@NotNull
			int radius) {
		super(world, null, null, null, center.getX(), center.getY(), center.getZ(), radius, false, DestructionType.DESTROY);
		this.center = center;
		this.world = world;
		this.radius = radius;
	}

	public void setLivingBase(
		@Nullable
			LivingEntity livingBase) {
		this.livingBase = livingBase;
	}

	public
	@Nullable
	LivingEntity getLivingBase() {
		return livingBase;
	}

	public void applyExplosion(Entity owner) {
		StopWatch watch = new StopWatch();
		watch.start();
		for (int dx = -radius; dx <= radius; dx++) {
			for (int dy = -radius; dy <= radius; dy++) {
				for (int dz = -radius; dz <= radius; dz++) {
					double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
					if (distance <= radius - 2) {
						BlockPos targetPos = center.add(dx, dy, dz);
						if (!isProtectedByHighResistanceBlock(center, targetPos)) {
							BlockState state = world.getBlockState(targetPos);
							Block block = state.getBlock();
							if (block != Blocks.BEDROCK && !state.isAir()) {
								block.onDestroyedByExplosion(world, targetPos, this);
								world.setBlockState(targetPos, Blocks.AIR.getDefaultState(), 3);
							}
						}
					}
				}
			}
		}

		radius = radius * 2;

		// Damage players within the explosion radius
		List<LivingEntity> entities = world.getEntitiesByClass(LivingEntity.class,
			new Box(center.getX() - radius, center.getY() - radius, center.getZ() - radius,
				center.getX() + radius, center.getY() + radius, center.getZ() + radius),
			(entity) -> true);

		for (LivingEntity entity : entities) {
			double distanceToEntity = center.getManhattanDistance(entity.getBlockPos());
			int damage = (int) ((radius - distanceToEntity) / radius * 200);

			// Check if the entity is protected by a high resistance block
			if (!isProtectedByHighResistanceBlock(center, entity.getBlockPos())) {
				entity.damage(entity.getWorld().getDamageSources().explosion(entity,owner), damage);
			}

			entity.damage(entity.getWorld().getDamageSources().explosion(entity,owner), 1);
			entity.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, 6000, 9));
		}

		watch.stop();
		RebornCore.LOGGER.info("The explosion took " + watch.getTime() + " milliseconds to explode");
	}

	private boolean isProtectedByHighResistanceBlock(BlockPos center, BlockPos targetPos) {
		int steps = (int) center.getManhattanDistance(targetPos);

		for (int step = 1; step <= steps; step++) {
			// Calculate the intermediate position
			double t = step / (double) steps;
			int x = (int) Math.round(center.getX() * (1 - t) + targetPos.getX() * t);
			int y = (int) Math.round(center.getY() * (1 - t) + targetPos.getY() * t);
			int z = (int) Math.round(center.getZ() * (1 - t) + targetPos.getZ() * t);

			BlockPos currentPos = new BlockPos(x, y, z);
			BlockState state = world.getBlockState(currentPos);
			float blastResistance = state.getBlock().getBlastResistance();

			if (blastResistance >= 1300) {
				return true; // Found a protecting block
			}
		}
		return false; // No protecting block found
	}

	@Override
	public
	@Nullable
	LivingEntity getCausingEntity() {
		return livingBase;
	}

	@Override
	public List<BlockPos> getAffectedBlocks() {
		List<BlockPos> poses = new ArrayList<>();
		for (int tx = -radius; tx < radius + 1; tx++) {
			for (int ty = -radius; ty < radius + 1; ty++) {
				for (int tz = -radius; tz < radius + 1; tz++) {
					if (Math.sqrt(Math.pow(tx, 2) + Math.pow(ty, 2) + Math.pow(tz, 2)) <= radius - 2) {
						BlockPos pos = center.add(tx, ty, tz);
						BlockState state = world.getBlockState(pos);
						Block block = state.getBlock();
						if (block != Blocks.BEDROCK && !state.isAir()) {
							poses.add(pos);
						}
					}
				}
			}
		}
		return poses;
	}
}
