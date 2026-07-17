package com.mastermarisa.maid_restaurant.uitls;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;

import java.util.UUID;

public class FakePlayerUtils {
    public static final GameProfile fakePlayerProfile;

    public static FakePlayer getPlayer(ServerLevel level) { return FakePlayerFactory.get(level,fakePlayerProfile); }

    static {
        fakePlayerProfile = new GameProfile(UUID.randomUUID(),"Arm");
    }
}
