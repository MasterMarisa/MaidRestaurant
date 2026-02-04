package com.mastermarisa.maid_restaurant.init;

import com.mastermarisa.maid_restaurant.events.*;
import net.neoforged.bus.api.IEventBus;

public interface InitEvents {
    static void register(IEventBus bus){
        bus.register(OnServerAboutToStart.class);
        bus.register(MaidTracker.class);
        bus.register(RequestDistributor.class);
        bus.register(OnEntityInteract.class);
        bus.register(BlockSelector.class);
        bus.register(OnAddReloadListeners.class);
        bus.register(OnServerAboutToStart.class);
        bus.register(OnMaidTaskEnable.class);
    }
}
