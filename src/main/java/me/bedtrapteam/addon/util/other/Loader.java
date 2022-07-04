package me.bedtrapteam.addon.util.other;

import me.bedtrapteam.addon.BedTrap;
import me.bedtrapteam.addon.commands.MoneyMod;
import me.bedtrapteam.addon.commands.Move;
import me.bedtrapteam.addon.modules.combat.AutoCrystal;
import me.bedtrapteam.addon.modules.combat.SilentCity;
import me.bedtrapteam.addon.modules.info.*;
import me.bedtrapteam.addon.modules.combat.*;
import me.bedtrapteam.addon.modules.hud.*;
import me.bedtrapteam.addon.modules.misc.*;
import me.bedtrapteam.addon.modules.combat.HeadProtect;
import me.bedtrapteam.addon.util.advanced.BedUtils;
import me.bedtrapteam.addon.util.advanced.CrystalUtils;
import me.bedtrapteam.addon.util.advanced.DeathUtils;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.systems.Systems;
import meteordevelopment.meteorclient.systems.commands.Commands;
import meteordevelopment.meteorclient.systems.hud.HUD;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.option.ServerList;

import java.lang.invoke.MethodHandles;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class Loader {
    public static ExecutorService executor = Executors.newSingleThreadExecutor();
    public static ExecutorService moduleExecutor = Executors.newFixedThreadPool(5);

    public static void init() {
        addCPvP();

        MeteorClient.EVENT_BUS.registerLambdaFactory("me.bedtrapteam.addon", (lookupInMethod, klass) -> (MethodHandles.Lookup) lookupInMethod.invoke(null, klass, MethodHandles.lookup())); // event handler
        BedUtils.init();
        CrystalUtils.init();
        DeathUtils.init();
        postInit();
    }

    public static void postInit() {
        //Modules
        BedTrap.addModules(
                // Info
                new AutoLogin(),
                new ChatEncrypt(),
                new Notifications(),
                new ChatConfig(),
                new AutoReKit(),
                new AutoExcuse(),
                new RPC(),
                new AutoEz(),
                new KillFx(),

                // Combat
                new QQuiver(),
                new AutoCrystal(),
                new AntiSurroundBlocks(),
                new AutoMinecart(),
                new BowBomb(),
                new Burrow(),
                new PistonAura(),
                new BedBomb(),
                new CevBreaker(),
                new HoleFill(),
                new SilentCity(),
                new CityBreaker(),
                new HeadProtect(),
                new SelfTrap(),
                new Surround(),
                new TNTAura(),
                new AutoTrap(),
                new PistonPush(),
                new AntiRegear(),
                new AnchorBomb(),
                new BowSpammer(),
                new AutoCrystalRewrite(),
                new FeetTrap(),

                // Misc
                new AntiRespawnLose(),
                new AutoBuild(),
                new ChestExplorer(),
                new OffHando(),
                new HandTweaks(),
                new LogOut(),
                new AntiLay(),
                new MultiTask(),
                new EFly(),
                new OldAnvil(),
                new Strafe(),
                new Phase(),
                new BedCrafter(),
                new Sevila(),
                new ChorusPredict(),
                new PistonPush(),
                new TimerFall(),
                new AutoBedTrap(),
                new LogSpots(),
                new PingSpoof(),
                new MiningTS()
        );

        // Hud
        HUD hud = Systems.get(HUD.class);
        hud.elements.add(new BedTrapHud(hud));
        hud.elements.add(new BetterCordsHud(hud));
        hud.elements.add(new WatermarkHud(hud));
        hud.elements.add(new WelcomeHud(hud));
        hud.elements.add(new ToastNotifications(hud));
        hud.elements.add(new OnlineTimeHud(hud));
        hud.elements.add(new CPSHud(hud));
        hud.elements.add(new YawHud(hud));
        hud.elements.add(new ArmorHud(hud));

        // Commands
        Commands.get().add(new MoneyMod());
        Commands.get().add(new Move());
    }

    public static void addCPvP(){
            ServerList servers = new ServerList(mc);
            servers.loadFile();

            boolean found = false;
            for (int i = 0; i < servers.size(); i++) {
                ServerInfo server = servers.get(i);

                if (server.address.contains("eu.cpvp.me")) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                servers.add(new ServerInfo("cpvp.me", "eu.cpvp.me", false), false);
                servers.saveFile();
            }
        }


    // Shutdown background auth threads
    public static void shutdown() {
        executor.shutdown();
        moduleExecutor.shutdown();
    }
}
