package team.creative.littleframes.common.data;

import java.net.URI;
import java.net.URISyntaxException;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.littleframes.LittleFrames;

public class LittleFrameData {
    
    public static LittleFrameData ofOldData(CompoundTag nbt) {
        LittleFrameData data = new LittleFrameData();
        try {
            data.uri = new URI(nbt.getString("url"));
        } catch (URISyntaxException e) {
            LittleFrames.LOGGER.error("Tried to load invalid url '{}'", nbt.getString("url"));
            data.uri = null;
        }
        if (nbt.contains("render"))
            data.renderDistance = nbt.getInt("render");
        if (nbt.contains("alpha"))
            data.alpha = nbt.getFloat("alpha");
        if (nbt.contains("brightness"))
            data.brightness = nbt.getFloat("brightness");
        
        data.volume = nbt.getFloat("volume");
        if (nbt.contains("min"))
            data.minDistance = nbt.getFloat("min");
        if (nbt.contains("max"))
            data.maxDistance = nbt.getFloat("max");
        
        data.tick = nbt.getInt("tick");
        data.loop = nbt.getBoolean("loop");
        
        data.refreshInterval = nbt.contains("refresh") ? nbt.getInt("refresh") : -1;
        if (data.refreshInterval > 0)
            data.refreshCounter = data.refreshInterval;
        return data;
    }
    
    private URI uri;
    public float brightness = 1;
    public float alpha = 1;
    
    public int renderDistance = 64;
    
    private float volume = 1;
    public float minDistance = 5;
    public float maxDistance = 20;
    
    public boolean loop = true;
    public int tick = 0;
    
    public int refreshInterval = -1;
    public int refreshCounter = 0;
    
    public LittleFrameData() {}
    
    public LittleFrameData(CompoundTag nbt) {
        try {
            if (nbt.contains("u"))
                uri = new URI(nbt.getString("u"));
            else
                uri = null;
        } catch (URISyntaxException e) {
            LittleFrames.LOGGER.error("Tried to load invalid url '{}'", nbt.getString("u"));
            uri = null;
        }
        if (nbt.contains("d"))
            renderDistance = nbt.getInt("d");
        if (nbt.contains("a"))
            alpha = nbt.getFloat("a");
        if (nbt.contains("b"))
            brightness = nbt.getFloat("b");
        
        volume = nbt.getFloat("v");
        if (nbt.contains("min"))
            minDistance = nbt.getFloat("min");
        if (nbt.contains("max"))
            maxDistance = nbt.getFloat("max");
        
        tick = nbt.getInt("t");
        loop = nbt.getBoolean("l");
        
        refreshInterval = nbt.contains("r") ? nbt.getInt("r") : -1;
        if (refreshInterval > 0)
            refreshCounter = refreshInterval;
    }
    
    public boolean hasURI() {
        return uri != null;
    }
    
    public void setURI(URI uri) {
        if (uri.toString().isBlank())
            this.uri = null;
        else
            this.uri = uri;
    }
    
    public void stop() {
        tick = 0;
    }
    
    public URI getURI() {
        return uri;
    }
    
    public String getURIPath() {
        return uri != null ? uri.toString() : "";
    }
    
    public CompoundTag save() {
        CompoundTag nbt = new CompoundTag();
        if (uri != null)
            nbt.putString("u", uri.toString());
        nbt.putInt("r", renderDistance);
        nbt.putFloat("a", alpha);
        nbt.putFloat("b", brightness);
        
        nbt.putFloat("v", volume);
        nbt.putFloat("min", minDistance);
        nbt.putFloat("max", maxDistance);
        
        nbt.putInt("t", tick);
        nbt.putBoolean("l", loop);
        if (refreshInterval < 0)
            nbt.remove("r");
        else
            nbt.putInt("r", refreshInterval);
        return nbt;
    }
    
    public void volume(float volume) {
        this.volume = volume;
    }
    
    public float volume() {
        return volume;
    }
    
    @OnlyIn(Dist.CLIENT)
    public float ingameVolume() {
        return volume * Minecraft.getInstance().options.getSoundSourceVolume(SoundSource.MASTER);
    }
}
