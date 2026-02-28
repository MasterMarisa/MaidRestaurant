package com.mastermarisa.maid_restaurant.advancements.rewards;

import com.mastermarisa.maid_restaurant.config.RestaurantConfig;
import com.mastermarisa.maid_restaurant.init.ModTrigger;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

public class GivePatchouliBookConfigTrigger extends SimpleCriterionTrigger<GivePatchouliBookConfigTrigger.Instance> {
    public void trigger(ServerPlayer serverPlayer) {
        super.trigger(serverPlayer, (instance) -> RestaurantConfig.GIVE_PATCHOULI_BOOK());
    }

    public Codec<Instance> codec() { return Instance.CODEC; }

    public static record Instance(Optional<ContextAwarePredicate> player) implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<GivePatchouliBookConfigTrigger.Instance> CODEC = RecordCodecBuilder.create((instance) -> instance.group(EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(GivePatchouliBookConfigTrigger.Instance::player)).apply(instance, GivePatchouliBookConfigTrigger.Instance::new));

        public static Criterion<Instance> instance() {
            return ModTrigger.GIVE_PATCHOULI_BOOK_CONFIG.get().createCriterion(new Instance(Optional.empty()));
        }
    }
}
