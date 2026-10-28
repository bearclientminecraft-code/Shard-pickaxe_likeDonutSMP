package dev.shardpickaxe;

import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public final class TripleMining {
	private static boolean active = false;

	private TripleMining() {
	}

	public static void register() {
		PlayerBlockBreakEvents.BEFORE.register(TripleMining::onBeforeBreak);
	}

	private static boolean onBeforeBreak(Level level, Player player, BlockPos pos, BlockState state, BlockEntity blockEntity) {
		if (active) {
			return true;
		}
		if (!(level instanceof ServerLevel serverLevel) || !(player instanceof ServerPlayer serverPlayer)) {
			return true;
		}

		ItemStack tool = serverPlayer.getMainHandItem();
		if (!ShardItems.isShard(tool) || ShardItems.isExpired(tool)) {
			return true;
		}

		Direction face = findFace(serverPlayer, pos);

		active = true;
		try {
			for (int a = -1; a <= 1; a++) {
				for (int b = -1; b <= 1; b++) {
					if (a == 0 && b == 0) {
						continue;
					}

					BlockPos target = switch (face.getAxis()) {
						case X -> pos.offset(0, a, b);
						case Y -> pos.offset(a, 0, b);
						case Z -> pos.offset(a, b, 0);
					};

					BlockState targetState = serverLevel.getBlockState(target);
					if (targetState.isAir()
							|| !targetState.is(BlockTags.MINEABLE_WITH_PICKAXE)
							|| targetState.getDestroySpeed(serverLevel, target) < 0.0F
							|| serverLevel.getBlockEntity(target) != null) {
						continue;
					}

					if (tool.isEmpty()) {
						return true;
					}

					serverPlayer.gameMode.destroyBlock(target);
				}
			}
		} finally {
			active = false;
		}

		return true;
	}

	private static Direction findFace(ServerPlayer player, BlockPos pos) {
		HitResult hit = player.pick(player.blockInteractionRange() + 1.0, 1.0F, false);
		if (hit instanceof BlockHitResult blockHit && blockHit.getBlockPos().equals(pos)) {
			return blockHit.getDirection();
		}

		float pitch = player.getXRot();
		if (pitch > 45.0F) {
			return Direction.UP;
		}
		if (pitch < -45.0F) {
			return Direction.DOWN;
		}
		return player.getDirection().getOpposite();
	}
}
