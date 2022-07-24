package net.digitalpear.zombie_cow.mixin;


import net.digitalpear.zombie_cow.ZombieCow;
import net.digitalpear.zombie_cow.entity.ZombieCowEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

import java.util.UUID;

@Mixin(CowEntity.class)
public class CowEntityMixin extends AnimalEntity {
    private UUID lightningId;

    protected CowEntityMixin(EntityType<? extends CowEntity> entityType, World world) {
        super(entityType, world);
    }

    @Nullable
    @Override
    public CowEntity createChild(ServerWorld serverWorld, PassiveEntity passiveEntity) {
        return EntityType.COW.create(serverWorld);
    }

    public void onStruckByLightning(ServerWorld world, LightningEntity lightning) {
        UUID uUID = lightning.getUuid();
        if (!uUID.equals(this.lightningId)) {
            this.lightningId = uUID;
            this.playSound(SoundEvents.ENTITY_MOOSHROOM_CONVERT, 2.0F, 1.0F);
            convertToZombieCow();
        }

    }

    public void convertToZombieCow() {
        if (!this.world.isClient()) {
            ((ServerWorld) this.world).spawnParticles(ParticleTypes.EXPLOSION, this.getX(), this.getBodyY(0.5D), this.getZ(), 1, 0.0D, 0.0D, 0.0D, 0.0D);
            this.discard();
            ZombieCowEntity cowEntity = ZombieCow.ZOMBIE_COW.create(this.world);
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
            this.world.spawnEntity(cowEntity);
        }

    }
}
