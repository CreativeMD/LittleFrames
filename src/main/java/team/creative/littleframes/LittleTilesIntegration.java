package team.creative.littleframes;

import team.creative.creativecore.common.gui.creator.GuiCreator;
import team.creative.littleframes.client.gui.GuiLittlePictureFrame;
import team.creative.littleframes.common.structure.LittlePictureFrame;
import team.creative.littletiles.common.gui.handler.LittleStructureGuiCreator;

public class LittleTilesIntegration {
    
    public static final LittleStructureGuiCreator LITTLE_FRAME_GUI = GuiCreator.register("little_frame",
        new LittleStructureGuiCreator((nbt, player, structure) -> new GuiLittlePictureFrame((LittlePictureFrame) structure)));
    
    public static void init() {}
}
