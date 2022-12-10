package de.deroq.clans.util;

import lombok.Getter;

/**
 * @author Miles
 * @since 10.12.2022
 */
public class Pair<K, V> {

    @Getter
    private final K left;

    @Getter
    private final K key;

    @Getter
    private final V right;

    @Getter
    private final V value;

    public Pair(K k, V v) {
        this.left = k;
        this.key = k;
        this.right = v;
        this.value = v;
    }


}
