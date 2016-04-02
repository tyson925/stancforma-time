package hu.stancforma.util

import java.io.Serializable
import java.util.*

public data class EnteringData(val date : Date, val enteringType : String, val userName : String ) : Serializable

public fun <K : Any, V : Any> putMapList(key: K, value: V, map: HashMap<K, LinkedList<V>>): MutableMap<K, LinkedList<V>> {
    if (!map.containsKey(key)) {

        val tmpSet = LinkedList<V>()
        tmpSet.add(value)

        map.put(key, tmpSet)
    } else {
        val tmpSet = map.get(key)
        tmpSet!!.add(value)
        map.put(key, tmpSet)
    }
    return map
}


