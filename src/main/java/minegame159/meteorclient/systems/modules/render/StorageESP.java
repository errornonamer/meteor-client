/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client/).
 * Copyright (c) 2021 Meteor Development.
 */

package minegame159.meteorclient.systems.modules.render;

import meteordevelopment.orbit.EventHandler;
import minegame159.meteorclient.events.render.RenderEvent;
import minegame159.meteorclient.events.render.RenderBlockEntityEvent;
import minegame159.meteorclient.rendering.Renderer;
import minegame159.meteorclient.rendering.ShapeMode;
import minegame159.meteorclient.settings.*;
import minegame159.meteorclient.systems.modules.Categories;
import minegame159.meteorclient.systems.modules.Module;
import minegame159.meteorclient.utils.entity.EntityUtils;
import minegame159.meteorclient.utils.render.RenderUtils;
import minegame159.meteorclient.utils.render.Outlines;
import minegame159.meteorclient.utils.render.color.Color;
import minegame159.meteorclient.utils.render.color.SettingColor;
import minegame159.meteorclient.utils.world.Dir;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.entity.*;
import net.minecraft.block.enums.ChestType;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;

import java.util.Arrays;
import java.util.List;

public class StorageESP extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    public enum Mode {
        Box,
        Outline
    }

    private final Setting<List<BlockEntityType<?>>> storageBlocks = sgGeneral.add(new StorageBlockListSetting.Builder()
            .name("storage-blocks")
            .description("Select the storage blocks to display.")
            .defaultValue(Arrays.asList(StorageBlockListSetting.STORAGE_BLOCKS))
            .build()
    );

    private final Setting<Boolean> tracers = sgGeneral.add(new BoolSetting.Builder()
            .name("tracers")
            .description("Draws tracers to storage blocks.")
            .defaultValue(false)
            .build()
    );

    private final Setting<ShapeMode> shapeMode = sgGeneral.add(new EnumSetting.Builder<ShapeMode>()
            .name("shape-mode")
            .description("How the shapes are rendered.")
            .defaultValue(ShapeMode.Both)
            .build()
    );

    private final Setting<StorageESP.Mode> mode = sgGeneral.add(new EnumSetting.Builder<StorageESP.Mode>()
            .name("mode")
            .description("Rendering mode.")
            .defaultValue(StorageESP.Mode.Box)
            .build()
    );

    private final Setting<SettingColor> chest = sgGeneral.add(new ColorSetting.Builder()
            .name("chest")
            .description("The color of chests.")
            .defaultValue(new SettingColor(255, 160, 0, 255))
            .build()
    );

    private final Setting<SettingColor> trappedChest = sgGeneral.add(new ColorSetting.Builder()
            .name("trapped-chest")
            .description("The color of trapped chests.")
            .defaultValue(new SettingColor(255, 0, 0, 255))
            .build()
    );

    private final Setting<SettingColor> barrel = sgGeneral.add(new ColorSetting.Builder()
            .name("barrel")
            .description("The color of barrels.")
            .defaultValue(new SettingColor(255, 160, 0, 255))
            .build()
    );

    private final Setting<SettingColor> shulker = sgGeneral.add(new ColorSetting.Builder()
            .name("shulker")
            .description("The color of Shulker Boxes.")
            .defaultValue(new SettingColor(255, 160, 0, 255))
            .build()
    );

    private final Setting<SettingColor> enderChest = sgGeneral.add(new ColorSetting.Builder()
            .name("ender-chest")
            .description("The color of Ender Chests.")
            .defaultValue(new SettingColor(120, 0, 255, 255))
            .build()
    );

    private final Setting<SettingColor> other = sgGeneral.add(new ColorSetting.Builder()
            .name("other")
            .description("The color of furnaces, dispenders, droppers and hoppers.")
            .defaultValue(new SettingColor(140, 140, 140, 255))
            .build()
    );

    private final Setting<Double> fadeDistance = sgGeneral.add(new DoubleSetting.Builder()
            .name("fade-distance")
            .description("The distance at which the color will fade.")
            .defaultValue(6)
            .min(0)
            .sliderMax(12)
            .build()
    );

    private final Color lineColor = new Color(0, 0, 0, 0);
    private final Color sideColor = new Color(0, 0, 0, 0);
    private boolean render;
    private int count;

    public StorageESP() {
        super(Categories.Render, "storage-esp", "Renders all specified storage blocks.");
    }

    private void getTileEntityColor(BlockEntity blockEntity) {
        render = false;

        if (!storageBlocks.get().contains(blockEntity.getType())) return;

        if (blockEntity instanceof TrappedChestBlockEntity) lineColor.set(trappedChest.get()); // Must come before ChestBlockEntity as it is the superclass of TrappedChestBlockEntity
        else if (blockEntity instanceof ChestBlockEntity) lineColor.set(chest.get());
        else if (blockEntity instanceof BarrelBlockEntity) lineColor.set(barrel.get());
        else if (blockEntity instanceof ShulkerBoxBlockEntity) lineColor.set(shulker.get());
        else if (blockEntity instanceof EnderChestBlockEntity) lineColor.set(enderChest.get());
        else if (blockEntity instanceof AbstractFurnaceBlockEntity || blockEntity instanceof DispenserBlockEntity || blockEntity instanceof HopperBlockEntity) lineColor.set(other.get());
        else return;

        render = true;

        if (shapeMode.get() == ShapeMode.Sides || shapeMode.get() == ShapeMode.Both) {
            sideColor.set(lineColor);
            sideColor.a -= 225;
            if (sideColor.a < 0) sideColor.a = 0;
        }
    }

    private void box(VertexConsumer buffer, float x1, double y1, double z1, double x2, double y2, double z2) {
        // top
        buffer.vertex(x1, y1, z1).color(sideColor.r, sideColor.g, sideColor.b, sideColor.a).texture(0.0f, 0.0f).light(0).normal(0.0f, 0.0f, 0.0f).next();
        buffer.vertex(x2, y1, z1).color(sideColor.r, sideColor.g, sideColor.b, sideColor.a).texture(0.0f, 0.0f).light(0).normal(0.0f, 0.0f, 0.0f).next();
        buffer.vertex(x2, y2, z1).color(sideColor.r, sideColor.g, sideColor.b, sideColor.a).texture(0.0f, 0.0f).light(0).normal(0.0f, 0.0f, 0.0f).next();
        buffer.vertex(x1, y2, z1).color(sideColor.r, sideColor.g, sideColor.b, sideColor.a).texture(0.0f, 0.0f).light(0).normal(0.0f, 0.0f, 0.0f).next();
        // bottom
        buffer.vertex(x1, y1, z2).color(sideColor.r, sideColor.g, sideColor.b, sideColor.a).texture(0.0f, 0.0f).light(0).normal(0.0f, 0.0f, 0.0f).next();
        buffer.vertex(x2, y1, z2).color(sideColor.r, sideColor.g, sideColor.b, sideColor.a).texture(0.0f, 0.0f).light(0).normal(0.0f, 0.0f, 0.0f).next();
        buffer.vertex(x2, y2, z2).color(sideColor.r, sideColor.g, sideColor.b, sideColor.a).texture(0.0f, 0.0f).light(0).normal(0.0f, 0.0f, 0.0f).next();
        buffer.vertex(x1, y2, z2).color(sideColor.r, sideColor.g, sideColor.b, sideColor.a).texture(0.0f, 0.0f).light(0).normal(0.0f, 0.0f, 0.0f).next();
        // sides
        buffer.vertex(x1, y1, z1).color(sideColor.r, sideColor.g, sideColor.b, sideColor.a).texture(0.0f, 0.0f).light(0).normal(0.0f, 0.0f, 0.0f).next();
        buffer.vertex(x2, y1, z1).color(sideColor.r, sideColor.g, sideColor.b, sideColor.a).texture(0.0f, 0.0f).light(0).normal(0.0f, 0.0f, 0.0f).next();
        buffer.vertex(x2, y1, z2).color(sideColor.r, sideColor.g, sideColor.b, sideColor.a).texture(0.0f, 0.0f).light(0).normal(0.0f, 0.0f, 0.0f).next();
        buffer.vertex(x1, y1, z2).color(sideColor.r, sideColor.g, sideColor.b, sideColor.a).texture(0.0f, 0.0f).light(0).normal(0.0f, 0.0f, 0.0f).next();

        buffer.vertex(x1, y1, z1).color(sideColor.r, sideColor.g, sideColor.b, sideColor.a).texture(0.0f, 0.0f).light(0).normal(0.0f, 0.0f, 0.0f).next();
        buffer.vertex(x1, y2, z1).color(sideColor.r, sideColor.g, sideColor.b, sideColor.a).texture(0.0f, 0.0f).light(0).normal(0.0f, 0.0f, 0.0f).next();
        buffer.vertex(x1, y2, z2).color(sideColor.r, sideColor.g, sideColor.b, sideColor.a).texture(0.0f, 0.0f).light(0).normal(0.0f, 0.0f, 0.0f).next();
        buffer.vertex(x1, y1, z2).color(sideColor.r, sideColor.g, sideColor.b, sideColor.a).texture(0.0f, 0.0f).light(0).normal(0.0f, 0.0f, 0.0f).next();

        buffer.vertex(x1, y2, z1).color(sideColor.r, sideColor.g, sideColor.b, sideColor.a).texture(0.0f, 0.0f).light(0).normal(0.0f, 0.0f, 0.0f).next();
        buffer.vertex(x2, y2, z1).color(sideColor.r, sideColor.g, sideColor.b, sideColor.a).texture(0.0f, 0.0f).light(0).normal(0.0f, 0.0f, 0.0f).next();
        buffer.vertex(x2, y2, z2).color(sideColor.r, sideColor.g, sideColor.b, sideColor.a).texture(0.0f, 0.0f).light(0).normal(0.0f, 0.0f, 0.0f).next();
        buffer.vertex(x1, y2, z2).color(sideColor.r, sideColor.g, sideColor.b, sideColor.a).texture(0.0f, 0.0f).light(0).normal(0.0f, 0.0f, 0.0f).next();

        buffer.vertex(x2, y1, z1).color(sideColor.r, sideColor.g, sideColor.b, sideColor.a).texture(0.0f, 0.0f).light(0).normal(0.0f, 0.0f, 0.0f).next();
        buffer.vertex(x2, y2, z1).color(sideColor.r, sideColor.g, sideColor.b, sideColor.a).texture(0.0f, 0.0f).light(0).normal(0.0f, 0.0f, 0.0f).next();
        buffer.vertex(x2, y2, z2).color(sideColor.r, sideColor.g, sideColor.b, sideColor.a).texture(0.0f, 0.0f).light(0).normal(0.0f, 0.0f, 0.0f).next();
        buffer.vertex(x2, y1, z1).color(sideColor.r, sideColor.g, sideColor.b, sideColor.a).texture(0.0f, 0.0f).light(0).normal(0.0f, 0.0f, 0.0f).next();
    }

    @EventHandler
    private void onRender(RenderEvent event) {
        count = 0;

        for (BlockEntity blockEntity : mc.world.blockEntities) {
            if (blockEntity.isRemoved() || !EntityUtils.isInRenderDistance(blockEntity)) continue;

            getTileEntityColor(blockEntity);

            if (render) {
                double dist = mc.player.squaredDistanceTo(blockEntity.getPos().getX() + 0.5, blockEntity.getPos().getY() + 0.5, blockEntity.getPos().getZ() + 0.5);
                double alpha = 1;
                if (dist <= fadeDistance.get() * fadeDistance.get()) alpha = dist / (fadeDistance.get() * fadeDistance.get());

                int prevLineA = lineColor.a;
                int prevSideA = sideColor.a;

                lineColor.a *= alpha;
                sideColor.a *= alpha;

                if (mode.get() == Mode.Box || BlockEntityRenderDispatcher.INSTANCE.get(blockEntity) == null) {
                    float x1 = blockEntity.getPos().getX();
                    double y1 = blockEntity.getPos().getY();
                    double z1 = blockEntity.getPos().getZ();

                    double x2 = blockEntity.getPos().getX() + 1;
                    double y2 = blockEntity.getPos().getY() + 1;
                    double z2 = blockEntity.getPos().getZ() + 1;

                    int excludeDir = 0;
                    if (blockEntity instanceof ChestBlockEntity) {
                        BlockState state = mc.world.getBlockState(blockEntity.getPos());
                        if ((state.getBlock() == Blocks.CHEST || state.getBlock() == Blocks.TRAPPED_CHEST) && state.get(ChestBlock.CHEST_TYPE) != ChestType.SINGLE) {
                            excludeDir = Dir.get(ChestBlock.getFacing(state));
                        }
                    }

                    if (blockEntity instanceof ChestBlockEntity || blockEntity instanceof EnderChestBlockEntity) {
                        double a = 1.0 / 16.0;

                        if (Dir.is(excludeDir, Dir.WEST)) x1 += a;
                        if (Dir.is(excludeDir, Dir.NORTH)) z1 += a;

                        if (Dir.is(excludeDir, Dir.EAST)) x2 -= a;
                        y2 -= a * 2;
                        if (Dir.is(excludeDir, Dir.SOUTH)) z2 -= a;
                    }
                    if (alpha >= 0.075) {
                        Renderer.boxWithLines(Renderer.NORMAL, Renderer.LINES, x1, y1, z1, x2, y2, z2, sideColor, lineColor, shapeMode.get(), excludeDir);
                    }
                }

                if (tracers.get()) RenderUtils.drawTracerToBlockEntity(blockEntity, lineColor, event);

                lineColor.a = prevLineA;
                sideColor.a = prevSideA;

                count++;
            }
        }
    }

    @EventHandler
    public void onBlockEntityRender(RenderBlockEntityEvent event) {
        if (mode.get() != Mode.Outline) { return; }

        getTileEntityColor(event.blockEntity);
        // rest will be handled in BlockEntityRenderDispatcher mixin
    }

    @Override
    public String getInfoString() {
        return Integer.toString(count);
    }

    public Color getColor() {
        return lineColor;
    }

    public boolean shouldRenderOutline() {
        return mode.get() == Mode.Outline && render;
    }

    public boolean isOutline() {
        return mode.get() == Mode.Outline;
    }

    public boolean isTargetBlock(Block block) {
        for (BlockEntityType<?> e : storageBlocks.get()) {
            if (e.supports(block)) {
                return true;
            }
        }
        return false;
    }

    public void renderFullBlockOutline(BlockEntity blockEntity) {
        float x1 = blockEntity.getPos().getX();
        double y1 = blockEntity.getPos().getY();
        double z1 = blockEntity.getPos().getZ();

        double x2 = blockEntity.getPos().getX() + 1;
        double y2 = blockEntity.getPos().getY() + 1;
        double z2 = blockEntity.getPos().getZ() + 1;
        box(Outlines.vertexConsumerProvider.getBuffer(RenderLayer.getSolid()), x1, y1, z1, x2, y2, z2);
    }
}
