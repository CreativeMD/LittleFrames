package team.creative.littleframes.client.gui;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import team.creative.creativecore.common.gui.controls.simple.GuiButton;
import team.creative.creativecore.common.gui.controls.simple.GuiTextfield;
import team.creative.creativecore.common.gui.style.GuiStyle;
import team.creative.creativecore.common.gui.style.display.StyleDisplay;
import team.creative.creativecore.common.util.text.TextBuilder;
import team.creative.littleframes.LittleFrames;

public class GuiUrlTextfield extends GuiTextfield {
    
    private final GuiButton saveButton;
    
    public GuiUrlTextfield(GuiButton saveButton, String name, String text) {
        super(name, text);
        this.saveButton = saveButton;
        this.setMaxStringLength(2048);
    }
    
    @Override
    public StyleDisplay getBorder(GuiStyle style, StyleDisplay display) {
        if (!canUse(true))
            return LittleFrames.CONFIG.whitelistEnabled ? GuiWarningStyles.DISABLED_BORDER : GuiWarningStyles.WARNING_BORDER;
        return super.getBorder(style, display);
    }
    
    @Override
    public StyleDisplay getBackground(GuiStyle style, StyleDisplay display) {
        if (!canUse(true))
            return LittleFrames.CONFIG.whitelistEnabled ? GuiWarningStyles.DISABLED_BACKGROUND : GuiWarningStyles.WARNING_BACKGROUND;
        return super.getBackground(style, display);
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        boolean pressed = super.keyPressed(keyCode, scanCode, modifiers);
        saveButton.setEnabled(canUse(false));
        return pressed;
    }
    
    @Override
    public List<Component> getTooltip() {
        if (!canUse(false))
            return new TextBuilder().text(ChatFormatting.RED + "" + ChatFormatting.BOLD).translate("label.littleframes.not_whitelisted").build();
        else if (!canUse(true))
            return new TextBuilder().text(ChatFormatting.GOLD + "").translate("label.littleframes.whitelist_warning").build();
        return null;
    }
    
    protected boolean canUse(boolean ignoreToggle) {
        return LittleFrames.CONFIG.canUse(getPlayer(), getText(), ignoreToggle);
    }
}
