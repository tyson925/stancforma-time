package hu.stancforma.util

import hu.stancforma.workTime.WorkTimeCalculation
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.hssf.util.HSSFColor
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.joda.time.DateTime
import org.joda.time.Minutes
import java.io.File
import java.io.FileOutputStream
import java.io.Serializable
import java.util.*

//public data class EnteringData(val date : Date, val enteringType : String, val userName : String ) : Serializable

data class EnteringData(val entering: List<DateTime>, val exit: List<DateTime>) : Serializable

data class WorkTimeData(val workTimeMinutes: Long, val begin: DateTime, val end: DateTime, val date: DateTime) : Serializable

data class UserData(val userName: String, val oraBer: Int, val bruttoBer: Int) : Serializable

val resultsRootDirectory = "./data/xls/"

fun getDayOfDate(date: DateTime): DateTime {
    return DateTime(date.year, date.monthOfYear, date.dayOfMonth, 0, 0)
}

fun getMuszakType(begin: DateTime, end: DateTime): String {
    if (begin.dayOfWeek == 6 || begin.dayOfWeek == 7) {
        return "HETVEGE"
    } else if (begin.hourOfDay <= 9) {
        return "REGGEL"
    } else if ((end.hourOfDay == 19 && end.minuteOfHour >= 30) || end.hourOfDay > 19) {
        return "ESTI"
    } else {
        return "NICS MUSZAK"
    }
}

fun writeWorkBook(workbook: XSSFWorkbook, fileName: String): String {
    val out = FileOutputStream(File(fileName))
    workbook.write(out)
    out.close()
    println("$fileName sikeresen legeneralva.")
    return "$fileName sikeresen legeneralva."

}

fun getMultiply(muszakType: String): Double {
    if ("HETVEGE".equals(muszakType)) {
        return 1.34
    } else if ("ESTI".equals(muszakType)) {
        return 1.1
    } else {
        return 1.0
    }
}

fun <K : Any, V : Any> putMapList(key: K, value: V, map: HashMap<K, LinkedList<V>>): MutableMap<K, LinkedList<V>> {
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

fun extractWorkerNameFromFile(fileName: String): String {
    val splittedFileName = fileName.split("/")
    return splittedFileName.last().split(".")[0]
}

fun extractNameFromFileName(fileName: String): String {
    val splittedFileName = fileName.split("_")

    if (splittedFileName.size < 3) {
        //println("File nev problema: " + file)
        return splittedFileName[0].split(".")[0]
    } else {
        return "${splittedFileName[0]}_${splittedFileName[1].split(".")[0]}"

    }
}

fun extractNameFromFileName(file: File): String {

    return extractNameFromFileName(file.name)
}

fun readUserDB(): Map<String, UserData> {
    val results = HashMap<String, UserData>()
    File("./data/db.txt").forEachLine { line ->
        val splittedLine = line.split(",")
        results.put(splittedLine[0], UserData(splittedLine[0], splittedLine[1].toInt(), splittedLine[2].toInt()))
    }
    return results
}

fun getDirectory(rootDirectory: String): String {
//    return rootDirectory.split("\\").dropLast(1).last()
    return rootDirectory.split("/").dropLast(1).last()
}

fun setColor(workbook: HSSFWorkbook, r: Byte, g: Byte, b: Byte): HSSFColor? {
    val palette = workbook.customPalette
    var hssfColor: HSSFColor? = null
    try {
        hssfColor = palette.findColor(r, g, b)
        if (hssfColor == null) {
            palette.setColorAtIndex(HSSFColor.LAVENDER.index, r, g, b)
            hssfColor = palette.getColor(HSSFColor.GREY_25_PERCENT.index)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }

    return hssfColor
}

fun getWorkTime(userTimeDataByDay: HashMap<Int, EnteringData>): LinkedList<WorkTimeData> {
    val results = LinkedList<WorkTimeData>()
    userTimeDataByDay.forEach { day, enterings ->
        var workMinutes = 0L
        enterings.entering.sortedDescending()
        enterings.exit.sortedDescending()
        if (enterings.entering.size == enterings.exit.size) {
            for (j in 0..enterings.entering.size - 1) {
                workMinutes += getDiff(enterings.entering[j], enterings.exit[j])
            }
            results.add(WorkTimeData(workMinutes, enterings.entering.last(), enterings.exit.first(), getDayOfDate(enterings.entering.last())))

        } else {
            if (enterings.entering.isNotEmpty() && enterings.exit.isNotEmpty()) {
                results.add(WorkTimeData(0L, enterings.entering.last(), enterings.exit.first(), getDayOfDate(enterings.entering.last())))
            } else if (enterings.entering.isNotEmpty() && enterings.exit.isEmpty()) {
                results.add(WorkTimeData(0L, enterings.entering.last(), enterings.entering.first(), getDayOfDate(enterings.entering.last())))
            } else if (enterings.entering.isEmpty() && enterings.exit.isNotEmpty()) {
                results.add(WorkTimeData(0L, enterings.exit.last(), enterings.exit.first(), getDayOfDate(enterings.exit.first())))
            }

            println("ki/belepesi problema: ${enterings.entering.first()}")
            WorkTimeCalculation.LOG.appendln("ki/belepesi problema: ${enterings.entering.first()}")
        }
    }
    return results
}

private fun getDiff(enter: DateTime, exit: DateTime): Int {
    return Minutes.minutesBetween(enter, exit).minutes
    //val diff = Duration.between(enter.toInstant(), exit.toInstant())
    //return diff.toMinutes()
}

