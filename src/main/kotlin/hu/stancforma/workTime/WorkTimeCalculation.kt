package hu.stancforma.workTime

import hu.stancforma.excel.CreateExcel
import hu.stancforma.util.EnteringData
import hu.stancforma.util.putMapList
import org.joda.time.DateTime
import org.joda.time.Minutes
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

public class WorkTimeCalculation {


    public fun readUserDatas(rootDirectory : String){

        File(rootDirectory).listFiles().filter { file -> file.name.endsWith(".txt") }.forEach { file ->
            readUserData(file)
        }
    }

    public fun readUserData(file : File) {
        //val file = File("./data/Nagy_F_0316_0404.txt")
        val lines = file.readLines(charset("ISO-8859-1"))
        val userTimeDataByDay = HashMap<Int, EnteringData>()
        val enterings = HashMap<Int, LinkedList<DateTime>>()
        val exits = HashMap<Int,LinkedList<DateTime>>()
        for (i in 6..lines.size - 5) {

            //println(lines[i].split(Regex("\\s")).size)
            val splittedLine = lines[i].split(Regex("\\s"))
            val date = parseDate(getItemInList(1, splittedLine), getItemInList(2, splittedLine))
            val day = date.dayOfMonth().get()
            val enteringType = getItemInList(5, splittedLine)

            if ("kilépés".equals(enteringType)) {
                putMapList(day, date,exits)
            } else if ("belépés".equals(enteringType)) {
                putMapList(day, date,enterings)
            } else {
                println("something worng at : " + splittedLine)
            }

            println("$date   ${getItemInList(5, splittedLine)}")
            //println(parseDate(getItemInList(1, splittedLine), getItemInList(2, splittedLine)))
        }
        enterings.forEach { day, enterings ->
            //TODO getOrELsere cserelni
            val exits = exits.getOrElse(day, { LinkedList<DateTime>() })
            userTimeDataByDay.put(day, EnteringData(enterings,exits))
        }
        val result = getWorkTime(userTimeDataByDay)
        println(result)
        printResults(result,2016,3)
        val createExcel = CreateExcel()
        val fileName = file.name.split(Regex("/")).last().split(".")[0]
        createExcel.createXlsToUserData(result,fileName)
    }

    private fun getWorkTime(userTimeDataByDay : HashMap<Int,EnteringData>) : Map<Int,Long>{
        val results = HashMap<Int,Long>()
        userTimeDataByDay.forEach { day, enterings ->
            var workMinutes = 0L
            enterings.entering.sortedDescending()
            enterings.exit.sortedDescending()
            if (enterings.entering.size == enterings.exit.size) {
                for (j in 0..enterings.entering.size - 1) {
                    workMinutes += getDiff(enterings.entering[j], enterings.exit[j])
                }
                results.put(day,workMinutes)
            } else {
                println("ki/belepesi problema")
            }
        }
        return results
    }

    private fun getItemInList(index: Int, text: List<String>): String {

        var tmpIndex = 0
        var res = ""
        for (i in 0..text.size - 1) {
            if (!"".equals(text[i])) {
                if (tmpIndex == index) {
                    res = text[i]
                }
                tmpIndex++
            }
        }
        return res
    }

    private fun parseDate(date: String, time: String): DateTime {

        val dateSplitted = date.split(Regex("\\."))
        val timeSplitted = time.split(Regex(":"))
        val tmpTime = DateTime(dateSplitted[2].toInt(), dateSplitted[1].toInt(), dateSplitted[0].toInt(), timeSplitted[0].toInt(), timeSplitted[1].toInt(), 0)

        //println(tmpTime.dayOfMonth().get())
        //calendar.set(dateSplitted[2].toInt(), dateSplitted[1].toInt(), dateSplitted[0].toInt(), timeSplitted[0].toInt(), timeSplitted[1].toInt(),0)
        //val tmpTime = calendar.time

        return tmpTime
    }

    private fun parseName(name1: String, name2: String): String {
        return "${name1} ${name2}".trim()
    }

    private fun getDiff(enter: DateTime, exit: DateTime): Int {
        return Minutes.minutesBetween(enter,exit).minutes
        //val diff = Duration.between(enter.toInstant(), exit.toInstant())
        //return diff.toMinutes()
    }

    public fun printResults(result : Map<Int,Long>,year: Int,mounth : Int){
        result.forEach { entity ->
            println("${year}.${mounth}.${entity.key},${entity.value}")
        }
    }


}

fun main(args: Array<String>) {
    val run = WorkTimeCalculation()
    //run.readOneDayData()
    run.readUserDatas("./data/txt")



}

