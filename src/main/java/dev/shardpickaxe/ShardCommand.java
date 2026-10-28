package dev.shardpickaxe;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.item.ItemStack;

public final class ShardCommand {
	private ShardCommand() {
	}

	public static void register() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
				dispatcher.register(Commands.literal("shard-pickaxe")
						.requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
						.then(Commands.literal("give")
								.executes(ctx -> give(ctx.getSource(), 1))
								.then(Commands.argument("count", IntegerArgumentType.integer(1, 64))
										.executes(ctx -> give(ctx.getSource(),
												IntegerArgumentType.getInteger(ctx, "count")))))));
	}

	private static int give(CommandSourceStack source, int count) throws CommandSyntaxException {
		ServerPlayer player = source.getPlayerOrException();

		for (int i = 0; i < count; i++) {
			ItemStack stack = ShardItems.create(source.registryAccess());
			if (!player.getInventory().add(stack)) {
				player.drop(stack, false);
			}
		}

		source.sendSuccess(() -> Component.literal("Gave " + count + " Shard Pickaxe(s)"), true);
		return count;
	}
}
