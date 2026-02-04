package com.mastermarisa.maid_restaurant.entity.attachment;

import com.mastermarisa.maid_restaurant.uitls.manager.CookTaskManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeType;

public class CookRequest {
    public ResourceLocation id;
    public RecipeType<?> type;
    public int count;
    public int requestedCount;

    public boolean is(RecipeType<?> other) {
        return type.equals(other);
    }

    public CookRequest(ResourceLocation id, RecipeType<?> type, int count) {
        this(id,type,count,count);
    }

    public CookRequest(String id,String UID,int count) {
        this(id,UID,count,count);
    }

    public CookRequest(ResourceLocation id, RecipeType<?> type, int count,int requestedCount) {
        this.id = id;
        this.type = type;
        this.count = count;
        this.requestedCount = requestedCount;
    }

    public CookRequest(String id,String UID,int count,int requestedCount) {
        this.id = ResourceLocation.parse(id);
        this.type = CookTaskManager.getType(UID);
        this.count = count;
        this.requestedCount = requestedCount;
    }
}
