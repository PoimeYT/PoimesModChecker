package com.poime.modchecker.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import com.poime.modchecker.PoimesModChecker;

import java.util.HashMap;
import java.util.Map;

public record ModListPacket(String username, Map<String, String> modList) implements CustomPayload {
    public static final Id<ModListPacket> ID = new Id<>(Identifier.of(PoimesModChecker.MOD_ID, "mod_list"));
    
    public static final PacketCodec<PacketByteBuf, ModListPacket> CODEC = PacketCodec.of(
        (data, buf) -> {
            buf.writeString(data.username());
            buf.writeInt(data.modList().size());
            data.modList().forEach((name, id) -> {
                buf.writeString(name);
                buf.writeString(id);
            });
        },
        buf -> {
            String username = buf.readString();
            int size = buf.readInt();
            Map<String, String> modList = new HashMap<>();
            for (int i = 0; i < size; i++) {
                modList.put(buf.readString(), buf.readString());
            }
            return new ModListPacket(username, modList);
        }
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
} 