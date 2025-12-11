package com.natamus.collective.fabric.callbacks;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;

public class CollectiveSoundEvents {
	private CollectiveSoundEvents() { }
	 
    public static final Event<Sound_Play> SOUND_PLAY = EventFactory.createArrayBacked(Sound_Play.class, callbacks -> (soundEngine, soundInstance) -> {
        for (Sound_Play callback : callbacks) {
        	if (!callback.onSoundPlay(soundEngine, soundInstance)) {
        		return false;
        	}
        }
        
        return true;
    });
    
	@FunctionalInterface
	public interface Sound_Play {
		 boolean onSoundPlay(SoundEngine soundEngine, SoundInstance soundInstance);
	}
}
