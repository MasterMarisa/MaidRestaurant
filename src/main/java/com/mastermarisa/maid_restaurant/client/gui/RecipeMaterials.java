package com.mastermarisa.maid_restaurant.client.gui;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.mastermarisa.maid_restaurant.MaidRestaurant;
import com.mastermarisa.maid_restaurant.api.ICookTask;
import com.mastermarisa.maid_restaurant.data.TagBlock;
import com.mastermarisa.maid_restaurant.maid.TaskCook;
import com.mastermarisa.maid_restaurant.request.CookRequest;
import com.mastermarisa.maid_restaurant.request.CookRequestHandler;
import com.mastermarisa.maid_restaurant.utils.CookTasks;
import com.mastermarisa.maid_restaurant.utils.component.RecipeData;
import com.mastermarisa.maid_restaurant.utils.component.StackPredicate;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Client-side "can this order actually be cooked?" check.
 *
 * <p>Runs entirely on the client against data the client already has: the synced recipe manager, the
 * player inventory and the item handlers of nearby blocks. Nothing here is authoritative - the server
 * still decides what actually gets cooked - it exists purely so the ordering screen can tell the
 * player up front which ingredients they are short of instead of silently accepting an order that
 * can never be fulfilled.
 */
public final class RecipeMaterials {

    /**
     * Horizontal/vertical block radius searched for container blocks, centred on the player.
     *
     * <p>This is the sharpest limitation of the whole check: storage just outside it is invisible, and
     * the player is then told to fetch items that are sitting in a chest a few blocks away. It used to
     * be 8, which is smaller than most kitchens. The scan is cached and skips positions with no block
     * entity, so raising it costs a palette lookup per position rather than a capability query.
     */
    private static final int CONTAINER_RANGE = 16;

    /**
     * How long the block scan is reused. Reading the player inventory is free and is redone on every
     * call, so only the expensive part - walking up to 4913 positions and asking each block entity for
     * an item handler - is cached. Caching the inventory too was a mistake: adding an ingredient while
     * the ordering screen was open could keep reporting the pre-change deficit for up to this many
     * ticks, and a stale answer is indistinguishable from a bug to the player.
     */
    private static final int BLOCK_SCAN_TICKS = 20;

    /** How far away a maid's inventory still counts as a source, in blocks. */
    private static final double MAID_RANGE = 24.0D;

    /** Set {@code -Dmaid_restaurant.debug_materials=true} to log what this check sees. */
    private static final boolean DEBUG =
            Boolean.getBoolean("maid_restaurant.debug_materials");

    private static final List<ItemStack> cachedBlockStacks = new ArrayList<>();
    private static long blockScanAtTick = Long.MIN_VALUE;

    /**
     * Cookers that hold ingredients, by registry name. Verified against the shipped jars: the block ids
     * are {@code pot} / {@code stockpot} / {@code steamer} in kaleidoscope_cookery, {@code cooking_pot}
     * in farmersdelight and {@code oven} / {@code blender} / {@code toaster} / {@code glass_drink_cup}
     * in bakeries.
     */
    private static final Set<String> COOK_BLOCKS = Set.of(
            "kaleidoscope_cookery:pot",
            "kaleidoscope_cookery:stockpot",
            "kaleidoscope_cookery:steamer",
            "farmersdelight:cooking_pot",
            "bakeries:oven",
            "bakeries:blender",
            "bakeries:toaster",
            "bakeries:glass_drink_cup"
    );

    /** Diagnostics only: the size of the available pool the last time it changed. */
    private static int lastLoggedPoolSize = -1;

    /** Total items the last check saw, for the warning message to report. */
    private static int lastPoolSize;
    /** How many of those came from containers rather than the player's own inventory. */
    private static int lastContainerItems;

    private RecipeMaterials() {}

    /**
     * @return one entry per shortfall, each named after the item that covers it and carrying the
     *         summed deficit as its count. Requirements that resolve to the same item are merged -
     *         a recipe listing lettuce three times reports "lettuce x3", not three separate lines -
     *         because that is also how the greedy requirement matching treats them.
     */
    public static List<ItemStack> missingOf(List<? extends OrderView> orders, int atMost) {
        List<ItemStack> missing = new ArrayList<>();
        if (orders.isEmpty()) return missing;

        List<ItemStack> available = availableStacks();
        Map<StackPredicate, ItemStack> displaySamples = new LinkedHashMap<>();
        LinkedHashMap<StackPredicate, Integer> needed = stillNeeded(orders, available, displaySamples);
        LinkedHashMap<ItemStack, Integer> shortfalls = new LinkedHashMap<>();

        for (Map.Entry<StackPredicate, Integer> entry : needed.entrySet()) {
            ItemStack display = represent(entry.getKey(), available, displaySamples);
            if (display.isEmpty()) continue;

            // Merge on the registry name, not the exact stack. Ingredient variants - different fish,
            // different eggs, anything tag-driven - are interchangeable for this requirement, and
            // keying on the precise stack split them into one row each. The first stack seen supplies
            // the icon and name; the counts add up.
            ItemStack key = display.copyWithCount(1);
            ItemStack existing = null;
            for (ItemStack seen : shortfalls.keySet()) {
                if (seen.getItem() == key.getItem()) {
                    existing = seen;
                    break;
                }
            }
            if (existing != null) shortfalls.merge(existing, entry.getValue(), Integer::sum);
            else shortfalls.put(key, entry.getValue());
        }

        for (Map.Entry<ItemStack, Integer> entry : shortfalls.entrySet()) {
            if (entry.getValue() <= 0) continue;
            missing.add(entry.getKey().copyWithCount(entry.getValue()));
            if (missing.size() >= atMost) break;
        }

        logReported(orders, available, missing);
        return missing;
    }

    /**
     * Reports what the check saw, whenever the reachable pool changes.
     *
     * <p>Logs each reported line with its registry name and full component string. Two rows that look
     * identical in chat are only identical if those match too - so if duplicates ever reappear, this
     * says whether they are genuinely the same stack or two variants that merely share a display name.
     * Enable with {@code -Dmaid_restaurant.debug_materials=true}.
     */
    private static void logReported(
            List<? extends OrderView> orders, List<ItemStack> available, List<ItemStack> missing) {

        if (!DEBUG || available.size() == lastLoggedPoolSize) return;
        lastLoggedPoolSize = available.size();

        MaidRestaurant.LOGGER.info(
                "[materials] pool={} stacks / {} items (containerItems={}); orders={}; reported={}",
                available.size(),
                lastPoolSize,
                lastContainerItems,
                orders.stream().map(order -> order.data().ID
                        + " asked=" + order.count()
                        + " queued=" + alreadyQueued(order.data().ID)
                        + " uncovered=" + uncovered(order)).toList(),
                missing.stream()
                        .map(m -> BuiltInRegistries.ITEM.getKey(m.getItem()) + " x" + m.getCount()
                                + " components=" + m.getComponents())
                        .toList()
        );
    }

    /**
     * Everything the player can still draw on to supply an order: their own inventory, the item handlers
     * of the container blocks around them, and the ingredients maids are already carrying.
     *
     * <p>The maid inventories matter because the maid is usually the one holding the ingredients: she
     * empties the chest into herself and then loads the pot, so a check that only looks at storage
     * declares everything missing while the meal is being cooked - which is exactly what players
     * reported. This still cannot see inside a pot, which is server-only; see {@link #blockContents}.
     *
     * <p>The player inventory is always read fresh; only the block scan is cached.
     */
    public static List<ItemStack> availableStacks() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null) return List.of();

        List<ItemStack> stacks = new ArrayList<>();
        collectPlayerInventory(minecraft, stacks);
        collectMaidInventories(minecraft, stacks);

        List<ItemStack> blocks = blockContents(minecraft);
        stacks.addAll(blocks);

        lastPoolSize = countItems(stacks);
        lastContainerItems = countItems(blocks);
        return stacks;
    }

    /**
     * Ingredients the maids around the player are carrying.
     *
     * <p>{@code EntityMaid.getAvailableInv} is a client-safe accessor for the synced maid inventory, and
     * the cast is done with {@code instanceof} rather than a typed import so this class stays loadable
     * even when Touhou Little Maid's entity class is not on the client's path.
     */
    private static void collectMaidInventories(Minecraft minecraft, List<ItemStack> out) {
        AABB around = minecraft.player.getBoundingBox().inflate(MAID_RANGE);
        for (Entity entity : minecraft.level.getEntitiesOfClass(Entity.class, around)) {
            if (!(entity instanceof EntityMaid maid)) continue;

            IItemHandler handler = maid.getAvailableInv(false);
            if (handler != null) collect(handler, out);
        }
    }

    private static int countItems(List<ItemStack> stacks) {
        int total = 0;
        for (ItemStack stack : stacks) total += stack.getCount();
        return total;
    }

    /**
     * Contents of every nearby container block, in a single pass.
     *
     * <p>Cooking blocks are deliberately excluded from this pool. Their contents are server-only:
     * {@code StockpotBlockEntity} and {@code PotBlockEntity} override {@code saveAdditional} /
     * {@code loadAdditional} but not {@code getUpdateTag} / {@code getUpdatePacket}, so their inputs are
     * never sent to the client, and {@code CapabilitiesRegistry} registers no item handler for them
     * either - only the oil pot has one. A client-side check therefore cannot see what a maid has
     * already loaded into a pot, and any attempt to would read an empty block entity.
     *
     * <p>That is why the report is purely about supply: it answers "does the storage around you still
     * hold what this order needs", not "will the maid succeed". {@link #isCookingBlockNear} exists so
     * the UI can say that difference out loud when it matters.
     */
    private static List<ItemStack> blockContents(Minecraft minecraft) {
        long now = minecraft.level.getGameTime();
        if (now - blockScanAtTick < BLOCK_SCAN_TICKS) return cachedBlockStacks;

        cachedBlockStacks.clear();
        BlockPos center = minecraft.player.blockPosition();
        for (BlockPos pos : BlockPos.withinManhattan(center, CONTAINER_RANGE, CONTAINER_RANGE, CONTAINER_RANGE)) {
            // The only client-visible cook state is the block itself, so cooking blocks are identified
            // by state here rather than through ICookTask.isValidWorkBlock, which needs a ServerLevel.
            if (isCookingBlock(minecraft.level.getBlockState(pos))) continue;

            // Blocks without a block entity have no handler and hold no items, so skip them outright
            // rather than paying for a capability lookup on every stone in range.
            if (minecraft.level.getBlockEntity(pos) == null) continue;

            IItemHandler handler = minecraft.level.getCapability(Capabilities.ItemHandler.BLOCK, pos, null);
            if (handler != null) collect(handler, cachedBlockStacks);
        }

        blockScanAtTick = now;
        return cachedBlockStacks;
    }

    /**
     * Whether a block is one of the cookers that hold ingredients. Matched by registry name against a
     * fixed list: this runs on the client, where {@code ICookTask.isValidWorkBlock} cannot be used
     * (it takes a {@code ServerLevel}), and the cookers the mod cares about are a known, small set.
     */
    private static boolean isCookingBlock(BlockState state) {
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        return COOK_BLOCKS.contains(id.toString());
    }

    /**
     * Whether a cooker stands nearby. The check itself can never see what is inside one, so this is how
     * the UI tells "you have not supplied these" apart from "a maid may already have loaded them".
     */
    public static boolean isCookingBlockNear() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null) return false;

        BlockPos center = minecraft.player.blockPosition();
        for (BlockPos pos : BlockPos.withinManhattan(center, CONTAINER_RANGE, CONTAINER_RANGE, CONTAINER_RANGE)) {
            if (isCookingBlock(minecraft.level.getBlockState(pos))) return true;
        }

        return false;
    }

    private static void collectPlayerInventory(Minecraft minecraft, List<ItemStack> out) {
        Inventory inventory = minecraft.player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (!stack.isEmpty()) out.add(stack);
        }
    }

    /**
     * How many items the last check considered reachable, across the player's inventory and every
     * container within {@link #CONTAINER_RANGE}.
     *
     * <p>Reported alongside the warning on purpose. When the player is told something is missing, the
     * first thing they need to know is whether the check found their storage at all - an empty or tiny
     * pool means the problem is the search radius, not the cupboard.
     */
    public static int lastPoolSize() {
        return lastPoolSize;
    }

    /** How many of {@link #lastPoolSize()} items came from containers rather than the inventory. */
    public static int lastContainerItems() {
        return lastContainerItems;
    }

    /** The block radius the scan used, so the warning can say how far it looked. */
    public static int scanRange() {
        return CONTAINER_RANGE;
    }

    /** Drops the cached scan, e.g. when the ordering screen closes. */
    public static void invalidate() {
        blockScanAtTick = Long.MIN_VALUE;
        cachedBlockStacks.clear();
        lastLoggedPoolSize = -1;
    }

    /**
     * @return how many of that item are reachable, counting the deficit-carrying stacks produced by
     *         {@link #missingOf} as well - so "missing 3" plus "have 2" reads as 2 available.
     */
    public static int countAvailable(ItemStack needed) {
        return countAvailable(needed, availableStacks());
    }

    public static int countAvailable(ItemStack needed, List<ItemStack> available) {
        int count = 0;
        for (ItemStack stack : available)
            if (ItemStack.isSameItemSameComponents(stack, needed)) count += stack.getCount();

        return count;
    }

    /**
     * Renders a missing list into tooltip lines, capped at {@code maxLines} with a trailing "..."
     * so a recipe with a dozen ingredients does not push the tooltip off screen.
     */
    public static List<Component> describe(List<ItemStack> missing, int maxLines) {
        List<Component> lines = new ArrayList<>();
        if (missing.isEmpty()) return lines;

        int shown = Math.min(missing.size(), maxLines);
        for (int i = 0; i < shown; i++) {
            ItemStack stack = missing.get(i);
            lines.add(Component.translatable(
                    "item.maid_restaurant.missing_entry",
                    stack.getHoverName(),
                    stack.getCount()
            ));
        }
        if (missing.size() > shown)
            lines.add(Component.translatable("item.maid_restaurant.missing_more", missing.size() - shown));

        return lines;
    }

    /**
     * Accumulates the still-needed amount of every requirement across all orders, one entry per
     * requirement instance and in recipe order.
     *
     * <p>Stock is charged per <em>item</em>, not per requirement. When a recipe lists the same
     * ingredient more than once those become several predicates that no {@code equals} could match,
     * and charging each of them against the full pool made every copy report the same item as missing
     * - which printed the same line repeatedly. Pooling instead means each item's count is spent once,
     * across every requirement that accepts it.
     *
     * <p>Requirements are then merged for display in {@link #missingOf}, on the item each resolves to.
     */
    private static LinkedHashMap<StackPredicate, Integer> stillNeeded(
            List<? extends OrderView> orders, List<ItemStack> available,
            Map<StackPredicate, ItemStack> displaySamples) {

        LinkedHashMap<StackPredicate, Integer> remain = new LinkedHashMap<>();
        for (OrderView order : orders) {
            ICookTask task = CookTasks.getTask(order.data().type);
            if (task == null) continue;

            RecipeManager manager = recipeManager();
            if (manager == null) continue;
            RecipeHolder<?> holder = manager.byKey(order.data().ID).orElse(null);
            if (holder == null) continue;

            // Mirrors the requirement list the cook task itself uses, so kitchen wares (oil, shovel,
            // ...) are reported as missing rather than being discovered only after the maid gives up.
            List<StackPredicate> required = new ArrayList<>(task.getIngredients(holder, Minecraft.getInstance().level));
            required.addAll(task.getKitchenWares());

            // Samples the task supplies for its own requirement list, which is the only way to name a
            // requirement that no recipe ingredient describes - a stockpot's water bucket and carrier,
            // a pot's oil. Without these the lines existed but had nothing to display and were dropped.
            List<ItemStack> samples = task.getIngredientDisplay(holder, Minecraft.getInstance().level);

            int[] owed = new int[required.size()];
            for (int i = 0; i < required.size(); i++) owed[i] = uncovered(order);

            // One entry per distinct item, carrying everything the player has of it. Quantities are
            // spent from this pool, so a stack can no longer satisfy the same requirement twice.
            Map<ItemStack, Integer> pool = new LinkedHashMap<>();
            for (ItemStack stack : available) {
                if (!stack.isEmpty()) pool.merge(stack.copyWithCount(1), stack.getCount(), Integer::sum);
            }

            for (Map.Entry<ItemStack, Integer> item : pool.entrySet()) {
                int left = item.getValue();
                for (int i = 0; i < required.size() && left > 0; i++) {
                    if (owed[i] <= 0 || !required.get(i).test(item.getKey())) continue;
                    int used = Math.min(owed[i], left);
                    owed[i] -= used;
                    left -= used;
                }
            }

            for (int i = 0; i < required.size(); i++) {
                if (owed[i] <= 0) continue;

                StackPredicate predicate = required.get(i);
                remain.merge(predicate, owed[i], Integer::sum);

                // Whoever gets here first supplies the name; the mapping is the same either way.
                ItemStack sample = i < samples.size() ? samples.get(i) : ItemStack.EMPTY;
                if (!sample.isEmpty()) displaySamples.putIfAbsent(predicate, sample);
            }
        }

        return remain;
    }

    /**
     * Names a requirement for display, in order of preference:
     *
     * <ol>
     *   <li>the sample the cook task supplied via
     *       {@link ICookTask#getIngredientDisplay} - the only source for requirements no recipe
     *       ingredient describes, such as a stockpot's water bucket and carrier or a pot's oil;</li>
     *   <li>an item the player already has that satisfies it;</li>
     *   <li>any recipe ingredient the predicate accepts, as a last resort.</li>
     * </ol>
     */
    private static ItemStack represent(
            StackPredicate predicate, List<ItemStack> available, Map<StackPredicate, ItemStack> displaySamples) {

        // The recipe's own item is preferred over one the player happens to hold, so the same
        // requirement cannot be split across two differently-named rows.
        ItemStack sample = displaySamples.get(predicate);
        if (sample != null && !sample.isEmpty()) return sample;

        for (ItemStack stack : available)
            if (!stack.isEmpty() && predicate.test(stack)) return stack;

        RecipeManager manager = recipeManager();
        if (manager == null) return ItemStack.EMPTY;

        for (RecipeHolder<?> holder : manager.getRecipes()) {
            for (var ingredient : holder.value().getIngredients()) {
                if (ingredient.isEmpty()) continue;
                for (ItemStack item : ingredient.getItems())
                    if (!item.isEmpty() && predicate.test(item)) return item;
            }
        }

        return ItemStack.EMPTY;
    }

    private static void collect(IItemHandler handler, List<ItemStack> out) {
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack stack = handler.getStackInSlot(i);
            if (!stack.isEmpty()) out.add(stack);
        }
    }

    private static @Nullable RecipeManager recipeManager() {
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft.level == null ? null : minecraft.level.getRecipeManager();
    }

    /**
     * How much of an order no maid has taken on yet.
     *
     * <p>This is the difference between "how much did I ask for" and "how much still needs materials".
     * A cook request carries {@code remain}, the number of servings its maid has yet to produce, and
     * those servings' ingredients have already been taken out of storage and loaded. Counting the whole
     * order again made every already-accepted serving look short, which is what printed the same line
     * once per serving - "missing stuffed dough food x7" for a single order a maid was already cooking.
     *
     * <p>The request handler is synced to the client, so this reads real state rather than a guess; a
     * cooking maid at the counter is what the player can see happening.
     */
    private static int uncovered(OrderView order) {
        return Math.max(0, order.count() - alreadyQueued(order.data().ID));
    }

    /** Servings of that recipe that maids nearby have accepted and not yet finished. */
    private static int alreadyQueued(ResourceLocation recipeId) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null) return 0;

        int queued = 0;
        AABB around = minecraft.player.getBoundingBox().inflate(MAID_RANGE);
        for (Entity entity : minecraft.level.getEntitiesOfClass(Entity.class, around)) {
            if (!(entity instanceof EntityMaid maid)) continue;
            if (!(maid.getTask() instanceof TaskCook)) continue;

            for (CookRequest request : maid.getData(CookRequestHandler.TYPE).toList()) {
                // Only the recipe id and the outstanding count are needed, and both survive the sync.
                if (recipeId.equals(request.id) && request.remain > 0) queued += request.remain;
            }
        }

        return queued;
    }

    /** Read-only view of one pending order, so this class does not depend on the screen type. */
    public interface OrderView {
        RecipeData data();

        int count();
    }
}
