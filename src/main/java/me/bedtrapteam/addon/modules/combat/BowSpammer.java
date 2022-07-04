package me.bedtrapteam.addon.modules.combat;

import me.bedtrapteam.addon.BedTrap;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.world.Timer;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

public class BowSpammer extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> actionDelay = sgGeneral.add(new DoubleSetting.Builder().name("action-delay").defaultValue(5).range(0, 10).build());
    private final Setting<Boolean> onRightClick = sgGeneral.add(new BoolSetting.Builder().name("on-right-click").defaultValue(true).build());
    private final Setting<Boolean> timer = sgGeneral.add(new BoolSetting.Builder().name("timer").defaultValue(true).build());
    private final Setting<Double> factor = sgGeneral.add(new DoubleSetting.Builder().name("factor").defaultValue(1.4).range(1, 2).build());
    private final Setting<Boolean> onGround = sgGeneral.add(new BoolSetting.Builder().name("on-ground").defaultValue(true).build());
    private final Setting<Boolean> autoSwap = sgGeneral.add(new BoolSetting.Builder().name("auto-swap").defaultValue(false).build());

    private boolean shouldSwap;
    private int prevSlot;

    public BowSpammer() {
        super(BedTrap.Combat, "bow-spammer", "Spamming arrows with bow.");
    }

    @Override
    public void onActivate() {
        prevSlot = -1;
    }

    @Override
    public void onDeactivate() {
        mc.options.useKey.setPressed(false);
        Modules.get().get(Timer.class).setOverride(1.0F);
    }

    @EventHandler
    public void onTick(TickEvent.Post event) {
        if (onGround.get() && !mc.player.isOnGround()) return;
        if (timer.get() && mc.options.useKey.isPressed()) Modules.get().get(Timer.class).setOverride(factor.get().floatValue());

        doSwap();
        if (holdingInfo() != null) {
            if (mc.player.getItemUseTime() < actionDelay.get()) return;
            if (!onRightClick.get()) mc.options.useKey.setPressed(true);

            mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, mc.player.getHorizontalFacing()));
            mc.getNetworkHandler().sendPacket(new PlayerInteractItemC2SPacket(holdingInfo(), 0));

            mc.player.clearActiveItem();
        } else Modules.get().get(Timer.class).setOverride(1.0F);
    }

    private Hand holdingInfo() {
        if (mc.player.getOffHandStack().getItem() != Items.BOW && mc.player.getMainHandStack().getItem() != Items.BOW)
            return null;

        return mc.player.getMainHandStack().getItem() != Items.BOW ? Hand.OFF_HAND : Hand.MAIN_HAND;
    }

    private void doSwap() {
        if (!autoSwap.get()) return;

        if (mc.options.useKey.isPressed()) {
            if (holdingInfo() != null) return;

            FindItemResult bow = InvUtils.findInHotbar(Items.BOW);
            if (!bow.found()) {
                info("Can't find bow, toggling...");
                toggle();
                return;
            }

            prevSlot = mc.player.getInventory().selectedSlot;
            InvUtils.swap(bow.slot(), false);
            shouldSwap = true;
        } else if (prevSlot != -1 && shouldSwap) {
            InvUtils.swap(prevSlot, false);
            shouldSwap = false;
        }
    }
}
