package com.mastermarisa.maid_restaurant.mixin;

import com.mastermarisa.maid_restaurant.recipe.RecipeCacheBuilder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.world.item.crafting.RecipeManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ReloadableServerResources.class)
public abstract class WorldRecipeLoaderMixin {
    @Shadow
    public abstract RecipeManager getRecipeManager();

    @Inject(method = "updateRegistryTags(Lnet/minecraft/core/RegistryAccess;)V", at = @At("RETURN"))
    private void load(RegistryAccess pRegistryAccess, CallbackInfo ci) {
        RecipeCacheBuilder.buildCache(getRecipeManager(), pRegistryAccess);
    }
}
