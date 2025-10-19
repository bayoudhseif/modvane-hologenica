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
            if (context.player() instanceof ServerPlayer player) {
                var blockEntity = player.level().getBlockEntity(packet.pos());

                if (blockEntity instanceof TelepadBlockEntity telepad) {
                    // Validate that the player is close enough to the telepad
                    double distance = player.distanceToSqr(packet.pos().getX(), packet.pos().getY(), packet.pos().getZ());

                    if (distance < 64.0) {
                        telepad.setTelepadName(packet.name());
                    }
                }
            }
        });
    }
}
