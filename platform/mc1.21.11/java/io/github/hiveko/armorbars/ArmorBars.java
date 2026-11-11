package io.github.hiveko.armorbars;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

/**
 * Minecraft 1.21.11 version of the mod (see src/main/java for 26.x): same bars, same position.
 * 1.21.11 is obfuscated, so this is compiled with Mojang names and remapped; "HUD hidden" is
 * Options.hideGui there.
 */
public class ArmorBars implements ClientModInitializer {
    public static final String MOD_ID = "microdurability-armor-bars";

    private static final EquipmentSlot[] ARMOR_SLOTS = {
            EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD
    };
    private static final int BAR_WIDTH = 13;
    private static final int BAR_HEIGHT = 2;
    private static final int BAR_SPACING = 3;

    /** Also show bars for armor that has not been damaged yet (full green bar). */
    private static final boolean SHOW_UNDAMAGED_ARMOR = true;

    @Override
    public void onInitializeClient() {
        HudElementRegistry.attachElementAfter(
                VanillaHudElements.HOTBAR,
                Identifier.fromNamespaceAndPath(MOD_ID, "armor_bars"),
                ArmorBars::render);
    }

    private static void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
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
        return mc.gameMode == null || !mc.gameMode.canHurtPlayer() || mc.options.hideGui;
    }

    private static void renderBar(GuiGraphics graphics, ItemStack stack, int x, int y) {
        if (stack.isEmpty() || !stack.isDamageableItem()) {
            return;
        }
        if (!SHOW_UNDAMAGED_ARMOR && !stack.isDamaged()) {
            return;
        }
        fill(graphics, x, y, BAR_WIDTH, BAR_HEIGHT, 0x000000);
        fill(graphics, x, y, stack.getBarWidth(), 1, stack.getBarColor());
    }

    private static void fill(GuiGraphics graphics, int x, int y, int width, int height, int rgb) {
        graphics.fill(x, y, x + width, y + height, ARGB.opaque(rgb));
    }
}
