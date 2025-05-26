package com.poime.modchecker;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import com.poime.modchecker.network.ModListPacket;
import java.util.HashMap;
import java.util.Map;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

public class PoimesModCheckerClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// Register the packet type (client only needs S2C)
		PayloadTypeRegistry.playS2C().register(ModListPacket.ID, ModListPacket.CODEC);

		// Register the packet handler
		ClientPlayNetworking.registerGlobalReceiver(ModListPacket.ID, (payload, context) -> {
			// This is just a response handler, we don't need to do anything here
		});

		// Send mod list when joining a server
		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
			if (client.player != null) {
				sendModList(client);
			}
		});
	}

	private void sendModList(MinecraftClient client) {
		Map<String, String> modList = new HashMap<>();
		FabricLoader.getInstance().getAllMods().forEach(mod -> {
			modList.put(mod.getMetadata().getName(), mod.getMetadata().getId());
		});

		ModListPacket packet = new ModListPacket(client.player.getName().getString(), modList);
		ClientPlayNetworking.send(packet);
	}
}