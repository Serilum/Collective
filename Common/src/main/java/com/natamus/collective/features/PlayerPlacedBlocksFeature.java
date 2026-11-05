package com.natamus.collective.features;

import com.natamus.collective.data.Constants;
import com.natamus.collective.functions.WorldFunctions;
import com.natamus.collective.objects.PlayerPlacedRegion;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.function.Predicate;

public class PlayerPlacedBlocksFeature {
	private static final int saveIntervalTicks = 600;
	private static final int unusedRegionTicks = 6000;

	private static final Set<Block> trackedBlocks = new HashSet<>();
	private static final List<TagKey<Block>> trackedBlockTags = new ArrayList<>();
	private static final List<Predicate<BlockState>> trackedBlockPredicates = new ArrayList<>();

	private static final WeakHashMap<Level, Map<String, PlayerPlacedRegion>> loadedRegions = new WeakHashMap<>();
	private static final WeakHashMap<Level, Set<Long>> brokenBlockPositions = new WeakHashMap<>();

	public static void trackBlock(Block block) {
		trackedBlocks.add(block);
	}

	public static void trackBlocks(Collection<Block> blocks) {
		trackedBlocks.addAll(blocks);
	}

	public static void trackBlockTag(TagKey<Block> blockTag) {
		trackedBlockTags.add(blockTag);
	}

	public static void trackBlocksMatching(Predicate<BlockState> blockPredicate) {
		trackedBlockPredicates.add(blockPredicate);
	}

	public static boolean isTrackingBlocks() {
		return !trackedBlocks.isEmpty() || !trackedBlockTags.isEmpty() || !trackedBlockPredicates.isEmpty();
	}

	public static boolean isTrackedBlock(BlockState blockState) {
		if (trackedBlocks.contains(blockState.getBlock())) {
			return true;
		}

		for (TagKey<Block> blockTag : trackedBlockTags) {
			if (blockState.is(blockTag)) {
				return true;
			}
		}

		for (Predicate<BlockState> blockPredicate : trackedBlockPredicates) {
			if (blockPredicate.test(blockState)) {
				return true;
			}
		}

		return false;
	}

	public static boolean isPlacedByPlayer(Level level, BlockPos pos) {
		if (!(level instanceof ServerLevel serverLevel) || !isTrackingBlocks()) {
			return false;
		}

		forgetIfNoLongerTracked(serverLevel, pos);
		return getRegion(serverLevel, pos).positions.contains(pos.asLong());
	}

	public static void onBlockPlaced(Level level, BlockPos pos, Player player) {
		if (!(level instanceof ServerLevel serverLevel) || player == null || !isTrackingBlocks()) {
			return;
		}

		if (!isTrackedBlock(level.getBlockState(pos))) {
			return;
		}

		PlayerPlacedRegion region = getRegion(serverLevel, pos);
		if (region.positions.add(pos.asLong())) {
			region.hasUnsavedChanges = true;
		}
	}

	public static void onBlockBroken(Level level, BlockPos pos) {
		if (!(level instanceof ServerLevel) || !isTrackingBlocks()) {
			return;
		}

		brokenBlockPositions.computeIfAbsent(level, k -> new HashSet<>()).add(pos.asLong());
	}

	public static void onWorldTick(ServerLevel serverLevel) {
		forgetBrokenBlocks(serverLevel);

		if (serverLevel.getGameTime() % saveIntervalTicks == 0) {
			saveChangedRegions(serverLevel);
			unloadUnusedRegions(serverLevel);
		}
	}

	public static void onWorldUnload(Level level) {
		if (level instanceof ServerLevel serverLevel) {
			forgetBrokenBlocks(serverLevel);
			saveChangedRegions(serverLevel);
			loadedRegions.remove(serverLevel);
		}
	}

	private static void forgetIfNoLongerTracked(ServerLevel serverLevel, BlockPos pos) {
		PlayerPlacedRegion region = getRegion(serverLevel, pos);
		if (!region.positions.contains(pos.asLong())) {
			return;
		}

		if (!isTrackedBlock(serverLevel.getBlockState(pos))) {
			region.positions.remove(pos.asLong());
			region.hasUnsavedChanges = true;
		}
	}

	private static void forgetBrokenBlocks(ServerLevel serverLevel) {
		Set<Long> brokenPositions = brokenBlockPositions.remove(serverLevel);
		if (brokenPositions == null) {
			return;
		}

		for (Long position : brokenPositions) {
			forgetIfNoLongerTracked(serverLevel, BlockPos.of(position));
		}
	}

	private static PlayerPlacedRegion getRegion(ServerLevel serverLevel, BlockPos pos) {
		Map<String, PlayerPlacedRegion> regions = loadedRegions.computeIfAbsent(serverLevel, k -> new HashMap<>());
		String regionName = getRegionName(pos);

		PlayerPlacedRegion region = regions.get(regionName);
		if (region == null) {
			region = loadRegion(serverLevel, regionName);
			regions.put(regionName, region);
		}

		region.lastUsedGameTime = serverLevel.getGameTime();
		return region;
	}

	private static String getRegionName(BlockPos pos) {
		int regionX = pos.getX() >> 9;
		int regionZ = pos.getZ() >> 9;
		return "r." + regionX + "." + regionZ;
	}

	private static PlayerPlacedRegion loadRegion(ServerLevel serverLevel, String regionName) {
		PlayerPlacedRegion region = new PlayerPlacedRegion();
		File file = getRegionFile(serverLevel, regionName);
		if (!file.isFile()) {
			return region;
		}

		try {
			for (String line : Files.readAllLines(file.toPath(), StandardCharsets.UTF_8)) {
				addPosition(region, line);
			}
		}
		catch (IOException ignored) {
			Constants.LOG.warn("Unable to read the player placed blocks in " + file.getPath());
		}

		return region;
	}

	private static void addPosition(PlayerPlacedRegion region, String line) {
		try {
			region.positions.add(Long.parseLong(line.strip()));
		}
		catch (NumberFormatException ignored) { }
	}

	private static void saveChangedRegions(ServerLevel serverLevel) {
		Map<String, PlayerPlacedRegion> regions = loadedRegions.get(serverLevel);
		if (regions == null) {
			return;
		}

		for (Map.Entry<String, PlayerPlacedRegion> entry : regions.entrySet()) {
			PlayerPlacedRegion region = entry.getValue();
			if (region.hasUnsavedChanges) {
				saveRegion(serverLevel, entry.getKey(), region);
			}
		}
	}

	private static void saveRegion(ServerLevel serverLevel, String regionName, PlayerPlacedRegion region) {
		File file = getRegionFile(serverLevel, regionName);
		region.hasUnsavedChanges = false;

		if (region.positions.isEmpty()) {
			boolean ignored = file.delete();
			return;
		}

		File folder = file.getParentFile();
		if (!folder.isDirectory() && !folder.mkdirs()) {
			return;
		}

		try {
			PrintWriter writer = new PrintWriter(file, StandardCharsets.UTF_8);
			for (Long position : region.positions) {
				writer.println(position);
			}
			writer.close();
		}
		catch (IOException ignored) {
			Constants.LOG.warn("Unable to save the player placed blocks in " + file.getPath());
		}
	}

	private static void unloadUnusedRegions(ServerLevel serverLevel) {
		Map<String, PlayerPlacedRegion> regions = loadedRegions.get(serverLevel);
		if (regions == null) {
			return;
		}

		long gameTime = serverLevel.getGameTime();
		regions.values().removeIf(region -> !region.hasUnsavedChanges && gameTime - region.lastUsedGameTime > unusedRegionTicks);
	}

	private static File getRegionFile(ServerLevel serverLevel, String regionName) {
		String dimensionFolder = WorldFunctions.getWorldDimensionName(serverLevel).replace(":", "_");
		return new File(WorldFunctions.getWorldPath(serverLevel) + File.separator + "data" + File.separator + "collective" + File.separator + "placed" + File.separator + dimensionFolder + File.separator + regionName + ".txt");
	}
}
