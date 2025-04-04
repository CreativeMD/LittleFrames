package team.creative.littleframes.client.gui;

import team.creative.creativecore.common.gui.control.simple.GuiSlider;

public class GuiSliderSingleDigit extends GuiSlider {
    
    public GuiSliderSingleDigit(String name, double value, double min, double max) {
        super(name, value, min, max);
    }
    
    @Override
    public void setValue(double value) {
        super.setValue(value != 0 ? Math.round(value * 10) / 10D : 0);
    }
    
}
