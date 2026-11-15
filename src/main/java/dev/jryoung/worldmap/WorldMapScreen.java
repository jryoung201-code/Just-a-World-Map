package dev.jryoung.worldmap;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public final class WorldMapScreen extends Screen {
    private static final int MAP_RADIUS = 12;
    private final MapData data;

    WorldMapScreen(MapData data) {
        super(Text.literal("Just a World Map"));
        this.data = data;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        int centerX = this.width / 2;
        int top = 48;
        int cell = Math.min(16, Math.max(8, (this.height - 90) / (MAP_RADIUS * 2 + 1)));
        int mapLeft = centerX - (MAP_RADIUS * cell + cell / 2);
        int playerChunkX = client.player == null ? 0 : client.player.getChunkPos().x;
        int playerChunkZ = client.player == null ? 0 : client.player.getChunkPos().z;

        context.drawCenteredTextWithShadow(textRenderer, "World Map", centerX, 12, 0xFFFFFF);
        context.drawCenteredTextWithShadow(textRenderer,
                "Seed: " + data.seed() + "  |  " + data.dimension(), centerX, 27, 0xBBBBBB);

        for (int dx = -MAP_RADIUS; dx <= MAP_RADIUS; dx++) {
            for (int dz = -MAP_RADIUS; dz <= MAP_RADIUS; dz++) {
                int chunkX = playerChunkX + dx;
                int chunkZ = playerChunkZ + dz;
                int px = mapLeft + (dx + MAP_RADIUS) * cell;
                int py = top + (dz + MAP_RADIUS) * cell;
                boolean known = data.discovered(chunkX, chunkZ);
                int color = known ? 0xFF526B48 : 0xFF202329;
                if (dx == 0 && dz == 0) color = 0xFF48B6FF;
                context.fill(px + 1, py + 1, px + cell - 1, py + cell - 1, color);
            }
        }

        for (MapData.Waypoint point : data.waypoints()) {
            int dx = Math.floorDiv(point.x(), 16) - playerChunkX;
            int dz = Math.floorDiv(point.z(), 16) - playerChunkZ;
            if (Math.abs(dx) > MAP_RADIUS || Math.abs(dz) > MAP_RADIUS) continue;
            int px = mapLeft + (dx + MAP_RADIUS) * cell;
            int py = top + (dz + MAP_RADIUS) * cell;
            int color = switch (point.tagType()) {
                case ICON -> 0xFFFFD54F;
                case BLOCK -> 0xFFFF7043;
                case TEXT -> 0xFFCE93D8;
            };
            context.fill(px, py, px + cell, py + cell, color);
            String marker = switch (point.tagType()) {
                case ICON -> "◆";
                case BLOCK -> "■";
                case TEXT -> "T";
            };
            context.drawTextWithShadow(textRenderer, marker, px + 2, py + 1, 0xFF191919);
            if (Math.abs(dx) <= 1 && Math.abs(dz) <= 1) {
                context.drawTextWithShadow(textRenderer, point.name(), px + cell + 2, py, 0xFFFFFFFF);
            }
        }

        int footer = top + (MAP_RADIUS * 2 + 1) * cell + 8;
        context.drawCenteredTextWithShadow(textRenderer,
                "M: map  |  N: add waypoint  |  B: cycle waypoint tag (icon / block / text)",
                centerX, footer, 0xFFDDDDDD);
        context.drawCenteredTextWithShadow(textRenderer,
                "Bright chunks have been loaded. Dim chunks will appear as you explore.",
                centerX, footer + 13, 0xFFAAAAAA);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldPause() { return false; }
}
