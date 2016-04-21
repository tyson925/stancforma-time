package hu.stancforma.util

import org.joda.time.DateTime
import java.io.Serializable
import java.util.*

//public data class EnteringData(val date : Date, val enteringType : String, val userName : String ) : Serializable

public data class EnteringData(val entering : List<DateTime>, val exit : List<DateTime>) : Serializable

public data class WorkTimeData(val workTimeMinutes : Long, val begin : DateTime,val end : DateTime, val date : DateTime) : Serializable



public val resultsRootDirectory = "./data/xls"

public fun getDayOfDate(date : DateTime) : DateTime{
    return DateTime(date.year,date.monthOfYear,date.dayOfMonth,0,0)
}

public fun getMuszakType(begin : DateTime,end: DateTime) : String {
    if (begin.dayOfWeek == 6 || begin.dayOfWeek == 7 ){
      return "HETVEGE"
    }
    else if (begin.hourOfDay<=9 ){
        return "REGGEL"
    } else if ((end.hourOfDay == 19 && end.minuteOfHour >=30) || end.hourOfDay > 19){
        return "ESTI"
    } else {
        return "NICS MUSZAK"
    }
}

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


