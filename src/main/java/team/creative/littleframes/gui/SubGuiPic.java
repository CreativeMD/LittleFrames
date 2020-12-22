package team.creative.littleframes.gui;

import com.creativemd.creativecore.common.gui.container.SubGui;
import com.creativemd.creativecore.common.gui.controls.gui.GuiAnalogeSlider;
import com.creativemd.creativecore.common.gui.controls.gui.GuiButton;
import com.creativemd.creativecore.common.gui.controls.gui.GuiCheckBox;
import com.creativemd.creativecore.common.gui.controls.gui.GuiIconButton;
import com.creativemd.creativecore.common.gui.controls.gui.GuiLabel;
import com.creativemd.creativecore.common.gui.controls.gui.GuiStateButton;
import com.creativemd.creativecore.common.gui.controls.gui.GuiSteppedSlider;
import com.creativemd.creativecore.common.gui.controls.gui.GuiTextfield;
import com.creativemd.creativecore.common.gui.event.gui.GuiControlClickEvent;
import com.creativemd.creativecore.common.utils.mc.ColorUtils;
import com.n247s.api.eventapi.eventsystem.CustomEventSubscribe;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import team.creative.littleframes.LittleFrames;
import team.creative.littleframes.block.TileEntityCreativeFrame;
import team.creative.littleframes.client.texture.TextureCache;
import team.creative.littleframes.client.texture.TextureSeeker;

@SideOnly(Side.CLIENT)
public class SubGuiPic extends SubGui {
	
	public TileEntityCreativeFrame frame;
	
	public float scaleMultiplier;
	
	public GuiTextfield url;
	public GuiButton save;
	
	public SubGuiPic(TileEntityCreativeFrame frame) {
		this(frame, false, 16);
	}
	
	public SubGuiPic(TileEntityCreativeFrame frame, boolean editFacing, int scaleSize) {
		super(200, editFacing ? 220 : 200);
		this.frame = frame;
		this.scaleMultiplier = 1F / (scaleSize);
	}
	
	@Override
	public void createControls() {
		url = new GuiUrlTextfield(this, "url", frame.getRealURL(), 0, 0, 194, 16);
		url.maxLength = 512;
		controls.add(url);
		controls.add(new GuiLabel(translate(frame.cache != null && frame.cache.getError() != null ? frame.cache.getError() : ""), 0, 20, ColorUtils.RED));
		controls.add(new GuiButton("in-size-x", "<", 49, 30, 5, 12) {
			
			@Override
			public void onClicked(int x, int y, int button) {
				GuiTextfield sizeX = (GuiTextfield) get("sizeX");
				float width = 1;
				try {
					width = Float.parseFloat(sizeX.text);
				} catch (Exception e) {
					width = 1;
				}
				int scaled = (int) (width / scaleMultiplier);
				scaled++;
				sizeX.text = Float.toString(scaled * scaleMultiplier);
			}
		}.setRotation(90));
		controls.add(new GuiButton("de-size-x", ">", 49, 40, 5, 12) {
			
			@Override
			public void onClicked(int x, int y, int button) {
				GuiTextfield sizeX = (GuiTextfield) get("sizeX");
				float width = 1;
				try {
					width = Float.parseFloat(sizeX.text);
				} catch (Exception e) {
					width = 1;
				}
				int scaled = (int) (width / scaleMultiplier);
				scaled--;
				sizeX.text = Float.toString(scaled * scaleMultiplier);
			}
		}.setRotation(90));
		
		controls.add(new GuiButton("in-size-y", "<", 145, 30, 5, 12) {
			
			@Override
			public void onClicked(int x, int y, int button) {
				GuiTextfield sizeY = (GuiTextfield) get("sizeY");
				float height = 1;
				try {
					height = Float.parseFloat(sizeY.text);
				} catch (Exception e) {
					height = 1;
				}
				int scaled = (int) (height / scaleMultiplier);
				scaled++;
				sizeY.text = Float.toString(scaled * scaleMultiplier);
			}
		}.setRotation(90));
		controls.add(new GuiButton("de-size-y", ">", 145, 40, 5, 12) {
			
			@Override
			public void onClicked(int x, int y, int button) {
				GuiTextfield sizeY = (GuiTextfield) get("sizeY");
				float height = 1;
				try {
					height = Float.parseFloat(sizeY.text);
				} catch (Exception e) {
					height = 1;
				}
				int scaled = (int) (height / scaleMultiplier);
				scaled--;
				sizeY.text = Float.toString(scaled * scaleMultiplier);
			}
		}.setRotation(90));
		
		controls.add(new GuiTextfield("sizeX", frame.getSizeX() + "", 0, 33, 40, 15).setFloatOnly());
		controls.add(new GuiTextfield("sizeY", frame.getSizeY() + "", 96, 33, 40, 15).setFloatOnly());
		
		controls.add(new GuiButton("reX", "x->y", 62, 33, 25, 15) {
			@Override
			public void onClicked(int x, int y, int button) {}
		});
		
		controls.add(new GuiButton("reY", "y->x", 158, 33, 25, 15) {
			@Override
			public void onClicked(int x, int y, int button) {}
		});
		
		controls.add(new GuiCheckBox("flipX", "flip (x-axis)", 0, 54, frame.flipX));
		controls.add(new GuiCheckBox("flipY", "flip (y-axis)", 75, 54, frame.flipY));
		
		controls.add(new GuiStateButton("posX", frame.min.x == 0 ? 0 : frame.max.x == 1 ? 2 : 1, 0, 68, 70, "left (x)", "center (x)", "right (x)"));
		controls.add(new GuiStateButton("posY", frame.min.y == 0 ? 0 : frame.max.y == 1 ? 2 : 1, 80, 68, 70, "top (y)", "center (y)", "bottom (y)"));
		
		controls.add(new GuiAnalogeSlider("rotation", 0, 93, 80, 10, frame.rotation, 0, 360));
		
		controls.add(new GuiCheckBox("visibleFrame", "visible frame", 90, 88, frame.visibleFrame));
		controls.add(new GuiCheckBox("bothSides", "both sides", 90, 99, frame.bothSides));
		
		controls.add(new GuiLabel("transparency:", 0, 110));
		controls.add(new GuiAnalogeSlider("transparency", 80, 112, 109, 5, frame.alpha, 0, 1));
		
		controls.add(new GuiLabel("brightness:", 0, 122));
		controls.add(new GuiAnalogeSlider("brightness", 80, 124, 109, 5, frame.brightness, 0, 1));
		
		controls.add(new GuiLabel("distance:", 0, 134));
		controls.add(new GuiSteppedSlider("renderDistance", 80, 136, 109, 5, frame.renderDistance, 5, 1024));
		
		controls.add(new GuiCheckBox("loop", "loop", 60, 168, frame.loop));
		controls.add(new GuiLabel("volume:", 0, 186));
		controls.add(new GuiAnalogeSlider("volume", 50, 188, 30, 5, frame.volume, 0, 1));
		
		controls.add(new GuiButton("reload", 102, 180) {
			
			@Override
			public void onClicked(int x, int y, int button) {
				synchronized (TextureSeeker.LOCK) {
					if (GuiScreen.isShiftKeyDown())
						TextureCache.reloadAll();
					else if (frame.cache != null)
						frame.cache.reload();
				}
			}
		}.setCustomTooltip("Hold shift to reload all"));
		
		save = new GuiButton("Save", 144, 180, 50) {
			@Override
			public void onClicked(int x, int y, int button) {
				NBTTagCompound nbt = new NBTTagCompound();
				GuiTextfield url = (GuiTextfield) get("url");
				GuiTextfield sizeX = (GuiTextfield) get("sizeX");
				GuiTextfield sizeY = (GuiTextfield) get("sizeY");
				
				GuiStateButton buttonPosX = (GuiStateButton) get("posX");
				GuiStateButton buttonPosY = (GuiStateButton) get("posY");
				GuiAnalogeSlider rotation = (GuiAnalogeSlider) get("rotation");
				
				GuiCheckBox flipX = (GuiCheckBox) get("flipX");
				GuiCheckBox flipY = (GuiCheckBox) get("flipY");
				GuiCheckBox visibleFrame = (GuiCheckBox) get("visibleFrame");
				GuiCheckBox bothSides = (GuiCheckBox) get("bothSides");
				
				GuiSteppedSlider renderDistance = (GuiSteppedSlider) get("renderDistance");
				
				GuiAnalogeSlider transparency = (GuiAnalogeSlider) get("transparency");
				GuiAnalogeSlider brightness = (GuiAnalogeSlider) get("brightness");
				
				GuiCheckBox loop = (GuiCheckBox) get("loop");
				GuiAnalogeSlider volume = (GuiAnalogeSlider) get("volume");
				
				nbt.setByte("posX", (byte) buttonPosX.getState());
				nbt.setByte("posY", (byte) buttonPosY.getState());
				
				nbt.setFloat("rotation", (float) rotation.value);
				
				nbt.setBoolean("flipX", flipX.value);
				nbt.setBoolean("flipY", flipY.value);
				nbt.setBoolean("visibleFrame", visibleFrame.value);
				nbt.setBoolean("bothSides", bothSides.value);
				
				nbt.setInteger("render", (int) renderDistance.value);
				
				nbt.setFloat("transparency", (float) transparency.value);
				nbt.setFloat("brightness", (float) brightness.value);
				
				nbt.setBoolean("loop", loop.value);
				nbt.setFloat("volume", (float) volume.value);
				
				nbt.setString("url", url.text);
				float posX = 1;
				float posY = 1;
				try {
					posX = Float.parseFloat(sizeX.text);
				} catch (Exception e) {
					posX = 1;
				}
				try {
					posY = Float.parseFloat(sizeY.text);
				} catch (Exception e) {
					posY = 1;
				}
				nbt.setFloat("x", posX);
				nbt.setFloat("y", posY);
				
				nbt.setInteger("type", 0);
				sendPacketToServer(nbt);
			}
		};
		save.setEnabled(LittleFrames.CONFIG.canUse(mc.player, url.text));
		controls.add(save);
		
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
	
	@CustomEventSubscribe
	public void onClicked(GuiControlClickEvent event) {
		if (event.source.is("reX") || event.source.is("reY")) {
			GuiTextfield sizeXField = (GuiTextfield) get("sizeX");
			GuiTextfield sizeYField = (GuiTextfield) get("sizeY");
			
			float x = 1;
			try {
				x = Float.parseFloat(sizeXField.text);
			} catch (Exception e) {
				x = 1;
			}
			
			float y = 1;
			try {
				y = Float.parseFloat(sizeYField.text);
			} catch (Exception e) {
				y = 1;
			}
			
			if (frame.display != null) {
				if (event.source.is("reX")) {
					sizeYField.text = "" + (frame.display.getHeight() / (frame.display.getWidth() / x));
				} else {
					sizeXField.text = "" + (frame.display.getWidth() / (frame.display.getHeight() / y));
				}
			}
		}
	}
	
}
