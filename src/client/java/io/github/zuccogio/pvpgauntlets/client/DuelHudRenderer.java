package io.github.zuccogio.pvpgauntlets.client;

import io.github.zuccogio.pvpgauntlets.network.DuelHudSyncPayload;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.PlayerSkinDrawer;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

public final class DuelHudRenderer {

    private static List<DuelHudSyncPayload.Entry> entries = List.of();

    private DuelHudRenderer() {}

    public static void setEntries(List<DuelHudSyncPayload.Entry> newEntries) {
        entries = List.copyOf(newEntries);
    }

    public static void clear() {
        entries = List.of();
    }

    public static void render(
            DrawContext context,
            RenderTickCounter tickCounter
    ) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.player == null
                || client.getNetworkHandler() == null
                || client.options.hudHidden
                || entries.isEmpty()) {
            return;
        }

        int x = DuelHudSettings.x;
        int y = DuelHudSettings.y;

        for (DuelHudSyncPayload.Entry duelEntry : entries) {

            PlayerListEntry playerEntry =
                    client.getNetworkHandler()
                            .getPlayerListEntry(duelEntry.opponentUuid());

            if (playerEntry == null) {
                continue;
            }

            int faceSize = DuelHudSettings.faceSize;

            /*
             * Stessa skin disponibile nella player list.
             * PlayerSkinDrawer disegna anche il secondo layer della testa
             * (cappello, capelli, ecc.).
             */
            PlayerSkinDrawer.draw(
                    context,
                    playerEntry.getSkinTextures(),
                    x,
                    y,
                    faceSize
            );

            Text playerName = Text.literal(
                    playerEntry.getProfile().getName()
            );

            Text stateText = switch (duelEntry.state()) {
                case STARTING -> Text.translatable(
                        "hud.pvpgauntlets.duel.starting",
                        duelEntry.seconds()
                ).formatted(Formatting.YELLOW);

                case FIGHTING -> Text.translatable(
                        "hud.pvpgauntlets.duel.fighting",
                        duelEntry.seconds()
                ).formatted(Formatting.RED);

                case VICTORY -> Text.translatable(
                        "hud.pvpgauntlets.duel.victory",
                        duelEntry.seconds()
                ).formatted(Formatting.GREEN);

                case LOOTING -> Text.translatable(
                        "hud.pvpgauntlets.duel.looting"
                ).formatted(Formatting.DARK_GREEN);
            };

            int textY =
                    y + (faceSize - client.textRenderer.fontHeight) / 2;

            int nameX =
                    x
                            + faceSize
                            + DuelHudSettings.faceTextSpacing;

            context.drawText(
                    client.textRenderer,
                    playerName,
                    nameX,
                    textY,
                    0xFFFFFFFF,
                    true
            );

            int stateX =
                    nameX
                            + client.textRenderer.getWidth(playerName)
                            + DuelHudSettings.nameStatusSpacing;

            context.drawText(
                    client.textRenderer,
                    stateText,
                    stateX,
                    textY,
                    0xFFFFFFFF,
                    true
            );

            y += faceSize + DuelHudSettings.rowSpacing;
        }
    }
}