package com.modvane.hologenica.network;

import com.modvane.hologenica.block.entity.TelepadBlockEntity;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import static com.modvane.hologenica.HologenicaMod.id;

// Packet to update telepad name from client to server
public record SetTelepadNamePacket(BlockPos pos, String name) implements CustomPacketPayload {

    // Validation constants
    private static final int MAX_TELEPAD_NAME_LENGTH = 32;
    private static final double MAX_INTERACTION_DISTANCE = 64.0;

    public static final Type<SetTelepadNamePacket> TYPE = new Type<>(id("set_telepad_name"));

    public static final StreamCodec<ByteBuf, SetTelepadNamePacket> STREAM_CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC,
        SetTelepadNamePacket::pos,
        ByteBufCodecs.STRING_UTF8,
        SetTelepadNamePacket::name,
        SetTelepadNamePacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    // Handle the packet on the server side
    public static void handle(SetTelepadNamePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;

            var blockEntity = player.level().getBlockEntity(packet.pos());
            if (!(blockEntity instanceof TelepadBlockEntity telepad)) return;

            // Validate that the player is close enough to the telepad
            double distance = player.distanceToSqr(packet.pos().getX(), packet.pos().getY(), packet.pos().getZ());
            if (distance >= MAX_INTERACTION_DISTANCE) return;

            // Validate and sanitize the name
            String sanitizedName = validateAndSanitizeName(packet.name());
            telepad.setTelepadName(sanitizedName);
        });
    }

    // Validate and sanitize telepad name
    private static String validateAndSanitizeName(String name) {
        if (name == null) return "";

        // Trim whitespace
        String sanitized = name.trim();

        // Enforce maximum length
        if (sanitized.length() > MAX_TELEPAD_NAME_LENGTH) {
            sanitized = sanitized.substring(0, MAX_TELEPAD_NAME_LENGTH);
        }

        // Remove any control characters
        sanitized = sanitized.replaceAll("\\p{Cntrl}", "");

        return sanitized;
    }
}
