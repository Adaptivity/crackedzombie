//  
//  =====GPL=============================================================
//  This program is free software; you can redistribute it and/or modify
//  it under the terms of the GNU General Public License as published by
//  the Free Software Foundation; version 2 dated June, 1991.
// 
//  This program is distributed in the hope that it will be useful, 
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU General Public License for more details.
// 
//  You should have received a copy of the GNU General Public License
//  along with this program;  if not, write to the Free Software
//  Foundation, Inc., 675 Mass Ave., Cambridge, MA 02139, USA.
//  =====================================================================
//
package com.walkingdead.common;

import cpw.mods.fml.common.FMLCommonHandler;
import java.util.LinkedList;

import net.minecraft.entity.EnumCreatureType;
import net.minecraftforge.common.ChestGenHooks;
import net.minecraftforge.common.DungeonHooks;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.item.ItemStack;
import net.minecraft.util.WeightedRandomChestContent;
import net.minecraft.world.biome.BiomeGenBase;
//import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.EntityRegistry;
import net.minecraft.init.Items;
import net.minecraftforge.common.BiomeDictionary.Type;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.config.Configuration;
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;

@Mod(
		modid = WalkingDead.modid,
		name = WalkingDead.name,
		version = WalkingDead.version
)

public class WalkingDead {

	public String getVersion()
	{
		return WalkingDead.version;
	}

	@Instance
	public static WalkingDead instance;

	public static final String version = "1.7.2";
	public static final String modid = "walkingdeadmod";
	public static final String name = "WalkingDead Mod";

	private int walkerSpawnProb;
	private int walkerSpawns;
	private boolean spawnCreepers;
	private boolean spawnZombies;
	private boolean spawnSkeletons;
	private boolean spawnEnderman;
	private boolean spawnSpiders;
	private boolean spawnSlime;
	private boolean randomSkins;
	private boolean doorBusting;

//	private static final Logger logger = LogManager.getLogger(WalkingDead.modid);
	@SidedProxy(
			clientSide = "com.walkingdead.client.ClientProxyWalkingDead",
			serverSide = "com.walkingdead.common.CommonProxyWalkingDead"
	)

	public static CommonProxyWalkingDead proxy;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
//		logger.setParent(FMLLog.getLogger());

		String generalComments = WalkingDead.name + " Config\nMichael Sheppard (crackedEgg)\n"
				+ " For Minecraft Version 1.7.2\n";
		String spawnProbComment = "walkerSpawnProb adjust to probability of walkers spawning,\n"
				+ "although the custom spawning most likely overrides this. the higher the\n"
				+ "the number the more likely walkers will spawn.";
		String walkerComment = "walkerSpawns adjusts the number of walkers spawned, play\n"
				+ "with it to see what you like. The higher the number the more\n"
				+ "walkers will spawn.";
		String creeperComment = "creeperSpawns, set to false to disable creeper spawning, set to true\n"
				+ "if you want to spawn creepers";
		String skeletonComment = "skeletonSpawns, set to false to disable skeleton spawning, set to true\n"
				+ "if you want to spawn skeletons";
		String zombieComment = "zombieSpawns, set to false to disable zombie spawning, set to true\n"
				+ "if you want to spawn zombies. Note that spawning zombies and other monsters\n"
				+ "will cause the number of walkers spawned to be reduced.";
		String endermanComment = "endermanSpawns, set to false to disable enderman spawning, set to true\n"
				+ "if you want to spawn enderman";
		String spiderComment = "spiderSpawns, set to false to disable spider spawning, set to true\n"
				+ "if you want to spawn spiders";
		String slimeComment = "slimeSpawns, set to false to disable slime spawning, set to true\n"
				+ "if you want to spawn slimes";
		String doorBustingComment = "doorBusting, set to true to have walkers try to break down doors,\n"
				+ "otherwise set to false. It's quieter.";

		Configuration config = new Configuration(event.getSuggestedConfigurationFile());
		config.load();

		walkerSpawnProb = config.get(Configuration.CATEGORY_GENERAL, "walkerSpawnProb", 10, spawnProbComment).getInt();
		walkerSpawns = config.get(Configuration.CATEGORY_GENERAL, "walkerSpawns", 60, walkerComment).getInt();
		spawnCreepers = config.get(Configuration.CATEGORY_GENERAL, "spawnCreepers", false, creeperComment).getBoolean(false);
		spawnSkeletons = config.get(Configuration.CATEGORY_GENERAL, "spawnSkeletons", false, skeletonComment).getBoolean(false);
		spawnZombies = config.get(Configuration.CATEGORY_GENERAL, "spawnZombies", false, zombieComment).getBoolean(false);
		spawnEnderman = config.get(Configuration.CATEGORY_GENERAL, "spawnEnderman", false, endermanComment).getBoolean(false);
		spawnSpiders = config.get(Configuration.CATEGORY_GENERAL, "spawnSpiders", true, spiderComment).getBoolean(false);
		spawnSlime = config.get(Configuration.CATEGORY_GENERAL, "spawnSlime", false, slimeComment).getBoolean(false);
		doorBusting = config.get(Configuration.CATEGORY_GENERAL, "doorBusting", false, doorBustingComment).getBoolean(false);

		config.addCustomCategoryComment(Configuration.CATEGORY_GENERAL, generalComments);

		config.save();

		int id = EntityRegistry.findGlobalUniqueEntityId();
		EntityRegistry.registerGlobalEntityID(EntityWalkingDead.class, "WalkingDead", id, 0x00AFAF, 0x799C45);

		proxy.registerRenderers();
	}

	@EventHandler
	public void Init(FMLInitializationEvent evt)
	{
		// placing this function here should allow the walkers to spawn in biomes
		// created by other mods provided those mods are loaded before this one.
//		logger.info("*** Scanning for available biomes");
		BiomeGenBase[] biomes = getBiomeList();

		EntityRegistry.addSpawn(EntityWalkingDead.class, walkerSpawnProb, 2, 10, EnumCreatureType.monster, biomes);

		// walkers should spawn in dungeon spawners
		DungeonHooks.addDungeonMob("WalkingDead", 200);
		// add steel swords to the loot. you may need these.
		ChestGenHooks.addItem(ChestGenHooks.DUNGEON_CHEST, new WeightedRandomChestContent(new ItemStack(Items.iron_sword), 1, 1, 4));

		// optionally remove creeper, skeleton, and zombie spawns for these biomes
		if (!spawnCreepers) {
			EntityRegistry.removeSpawn(EntityCreeper.class, EnumCreatureType.monster, biomes);
//			logger.info("*** Removing creeper spawns");
		}
		if (!spawnSkeletons) {
			EntityRegistry.removeSpawn(EntitySkeleton.class, EnumCreatureType.monster, biomes);
			DungeonHooks.removeDungeonMob("Skeleton");
//			logger.info("*** Removing skeleton spawns and dungeon spawners");
		}
		if (!spawnZombies) {
			EntityRegistry.removeSpawn(EntityZombie.class, EnumCreatureType.monster, biomes);
			DungeonHooks.removeDungeonMob("Zombie");
//			logger.info("*** Removing zombie spawns and dungeon spawners");
		}
		if (!spawnEnderman) {
			EntityRegistry.removeSpawn(EntityEnderman.class, EnumCreatureType.monster, biomes);
//			logger.info("*** Removing enderman spawns");
		}
		if (!spawnSpiders) {
			EntityRegistry.removeSpawn(EntitySpider.class, EnumCreatureType.monster, biomes);
			DungeonHooks.removeDungeonMob("Spider");
//			logger.info("*** Removing spider spawns and dungeon spawners");
		}
		if (!spawnSlime) {
			EntityRegistry.removeSpawn(EntitySlime.class, EnumCreatureType.monster, biomes);
//			logger.info("*** Removing slime spawns");
		}

		FMLCommonHandler.instance().bus().register(new WorldTickHandler());
	}

//    @EventHandler
//	public void PostInit(FMLPostInitializationEvent event) {
//		BiomeDictionary.registerAllBiomes();
//	}
	// This function should get all biomes that are derived from BiomeGenBase,
	// even those from other mods.
	public BiomeGenBase[] getBiomeList()
	{
		LinkedList linkedlist = new LinkedList<BiomeGenBase>();
		Type[] t = {Type.FOREST, Type.PLAINS, Type.MOUNTAIN, Type.HILLS, Type.SWAMP, Type.MAGICAL,
			Type.DESERT, Type.FROZEN, Type.JUNGLE, Type.WASTELAND, Type.BEACH, Type.MUSHROOM};

		for (Type type : t) {
			BiomeGenBase[] biomes = BiomeDictionary.getBiomesForType(type);
			for (BiomeGenBase bgb : biomes) {
				if (!linkedlist.contains(bgb)) {
//                    String s = " >>> Adding " + bgb.biomeName + " for spawning";
//                    logger.info(s);
					linkedlist.add(bgb);
				}
			}
		}
		return (BiomeGenBase[]) linkedlist.toArray(new BiomeGenBase[0]);
	}

	public int getWalkerSpawns()
	{
		return walkerSpawns;
	}

	public boolean getRandomSkins()
	{
		return randomSkins;
	}

	public boolean getDoorBusting()
	{
		return doorBusting;
	}

}
