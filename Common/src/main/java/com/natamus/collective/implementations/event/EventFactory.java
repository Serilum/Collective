package com.natamus.collective.implementations.event;

import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;

/*
	Original code used from https://github.com/FabricMC/fabric.
 */
public final class EventFactory {
	private EventFactory() { }

	/**
	 * Create an "array-backed" Event instance.
	 *
	 * <p>If your factory simply delegates to the listeners without adding custom behavior,
	 * consider using {@linkplain #createArrayBacked(Class, Object, Function) the other overload}
	 * if performance of this event is critical.
	 *
	 * @param type           The listener class type.
	 * @param invokerFactory The invoker factory, combining multiple listeners into one instance.
	 * @param <T>            The listener type.
	 * @return The Event instance.
	 */
	public static <T> Event<T> createArrayBacked(Class<? super T> type, Function<T[], T> invokerFactory) {
		return EventFactoryImpl.createArrayBacked(type, invokerFactory);
	}

	/**
	 * Create an "array-backed" Event instance with a custom empty invoker,
	 * for an event whose {@code invokerFactory} only delegates to the listeners.
	 * <ul>
	 *   <li>If there is no listener, the custom empty invoker will be used.</li>
	 *   <li><b>If there is only one listener, that one will be used as the invoker
	 *   and the factory will not be called.</b></li>
	 *   <li>Only when there are at least two listeners will the factory be used.</li>
	 * </ul>
	 *
	 * <p>Having a custom empty invoker (of type (...) -&gt; {}) increases performance
	 * relative to iterating over an empty array; however, it only really matters
	 * if the event is executed thousands of times a second.
	 *
	 * @param type           The listener class type.
	 * @param emptyInvoker   The custom empty invoker.
	 * @param invokerFactory The invoker factory, combining multiple listeners into one instance.
	 * @param <T>            The listener type.
	 * @return The Event instance.
	 */
	public static <T> Event<T> createArrayBacked(Class<T> type, T emptyInvoker, Function<T[], T> invokerFactory) {
		return createArrayBacked(type, listeners -> {
			if (listeners.length == 0) {
				return emptyInvoker;
			} else if (listeners.length == 1) {
				return listeners[0];
			} else {
				return invokerFactory.apply(listeners);
			}
		});
	}

	/**
	 * Create an array-backed event with a list of default phases that get invoked in order.
	 * Exposing the identifiers of the default phases as {@code public static final} constants is encouraged.
	 *
	 * <p>An event phase is a named group of listeners, which may be ordered before or after other groups of listeners.
	 * This allows some listeners to take priority over other listeners.
	 * Adding separate events should be considered before making use of multiple event phases.
	 *
	 * <p>Phases may be freely added to events created with any of the factory functions,
	 * however using this function is preferred for widely used event phases.
	 * If more phases are necessary, discussion with the author of the Event is encouraged.
	 *
	 * <p>Refer to {@link Event#addPhaseOrdering} for an explanation of event phases.
	 *
	 * @param type           The listener class type.
	 * @param invokerFactory The invoker factory, combining multiple listeners into one instance.
	 * @param defaultPhases  The default phases of this event, in the correct order. Must contain {@link Event#DEFAULT_PHASE}.
	 * @param <T>            The listener type.
	 * @return The Event instance.
	 */
	public static <T> Event<T> createWithPhases(Class<? super T> type, Function<T[], T> invokerFactory, ResourceLocation... defaultPhases) {
		EventFactoryImpl.ensureContainsDefault(defaultPhases);
		EventFactoryImpl.ensureNoDuplicates(defaultPhases);

		Event<T> event = createArrayBacked(type, invokerFactory);

		for (int i = 1; i < defaultPhases.length; ++i) {
			event.addPhaseOrdering(defaultPhases[i-1], defaultPhases[i]);
		}

		return event;
	}

	/**
	 * @deprecated This is not to be used in events anymore.
	 */
	@Deprecated
	public static String getHandlerName(Object handler) {
		return handler.getClass().getName();
	}

	/**
	 * @deprecated Always returns {@code false}, do not use. This is not to be used in events anymore, standard Java profilers will do fine.
	 */
	@Deprecated
	public static boolean isProfilingEnabled() {
		return false;
	}

	/**
	 * Invalidate and re-create all existing "invoker" instances across
	 * events created by this EventFactory. Use this if, for instance,
	 * the profilingEnabled field changes.
	 *
	 * @deprecated Do not use, will be removed in a future release.
	 */
	@Deprecated(forRemoval = true)
	public static void invalidate() {
		EventFactoryImpl.invalidate();
	}
}
