package de.deroq.clans.util;

import lombok.Getter;

/**
 * @author Miles
 * @since 10.12.2022
 */
public class Pair<K, V> {

    private final K k;

    private final V v;

    public Pair(K k, V v) {
        this.k = k;
        this.v = v;
    }

    public K getKey() {
        return k;
    }

    public V getValue() {
        return v;
    }

    public K getLeft() {
        return k;
    }

    public V getRight() {
        return v;
    }
}
