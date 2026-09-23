package com.znistudios.modstudio.client.mixin;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.sugar.Local;
import com.znistudios.modstudio.client.entry.StudioEntryPoints;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.network.chat.Component;

/**
 * Adds the Zni's Mod Studio icon button to the pause menu's existing row of small icon
 * buttons. This is the only vanilla change the mod makes: one child added to one layout,
 * before vanilla arranges it, so spacing and centering stay vanilla's.
 *
 * <p>{@code require = 0}: if a future Minecraft update moves this code, the game still
 * loads and {@code StudioEntryPoints} places the button beside "Back to Game" instead.
 */
@Mixin(PauseScreen.class)
public abstract class PauseScreenMixin extends Screen {
	protected PauseScreenMixin(Component title) {
		super(title);
	}

	@Definition(id = "integratedServer", local = @Local(type = IntegratedServer.class, name = "integratedServer"))
	@Expression("integratedServer = ?")
	@Inject(method = "createPauseMenu", at = @At("MIXINEXTRAS:EXPRESSION"), require = 0)
	private void znis_mod_studio$addIconButton(CallbackInfo ci, @Local(name = "iconButtonRow") LinearLayout iconButtonRow) {
		iconButtonRow.addChild(StudioEntryPoints.createButton(this));
	}
}
