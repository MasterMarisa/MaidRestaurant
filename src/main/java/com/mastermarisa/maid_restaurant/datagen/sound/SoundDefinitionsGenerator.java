package com.mastermarisa.maid_restaurant.datagen.sound;

import com.mastermarisa.maid_restaurant.MaidRestaurant;
import com.mastermarisa.maid_restaurant.init.ModSounds;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.SoundDefinition;
import net.minecraftforge.common.data.SoundDefinitionsProvider;

public class SoundDefinitionsGenerator extends SoundDefinitionsProvider {
    public SoundDefinitionsGenerator(PackOutput output, ExistingFileHelper helper) {
        super(output, MaidRestaurant.MOD_ID, helper);
    }

    @Override
    public void registerSounds() {
        SoundDefinition orderBell = definition().subtitle(subtitle("block.order_bell"))
                .with(sound("block/order_bell"));
        this.add(ModSounds.ORDER_BELL, orderBell);
    }

    protected static SoundDefinition.Sound sound(final String name) {
        return sound(new ResourceLocation(MaidRestaurant.MOD_ID, name));
    }

    protected static String subtitle(String subtitle) {
        return "subtitles.%s.%s".formatted(MaidRestaurant.MOD_ID, subtitle);
    }
}
