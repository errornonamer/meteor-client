/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client/).
 * Copyright (c) 2021 Meteor Development.
 */

package minegame159.meteorclient.systems.modules.misc;

import meteordevelopment.orbit.EventHandler;
import minegame159.meteorclient.events.game.GameLeftEvent;
import minegame159.meteorclient.events.packets.PacketEvent;
import minegame159.meteorclient.events.world.TickEvent;
import minegame159.meteorclient.systems.modules.Categories;
import minegame159.meteorclient.systems.modules.Module;
import minegame159.meteorclient.utils.entity.EntityUtils;
import minegame159.meteorclient.utils.player.ChatUtils;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;

import java.util.Objects;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

public class AntiVanish extends Module {
    private final Queue<UUID> toLookup = new ConcurrentLinkedQueue<UUID>();
    private long lastTick = 0;

    @Override
    public void onDeactivate() {
        toLookup.clear();
    }
    @EventHandler
    public void onLeave(GameLeftEvent event) {
        toLookup.clear();
    }

    @EventHandler
    public void onPacket(PacketEvent.Receive event) {
        if (event.packet instanceof PlayerListS2CPacket) {
            PlayerListS2CPacket packet = (PlayerListS2CPacket) event.packet;
            if (packet.getAction() == PlayerListS2CPacket.Action.UPDATE_LATENCY) {
                try {
                    for (PlayerListS2CPacket.Entry entry : packet.getEntries()) {
                        if (mc.getNetworkHandler().getPlayerListEntry(entry.getProfile().getId()) != null)
                            continue;
                        toLookup.add(entry.getProfile().getId());
                    }
                } catch (Exception ignore) {}
            }
        }
    }

    @EventHandler
    public void onTick(TickEvent.Post event) {
        long time = mc.world.getTime();
        UUID lookup;

        if (Math.abs(lastTick - time) > 100 && (lookup = toLookup.poll()) != null) {
            try {
                String name = EntityUtils.getPlayerNameFromUUID(lookup);
                if (name != null) {
                    ChatUtils.moduleWarning(this, name + " has gone into vanish.");
                }
            } catch (Exception ignore) {}
            lastTick = time;
        }
    }

    public AntiVanish() {
        super(Categories.Misc, "anti-vanish", "Notifies user when a admin uses /vanish");
    }
}
