package com.mastermarisa.maid_restaurant.core.plan;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.mastermarisa.maid_restaurant.api.IMaidStorage;
import com.mastermarisa.maid_restaurant.core.storage.StorageRegistry;
import com.mastermarisa.maid_restaurant.data.zone.AbstractZone;
import com.mastermarisa.maid_restaurant.uitls.ChefScheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.crafting.Ingredient;

import javax.annotation.Nullable;
import java.util.*;

public final class MaidSupplySource implements ISupplySource {
    private static final int POSITION_CACHE_TICKS = 100;
    private static final Map<AbstractZone, CachedPositions> POSITION_CACHE = new WeakHashMap<>();

    private final ServerLevel level;
    private final AbstractZone zone;
    private final BlockPos center;

    private MaidSupplySource(ServerLevel level, AbstractZone zone, BlockPos center) {
        this.level = level;
        this.zone = zone;
        this.center = center;
    }

    @Nullable
    public static MaidSupplySource of(ServerLevel level, EntityMaid maid) {
        AbstractZone zone = ChefScheduler.getStorageZone(maid);
        return zone == null ? null : new MaidSupplySource(level, zone, maid.blockPosition());
    }

    @Override
    public List<SupplyTarget> find(Ingredient ingredient, int want) {
        List<Weighted> found = new ArrayList<>();
        for (BlockPos pos : containers()) {
            IMaidStorage storage = StorageRegistry.tryGetAt(level, pos);
            if (storage == null) continue;

            int available = storage.count(level, pos, ingredient);
            if (available > 0) {
                found.add(new Weighted(new SupplyTarget(pos, Math.min(available, want)), storage.getPriority()));
            }
        }

        found.sort(Comparator.comparingInt(Weighted::priority)
                .thenComparingDouble(weighted -> weighted.target().pos().distSqr(center)));

        List<SupplyTarget> targets = new ArrayList<>(found.size());
        for (Weighted weighted : found) {
            targets.add(weighted.target());
        }
        return targets;
    }

    private List<BlockPos> containers() {
        long now = level.getGameTime();
        CachedPositions cached = POSITION_CACHE.get(zone);
        if (cached != null && now < cached.expiresAt()) return cached.positions();

        List<BlockPos> positions = new ArrayList<>();
        for (BlockPos pos : zone) {
            if (StorageRegistry.tryGetAt(level, pos) != null) {
                positions.add(pos.immutable());
            }
        }
        POSITION_CACHE.put(zone, new CachedPositions(now + POSITION_CACHE_TICKS, positions));
        return positions;
    }

    private record CachedPositions(long expiresAt, List<BlockPos> positions) {}

    private record Weighted(SupplyTarget target, int priority) {}
}
