package com.mastermarisa.maid_restaurant.init.registry;

import com.mastermarisa.maid_restaurant.MaidRestaurant;
import com.mastermarisa.maid_restaurant.core.schedule.CookingRequest;
import com.mastermarisa.maid_restaurant.core.schedule.RequestBus;
import com.mastermarisa.maid_restaurant.core.tree.RecipeNode;
import com.mastermarisa.maid_restaurant.init.ModItems;
import com.mastermarisa.maid_restaurant.item.CookingGuideItem;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MaidRestaurant.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CommandRegistry {
    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(Commands.literal("maid_restaurant")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("send_request")
                        .then(Commands.argument("restaurant_id", StringArgumentType.string())
                                .executes(context -> {
                                    Player player = context.getSource().getPlayer();
                                    if (player == null) {
                                        context.getSource().sendFailure(Component.literal("§c此命令只能由玩家执行"));
                                        return 1;
                                    }
                                    ServerLevel level = context.getSource().getLevel();
                                    String id = StringArgumentType.getString(context, "restaurant_id");
                                    return sendRequest(level, player, id, 1, context);
                                })
                                .then(Commands.argument("count", IntegerArgumentType.integer(1, 64))
                                        .executes(context -> {
                                            Player player = context.getSource().getPlayer();
                                            if (player == null) {
                                                context.getSource().sendFailure(Component.literal("§c此命令只能由玩家执行"));
                                                return 1;
                                            }
                                            ServerLevel level = context.getSource().getLevel();
                                            String id = StringArgumentType.getString(context, "restaurant_id");
                                            int count = IntegerArgumentType.getInteger(context, "count");
                                            return sendRequest(level, player, id, count, context);
                                        })
                                )
                        )
                )
                .then(Commands.literal("clear_request")
                        .then(Commands.argument("restaurant_id", StringArgumentType.string())
                                .executes(context -> {
                                    ServerLevel level = context.getSource().getLevel();
                                    String id = StringArgumentType.getString(context, "restaurant_id");
                                    RequestBus.getInstance(level, CookingRequest.class).clear(id);
                                    return 1;
                                })))
        );
    }

    private static int sendRequest(ServerLevel level, Player player, String restaurantId, int count, CommandContext<CommandSourceStack> context) {
        ItemStack itemInHand = player.getMainHandItem();
        if (!itemInHand.is(ModItems.COOKING_GUIDE.get()) || !itemInHand.hasTag()) {
            context.getSource().sendFailure(Component.literal("§c请手持有效的烹饪指南"));
            return 1;
        }

        CookingRequest request = new CookingRequest(RecipeNode.fromNBT(CookingGuideItem.getRecipeRoot(itemInHand)), count);
        request.root.applyOutputCount(level, count);
        RequestBus.getInstance(level, CookingRequest.class).enqueue(restaurantId, request);
        context.getSource().sendSuccess(() -> Component.literal("§a成功发送委托！"), true);
        return 1;
    }
}
