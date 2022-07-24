package net.digitalpear.zombie_cow.client;

import net.digitalpear.zombie_cow.ZombieCow;
import net.digitalpear.zombie_cow.entity.ZombieCowEntityRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer;

@Environment(EnvType.CLIENT)
public class ZombieCowClient implements ClientModInitializer {

	@Override
	public void onInitializeClient(ModContainer mod) {
		EntityRendererRegistry.register(ZombieCow.ZOMBIE_COW, ZombieCowEntityRenderer::new);
	}
}
