package io.github.zuccogio.pvpgauntlets.client;

import io.github.zuccogio.pvpgauntlets.network.DuelHudSyncPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

public class PvPGauntletsClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {

		ClientPlayNetworking.registerGlobalReceiver(
				DuelHudSyncPayload.ID,
				(payload, context) ->
						DuelHudRenderer.setEntries(payload.entries())
		);

		ClientPlayConnectionEvents.DISCONNECT.register(
				(handler, client) -> DuelHudRenderer.clear()
		);

		HudRenderCallback.EVENT.register(
				DuelHudRenderer::render
		);
	}
}