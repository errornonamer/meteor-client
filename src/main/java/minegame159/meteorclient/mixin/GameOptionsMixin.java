package minegame159.meteorclient.mixin;

/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client/).
 * Copyright (c) 2021 Meteor Development.
 */

import minegame159.meteorclient.systems.modules.Modules;
import minegame159.meteorclient.systems.modules.render.CustomFOV;
import minegame159.meteorclient.systems.modules.render.Fullbright;
import minegame159.meteorclient.systems.modules.render.Fullbright.StaticListener;
import minegame159.meteorclient.systems.modules.render.Zoom;
import minegame159.meteorclient.utils.misc.input.KeyBinds;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.options.GameOptions;
import net.minecraft.client.options.KeyBinding;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.io.File;

@Mixin(GameOptions.class)
public abstract class GameOptionsMixin {
    @Shadow @Final @Mutable
    public KeyBinding[] keysAll;
    @Shadow public double mouseSensitivity;     //zoom
    @Shadow public boolean smoothCameraEnabled; //zoom
    @Shadow public double fov;                  //customfov
    @Shadow public double gamma;                //fullbright

    @Inject(method = "write", at = @At("HEAD"))
    public void onWriteHead(CallbackInfo info) {
        CustomFOV cfov = Modules.get().get(CustomFOV.class);
        Fullbright fb = Modules.get().get(Fullbright.class);
        Zoom z = Modules.get().get(Zoom.class);

        if (cfov.isActive()) {
            fov = cfov._fov;
        }
        if (fb.isActive()) {
            gamma = StaticListener.prevGamma;
        }
        if (z.isActive()) {
            mouseSensitivity = z.preMouseSensitivity;
            smoothCameraEnabled = z.preCinematic;
        }
    }

    //@Inject(method = "write", at = @At("RETURN"))
    //public void onWriteReturn(CallbackInfo info) {
        // none of the above needs to be restored manually since they're being constantly updated on tick
        // if you add any module that modifies gameoption and does not update on tick, add here
    //}

    @Inject(method = "<init>", at = @At(value = "FIELD", target = "Lnet/minecraft/client/options/GameOptions;keysAll:[Lnet/minecraft/client/options/KeyBinding;", opcode = Opcodes.PUTFIELD, shift = At.Shift.AFTER))
    private void onInitAfterKeysAll(MinecraftClient client, File optionsFile, CallbackInfo info) {
        keysAll = KeyBinds.apply(keysAll);
    }
}
