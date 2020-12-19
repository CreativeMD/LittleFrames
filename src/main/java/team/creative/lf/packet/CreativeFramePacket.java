package team.creative.lf.packet;

import com.creativemd.creativecore.common.packet.CreativeCorePacket;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import team.creative.lf.block.TileEntityCreativeFrame;

public class CreativeFramePacket extends CreativeCorePacket {
	
	public BlockPos pos;
	public boolean playing;
	public int tick;
	
	public CreativeFramePacket() {}
	
	public CreativeFramePacket(BlockPos pos, boolean playing, int tick) {
		this.pos = pos;
		this.playing = playing;
		this.tick = tick;
	}
	
	@Override
	public void writeBytes(ByteBuf buf) {
		writePos(buf, pos);
		buf.writeBoolean(playing);
		buf.writeInt(tick);
	}
	
	@Override
	public void readBytes(ByteBuf buf) {
		pos = readPos(buf);
		playing = buf.readBoolean();
		tick = buf.readInt();
	}
	
	@Override
	public void executeClient(EntityPlayer player) {
		TileEntity te = player.world.getTileEntity(pos);
		if (te instanceof TileEntityCreativeFrame) {
			((TileEntityCreativeFrame) te).playing = playing;
			((TileEntityCreativeFrame) te).tick = tick;
			if (playing)
				((TileEntityCreativeFrame) te).display.resume((TileEntityCreativeFrame) te);
			else
				((TileEntityCreativeFrame) te).display.pause((TileEntityCreativeFrame) te);
		}
	}
	
	@Override
	public void executeServer(EntityPlayer player) {
		
	}
	
}
