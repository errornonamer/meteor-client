package minegame159.meteorclient.modules.world;

import meteordevelopment.orbit.EventHandler;
import minegame159.meteorclient.events.world.TickEvent;
import minegame159.meteorclient.modules.Categories;
import minegame159.meteorclient.modules.Module;
import minegame159.meteorclient.modules.Modules;
import minegame159.meteorclient.utils.world.TickRate;

public class TPSSync extends Module {
    public TPSSync() {
        super(Categories.World, "tps-sync", "Attemps to sync client tickrate with server's");
    }

    private boolean timerState = false;

    @Override
    public void onActivate() {
        Timer timer = Modules.get().get(Timer.class);
        timerState = timer.isActive();
        if (!timerState) {
            timer.toggle();
        }
    }

    @Override
    public void onDeactivate() {
        Timer timer = Modules.get().get(Timer.class);
        timer.setOverride(-1);
        if (timer.isActive() != timerState)
            timer.toggle();
    }
    
    @EventHandler
    private void onTick(TickEvent.Post event) {
        Timer timer = Modules.get().get(Timer.class);
        timer.setOverride(TickRate.INSTANCE.getTickRate() / 20);
    }
}
