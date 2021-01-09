package team.creative.littleframes.common.packet;

import com.creativemd.creativecore.common.packet.CreativeCorePacket;
import com.creativemd.littletiles.common.action.LittleAction;
import com.creativemd.littletiles.common.action.LittleActionException;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.tile.math.location.StructureLocation;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import team.creative.littleframes.common.structure.LittleFrame;

public class LittleFramePacket extends CreativeCorePacket {
    
    public StructureLocation location;
    public boolean playing;
    public int tick;
    
    public LittleFramePacket() {}
    
    public LittleFramePacket(StructureLocation location, boolean playing, int tick) {
        this.location = location;
        this.playing = playing;
        this.tick = tick;
    }
    
    @Override
    public void writeBytes(ByteBuf buf) {
        LittleAction.writeStructureLocation(location, buf);
        buf.writeBoolean(playing);
        buf.writeInt(tick);
    }
    
    @Override
    public void readBytes(ByteBuf buf) {
        location = LittleAction.readStructureLocation(buf);
        playing = buf.readBoolean();
        tick = buf.readInt();
    }
    
    @Override
    public void executeClient(EntityPlayer player) {
        try {
            LittleStructure structure = location.find(player.world);
            if (structure instanceof LittleFrame) {
                LittleFrame frame = (LittleFrame) structure;
                frame.playing = playing;
                frame.tick = tick;
                if (playing)
                    frame.display.resume(frame.getURL(), frame.volume, frame.playing, frame.loop, frame.tick);
                else
                    frame.display.pause(frame.getURL(), frame.volume, frame.playing, frame.loop, frame.tick);
            }
        } catch (LittleActionException e) {}
        
    }
    
    @Override
    public void executeServer(EntityPlayer player) {
        
    }
    
}
