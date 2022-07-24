package net.digitalpear.zombie_cow.entity;

import net.digitalpear.zombie_cow.ZombieCow;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.recipe.Ingredient;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class ZombieCowEntity extends AnimalEntity {


    private static final TrackedData<Boolean> HAS_SKIN;
    private static final TrackedData<Boolean> CONVERTING;
    private int conversionTimer;
    private UUID converter;


    public ZombieCowEntity(EntityType<? extends ZombieCowEntity> entityType, World world) {
        super(entityType, world);
    }


    protected void initGoals() {
        this.goalSelector.add(0, new SwimGoal(this));
        this.goalSelector.add(1, new EscapeDangerGoal(this, 2.0D));
        this.goalSelector.add(2, new AnimalMateGoal(this, 1.0D));
        this.goalSelector.add(3, new TemptGoal(this, 1.25D, Ingredient.ofItems(Items.ROTTEN_FLESH), false));
        this.goalSelector.add(4, new FollowParentGoal(this, 1.25D));
        this.goalSelector.add(5, new WanderAroundFarGoal(this, 1.0D));
        this.goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 6.0F));
        this.goalSelector.add(7, new LookAroundGoal(this));
    }
    public EntityGroup getGroup() {
        return EntityGroup.UNDEAD;
    }
    public static DefaultAttributeContainer.Builder createZombieCowAttributes() {
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 10.0D)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.20000000298023224D);
    }

    protected SoundEvent getAmbientSound() {
        return SoundEvents.ENTITY_COW_AMBIENT;
    }

    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_COW_HURT;
    }

    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_COW_DEATH;
    }

    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.ENTITY_COW_STEP, 0.15F, 1.0F);
    }

    protected float getSoundVolume() {
        return 0.4F;
    }

    public void tick() {
        if (!this.world.isClient && this.isAlive() && this.isConverting()) {
            int i = this.getConversionRate();
            this.conversionTimer -= i;
            if (this.conversionTimer <= 0) {
                this.finishConversion(this.world);
            }
        }

        super.tick();
    }

    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(HAS_SKIN, true);
        this.dataTracker.startTracking(CONVERTING, false);
    }

    public boolean hasSkin() {
        return this.dataTracker.get(HAS_SKIN);
    }
    public boolean canBeSheared(){
        return this.hasSkin() && !this.isBaby();
    }

    public void setSkin(){
        this.dataTracker.set(HAS_SKIN, !this.hasSkin());
    }


    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        ItemStack itemStack = player.getStackInHand(hand);
        if (itemStack.isOf(Items.BUCKET) && !this.isBaby()) {
            player.playSound(SoundEvents.ENTITY_COW_MILK, 1.0F, 1.0F);
            ItemStack itemStack2 = ItemUsage.exchangeStack(itemStack, player, Items.MILK_BUCKET.getDefaultStack());
            player.setStackInHand(hand, itemStack2);
            return ActionResult.success(this.world.isClient);
        } else if (itemStack.isOf(Items.SHEARS) && this.canBeSheared()) {
            if (!this.world.isClient) {
                this.damage(DamageSource.GENERIC, 1);
                this.setSkin();
                this.world.playSoundFromEntity(null, this, SoundEvents.ENTITY_SHEEP_SHEAR, SoundCategory.PLAYERS, 1.0F, 1.0F);
                this.emitGameEvent(GameEvent.SHEAR, player);
                dropLeather();
                itemStack.damage(1, player, (playerx) -> playerx.sendToolBreakStatus(hand));
                return ActionResult.SUCCESS;
            }
        }else if (itemStack.isOf(Items.GOLDEN_APPLE)) {
            if (this.hasStatusEffect(StatusEffects.WEAKNESS)) {
                this.world.playSoundFromEntity(null, this, SoundEvents.ENTITY_CAT_EAT, SoundCategory.PLAYERS, 1.0F, 1.0F);
                if (!player.getAbilities().creativeMode) {
                    itemStack.decrement(1);
                }

                if (!this.world.isClient) {
                    this.setConverting(player.getUuid(), this.random.nextInt(2401) + 3600);
                }

                return ActionResult.SUCCESS;
            } else {
                return ActionResult.CONSUME;
            }
        }
            else if (itemStack.isOf(Items.ROTTEN_FLESH) && !this.hasSkin()) {
                if (!this.world.isClient) {
                    this.world.playSoundFromEntity(null, this, SoundEvents.ENTITY_CAT_EAT, SoundCategory.PLAYERS, 1.0F, 1.0F);
                    this.setSkin();
                    itemStack.decrement(1);
                    return ActionResult.SUCCESS;
                }
                else {
                    return ActionResult.CONSUME;
                }
            }
        return super.interactMob(player, hand);
    }


    public void dropLeather(){
        int i = 1 + this.random.nextInt(3);
        for(int j = 0; j < i; ++j) {
            ItemEntity itemEntity = this.dropItem(Items.LEATHER, 1);
            if (itemEntity != null) {
                itemEntity.setVelocity(itemEntity.getVelocity().add((this.random.nextFloat() - this.random.nextFloat()) * 0.1F, this.random.nextFloat() * 0.05F, (this.random.nextFloat() - this.random.nextFloat()) * 0.1F));
            }
        }
    }


    public ZombieCowEntity createChild(ServerWorld serverWorld, PassiveEntity passiveEntity) {
        return ZombieCow.ZOMBIE_COW.create(serverWorld);
    }

    protected float getActiveEyeHeight(EntityPose pose, EntityDimensions dimensions) {
        return this.isBaby() ? dimensions.height * 0.95F : 1.3F;
    }
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);

        nbt.putInt("ConversionTime", this.isConverting() ? this.conversionTimer : -1);
        if (this.converter != null) {
            nbt.putUuid("ConversionPlayer", this.converter);
        }
    }

    //Conversion to cow
    private void setConverting(@Nullable UUID uuid, int delay) {
        this.converter = uuid;
        this.conversionTimer = delay;
        this.getDataTracker().set(CONVERTING, true);
        this.removeStatusEffect(StatusEffects.WEAKNESS);
        this.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, delay, Math.min(this.world.getDifficulty().getId() - 1, 0)));
        this.world.sendEntityStatus(this, (byte)16);
    }
    public boolean isConverting() {
        return this.getDataTracker().get(CONVERTING);
    }
    public boolean canImmediatelyDespawn(double distanceSquared) {
        return !this.isConverting();
    }

    private void finishConversion(World world) {
        CowEntity cowEntity = this.convertTo(EntityType.COW, false);
        cowEntity.refreshPositionAndAngles(this.getX(), this.getY(), this.getZ(), this.getYaw(), this.getPitch());
        cowEntity.setHealth(this.getHealth());
        cowEntity.bodyYaw = this.bodyYaw;
        if (this.hasCustomName()) {
            cowEntity.setCustomName(this.getCustomName());
            cowEntity.setCustomNameVisible(this.isCustomNameVisible());
        }

        if (this.isPersistent()) {
            cowEntity.setPersistent();
        }

        cowEntity.setInvulnerable(this.isInvulnerable());
    }
    private int getConversionRate() {
        int i = 1;
        if (this.random.nextFloat() < 0.01F) {
            int j = 0;
            BlockPos.Mutable mutable = new BlockPos.Mutable();

            for(int k = (int)this.getX() - 4; k < (int)this.getX() + 4 && j < 14; ++k) {
                for(int l = (int)this.getY() - 4; l < (int)this.getY() + 4 && j < 14; ++l) {
                    for(int m = (int)this.getZ() - 4; m < (int)this.getZ() + 4 && j < 14; ++m) {
                        BlockState blockState = this.world.getBlockState(mutable.set(k, l, m));
                        if (blockState.isIn(BlockTags.CROPS) || blockState.isIn(BlockTags.DIRT) || blockState.isOf(Blocks.HAY_BLOCK)) {
                            if (this.random.nextFloat() < 0.3F) {
                                ++i;
                            }

                            ++j;
                        }
                    }
                }
            }
        }

        return i;
    }
    static {
        HAS_SKIN = DataTracker.registerData(ZombieCowEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
        CONVERTING = DataTracker.registerData(ZombieCowEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    }

}
