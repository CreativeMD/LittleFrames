package team.creative.littleframes.client.gui;

import java.util.ArrayList;

import com.creativemd.creativecore.common.gui.GuiRenderHelper;
import com.creativemd.creativecore.common.gui.client.style.ColoredDisplayStyle;
import com.creativemd.creativecore.common.gui.client.style.Style;
import com.creativemd.creativecore.common.gui.controls.gui.GuiButton;
import com.creativemd.creativecore.common.gui.controls.gui.GuiTextfield;
import com.google.common.collect.Lists;

import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import team.creative.littleframes.LittleFrames;

public class GuiUrlTextfield extends GuiTextfield {
    public static final Style DISABLED = new Style("disabled", new ColoredDisplayStyle(50, 0, 0), new ColoredDisplayStyle(150, 90, 90), new ColoredDisplayStyle(180, 100, 100), new ColoredDisplayStyle(220, 198, 198), new ColoredDisplayStyle(50, 0, 0, 100));
    public static final Style WARNING = new Style("warning", new ColoredDisplayStyle(50, 50, 0), new ColoredDisplayStyle(150, 150, 90), new ColoredDisplayStyle(180, 180, 100), new ColoredDisplayStyle(220, 220, 198), new ColoredDisplayStyle(50, 50, 0, 100));
    private GuiButton saveButton;
    
    public GuiUrlTextfield(GuiButton saveButton, String name, String text, int x, int y, int width, int height) {
        super(name, text, x, y, width, height);
        this.saveButton = saveButton;
    }
    
    @Override
    protected void renderBackground(GuiRenderHelper helper, Style style) {
        if (!canUse(true))
            style = LittleFrames.CONFIG.whitelistEnabled ? DISABLED : WARNING;
        super.renderBackground(helper, style);
    }
    
    @Override
    public boolean onKeyPressed(char character, int key) {
        boolean pressed = super.onKeyPressed(character, key);
        saveButton.setEnabled(canUse(false));
        return pressed;
    }
    
    @Override
    public ArrayList<String> getTooltip() {
        if (!canUse(false))
            return Lists.newArrayList(TextFormatting.RED.toString() + TextFormatting.BOLD.toString() + I18n.translateToLocal("label.littleframes.not_whitelisted.name"));
        else if (!canUse(true))
            return Lists.newArrayList(TextFormatting.GOLD + I18n.translateToLocal("label.littleframes.whitelist_warning.name"));
        return Lists.newArrayList();
    }
    
    protected boolean canUse(boolean ignoreToggle) {
        return LittleFrames.CONFIG.canUse(mc.player, text, ignoreToggle);
    }
}
