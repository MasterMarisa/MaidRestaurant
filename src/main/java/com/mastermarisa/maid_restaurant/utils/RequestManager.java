package com.mastermarisa.maid_restaurant.utils;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.mastermarisa.maid_restaurant.api.request.IRequest;
import com.mastermarisa.maid_restaurant.event.MaidTracker;
import com.mastermarisa.maid_restaurant.maid.TaskCook;
import com.mastermarisa.maid_restaurant.maid.TaskWaiter;
import com.mastermarisa.maid_restaurant.request.CookRequest;
import com.mastermarisa.maid_restaurant.request.CookRequestHandler;
import com.mastermarisa.maid_restaurant.request.ServeRequest;
import com.mastermarisa.maid_restaurant.request.ServeRequestHandler;
import com.mastermarisa.maid_restaurant.request.world.WorldCookRequestHandler;
import com.mastermarisa.maid_restaurant.request.world.WorldServeRequestHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RequestManager {
    private static final double MAX_RANGE = 50.0D;

    public static @Nullable IRequest peek(EntityMaid maid, int type) {
        switch (type) {
            case CookRequest.TYPE -> {
                return maid.getData(CookRequestHandler.TYPE).getFirst();
            }
            case ServeRequest.TYPE -> {
                ServeRequest request = maid.getData(ServeRequestHandler.TYPE).getFirst();
                if (request != null && request.toServe.getCount() <= 0) {
                    maid.getData(ServeRequestHandler.TYPE).removeFirst();
                    return peek(maid, ServeRequest.TYPE);
                }
                return request;
            }
        }
        return null;
    }

    public static @Nullable IRequest pop(EntityMaid maid, int type) {
        switch (type) {
            case CookRequest.TYPE -> {
                return maid.getData(CookRequestHandler.TYPE).removeFirst();
            }
            case ServeRequest.TYPE -> {
                return maid.getData(ServeRequestHandler.TYPE).removeFirst();
            }
        }
        return null;
    }

    public static void post(ServerLevel level, IRequest request, int type) {
        switch (type) {
            case CookRequest.TYPE -> level.getData(WorldCookRequestHandler.TYPE).add((CookRequest) request);
            case ServeRequest.TYPE -> level.getData(WorldServeRequestHandler.TYPE).add((ServeRequest) request);
        }
    }

    public static void tryDistributeRequests(ServerLevel level) {
        tryDistributeCookRequest(level);
        tryDistributeServeRequest(level);
    }

    private static void tryDistributeCookRequest(ServerLevel level) {
        List<CookRequest> toRemove = new ArrayList<>();
        WorldCookRequestHandler handler = level.getData(WorldCookRequestHandler.TYPE);
        for (var request : handler.toList()) {
            BlockPos pos = EncodeUtils.decode(request.targets[0]);
            List<EntityMaid> cookers = MaidTracker.maids.stream().filter(maid -> maid.getTask() instanceof TaskCook).filter(m-> {
                CookRequestHandler requests = m.getData(CookRequestHandler.TYPE);
                return requests.accept && requests.size() < 7 && pos.distSqr(m.blockPosition()) <= Math.pow(MAX_RANGE, 2.0D);
            }).toList();
            if (!cookers.isEmpty()) {
                cookers = new ArrayList<>(cookers);
                Collections.shuffle(cookers);
                EntityMaid target = cookers.getFirst();
                target.getData(CookRequestHandler.TYPE).add(request);
                toRemove.add(request);
            }
        }

        for (var request : toRemove) {
            handler.remove(request);
        }
    }

    public static void tryDistributeServeRequest(ServerLevel level) {
        List<ServeRequest> toRemove = new ArrayList<>();
        WorldServeRequestHandler handler = level.getData(WorldServeRequestHandler.TYPE);
        for (var request : handler.toList()) {
            BlockPos pos = request.targets.getFirst();
            List<EntityMaid> waiters = MaidTracker.maids.stream().filter(maid -> maid.getTask() instanceof TaskWaiter).filter(m-> {
                ServeRequestHandler requests = m.getData(ServeRequestHandler.TYPE);
                return requests.size() < 5 && pos.distSqr(m.blockPosition()) <= Math.pow(MAX_RANGE,2.0D);
            }).toList();
            if (!waiters.isEmpty()) {
                waiters = new ArrayList<>(waiters);
                Collections.shuffle(waiters);
                EntityMaid waiter = waiters.getFirst();
                ServeRequestHandler serveRequestHandler = waiter.getData(ServeRequestHandler.TYPE);
                serveRequestHandler.add(request);
                serveRequestHandler.toList().forEach(serveRequest -> {
                    serveRequest.targets.forEach(p -> BlockUsageManager.addUser(p,waiter.getUUID()));
                });
                toRemove.add(request);
            }
        }

        for (var request : toRemove) {
            handler.remove(request);
        }
    }
}
