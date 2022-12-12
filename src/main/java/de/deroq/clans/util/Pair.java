package de.deroq.clans.util;

/**
 * @author Miles
 * @since 12.12.2022
 */
public abstract class Pair<K, V> {

    private final K k;

    private final V v;

    private Pair(K k, V v) {
        this.k = k;
        this.v = v;
    }

    public static <K, V> Pair<K, V> of(K k, V v) {
        return new Pair<K, V>(k, v) {
            @Override
            public K getKey() {
                return super.getKey();
            }

            @Override
            public V getValue() {
                return super.getValue();
            }

            @Override
            public K getLeft() {
                return super.getLeft();
            }

            @Override
            public V getRight() {
                return super.getRight();
            }
        };
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