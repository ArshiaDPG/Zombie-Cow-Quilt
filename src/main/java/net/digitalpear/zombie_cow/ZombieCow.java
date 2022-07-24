package net.digitalpear.zombie_cow;

import net.digitalpear.zombie_cow.entity.ZombieCowEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.BiomeKeys;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.worldgen.biome.api.BiomeModifications;
import org.quiltmc.qsl.worldgen.biome.api.BiomeSelectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZombieCow implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("Zombie Cow");
	public static String MOD_ID = "zombie_cow";

	//Register cow entity
	public static final EntityType<ZombieCowEntity> ZOMBIE_COW = Registry.register(
			Registry.ENTITY_TYPE, new Identifier(MOD_ID, "zombie_cow"), FabricEntityTypeBuilder
					.create(SpawnGroup.CREATURE, ZombieCowEntity::new)
					.dimensions(EntityDimensions.fixed(0.9F, 1.4F)).build());

	//Register Spawn egg
	public static final Item ZOMBIE_COW_SPAWN_EGG = Registry.register(Registry.ITEM, new Identifier(MOD_ID, "zombie_cow_spawn_egg"), new SpawnEggItem(ZOMBIE_COW, 4470310, 7969893, new Item.Settings().group(ItemGroup.MISC)));

	@Override
	public void onInitialize(ModContainer mod) {

		FabricDefaultAttributeRegistry.register(ZOMBIE_COW, ZombieCowEntity.createZombieCowAttributes());


		BiomeModifications.addSpawn(BiomeSelectors.includeByKey(BiomeKeys.SOUL_SAND_VALLEY), SpawnGroup.MONSTER, ZOMBIE_COW, 4, 1, 2);

		LOGGER.info("Hello Quilt world from {}!", mod.metadata().name());
	}
}
