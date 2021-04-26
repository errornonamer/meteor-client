/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client/).
 * Copyright (c) 2021 Meteor Development.
 */

package minegame159.meteorclient.mixin;

import minegame159.meteorclient.systems.modules.Modules;
import minegame159.meteorclient.systems.modules.render.Chams;
import minegame159.meteorclient.systems.modules.render.Freecam;
import minegame159.meteorclient.utils.Utils;
import minegame159.meteorclient.utils.misc.text.TextUtils;
import minegame159.meteorclient.utils.player.Rotations;
import minegame159.meteorclient.utils.render.color.Color;
import minegame159.meteorclient.utils.render.Outlines;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.AbstractTeam;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import static org.lwjgl.opengl.GL11.*;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, M extends EntityModel<T>> {
    @Shadow protected abstract void render(T livingEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i);
    @Shadow @Nullable protected abstract RenderLayer getRenderLayer(T entity, boolean showBody, boolean translucent, boolean showOutline);

    //Freecam
    @Redirect(method = "hasLabel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;getCameraEntity()Lnet/minecraft/entity/Entity;"))
    private Entity hasLabelGetCameraEntityProxy(MinecraftClient mc) {
        if (Modules.get().isActive(Freecam.class)) return null;
        return mc.getCameraEntity();
    }

    //3rd Person Rotation
    @ModifyVariable(method = "render", ordinal = 2, at = @At(value = "STORE", ordinal = 0))
    public float changeYaw(float oldValue, LivingEntity entity) {
        if (entity.equals(Utils.mc.player) && Rotations.rotationTimer < 10) return Rotations.serverYaw;
        return oldValue;
    }

    @ModifyVariable(method = "render", ordinal = 3, at = @At(value = "STORE", ordinal = 0))
    public float changeHeadYaw(float oldValue, LivingEntity entity) {
        if (entity.equals(Utils.mc.player) && Rotations.rotationTimer < 10) return Rotations.serverYaw;
        return oldValue;
    }

    @ModifyVariable(method = "render", ordinal = 5, at = @At(value = "STORE", ordinal = 3))
    public float changePitch(float oldValue, LivingEntity entity) {
        if (entity.equals(Utils.mc.player) && Rotations.rotationTimer < 10) return Rotations.serverPitch;
        return oldValue;
    }

    // Player model rendering in main menu
    @Redirect(method = "hasLabel", at = @At(value = "INVOKE", target = "net.minecraft.client.network.ClientPlayerEntity.getScoreboardTeam()Lnet/minecraft/scoreboard/AbstractTeam;"))
    private AbstractTeam hasLabelClientPlayerEntityGetScoreboardTeamProxy(ClientPlayerEntity player) {
        if (player == null) return null;
        return player.getScoreboardTeam();
    }

    //Chams

    //Depth
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void onRender(T livingEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
        Chams chams = Modules.get().get(Chams.class);
        if (chams.rendering) return;
        if (vertexConsumerProvider == Outlines.vertexConsumerProvider) return;

        if (chams.isActive()) {
            chams.rendering = true;
            if (chams.shouldRender(livingEntity)) {
                // ignore z
                glEnable(GL_POLYGON_OFFSET_FILL);
                glDisable(GL_DEPTH_TEST);
                glDepthMask(false);
                glPolygonOffset(1.0f, -1100000.0f);

                chams.xqz = true;
                render(livingEntity, f, g, matrixStack, vertexConsumerProvider, chams.fullbrightInv.get() ? (15 << 4 | 15 << 20) : i);
                chams.xqz = false;

                glPolygonOffset(1.0f, 1099999.99f);
                glDepthMask(true);
                glEnable(GL_DEPTH_TEST);
                glDisable(GL_POLYGON_OFFSET_FILL);

                render(livingEntity, f, g, matrixStack, vertexConsumerProvider, chams.overrideVisible.get() && chams.fullbright.get() ? (15 << 4 | 15 << 20) : i);
                glPolygonOffset(1.0f, 0.01f);
            } else {
                render(livingEntity, f, g, matrixStack, vertexConsumerProvider, chams.overrideVisible.get() && chams.fullbright.get() ? (15 << 4 | 15 << 20) : i);
            }
            ci.cancel();
            chams.rendering = false;
            chams.xqz = false;
        }
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/LivingEntityRenderer;getRenderLayer(Lnet/minecraft/entity/LivingEntity;ZZZ)Lnet/minecraft/client/render/RenderLayer;"))
    private RenderLayer getRenderLayer(LivingEntityRenderer<T, M> livingEntityRenderer, T livingEntity, boolean showBody, boolean translucent, boolean showOutline) {
        Chams module = Modules.get().get(Chams.class);
        if (!module.isActive()) return getRenderLayer(livingEntity, showBody, translucent, showOutline);
        if (module.ignoreSelf.get() && livingEntity == MinecraftClient.getInstance().player) return getRenderLayer(livingEntity, showBody, translucent, showOutline);

        boolean hasTexture = (!module.xqz && (!module.overrideVisible.get() || module.texture.get())) ||
                (module.xqz && module.textureInv.get());
        return hasTexture ? getRenderLayer(livingEntity, module.xqz || showBody, translucent, showOutline) : RenderLayer.getItemEntityTranslucentCull(Chams.BLANK);
    }

    @SuppressWarnings("UnresolvedMixinReference")
    @ModifyArgs(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/model/EntityModel;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;IIFFFF)V"))
    private void modifyColor(Args args, T livingEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
        Chams module = Modules.get().get(Chams.class);
        if (!module.isActive()) return;
        if (module.ignoreSelf.get() && livingEntity == MinecraftClient.getInstance().player) return;
        if (!module.xqz && !module.overrideVisible.get()) return;

        Color color = module.getColor(livingEntity);
        args.set(4, color.r / 255f);
        args.set(5, color.g / 255f);
        args.set(6, color.b / 255f);
        args.set(7, color.a / 255f);
    }

    /*@Inject(method = "render", at = @At("HEAD"))
    private void renderHead(T livingEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
        Chams chams = Modules.get().get(Chams.class);

        if (chams.isActive() && chams.shouldRender(livingEntity)) {
            glEnable(GL_POLYGON_OFFSET_FILL);
            glPolygonOffset(1.0f, -1100000.0f);
        }
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void renderTail(T livingEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
        Chams chams = Modules.get().get(Chams.class);

        if (chams.isActive() && chams.shouldRender(livingEntity)) {
            glPolygonOffset(1.0f, 1100000.0f);
            glDisable(GL_POLYGON_OFFSET_FILL);
        }
    }*/

    //Player stuff below

    // Scale
    @ModifyArgs(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;scale(FFF)V"))
    private void modifyScale(Args args, T livingEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
        Chams module = Modules.get().get(Chams.class);
        if (!module.isActive() || !module.players.get() || !(livingEntity instanceof PlayerEntity)) return;
        if (module.ignoreSelf.get() && livingEntity == MinecraftClient.getInstance().player) return;

        args.set(0, -module.playersScale.get().floatValue());
        args.set(1, -module.playersScale.get().floatValue());
        args.set(2, module.playersScale.get().floatValue());
    }
}
