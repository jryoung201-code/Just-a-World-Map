package dev.jryoung.worldmap;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;

final class MapData {
    enum TagType { ICON, BLOCK, TEXT }
    record Waypoint(int x, int y, int z, String name, TagType tagType) {}

    private static MapData current;
    private final long seed;
    private final String dimension;
    private final Path file;
    private final Set<Long> discovered = new LinkedHashSet<>();
    private final Set<Waypoint> waypoints = new LinkedHashSet<>();
    private TagType nextTagType = TagType.ICON;

    private MapData(long seed, String dimension) {
        this.seed = seed;
        this.dimension = dimension;
        String worldId = Long.toUnsignedString(seed, 36) + "_" + dimension.replaceAll("[^a-zA-Z0-9_-]", "_");
        this.file = net.fabricmc.loader.api.FabricLoader.getInstance().getConfigDir()
                .resolve("just-a-world-map").resolve(worldId + ".txt");
        load();
    }

    static MapData forWorld(ClientWorld world) {
        long seed = world.getSeed();
        String dimension = world.getRegistryKey().getValue().toString();
        if (current == null || current.seed != seed || !current.dimension.equals(dimension)) {
            current = new MapData(seed, dimension);
        }
        return current;
    }

    long seed() { return seed; }
    String dimension() { return dimension; }
    Set<Waypoint> waypoints() { return Set.copyOf(waypoints); }
    boolean discovered(int x, int z) { return discovered.contains(pack(x, z)); }

    void markDiscovered(int x, int z) {
        if (discovered.add(pack(x, z))) save();
    }

    void cycleTagType() {
        nextTagType = TagType.values()[(nextTagType.ordinal() + 1) % TagType.values().length];
    }

    String nextWaypointName() { return "Waypoint " + (waypoints.size() + 1); }

    void addWaypoint(int x, int y, int z, String name) {
        waypoints.add(new Waypoint(x, y, z, name, nextTagType));
        save();
    }

    private static long pack(int x, int z) {
        return ((long) x << 32) ^ (z & 0xffffffffL);
    }

    private void load() {
        if (!Files.exists(file)) return;
        try {
            for (String line : Files.readAllLines(file)) {
                String[] parts = line.split("\\|", -1);
                if (parts.length == 2 && parts[0].equals("C")) {
                    discovered.add(Long.parseLong(parts[1]));
                } else if (parts.length == 7 && parts[0].equals("W")) {
                    waypoints.add(new Waypoint(Integer.parseInt(parts[1]), Integer.parseInt(parts[2]),
                            Integer.parseInt(parts[3]), parts[4].replace("%7C", "|"),
                            TagType.valueOf(parts[5])));
                }
            }
        } catch (IOException | IllegalArgumentException ignored) {
            // Keep the rest of the map usable if one saved row is damaged.
        }
    }

    private void save() {
        try {
            Files.createDirectories(file.getParent());
            var lines = new java.util.ArrayList<String>();
            for (long chunk : discovered) lines.add("C|" + chunk);
            for (Waypoint point : waypoints) {
                lines.add("W|" + point.x + "|" + point.y + "|" + point.z + "|"
                        + point.name.replace("|", "%7C") + "|" + point.tagType + "|");
            }
            Files.write(file, lines);
        } catch (IOException ignored) {
            // Map remains usable for this session if local storage is unavailable.
        }
    }
}
