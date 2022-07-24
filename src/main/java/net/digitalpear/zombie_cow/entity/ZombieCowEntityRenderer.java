package net.digitalpear.zombie_cow.entity;

import net.digitalpear.zombie_cow.ZombieCow;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.EntityRendererFactory.Context;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class ZombieCowEntityRenderer extends MobEntityRenderer<ZombieCowEntity, ZombieCowEntityModel<ZombieCowEntity>> {
    private static final Identifier TEXTURE = new Identifier(ZombieCow.MOD_ID,"textures/entity/zombie_cow.png");
    private static final Identifier TEXTURE_SHEARED = new Identifier(ZombieCow.MOD_ID,"textures/entity/zombie_cow_sheared.png");

	public ZombieCowEntityRenderer(Context context) {
		super(context, new ZombieCowEntityModel(context.getPart(EntityModelLayers.COW)), 0.7F);
	}




    @Override
    public Identifier getTexture(ZombieCowEntity entity) {
        return entity.hasSkin() ? TEXTURE : TEXTURE_SHEARED;
    }

}
