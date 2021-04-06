package minegame159.meteorclient.mixin;

import minegame159.meteorclient.systems.modules.Modules;
import minegame159.meteorclient.systems.modules.render.CustomFOV;
import minegame159.meteorclient.systems.modules.render.Fullbright;
import minegame159.meteorclient.systems.modules.render.Fullbright.StaticListener;
import minegame159.meteorclient.systems.modules.render.Zoom;
import net.minecraft.client.options.GameOptions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameOptions.class)
public abstract class GameOptionsMixin {
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
}
