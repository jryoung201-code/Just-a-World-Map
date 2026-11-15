package dev.jryoung.worldmap;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import net.minecraft.world.chunk.ChunkStatus;
import org.lwjgl.glfw.GLFW;

public final class JustAWorldMapClient implements ClientModInitializer {
    private static final KeyBinding.Category CATEGORY =
            KeyBinding.Category.create(Identifier.of("just_a_world_map"));
    private static KeyBinding openMap;
    private static KeyBinding addWaypoint;
    private static KeyBinding cycleTag;

    @Override
    public void onInitializeClient() {
        openMap = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.just_a_world_map.open_map", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_M, CATEGORY));
        addWaypoint = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.just_a_world_map.add_waypoint", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_N, CATEGORY));
        cycleTag = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.just_a_world_map.cycle_tag", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_B, CATEGORY));

        ClientTickEvents.END_CLIENT_TICK.register(client -> tick(client));
    }

    private static void tick(MinecraftClient client) {
        if (client.world == null || client.player == null) return;

        long seed = client.getServer() == null ? 0L : client.getServer().getOverworld().getSeed();
        MapData data = MapData.forWorld(client.world, seed);
        int centerX = client.player.getChunkPos().x;
        int centerZ = client.player.getChunkPos().z;
        int radius = client.options.getViewDistance().getValue();

        for (int x = centerX - radius; x <= centerX + radius; x++) {
            for (int z = centerZ - radius; z <= centerZ + radius; z++) {
                if (client.world.getChunkManager().getChunk(x, z, ChunkStatus.FULL, false) != null) {
                    data.markDiscovered(x, z);
                }
            }
        }

        while (openMap.wasPressed()) client.setScreen(new WorldMapScreen(data));
        while (cycleTag.wasPressed()) data.cycleTagType();
        while (addWaypoint.wasPressed()) {
            data.addWaypoint(client.player.getBlockPos().getX(), client.player.getBlockPos().getY(),
                    client.player.getBlockPos().getZ(), data.nextWaypointName());
        }
    }
}
