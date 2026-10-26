package com.github.kay9.dragonmounts.client;

import com.github.kay9.dragonmounts.DMLConfig;
import com.github.kay9.dragonmounts.DragonMountsLegacy;
import com.github.kay9.dragonmounts.dragon.TameableDragon;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.function.Consumer;

public class KeyMappings
{
    private static final String KEY_CATEGORY = "key.category." + DragonMountsLegacy.MOD_ID;

    public static final KeyMapping FLIGHT_DESCENT_KEY = keymap("flight_descent", GLFW.GLFW_KEY_Z);
    public static final KeyMapping CAMERA_CONTROLS = keymap("camera_flight", GLFW.GLFW_KEY_F6);

    @SuppressWarnings({"ConstantConditions"})
    private static KeyMapping keymap(String name, int defaultMapping)
    {
        return new KeyMapping(String.format("key.%s.%s", DragonMountsLegacy.MOD_ID, name), defaultMapping, KEY_CATEGORY);
    }

    public static void registerKeybinds(Consumer<KeyMapping> registrar)
    {
        registrar.accept(FLIGHT_DESCENT_KEY);
        registrar.accept(CAMERA_CONTROLS);
    }

    // Fabric port: replaces the raw-key handleKeyPress(int, int); called from the client tick by DragonMountsFabricClient.
    // The KeyMapping is consumed every tick so presses made while not riding a dragon don't pile up.
    public static void handleCameraControlsKey()
    {
        while (CAMERA_CONTROLS.consumeClick())
        {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player != null && player.getVehicle() instanceof TameableDragon d)
            {
                DMLConfig.CAMERA_DRIVEN_FLIGHT.set(!DMLConfig.cameraDrivenFlight());
                player.displayClientMessage(Component.translatable("mount.dragon.camera_controls." + (DMLConfig.cameraDrivenFlight()? "enabled" : "disabled"), d.getDisplayName()), true);
            }
        }
    }
}
