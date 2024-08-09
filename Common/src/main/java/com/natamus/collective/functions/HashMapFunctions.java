package com.natamus.collective.functions;

import java.util.Map;
import java.util.function.Function;

public class HashMapFunctions {
    public static <K, V> V computeIfAbsent(Map<K, V> cache, K key, Function<? super K, ? extends V> function) {
        V result = cache.get(key);

        if (result == null) {
            result = function.apply(key);
            cache.put(key, result);
        }

        return result;
    }
}