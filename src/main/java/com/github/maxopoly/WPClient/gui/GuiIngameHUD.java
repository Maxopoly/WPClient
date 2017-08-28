package com.github.maxopoly.WPClient.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.fml.common.FMLLog;

public class GuiIngameHUD extends GuiIngameForge {

	public GuiIngameHUD(Minecraft mc) {
		super(mc);
	}

	@Override
	protected void renderAttackIndicator(float p_184045_1_, ScaledResolution p_184045_2_) {
		// super.renderAttackIndicator(p_184045_1_, p_184045_2_);
		FMLLog.getLogger().info("Drawing own indicator");
		int centerX = p_184045_2_.getScaledWidth() / 2;
		int centerZ = p_184045_2_.getScaledHeight() / 2;
		drawModalRectWithCustomSizedTexture(centerX, centerZ, 96.0F, 32.0F, 32, 32, 256.0F, 256.0F);
	}

}
