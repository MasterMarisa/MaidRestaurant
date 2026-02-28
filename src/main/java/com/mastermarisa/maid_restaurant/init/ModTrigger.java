package com.mastermarisa.maid_restaurant.init;

import com.mastermarisa.maid_restaurant.advancements.rewards.GivePatchouliBookConfigTrigger;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModTrigger {
    private static final DeferredRegister<CriterionTrigger<?>> TRIGGERS;
    public static final DeferredHolder<CriterionTrigger<?>, GivePatchouliBookConfigTrigger> GIVE_PATCHOULI_BOOK_CONFIG;

    static {
        TRIGGERS = DeferredRegister.create(Registries.TRIGGER_TYPE, "maid_restaurant");
        GIVE_PATCHOULI_BOOK_CONFIG = TRIGGERS.register("give_patchouli_book_config", GivePatchouliBookConfigTrigger::new);
    }

    public static void register(IEventBus mod) {
        TRIGGERS.register(mod);
    }
}
