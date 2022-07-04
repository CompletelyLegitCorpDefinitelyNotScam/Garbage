package me.bedtrapteam.addon.modules.misc;

import me.bedtrapteam.addon.BedTrap;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.EntityPose;
import net.minecraft.item.Items;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.c2s.play.KeepAliveC2SPacket;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class PingSpoof extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    public final Setting<Integer> ping = sgGeneral.add(new IntSetting.Builder().name("ping").defaultValue(200).range(100, 2000).build());

    private final Executor packetManager = Executors.newSingleThreadExecutor();
    private final List<Packet<?>> packets = new ArrayList<>();
    private boolean sendingPackets;
    private boolean sendPackets;

    public PingSpoof() {
        super(BedTrap.Misc, "ping=spoof", "Makes your ping higher, may not work on some servers.");
    }

    @Override
    public void onActivate() {
        sendingPackets = false;
        sendPackets = false;

        packetManager.execute(this::managePackets);
    }

    private void managePackets() {
        while (this.isActive()) {
            try {
                Thread.sleep(ping.get());
            } catch (Exception ignored) {}
            sendPackets = true;
        }
    }

    @EventHandler
    private void onSendPacket(PacketEvent.Send event) {
        if (sendingPackets) return;
        if (event.packet instanceof KeepAliveC2SPacket) {
            packets.add(event.packet);
            event.cancel();
        }

        if (sendPackets) {
            sendingPackets = true;
            packets.forEach(packet -> {
                try {
                    mc.getNetworkHandler().sendPacket(packet);
                } catch (Exception e) {
                    e.fillInStackTrace();
                }
            });
            sendingPackets = false;
            packets.clear();
            sendPackets = false;
        }
    }
}
