package com.mastermarisa.maid_restaurant.init;

import com.mastermarisa.maid_restaurant.MaidRestaurant;
import com.mastermarisa.maid_restaurant.block.HorizontalDirectionalOnlyBlock;
import com.mastermarisa.maid_restaurant.block.OrderBellBlock;
import com.mastermarisa.maid_restaurant.blockentity.OrderBellBlockEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@SuppressWarnings({"DataFlowIssue"})
public interface ModBlocks {
    DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MaidRestaurant.MOD_ID);
    DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MaidRestaurant.MOD_ID);

    RegistryObject<Block> CREATIVE_CRATE = BLOCKS.register("creative_crate",
            () -> new HorizontalDirectionalOnlyBlock(
                    BlockBehaviour.Properties.of()
                            .sound(SoundType.WOOD)
                            .strength(-1, 3600000)
                            .ignitedByLava()
                            .forceSolidOn(),
                    null));

    RegistryObject<Block> ORDER_BELL = BLOCKS.register("order_bell",
            () -> new OrderBellBlock(
                    BlockBehaviour.Properties.of()
                            .sound(SoundType.LANTERN)
                            .noOcclusion()
                            .strength(4, 10)
            ));

    RegistryObject<BlockEntityType<OrderBellBlockEntity>> ORDER_BELL_BE = BLOCK_ENTITIES.register("order_bell", () -> BlockEntityType.Builder.of(OrderBellBlockEntity::new, ORDER_BELL.get()).build(null));
}
