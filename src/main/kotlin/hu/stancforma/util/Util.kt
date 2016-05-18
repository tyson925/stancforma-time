package hu.stancforma.util

import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.hssf.util.HSSFColor
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.joda.time.DateTime
import java.io.Serializable
import java.util.*
import java.io.File
import java.io.FileOutputStream

//public data class EnteringData(val date : Date, val enteringType : String, val userName : String ) : Serializable

public data class EnteringData(val entering : List<DateTime>, val exit : List<DateTime>) : Serializable

public data class WorkTimeData(val workTimeMinutes : Long, val begin : DateTime,val end : DateTime, val date : DateTime) : Serializable

public data class UserData(val userName : String,val oraBer : Int, val bruttoBer : Int) : Serializable

public val resultsRootDirectory = "./data/xls/"

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

public fun writeWorkBook(workbook: XSSFWorkbook, fileName: String) {
    val out = FileOutputStream(File(fileName))
    workbook.write(out)
    out.close()
    println("$fileName written successfully on disk.")


}

public fun getMultiply(muszakType : String) :Double{
    if ("HETVEGE".equals(muszakType)){
        return 1.34
    } else if ("ESTI".equals(muszakType)){
        return 1.1
    } else {
        return 1.0
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


public fun extractNameFromFileName(file : File) : String {

    val splittedFileName = file.name.split("_")
//    if ("Klausenberger".equals(splittedFileName[0])){

  //  } else {
        if (splittedFileName.size< 2 ){
            //println("File nev problema: " + file)
            return splittedFileName[0]
        } else {
            return "${splittedFileName[0]}_${splittedFileName[1].split(".")[0]}"

        }

    //}
}

public fun readUserDB() : Map<String,UserData>{
    val results = HashMap<String,UserData>()
    File("./data/db.txt").forEachLine { line ->
        val splittedLine = line.split(",")
        results.put(splittedLine[0],UserData(splittedLine[0],splittedLine[1].toInt(),splittedLine[2].toInt()))
    }
    return results
}

public fun getDirectory(rootDirectory : String) : String{
    //return rootDirectory.split("\\").dropLast(1).last()
    return rootDirectory.split("/").dropLast(1).last()
}

public fun setColor(workbook : HSSFWorkbook, r : Byte, g : Byte, b : Byte) : HSSFColor? {
    val palette = workbook.getCustomPalette();
    var hssfColor : HSSFColor? = null;
    try {
        hssfColor= palette.findColor(r, g, b);
        if (hssfColor == null ){
            palette.setColorAtIndex(HSSFColor.LAVENDER.index, r, g,b);
            hssfColor = palette.getColor(HSSFColor.GREY_25_PERCENT.index);
        }
    } catch (e : Exception) {
        e.printStackTrace()
    }

    return hssfColor
}