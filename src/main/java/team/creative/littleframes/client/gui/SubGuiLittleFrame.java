package team.creative.littleframes.client.gui;

import com.creativemd.creativecore.common.gui.container.SubGui;
import com.creativemd.creativecore.common.gui.controls.gui.GuiAnalogeSlider;
import com.creativemd.creativecore.common.gui.controls.gui.GuiButton;
import com.creativemd.creativecore.common.gui.controls.gui.GuiCheckBox;
import com.creativemd.creativecore.common.gui.controls.gui.GuiIconButton;
import com.creativemd.creativecore.common.gui.controls.gui.GuiLabel;
import com.creativemd.creativecore.common.gui.controls.gui.GuiStateButton;
import com.creativemd.creativecore.common.gui.controls.gui.GuiSteppedSlider;
import com.creativemd.creativecore.common.gui.controls.gui.GuiTextfield;
import com.creativemd.creativecore.common.utils.mc.ColorUtils;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import team.creative.littleframes.LittleFrames;
import team.creative.littleframes.client.texture.TextureCache;
import team.creative.littleframes.client.texture.TextureSeeker;
import team.creative.littleframes.common.structure.LittleFrame;

@SideOnly(Side.CLIENT)
public class SubGuiLittleFrame extends SubGui {
	
	public LittleFrame frame;
	
	public GuiTextfield url;
	public GuiButton save;
	
	public SubGuiLittleFrame(LittleFrame frame) {
		this(frame, false, 16);
	}
	
	public SubGuiLittleFrame(LittleFrame frame, boolean editFacing, int scaleSize) {
		super(200, editFacing ? 220 : 200);
		this.frame = frame;
	}
	
	@Override
	public void createControls() {
		save = new GuiButton(translate("gui.creative_frame.save"), 144, 180, 50) {
			@Override
			public void onClicked(int x, int y, int button) {
				NBTTagCompound nbt = new NBTTagCompound();
				GuiTextfield url = (GuiTextfield) get("url");
				GuiSteppedSlider renderDistance = (GuiSteppedSlider) get("renderDistance");
				
				GuiStateButton fit = (GuiStateButton) get("fit");
				
				GuiAnalogeSlider transparency = (GuiAnalogeSlider) get("transparency");
				GuiAnalogeSlider brightness = (GuiAnalogeSlider) get("brightness");
				
				GuiCheckBox loop = (GuiCheckBox) get("loop");
				GuiAnalogeSlider volume = (GuiAnalogeSlider) get("volume");
				
				nbt.setInteger("fit", fit.getState());
				
				nbt.setInteger("render", (int) renderDistance.value);
				
				nbt.setFloat("transparency", (float) transparency.value);
				nbt.setFloat("brightness", (float) brightness.value);
				
				nbt.setBoolean("loop", loop.value);
				nbt.setFloat("volume", (float) volume.value);
				
				nbt.setString("url", url.text);
				
				nbt.setInteger("type", 0);
				sendPacketToServer(nbt);
			}
		};
		
		url = new GuiUrlTextfield(save, "url", frame.getRealURL(), 0, 0, 194, 16);
		url.maxLength = 512;
		controls.add(url);
		controls.add(new GuiLabel(translate(frame.cache != null && frame.cache.getError() != null ? frame.cache.getError() : ""), 0, 20, ColorUtils.RED));
		
		save.setEnabled(LittleFrames.CONFIG.canUse(mc.player, url.text));
		controls.add(save);
		
		String[] args = new String[LittleFrame.FitMode.values().length];
		for (int i = 0; i < args.length; i++)
			args[i] = translate("gui.little_frame.fitmode." + LittleFrame.FitMode.values()[i].name());
		controls.add(new GuiStateButton("fit", frame.fitMode.ordinal(), 0, 68, 70, args));
		
		controls.add(new GuiLabel(translate("gui.creative_frame.transparency"), 0, 110));
		controls.add(new GuiAnalogeSlider("transparency", 80, 112, 109, 5, frame.alpha, 0, 1));
		
		controls.add(new GuiLabel(translate("gui.creative_frame.brightness"), 0, 122));
		controls.add(new GuiAnalogeSlider("brightness", 80, 124, 109, 5, frame.brightness, 0, 1));
		
		controls.add(new GuiLabel(translate("gui.creative_frame.distance"), 0, 134));
		controls.add(new GuiSteppedSlider("renderDistance", 80, 136, 109, 5, frame.renderDistance, 5, 1024));
		
		controls.add(new GuiCheckBox("loop", translate("gui.creative_frame.loop"), 60, 168, frame.loop));
		controls.add(new GuiLabel(translate("gui.creative_frame.volume"), 0, 186));
		controls.add(new GuiAnalogeSlider("volume", 50, 188, 30, 5, frame.volume, 0, 1));
		
		controls.add(new GuiButton(translate("gui.creative_frame.reload"), 102, 180) {
			
			@Override
			public void onClicked(int x, int y, int button) {
				synchronized (TextureSeeker.LOCK) {
					if (GuiScreen.isShiftKeyDown())
						TextureCache.reloadAll();
					else if (frame.cache != null)
						frame.cache.reload();
				}
			}
		}.setCustomTooltip(translate("gui.creative_frame.reloadtooltip")));
		
		controls.add(new GuiIconButton("play", 0, 168, 10) {
			
			@Override
			public void onClicked(int x, int y, int button) {
				NBTTagCompound nbt = new NBTTagCompound();
				nbt.setBoolean("play", true);
				sendPacketToServer(nbt);
			}
		});
		controls.add(new GuiIconButton("pause", 20, 168, 9) {
			
			@Override
			public void onClicked(int x, int y, int button) {
				NBTTagCompound nbt = new NBTTagCompound();
				nbt.setBoolean("pause", true);
				sendPacketToServer(nbt);
			}
		});
		controls.add(new GuiIconButton("stop", 40, 168, 11) {
			
			@Override
			public void onClicked(int x, int y, int button) {
				NBTTagCompound nbt = new NBTTagCompound();
				nbt.setBoolean("stop", true);
				sendPacketToServer(nbt);
			}
		});
	}
	
}
