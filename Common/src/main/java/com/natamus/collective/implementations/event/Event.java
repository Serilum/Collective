package com.natamus.collective.implementations.event;

import com.natamus.collective.util.CollectiveReference;
import net.minecraft.resources.ResourceLocation;

/*
	Original code used from https://github.com/FabricMC/fabric.
 */
public abstract class Event<T> {
	/**
	 * The invoker field. This should be updated by the implementation to
	 * always refer to an instance containing all code that should be
	 * executed upon event emission.
	 */
	protected volatile T invoker;

	/**
	 * Returns the invoker instance.
	 *
	 * <p>An "invoker" is an object which hides multiple registered
	 * listeners of type T under one instance of type T, executing
	 * them and leaving early as necessary.
	 *
	 * @return The invoker instance.
	 */
	public final T invoker() {
		return invoker;
	}

	/**
	 * Register a listener to the event, in the default phase.
	 * Have a look at {@link #addPhaseOrdering} for an explanation of event phases.
	 *
	 * @param listener The desired listener.
	 */
	public abstract void register(T listener);

	/**
	 * The identifier of the default phase.
	 */
	public static final ResourceLocation DEFAULT_PHASE = ResourceLocation.fromNamespaceAndPath(CollectiveReference.MOD_ID, "default");

	/**
	 * Register a listener to the event for the specified phase.
	 *
	 * @param phase Identifier of the phase this listener should be registered for. It will be created if it didn't exist yet.
	 * @param listener The desired listener.
	 */
	public void register(ResourceLocation phase, T listener) {
		// This is done to keep compatibility with existing Event subclasses, but they should really not be subclassing Event.
		register(listener);
	}

	/**
	 * Request that listeners registered for one phase be executed before listeners registered for another phase.
	 * registering phase ordering dependencies.
	 *
	 * <p>Incompatible ordering constraints such as cycles will lead to inconsistent behavior:
	 * some constraints will be respected and some will be ignored. If this happens, a warning will be logged.
	 *
	 * @param firstPhase The identifier of the phase that should run before the other. It will be created if it didn't exist yet.
	 * @param secondPhase The identifier of the phase that should run after the other. It will be created if it didn't exist yet.
	 */
	public void addPhaseOrdering(ResourceLocation firstPhase, ResourceLocation secondPhase) {
		// This is not abstract to avoid breaking existing Event subclasses, but they should really not be subclassing Event.
	}
}
