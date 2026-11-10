package io.github.hiveko.armorbars;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

/**
 * Draws a tiny durability bar for each worn armor piece above the hotbar (boots at the bottom,
 * helmet at the top). Based on microDurability by pajic (MIT): same position and look, but only
 * the armor bars, without the low-durability warning or a config screen.
 */
public class ArmorBars implements ClientModInitializer {
    public static final String MOD_ID = "microdurability-armor-bars";

    private static final EquipmentSlot[] ARMOR_SLOTS = {
            EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD
    };
    private static final int BAR_WIDTH = 13;
    private static final int BAR_HEIGHT = 2;
    private static final int BAR_SPACING = 3;

    /**
     * "HUD hidden" (F1) check. 26.1.x keeps it in Options.hideGui, 26.2 and newer in
     * Gui.hud.isHidden(). Minecraft 26.x is not obfuscated, so the one that exists is looked up
     * by name once; this keeps one jar for all versions.
     */
    private static final MethodHandle OPTIONS_HIDE_GUI;
    private static final MethodHandle GUI_HUD;
    private static final MethodHandle HUD_IS_HIDDEN;

    static {
        MethodHandles.Lookup lookup = MethodHandles.publicLookup();
        MethodHandle hideGui = null, hud = null, isHidden = null;
        try {
            hideGui = lookup.findGetter(net.minecraft.client.Options.class, "hideGui", boolean.class);
        } catch (ReflectiveOperationException e) {
            try {
                Class<?> hudClass = Class.forName("net.minecraft.client.gui.Hud");
                hud = lookup.findGetter(net.minecraft.client.gui.Gui.class, "hud", hudClass);
                isHidden = lookup.findVirtual(hudClass, "isHidden", MethodType.methodType(boolean.class));
            } catch (ReflectiveOperationException e2) {
                // neither found: the bars are simply also shown with F1
            }
        }
        OPTIONS_HIDE_GUI = hideGui;
        GUI_HUD = hud;
        HUD_IS_HIDDEN = isHidden;
    }

    /** Also show bars for armor that has not been damaged yet (full green bar). */
    private static final boolean SHOW_UNDAMAGED_ARMOR = true;

    @Override
    public void onInitializeClient() {
        HudElementRegistry.attachElementAfter(
                VanillaHudElements.HOTBAR,
                Identifier.fromNamespaceAndPath(MOD_ID, "armor_bars"),
                ArmorBars::render);
    }

    private static void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || isStatusAreaHidden(mc)) {
            return;
        }

        int x = mc.getWindow().getGuiScaledWidth() / 2 - 7;
        int y = mc.getWindow().getGuiScaledHeight() - 30;
        if (player.experienceLevel > 0) {
            y -= 6;
        }

        for (EquipmentSlot slot : ARMOR_SLOTS) {
            y -= BAR_SPACING;
            renderBar(graphics, player.getItemBySlot(slot), x, y);
        }
    }

    /** Hidden in creative/spectator (no health bar) and when the HUD is hidden (F1). */
    private static boolean isStatusAreaHidden(Minecraft mc) {
        return mc.gameMode == null || !mc.gameMode.canHurtPlayer() || isHudHidden(mc);
    }

    private static boolean isHudHidden(Minecraft mc) {
        try {
            if (OPTIONS_HIDE_GUI != null) {
                return (boolean) OPTIONS_HIDE_GUI.invoke(mc.options);
            }
            if (GUI_HUD != null) {
                return (boolean) HUD_IS_HIDDEN.invoke(GUI_HUD.invoke(mc.gui));
            }
        } catch (Throwable ignored) {
        }
        return false;
    }

    private static void renderBar(GuiGraphicsExtractor graphics, ItemStack stack, int x, int y) {
        if (stack.isEmpty() || !stack.isDamageableItem()) {
            return;
        }
        if (!SHOW_UNDAMAGED_ARMOR && !stack.isDamaged()) {
            return;
        }
        fill(graphics, x, y, BAR_WIDTH, BAR_HEIGHT, 0x000000);
        fill(graphics, x, y, stack.getBarWidth(), 1, stack.getBarColor());
    }

    private static void fill(GuiGraphicsExtractor graphics, int x, int y, int width, int height, int rgb) {
        graphics.fill(x, y, x + width, y + height, ARGB.opaque(rgb));
    }
}
