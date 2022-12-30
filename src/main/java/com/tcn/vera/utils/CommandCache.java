/*
 * Vera - a common library for all of TCN's discord bots.
 *
 * Copyright (C) 2022 Thomas Wessel and the rest of Team Creative Name
 *
 *
 * This library is licensed under the GNU Lesser General Public License v2.1
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 * USA
 *
 *
 * For more information, please check out the original repository of this project on github
 * https://github.com/Team-Creative-Name/Vera
 */
package com.tcn.vera.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

//This class is a modified version of the one in Almighty-Alpaca/JDA-Butler
public class CommandCache<K, V> {
    private final Map<K, V> map = new HashMap<>();

    private final K[] keys;

    private int currIndex = 0;

    @SuppressWarnings("unchecked")
    public CommandCache(int size) {
        if (size < 1) {
            throw new IllegalArgumentException("Cache size must be at least 1 in size!");
        } else {
            this.keys = (K[]) new Object[size];
        }
    }

    public V find(Predicate<K> toFind) {
        return map.entrySet()
                .stream()
                .filter(it -> toFind.test(it.getKey()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
    }

    public void add(K key, V value) {
        if (keys[currIndex] != null) {
            map.remove(keys[currIndex]);
        }
        map.put(key, value);
        keys[currIndex] = key;
        currIndex = (currIndex + 1) % keys.length;
    }

    public void remove(K key) {
        if (!contains(key)) {
        }
    }

    public boolean contains(K key) {
        return map.containsKey(key);
    }

    public V get(K key) {
        return map.get(key);
    }

}
