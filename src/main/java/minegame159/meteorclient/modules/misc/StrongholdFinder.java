package minegame159.meteorclient.modules.misc;

import meteordevelopment.orbit.EventHandler;
import minegame159.meteorclient.events.entity.EntityAddedEvent;
import minegame159.meteorclient.events.entity.EntityRemovedEvent;
import minegame159.meteorclient.events.render.RenderEvent;
import minegame159.meteorclient.modules.Module;
import minegame159.meteorclient.modules.Categories;
import minegame159.meteorclient.rendering.Renderer;
import minegame159.meteorclient.settings.BoolSetting;
import minegame159.meteorclient.settings.ColorSetting;
import minegame159.meteorclient.settings.Setting;
import minegame159.meteorclient.settings.SettingGroup;
import minegame159.meteorclient.utils.player.ChatUtils;
import minegame159.meteorclient.utils.render.color.SettingColor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EyeOfEnderEntity;

import java.util.ArrayList;
import java.util.List;

public class StrongholdFinder extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> draw = sgGeneral.add(new BoolSetting.Builder()
            .name("draw")
            .description("Draw line intersections used for calculation.")
            .defaultValue(false)
            .build());
    
    private final Setting<SettingColor> color = sgGeneral.add(new ColorSetting.Builder()
            .name("color")
            .description("Color of line intersections.")
            .defaultValue(new SettingColor(255, 255, 255, 255))
            .build()
    );

    private final List<EyeData> eyes = new ArrayList<>();
    private final List<Line> renderLines = new ArrayList<>();
    private double predictedPosX = 0;
    private double predictedPosZ = 0;

    @Override
    public void onActivate() {
        ChatUtils.moduleInfo(this, "Throw eye of ender at any location to start");
    }

    @Override
    public void onDeactivate() {
        eyes.clear();
        renderLines.clear();
        predictedPosX = 0;
    }

    @EventHandler
    public void onEntityAdded(EntityAddedEvent event) {
        Entity entity = event.entity;
        if (entity instanceof EyeOfEnderEntity) {
            eyes.add(new EyeData(entity.getX(), entity.getY(), entity.getZ()));
            ChatUtils.moduleInfo(this, "Found a eye, waiting it to stop to get angle.");
        }
    }

    @EventHandler
    public void onEntityRemoved(EntityRemovedEvent event) {
        Entity entity = event.entity;
        if (entity instanceof EyeOfEnderEntity && !eyes.isEmpty()) {
            EyeData eye = eyes.get(eyes.size() - 1);
            eye.onRemoved(entity.getX(), entity.getZ());
            renderLines.add(eye.asLine());
            ChatUtils.moduleInfo(this, "Eye stopped. x %f z %f a %f", eye.x, eye.z, Math.toDegrees(eye.angle));
            UpdatePrediction();
        }
    }

    @EventHandler
    public void onRender(RenderEvent event) {
        if (!draw.get() || renderLines.isEmpty()) {
            return;
        }

        for (Line l : renderLines) {
            Renderer.LINES.line(l.x1, l.y1, l.z1, l.x2, l.y2, l.z2, color.get());
        }
        if (predictedPosX != 0) {
            Renderer.LINES.line(predictedPosX, 0, predictedPosZ, predictedPosX, 255, predictedPosZ, color.get());
        }
    }

    private void UpdatePrediction() {
        if (eyes.size() < 2) {
            return;
        }

        predictedPosX = 0;
        predictedPosZ = 0;

        for (int a = 0; a < eyes.size() - 1; a++) {
            for (int b = a + 1; b < eyes.size(); b++) {
                EyeData e1 = eyes.get(a);
                EyeData e2 = eyes.get(b);

                if (!e1.complete || !e2.complete) {
                    continue;
                }

                double m1 = Math.tan(-e1.angle);
                double m2 = Math.tan(-e2.angle);
                double z = (e2.x - e1.x - m2 * e2.z + m1 * e1.z) / (m1 - m2);
                double x1 = m1 * (z - e1.z) + e1.x;
                double x2 = m2 * (z - e2.z) + e2.x;

                if (predictedPosX == 0) {
                    predictedPosX = (x1 + x2) * 0.5;
                    predictedPosZ = z;
                } else {
                    predictedPosX += (x1 + x2) * 0.5;
                    predictedPosZ += z;
                    predictedPosX *= 0.5;
                    predictedPosZ *= 0.5;
                }
            }
        }

        ChatUtils.moduleInfo(this, "Stronghold location is at %f %f (average of prediction from %d eyes)", predictedPosX, predictedPosZ, eyes.size());
    }

    public StrongholdFinder() {
        super(Categories.Misc, "stronghold-finder", "Find location of stronghold with only two eyes.");
    }

    private class Line {
        Line(double x1, double x2, double y1, double y2, double z1, double z2) {
            this.x1 = x1;
            this.x2 = x2;
            this.y1 = y1;
            this.y2 = y2;
            this.z1 = z1;
            this.z2 = z2;
        }
        public double x1;
        public double x2;
        public double y1;
        public double y2;
        public double z1;
        public double z2;
    }

    private class EyeData {
        EyeData(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
            xd = 0;
            zd = 0;
            angle = 0;
            complete = false;
        }
        public void onRemoved(double x, double z) {
            xd = x - this.x;
            zd = z - this.z;
            angle = Math.atan2(zd, xd) - Math.PI * 0.5;
            complete = true;
        }
        public Line asLine() {
            return new Line(x, x + xd * 50000, y, y, z, z + zd * 50000);
        }

        public double x;
        public double y;
        public double z;
        public double xd;
        public double zd;
        public double angle;
        public boolean complete;
    }
}
