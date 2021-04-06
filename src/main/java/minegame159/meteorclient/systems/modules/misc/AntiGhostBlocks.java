/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client/).
 * Copyright (c) 2021 Meteor Development.
 */

package minegame159.meteorclient.systems.modules.misc;

//Created by errornonamer 02/06/2021

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import meteordevelopment.orbit.EventHandler;
import minegame159.meteorclient.events.entity.player.BreakBlockEvent;
import minegame159.meteorclient.events.entity.player.PlaceBlockEvent;
import minegame159.meteorclient.events.game.GameLeftEvent;
import minegame159.meteorclient.events.packets.PacketEvent;
import minegame159.meteorclient.events.world.TickEvent;
import minegame159.meteorclient.systems.modules.Categories;
import minegame159.meteorclient.systems.modules.Module;
import minegame159.meteorclient.settings.IntSetting;
import minegame159.meteorclient.settings.Setting;
import minegame159.meteorclient.settings.SettingGroup;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class AntiGhostBlocks extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> requestDelay = sgGeneral.add(new IntSetting.Builder()
        .name("delay")
        .description("delay between updating block and sending request")
        .defaultValue(3)
        .min(1)
        .sliderMin(1)
        .sliderMax(200)
        .build()
    );

    private final Setting<Integer> sendDelay = sgGeneral.add(new IntSetting.Builder()
        .name("delay")
        .description("delay between requests")
        .defaultValue(5)
        .min(1)
        .sliderMin(1)
        .sliderMax(200)
        .build()
    );
    
    private final HashMap<BlockPos, Long> blocks = new HashMap<BlockPos, Long>();
    private boolean lock = false;   // gay but idk how to do cocurrent stuff in java
    private long lastRequest = 0;

    @EventHandler
    private void onBlockBreak(BreakBlockEvent event) {
        lock = true;
        blocks.put(event.blockPos.toImmutable(), mc.world.getTime());
        lock = false;
    }

    @EventHandler
    private void onBlockPlace(PlaceBlockEvent event) {
        lock = true;
        blocks.put(event.blockPos.toImmutable(), mc.world.getTime());
        lock = false;
    }

    @EventHandler
    private void onGameDisconnect(GameLeftEvent event) {
        blocks.clear();
        lastRequest = 0;
    }

    @EventHandler
    private void onPacket(PacketEvent.Receive event) {
        if (lock) {
            return;
        }
        if (event.packet instanceof BlockUpdateS2CPacket) { // normal block update
            BlockUpdateS2CPacket packet = (BlockUpdateS2CPacket) event.packet;

            if (blocks.containsKey(packet.getPos())) {
                // got update, no need to request another one
                lock = true;
                blocks.remove(packet.getPos());
                lock = false;
            }
        }
        if (event.packet instanceof BlockEntityUpdateS2CPacket) { // block entity update
            BlockEntityUpdateS2CPacket packet = (BlockEntityUpdateS2CPacket) event.packet;

            if (blocks.containsKey(packet.getPos())) {
                lock = true;
                blocks.remove(packet.getPos());
                lock = false;
            }
        }
    }
    
    @EventHandler
    private void onTick(TickEvent.Post event) {
        long time = mc.world.getTime();

        if (blocks.isEmpty() || time - lastRequest < sendDelay.get() || lock) {
            return;
        }

        List<BlockPos> toRemove = new ArrayList<BlockPos>();

        blocks.forEach((pos, ptime) -> {
            if (toRemove.isEmpty() && time - ptime >= requestDelay.get()) { // still didn't get update, request server to send one.
                    mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(
                        PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, 
                        pos, 
                        Direction.UP));
                toRemove.add(pos.toImmutable());
                lastRequest = time;
            }
        });
        lock = true;
        toRemove.forEach(pos -> {
            blocks.remove(pos);
        });
        lock = false;
    }
    
    public AntiGhostBlocks() {
        super(Categories.Misc, "anti-ghost-blocks", "Automatically remove ghost blocks.");
    }
}
