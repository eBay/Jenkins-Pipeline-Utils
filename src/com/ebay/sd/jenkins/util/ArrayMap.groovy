/* ************************************************************
Copyright 2018 eBay Inc.
Developer: Yinon Avraham

Use of this source code is governed by an MIT-style
license that can be found in the LICENSE file or at
https://opensource.org/licenses/MIT.
**************************************************************/

package com.ebay.sd.jenkins.util

/**
 * A utility class to give semi-map functionality which is compatible with jenkins pipeline script serialization requirements (CPS).
 */
class ArrayMap implements Serializable {

    private final ArrayWrapper array

    /**
     * Create a new ArrayMap instance and optionally populate it with a given map
     * @param map (optional) a map with initial key-value entries for this map
     */
    ArrayMap(Map map = [:]) {
        def list = new ArrayList(map.size() + 1)
        for (e in map) {
            list.add(e.key)
            list.add(e.value)
        }
        array = new ArrayWrapper(list)
    }

    /**
     * Put a key-value entry in the map
     * @param key the key to use
     * @param value the value to put (can be null)
     * @return the old value if existed, or null
     */
    def put(key, value) {
        def old
        int index = indexOf(key)
        if (index < 0) {
            array.add(key)
            array.add(value)
        } else {
            old = array.get(index + 1)
            array.set(index, key)
            array.set(index + 1, value)
        }
        old
    }

    /**
     * get a value by its key. Optionally, provide a creator closure which can be used for creating a new value if there is no matching for the given key.
     * @param key the key of the value to get
     * @param creator (optional) a closure to use when a value is missing. E.g.: map.get('key', { -> 'new value'})
     * @return the matching value
     */
    def get(key, Closure creator = null) {
        def value
        int index = indexOf(key)
        if (index >= 0) {
            value = array.get(index + 1)
        } else if (creator) {
            value = creator.call()
            array.add(key)
            array.add(value)
        }
        value
    }

    /**
     * Run a given closure for each entry in the map. E.g.: map.forEach({ k, v -> doSomething(k,v) })
     * @param closure the closure to call. Needs to accept 2 arguments: key, value
     */
    void forEach(Closure closure) {
        for (int index = 0; index < array.size(); index += 2) {
            closure.call array.get(index), array.get(index + 1)
        }
    }

    /**
     * The size of the map - i.e. the number of entries
     */
    int size() {
        array.size() / 2
    }

    /**
     * Return a string representation of this map
     */
    String toString() {
        String str = '{'
        forEach({ key, value ->
            if (str.length() != 1) {
                str += ', '
            }
            str += ('' + key + '=' + value)
        })
        str += '}'
        str
    }

    private int indexOf(key) {
        for (int index = 0; index < array.size(); index += 2) {
            if (array.get(index) == key) {
                return index
            }
        }
        -1
    }
}
