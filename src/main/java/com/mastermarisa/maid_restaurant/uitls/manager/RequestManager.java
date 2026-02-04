package com.mastermarisa.maid_restaurant.uitls.manager;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.mastermarisa.maid_restaurant.MaidRestaurant;
import com.mastermarisa.maid_restaurant.api.ICookTask;
import com.mastermarisa.maid_restaurant.entity.attachment.CookRequest;
import com.mastermarisa.maid_restaurant.entity.attachment.CookRequestQueue;
import com.mastermarisa.maid_restaurant.entity.attachment.ServeRequest;
import com.mastermarisa.maid_restaurant.entity.attachment.ServeRequestQueue;
import com.mastermarisa.maid_restaurant.events.MaidTracker;
import com.mastermarisa.maid_restaurant.task.TaskCooker;
import com.mastermarisa.maid_restaurant.task.TaskWaiter;
import com.mastermarisa.maid_restaurant.uitls.RecipeUtils;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class RequestManager {
    protected static final ConcurrentLinkedQueue<ServeRequest> toServe = new ConcurrentLinkedQueue<>();
    protected static final ConcurrentLinkedQueue<Pair<CookRequest, List<BlockPos>>> toOrder = new ConcurrentLinkedQueue<>();
    private static final double maxRange = 50.0D;

    public static Optional<ICookTask> getCurrentTask(EntityMaid maid) {
        return peekCookRequest(maid).flatMap(request -> CookTaskManager.getTask(request.type));
    }

    public static Optional<CookRequest> peekCookRequest(EntityMaid maid) {
        CookRequestQueue queue = maid.getData(CookRequestQueue.TYPE);
        return queue.peek();
    }

    public static Optional<CookRequest> popCookRequest(EntityMaid maid) {
        CookRequestQueue queue = maid.getData(CookRequestQueue.TYPE);
        Optional<CookRequest> request = queue.pop();
        maid.setData(CookRequestQueue.TYPE,queue);
        return request;
    }

    public static Optional<ServeRequest> peekServeRequest(EntityMaid maid) {
        ServeRequestQueue queue = maid.getData(ServeRequestQueue.TYPE);
        return queue.peek();
    }

    public static Optional<ServeRequest> popServeRequest(EntityMaid maid) {
        ServeRequestQueue queue = maid.getData(ServeRequestQueue.TYPE);
        Optional<ServeRequest> request = queue.pop();
        maid.setData(ServeRequestQueue.TYPE,queue);
        return request;
    }

    public static void addRequest(EntityMaid maid, CookRequest request) {
        CookRequestQueue queue = maid.getData(CookRequestQueue.TYPE);
        queue.add(request);
        maid.setData(CookRequestQueue.TYPE,queue);
        MaidRestaurant.LOGGER.debug("COOK_ADDED");
    }

    public static void addRequest(EntityMaid maid, ServeRequest request) {
        ServeRequestQueue queue = maid.getData(ServeRequestQueue.TYPE);
        queue.add(request);
        maid.setData(ServeRequestQueue.TYPE,queue);
        MaidRestaurant.LOGGER.debug("SERVE_ADDED");
    }

    public static ServeRequest fromCookRequest(CookRequest request, EntityMaid cooker, LinkedList<BlockPos> targetTables) {
        ServeRequest serveRequest = new ServeRequest();
        serveRequest.targetTables = targetTables;
        serveRequest.provider = cooker.getUUID();
        ICookTask task = CookTaskManager.getTask(request.type).get();
        ItemStack result = task.getResult(RecipeUtils.byKeyTyped(request.type,request.id));
        serveRequest.requestedCount = result.getCount() * request.requestedCount;
        serveRequest.toServe = result.copyWithCount(result.getCount() * request.count);

        return serveRequest;
    }

    public static void postServeRequest(ServeRequest request) {
        toServe.add(request);
    }

    public static void postCookRequest(CookRequest request, List<BlockPos> tables) {
        toOrder.add(Pair.of(request,tables));
    }

    public static void trySendRequests(ServerLevel level) {
        tryPostCookRequest(level);
        tryPostServeRequest(level);
    }

    public static void tryPostServeRequest(ServerLevel level) {
        ServeRequest request = toServe.peek();
        if (request != null) {
            if (level.getEntity(request.provider) instanceof EntityMaid cooker && cooker.getTask() instanceof TaskCooker) {
                List<EntityMaid> waiters = MaidTracker.maids.stream().filter(m->m.getTask() instanceof TaskWaiter).filter(m->
                        m.getData(ServeRequestQueue.TYPE).size() < 4 && m.distanceToSqr(cooker) <= Math.pow(maxRange,2.0D)
                ).toList();
                if (!waiters.isEmpty()) {
                    waiters = new ArrayList<>(waiters);
                    Collections.shuffle(waiters);
                    EntityMaid waiter = waiters.getFirst();
                    addRequest(waiter,request);
                    toServe.poll();
                }
            }
        }
    }

    private static void tryPostCookRequest(ServerLevel level) {
        var pair = toOrder.peek();
        if (pair != null) {
            BlockPos pos = pair.right().getLast();
            List<EntityMaid> cookers = MaidTracker.maids.stream().filter(m->m.getTask() instanceof TaskCooker).filter(m->
                    m.getData(CookRequestQueue.TYPE).size() < 7 && pos.distSqr(m.blockPosition()) <= Math.pow(maxRange,2.0D)
            ).toList();
            if (!cookers.isEmpty()) {
                cookers = new ArrayList<>(cookers);
                Collections.shuffle(cookers);
                EntityMaid target = cookers.getFirst();
                addRequest(target,pair.left());
                addRequest(target,fromCookRequest(pair.left(),target,new LinkedList<>(pair.right())));
                toOrder.poll();
            }
        }
    }
}
