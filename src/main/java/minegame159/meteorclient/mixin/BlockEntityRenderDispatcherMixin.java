/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client/).
 * Copyright (c) 2021 Meteor Development.
 */

package minegame159.meteorclient.mixin;

import minegame159.meteorclient.MeteorClient;
import minegame159.meteorclient.events.render.RenderBlockEntityEvent;
import minegame159.meteorclient.systems.modules.Modules;
import minegame159.meteorclient.systems.modules.render.*;
import minegame159.meteorclient.utils.render.Outlines;
import minegame159.meteorclient.utils.render.color.Color;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.render.*;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockEntityRenderDispatcher.class)
public abstract class BlockEntityRenderDispatcherMixin {
    @Shadow protected abstract <E extends BlockEntity> void render(E blockEntity, float tickDelta, MatrixStack matrix, VertexConsumerProvider vertexConsumerProvider);

    @Inject(method = "render(Lnet/minecraft/block/entity/BlockEntity;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;)V", at = @At("HEAD"), cancellable = true)
    private <E extends BlockEntity> void onRenderEntity(E blockEntity, float tickDelta, MatrixStack matrix, VertexConsumerProvider vertexConsumerProvider, CallbackInfo info) {
        RenderBlockEntityEvent event = MeteorClient.EVENT_BUS.post(RenderBlockEntityEvent.get(blockEntity));
        if (event.isCancelled()) info.cancel();

        if (vertexConsumerProvider == Outlines.vertexConsumerProvider) return;

        StorageESP sesp = Modules.get().get(StorageESP.class);
        if (!sesp.isActive() || !sesp.isOutline()) return;

        Color c = sesp.getColor();

        WorldRenderer wr = MinecraftClient.getInstance().worldRenderer;
        WorldRendererAccessor wra = (WorldRendererAccessor) wr;

        Framebuffer fbo = wr.getEntityOutlinesFramebuffer();
        wra.setEntityOutlinesFramebuffer(Outlines.outlinesFbo);

        Outlines.vertexConsumerProvider.setColor(c.r, c.g, c.b, c.a);
        render(blockEntity, tickDelta, matrix, Outlines.vertexConsumerProvider);

        wra.setEntityOutlinesFramebuffer(fbo);
    }
}
