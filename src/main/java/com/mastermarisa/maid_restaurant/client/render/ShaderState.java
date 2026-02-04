package com.mastermarisa.maid_restaurant.client.render;

import net.irisshaders.iris.api.v0.IrisApi;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ShaderState {
    public static boolean shaderEnabled(){
        return IrisApi.getInstance().isShaderPackInUse();
    }
}
