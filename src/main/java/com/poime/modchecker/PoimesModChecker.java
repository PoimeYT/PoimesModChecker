package com.poime.modchecker;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.poime.modchecker.network.ModListPacket;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class PoimesModChecker implements ModInitializer {
	public static final String MOD_ID = "poimes-mod-checker";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	private static final Set<String> serverMods;

	static {
		serverMods = FabricLoader.getInstance().getAllMods().stream()
				.map(mod -> mod.getMetadata().getId())
				.collect(Collectors.toSet());
	}

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		// Register the packet type (server only needs C2S)
		PayloadTypeRegistry.playC2S().register(ModListPacket.ID, ModListPacket.CODEC);

		// Register the packet handler
		ServerPlayNetworking.registerGlobalReceiver(ModListPacket.ID, (payload, context) -> {
			handleModList(payload);
		});
	}

	private void handleModList(ModListPacket packet) {
		String username = packet.username();
		Map<String, String> clientMods = packet.modList();
		
		// Create PlayerMods directory if it doesn't exist
		Path playerModsDir = Paths.get("PlayerMods");
		if (!Files.exists(playerModsDir)) {
			try {
				Files.createDirectory(playerModsDir);
			} catch (IOException e) {
				LOGGER.error("Failed to create PlayerMods directory", e);
				return;
			}
		}

		// Create the log file
		String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
		File logFile = new File("PlayerMods/" + username + "_" + timestamp + ".txt");
		
		try (FileWriter writer = new FileWriter(logFile)) {
			writer.write("User: " + username + "\n");
			writer.write("Mods Installed on Client:\n");
			
			for (Map.Entry<String, String> mod : clientMods.entrySet()) {
				String modName = mod.getKey();
				String modId = mod.getValue();
				boolean isOnServer = serverMods.contains(modId);
				writer.write(String.format("[%s] : [%s] %s\n", 
					modName, 
					modId,
					isOnServer ? "(Server has this mod)" : "(Server does not have this mod)"));
			}
		} catch (IOException e) {
			LOGGER.error("Failed to write mod list for " + username, e);
		}

		// Log to console
		LOGGER.info("Received mod list from " + username);
		LOGGER.info("Mod list saved to: " + logFile.getAbsolutePath());
	}
}