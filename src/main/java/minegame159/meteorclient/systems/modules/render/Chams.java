/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client/).
 * Copyright (c) 2021 Meteor Development.
 */

package minegame159.meteorclient.systems.modules.render;

import com.sun.org.apache.xpath.internal.operations.Bool;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import minegame159.meteorclient.settings.*;
import minegame159.meteorclient.systems.modules.Categories;
import minegame159.meteorclient.systems.modules.Module;
import minegame159.meteorclient.utils.Utils;
import minegame159.meteorclient.utils.entity.EntityUtils;
import minegame159.meteorclient.utils.render.color.Color;
import minegame159.meteorclient.utils.render.color.SettingColor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.util.Identifier;
import org.lwjgl.system.CallbackI;

public class Chams extends Module {
    private final SettingGroup sgVisible = settings.createGroup("Visible");
    private final SettingGroup sgThroughWalls = settings.createGroup("Through Walls");
    private final SettingGroup sgPlayers = settings.createGroup("Players");
    private final SettingGroup sgCrystals = settings.createGroup("Crystals");
    private final SettingGroup sgHand = settings.createGroup("Hand");

    // Visible
    public final Setting<Boolean> overrideVisible = sgVisible.add(new BoolSetting.Builder()
            .name("override-visible")
            .description("Override rendering of visible entity.")
            .defaultValue(false)
            .build()
    );

    private final Setting<SettingColor> playersColor = sgVisible.add(new ColorSetting.Builder()
            .name("players-color")
            .description("The other player's color.")
            .defaultValue(new SettingColor(255, 255, 255))
            .build()
    );

    private final Setting<SettingColor> animalsColor = sgVisible.add(new ColorSetting.Builder()
            .name("animals-color")
            .description("The animal's color.")
            .defaultValue(new SettingColor(25, 255, 25, 255))
            .build()
    );

    private final Setting<SettingColor> waterAnimalsColor = sgVisible.add(new ColorSetting.Builder()
            .name("water-animals-color")
            .description("The water animal's color.")
            .defaultValue(new SettingColor(25, 25, 255, 255))
            .build()
    );

    private final Setting<SettingColor> monstersColor = sgVisible.add(new ColorSetting.Builder()
            .name("monsters-color")
            .description("The monster's color.")
            .defaultValue(new SettingColor(255, 25, 25, 255))
            .build()
    );

    private final Setting<SettingColor> ambientColor = sgVisible.add(new ColorSetting.Builder()
            .name("ambient-color")
            .description("The ambient's color.")
            .defaultValue(new SettingColor(25, 25, 25, 255))
            .build()
    );

    private final Setting<SettingColor> miscColor = sgVisible.add(new ColorSetting.Builder()
            .name("misc-color")
            .description("The misc color.")
            .defaultValue(new SettingColor(175, 175, 175, 255))
            .build()
    );

    private final Setting<Boolean> useNameColor = sgVisible.add(new BoolSetting.Builder()
            .name("use-name-color")
            .description("Uses players name color for the color.")
            .defaultValue(false)
            .build()
    );

    public final Setting<Boolean> texture = sgVisible.add(new BoolSetting.Builder()
            .name("texture")
            .description("Enables textures.")
            .defaultValue(false)
            .build()
    );

    public final Setting<Boolean> fullbright = sgVisible.add(new BoolSetting.Builder()
            .name("fullbright")
            .description("Enables fullbright.")
            .defaultValue(false)
            .build()
    );

    // Through walls
    public final Setting<Object2BooleanMap<EntityType<?>>> entities = sgThroughWalls.add(new EntityTypeListSetting.Builder()
            .name("entities")
            .description("Select entities to show through walls.")
            .defaultValue(Utils.asObject2BooleanOpenHashMap(EntityType.PLAYER))
            .build()
    );

    private final Setting<SettingColor> playersColorInv = sgThroughWalls.add(new ColorSetting.Builder()
            .name("players-color-invisible")
            .description("The other player's color.")
            .defaultValue(new SettingColor(255, 255, 255))
            .build()
    );

    private final Setting<SettingColor> animalsColorInv = sgThroughWalls.add(new ColorSetting.Builder()
            .name("animals-color-invisible")
            .description("The animal's color.")
            .defaultValue(new SettingColor(25, 255, 25, 255))
            .build()
    );

    private final Setting<SettingColor> waterAnimalsColorInv = sgThroughWalls.add(new ColorSetting.Builder()
            .name("water-animals-color-invisible")
            .description("The water animal's color.")
            .defaultValue(new SettingColor(25, 25, 255, 255))
            .build()
    );

    private final Setting<SettingColor> monstersColorInv = sgThroughWalls.add(new ColorSetting.Builder()
            .name("monsters-color-invisible")
            .description("The monster's color.")
            .defaultValue(new SettingColor(255, 25, 25, 255))
            .build()
    );

    private final Setting<SettingColor> ambientColorInv = sgThroughWalls.add(new ColorSetting.Builder()
            .name("ambient-color-invisible")
            .description("The ambient's color.")
            .defaultValue(new SettingColor(25, 25, 25, 255))
            .build()
    );

    private final Setting<SettingColor> miscColorInv = sgThroughWalls.add(new ColorSetting.Builder()
            .name("misc-color-invisible")
            .description("The misc color.")
            .defaultValue(new SettingColor(175, 175, 175, 255))
            .build()
    );

    private final Setting<Boolean> useNameColorInv = sgThroughWalls.add(new BoolSetting.Builder()
            .name("use-name-color-invisible")
            .description("Uses players name color for the color.")
            .defaultValue(false)
            .build()
    );

    public final Setting<Boolean> textureInv = sgThroughWalls.add(new BoolSetting.Builder()
            .name("texture-invisible")
            .description("Enables textures.")
            .defaultValue(false)
            .build()
    );

    public final Setting<Boolean> fullbrightInv = sgThroughWalls.add(new BoolSetting.Builder()
            .name("fullbright-invisible")
            .description("Enables fullbright.")
            .defaultValue(false)
            .build()
    );

    //Players

    public final Setting<Boolean> players = sgPlayers.add(new BoolSetting.Builder()
            .name("enabled")
            .description("Enables model tweaks for players.")
            .defaultValue(true)
            .build()
    );

    public final Setting<Boolean> ignoreSelf = sgPlayers.add(new BoolSetting.Builder()
            .name("ignore-self")
            .description("Ignores yourself when tweaking player models.")
            .defaultValue(true)
            .build()
    );

    public final Setting<Double> playersScale = sgPlayers.add(new DoubleSetting.Builder()
            .name("scale")
            .description("Players scale.")
            .defaultValue(1.0)
            .min(0.0)
            .build()
    );

    //Crystals

    public final Setting<Boolean> crystals = sgCrystals.add(new BoolSetting.Builder()
            .name("enabled")
            .description("Enables model tweaks for end crystals.")
            .defaultValue(true)
            .build()
    );

    public final Setting<Double> crystalsScale = sgCrystals.add(new DoubleSetting.Builder()
            .name("scale")
            .description("Crystal scale.")
            .defaultValue(0.6)
            .min(0)
            .build()
    );

    public final Setting<Double> crystalsBounce = sgCrystals.add(new DoubleSetting.Builder()
            .name("bounce")
            .description("How high crystals bounce.")
            .defaultValue(0.3)
            .min(0.0)
            .build()
    );

    public final Setting<Double> crystalsRotationSpeed = sgCrystals.add(new DoubleSetting.Builder()
            .name("rotation-speed")
            .description("Multiplies the roation speed of the crystal.")
            .defaultValue(3)
            .min(0)
            .build()
    );

    public final Setting<Boolean> crystalsTexture = sgCrystals.add(new BoolSetting.Builder()
            .name("texture")
            .description("Whether to render crystal model textures.")
            .defaultValue(false)
            .build()
    );

    public final Setting<Boolean> renderCore = sgCrystals.add(new BoolSetting.Builder()
            .name("render-core")
            .description("Enables rendering of the core of the crystal.")
            .defaultValue(false)
            .build()
    );

    public final Setting<SettingColor> crystalsCoreColor = sgCrystals.add(new ColorSetting.Builder()
            .name("core-color")
            .description("The color of end crystal models.")
            .defaultValue(new SettingColor(0, 255, 255, 100))
            .build()
    );

    public final Setting<Boolean> renderFrame1 = sgCrystals.add(new BoolSetting.Builder()
            .name("render-frame-1")
            .description("Enables rendering of the frame of the crystal.")
            .defaultValue(true)
            .build()
    );

    public final Setting<SettingColor> crystalsFrame1Color = sgCrystals.add(new ColorSetting.Builder()
            .name("frame-1-color")
            .description("The color of end crystal models.")
            .defaultValue(new SettingColor(0, 255, 255, 100))
            .build()
    );

    public final Setting<Boolean> renderFrame2 = sgCrystals.add(new BoolSetting.Builder()
            .name("render-frame-2")
            .description("Enables rendering of the frame of the crystal.")
            .defaultValue(true)
            .build()
    );

    public final Setting<SettingColor> crystalsFrame2Color = sgCrystals.add(new ColorSetting.Builder()
            .name("frame-2-color")
            .description("The color of end crystal models.")
            .defaultValue(new SettingColor(0, 255, 255, 100))
            .build()
    );

    // Hand
    public final Setting<Boolean> hand = sgHand.add(new BoolSetting.Builder()
            .name("enabled")
            .description("Enables tweaks of hand rendering.")
            .defaultValue(true)
            .build()
    );

    public final Setting<Boolean> handTexture = sgHand.add(new BoolSetting.Builder()
            .name("texture")
            .description("Whether to render hand textures.")
            .defaultValue(false)
            .build()
    );

    public final Setting<SettingColor> handColor = sgHand.add(new ColorSetting.Builder()
            .name("hand-color")
            .description("The color of your hand.")
            .defaultValue(new SettingColor(0, 255, 255, 100))
            .build()
    );

    public boolean xqz = false;
    public boolean rendering = false;

    public Color getColor(Entity entity) {
        if (xqz) return EntityUtils.getEntityColor(entity, playersColorInv.get(), animalsColorInv.get(), waterAnimalsColorInv.get(), monstersColorInv.get(), ambientColorInv.get(), miscColorInv.get(), useNameColorInv.get());
        return EntityUtils.getEntityColor(entity, playersColor.get(), animalsColor.get(), waterAnimalsColor.get(), monstersColor.get(), ambientColor.get(), miscColor.get(), useNameColor.get());
    }

    public static final Identifier BLANK = new Identifier("meteor-client", "textures/blank.png");

    public Chams() {
        super(Categories.Render, "chams", "Renders entities through walls.");
    }

    public boolean shouldRender(Entity entity) {
        return isActive() && entities.get().getBoolean(entity.getType());
    }
}