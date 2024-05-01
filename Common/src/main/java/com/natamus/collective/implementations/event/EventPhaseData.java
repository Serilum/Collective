package com.natamus.collective.implementations.event;

import net.minecraft.resources.ResourceLocation;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/*
	Original code used from https://github.com/FabricMC/fabric.
 */
class EventPhaseData<T> {
	final ResourceLocation id;
	T[] listeners;
	final List<EventPhaseData<T>> subsequentPhases = new ArrayList<>();
	final List<EventPhaseData<T>> previousPhases = new ArrayList<>();
	int visitStatus = 0; // 0: not visited, 1: visiting, 2: visited

	@SuppressWarnings("unchecked")
	EventPhaseData(ResourceLocation id, Class<?> listenerClass) {
		this.id = id;
		this.listeners = (T[]) Array.newInstance(listenerClass, 0);
	}

	void addListener(T listener) {
		int oldLength = listeners.length;
		listeners = Arrays.copyOf(listeners, oldLength + 1);
		listeners[oldLength] = listener;
	}
}
