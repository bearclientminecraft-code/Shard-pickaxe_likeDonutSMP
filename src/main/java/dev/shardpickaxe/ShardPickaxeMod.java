package dev.shardpickaxe;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class ShardPickaxeMod implements ModInitializer {
	public static final String MOD_ID = "shardpickaxe";

	private int ticks = 0;

	@Override
	public void onInitialize() {
		ShardCommand.register();
		TripleMining.register();
		ServerTickEvents.END_SERVER_TICK.register(this::onServerTick);
	}

	private void onServerTick(MinecraftServer server) {
		if (++ticks % 20 != 0) {
			return;
		}
		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			removeExpired(player);
		}
	}

	private static void removeExpired(ServerPlayer player) {
		Inventory inventory = player.getInventory();
		for (int i = 0; i < inventory.getContainerSize(); i++) {
			ItemStack stack = inventory.getItem(i);
			if (ShardItems.isShard(stack) && ShardItems.isExpired(stack)) {
				inventory.setItem(i, ItemStack.EMPTY);
				player.sendSystemMessage(Component.literal("Your Shard Pickaxe has self-destructed."));
			}
		}
	}
}
