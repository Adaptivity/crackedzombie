package walkingdead.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import net.minecraft.src.BiomeGenBase;
import net.minecraft.src.Block;
import net.minecraft.src.Chunk;
import net.minecraft.src.ChunkCoordIntPair;
import net.minecraft.src.ChunkCoordinates;
import net.minecraft.src.ChunkPosition;
import net.minecraft.src.EntityLiving;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.EntitySkeleton;
import net.minecraft.src.EntitySpider;
import net.minecraft.src.EntityZombie;
import net.minecraft.src.EnumCreatureType;
import net.minecraft.src.Material;
import net.minecraft.src.MathHelper;
import net.minecraft.src.SpawnListEntry;
import net.minecraft.src.WeightedRandom;
import net.minecraft.src.World;
import net.minecraft.src.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingSpecialSpawnEvent;

public final class SpawnerWalkingDead {
	private static HashMap eligibleChunksForSpawning = new HashMap();

	protected static ChunkPosition getRandomSpawningPointInChunk(World world, int x, int z) {
		Chunk chunk = world.getChunkFromChunkCoords(x, z);
		int newX = x * 16 + world.rand.nextInt(16);
		int newY = world.rand.nextInt(chunk == null ? world.getActualHeight() : chunk.getTopFilledSegment() + 16 - 1);
		int newZ = z * 16 + world.rand.nextInt(16);
		
		return new ChunkPosition(newX, newY, newZ);
	}
	
	public static final int findChunksForSpawning(WorldServer worldServer) {
		eligibleChunksForSpawning.clear();

        for (int entities = 0; entities < worldServer.playerEntities.size(); ++entities) {
            EntityPlayer entityPlayer = (EntityPlayer)worldServer.playerEntities.get(entities);
            int playerX = MathHelper.floor_double(entityPlayer.posX / 16.0D);
            int playerZ = MathHelper.floor_double(entityPlayer.posZ / 16.0D);
            byte range = 8;

            for (int x = -range; x <= range; ++x) {
                for (int z = -range; z <= range; ++z) {
                    boolean inRange = x == -range || x == range || z == -range || z == range;
                    ChunkCoordIntPair chunkCoord = new ChunkCoordIntPair(x + playerX, z + playerZ);

                    if (!inRange)  {
                        eligibleChunksForSpawning.put(chunkCoord, Boolean.valueOf(false));
                    } else if (!eligibleChunksForSpawning.containsKey(chunkCoord)) {
                        eligibleChunksForSpawning.put(chunkCoord, Boolean.valueOf(true));
                    }
                }
            }
        }
        
        int eligibleChunks = 0;
        ChunkCoordinates spawnPoint = worldServer.getSpawnPoint();
        EnumCreatureType creatureType = EnumCreatureType.monster;

//        if (worldServer.countEntities(creatureType.getCreatureClass()) <= creatureType.getMaxNumberOfCreature() * eligibleChunksForSpawning.size() / 256) {
            Iterator iter = eligibleChunksForSpawning.keySet().iterator();
            ArrayList<ChunkCoordIntPair> tmp = new ArrayList(eligibleChunksForSpawning.keySet());
            Collections.shuffle(tmp);
            iter = tmp.iterator();
            
//            label110:
            	
            while (iter.hasNext()) { // iterate through the eligible chunks
                ChunkCoordIntPair chunkPair = (ChunkCoordIntPair)iter.next();
                int nCreaturesSpawnable = worldServer.countEntities(creatureType.getCreatureClass());
                if (nCreaturesSpawnable > creatureType.getMaxNumberOfCreature() + 10) {
                	continue;
                }
                
//                BiomeGenBase biome = worldServer.getBiomeGenForCoords(chunkPair.chunkXPos, chunkPair.chunkZPos);
//                List list = biome.getSpawnableList(EnumCreatureType.monster);
//                if (!list.isEmpty()) {
//                	SpawnListEntry spawn = (SpawnListEntry)WeightedRandom.getRandomItem(worldServer.rand, list);
//                    int numspawns = spawn.minGroupCount + worldServer.rand.nextInt(1 + spawn.maxGroupCount - spawn.minGroupCount);
//                }

                if (!((Boolean)eligibleChunksForSpawning.get(chunkPair)).booleanValue()) {
                    ChunkPosition chunkPos = getRandomSpawningPointInChunk(worldServer, chunkPair.chunkXPos, chunkPair.chunkZPos);
//                    int chunkX = chunkPos.x;
//                    int chunkY = chunkPos.y;
//                    int chunkZ = chunkPos.z;

                    boolean normalBlock = worldServer.isBlockNormalCube(chunkPos.x, chunkPos.y, chunkPos.z);
                    Material material = worldServer.getBlockMaterial(chunkPos.x, chunkPos.y, chunkPos.z);
                    if (!normalBlock && material == creatureType.getCreatureMaterial()) {
                        int nSpawned = 0;
                        int outerLoop = 0;

                        while (outerLoop < 3) {
                            int newX = chunkPos.x;
                            int newY = chunkPos.y;
                            int newZ = chunkPos.z;
                            final byte chunkRange = 6;
                            int chunkRangeIterations = 0;

                            while (true) {
                                if (chunkRangeIterations < 4) {
                                    newX += worldServer.rand.nextInt(chunkRange) - worldServer.rand.nextInt(chunkRange);
                                    newY += worldServer.rand.nextInt(1) - worldServer.rand.nextInt(1);
                                    newZ += worldServer.rand.nextInt(chunkRange) - worldServer.rand.nextInt(chunkRange);

                                    if (canCreatureTypeSpawnAtLocation(creatureType, worldServer, newX, newY, newZ)) {
                                        float adjX = (float)newX + 0.5F;
                                        float adjY = (float)newY;
                                        float adjZ = (float)newZ + 0.5F;

                                        if (worldServer.getClosestPlayer((double)adjX, (double)adjY, (double)adjZ, 16.0D) == null) {
                                            float deltaX = adjX - (float)spawnPoint.posX;
                                            float deltaY = adjY - (float)spawnPoint.posY;
                                            float deltaZ = adjZ - (float)spawnPoint.posZ;
                                            float magnitude = deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ;

                                            if (magnitude >= 576.0F) {
                                                EntityWalkingDead walker = new EntityWalkingDead(worldServer);
                                                walker.setLocationAndAngles((double)adjX, (double)adjY, (double)adjZ, worldServer.rand.nextFloat() * 360.0F, 0.0F);

                                                if (walker.getCanSpawnHere() && nSpawned < walker.getMaxSpawnedInChunk()) {
                                                    ++nSpawned;
                                                    boolean spawned = worldServer.spawnEntityInWorld(walker);
                                                    walker.initCreature();
                                                    if (spawned) {
                                                    	System.out.println("Spawned a walker: " + adjX + ", " + adjY + ", " + adjZ + "(" + nSpawned + ")");
                                                    }

//                                                    if (nSpawned >= walker.getMaxSpawnedInChunk()) {
//                                                        continue label110;
//                                                    }
                                                }
                                                eligibleChunks += nSpawned;
                                            }
                                        }
                                    }
                                    ++chunkRangeIterations;
                                    continue;
                                }
                                ++outerLoop;
                                break;
                            }
                        }
                    }
                }
            }
//        }
//    }
        return eligibleChunks;
    }

	public static boolean canCreatureTypeSpawnAtLocation(EnumCreatureType creatureType, World world, int x, int y, int z) {
		if (!world.doesBlockHaveSolidTopSurface(x, y - 1, z)) {
			return false;
		} else {
			int blockID = world.getBlockId(x, y - 1, z);
			boolean spawnBlock = (Block.blocksList[blockID] != null && Block.blocksList[blockID].canCreatureSpawn(creatureType, world, x, y - 1, z));
			return spawnBlock && blockID != Block.bedrock.blockID && !world.isBlockNormalCube(x, y, z) && !world.getBlockMaterial(x, y, z).isLiquid() && !world.isBlockNormalCube(x, y + 1, z);
		}
	}

}
