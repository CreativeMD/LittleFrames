package team.creative.littleframes.common.packet;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import team.creative.creativecore.common.network.CreativePacket;
import team.creative.littleframes.common.structure.LittlePictureFrame;
import team.creative.littletiles.common.action.exception.LittleActionException;
import team.creative.littletiles.common.math.location.StructureLocation;
import team.creative.littletiles.common.structure.LittleStructure;

public class LittlePictureFramePacket extends CreativePacket {
    
    public StructureLocation location;
    public boolean playing;
    public int tick;
    
    public LittlePictureFramePacket() {}
    
    public LittlePictureFramePacket(StructureLocation location, boolean playing, int tick) {
        this.location = location;
        this.playing = playing;
        this.tick = tick;
    }
    
    @Override
    public void executeClient(Player player) {
        try {
            LittleStructure structure = location.find(player.level());
            if (structure instanceof LittlePictureFrame) {
                LittlePictureFrame frame = (LittlePictureFrame) structure;
                
                frame.data.tick = tick;
                
                if (frame.display != null) {
                    if (playing)
                        frame.display.resume(frame.data, playing);
                    else
                        frame.display.pause(frame.data, playing);
                }
            }
        } catch (LittleActionException e) {}
    }
    
    @Override
    public void executeServer(ServerPlayer player) {}
    
}
