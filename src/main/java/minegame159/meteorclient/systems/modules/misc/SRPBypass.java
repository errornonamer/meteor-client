/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client/).
 * Copyright (c) 2021 Meteor Development.
 */

package minegame159.meteorclient.systems.modules.misc;

import minegame159.meteorclient.systems.modules.Categories;
import minegame159.meteorclient.systems.modules.Module;

public class SRPBypass extends Module {
    public SRPBypass() {
        super(Categories.Misc, "SRP-bypass", "Spoofs ResourcePackStatus packet to bypass force pack plugins.");
    }
}
