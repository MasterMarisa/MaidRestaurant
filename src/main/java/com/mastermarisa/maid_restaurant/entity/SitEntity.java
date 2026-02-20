package com.mastermarisa.maid_restaurant.entity;

import com.github.tartaricacid.touhoulittlemaid.entity.item.EntitySit;
import com.mastermarisa.maid_restaurant.MaidRestaurant;
import com.mastermarisa.maid_restaurant.data.TagBlock;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.EntityType.Builder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SitEntity extends Entity {
    public static final EntityType<Entity> TYPE;
    private int passengerTick;

    public SitEntity(EntityType<?> entityTypeIn, Level worldIn) {
        super(entityTypeIn, worldIn);
        this.noPhysics = true;
        this.blocksBuilding = false;
        this.passengerTick = 0;
    }

    public SitEntity(Level worldIn, BlockPos pos) {
        this(TYPE, worldIn);
        this.noPhysics = true;
        this.blocksBuilding = false;
        this.setPos((double)pos.getX() + (double)0.5F, (double)pos.getY() + (double)0.4375F, (double)pos.getZ() + (double)0.5F);
    }

    public SitEntity(Level worldIn, BlockPos pos, double y) {
        this(TYPE, worldIn);
        this.noPhysics = true;
        this.blocksBuilding = false;
        this.setPos((double)pos.getX() + (double)0.5F, (double)pos.getY() + y, (double)pos.getZ() + (double)0.5F);
    }

    public Vec3 getPassengerRidingPosition(Entity entity) {
        return super.getPassengerRidingPosition(entity).add((double)0.0F, (double)-0.0625F, (double)0.0F);
    }

    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    protected void readAdditionalSaveData(CompoundTag tag) {
    }

    protected void addAdditionalSaveData(CompoundTag tag) {
    }

    public void tick() {
        if (!this.level().isClientSide) {
            this.checkBelowWorld();
            this.checkPassengers();
            if (this.tickCount % 20 == 0) {
                BlockState blockState = this.level().getBlockState(this.blockPosition());
                if (!blockState.is(TagBlock.SIT_BLOCK)) {
                    this.discard();
                }
            }
        }

    }

    private void checkPassengers() {
        if (this.getPassengers().isEmpty()) {
            ++this.passengerTick;
        } else {
            this.passengerTick = 0;
        }

        if (this.passengerTick > 10) {
            this.discard();
        }

    }

    public boolean skipAttackInteraction(Entity targetEntity) {
        return true;
    }

    public boolean hurt(DamageSource damageSource, float damageAmount) {
        return false;
    }

    public void move(MoverType moverType, Vec3 movement) {
    }

    public void push(Entity pushedEntity) {
    }

    public void push(double x, double y, double z) {
    }

    protected boolean repositionEntityAfterLoad() {
        return false;
    }

    public void thunderHit(ServerLevel serverLevel, LightningBolt lightningBolt) {
    }

    public void refreshDimensions() {
    }

    public boolean canCollideWith(Entity entity) {
        return false;
    }

    public boolean isInvisible() {
        return true;
    }

    public boolean isInvisibleTo(Player player) {
        return true;
    }

    public boolean shouldRenderAtSqrDistance(double distance) {
        return false;
    }

    public boolean shouldRender(double x, double y, double z) {
        return false;
    }

    public boolean displayFireAnimation() {
        return false;
    }

    static {
        TYPE = EntityType.Builder.of(SitEntity::new, MobCategory.MISC).sized(0.5F, 0.1F).clientTrackingRange(10).fireImmune().noSummon().build(MaidRestaurant.resourceLocation("sit_entity").toString());
    }
}
