package com.mastermarisa.maid_restaurant.uitls;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;

import java.util.UUID;

public class FakePlayerUtil {
    public static final GameProfile fakePlayerProfile = new GameProfile(UUID.randomUUID(),"Restaurant Fake Player");

    public static FakePlayer getPlayer(ServerLevel level) { return FakePlayerFactory.get(level, fakePlayerProfile); }
}