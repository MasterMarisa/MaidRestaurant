package com.mastermarisa.maid_restaurant.init;

import com.mastermarisa.maid_restaurant.MaidRestaurant;
import com.mastermarisa.maid_restaurant.core.schedule.CookingRequest;
import com.mastermarisa.maid_restaurant.core.schedule.RequestBus;
import com.mastermarisa.maid_restaurant.core.tree.RecipeNode;
import com.mastermarisa.maid_restaurant.item.CookingGuideItem;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
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
                                            ServerLevel level = context.getSource().getLevel();
                                            String id = StringArgumentType.getString(context, "restaurant_id");
                                            if (player != null) {
                                                ItemStack itemInHand = player.getMainHandItem();
                                                if (itemInHand.is(ModItems.COOKING_GUIDE.get()) && itemInHand.hasTag()) {
                                                    RequestBus.getInstance(level, CookingRequest.class).enqueue(id, new CookingRequest(RecipeNode.fromNBT(CookingGuideItem.getRecipeRoot(itemInHand)), 1));
                                                    player.sendSystemMessage(Component.literal("Successfully Sent!"));
                                                    return 1;
                                                }
                                            }
                                            return 0;
                                        }))
        ));
    }
}
