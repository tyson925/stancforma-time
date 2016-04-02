package hu.stancforma.workTime

import hu.stancforma.util.EnteringData
import hu.stancforma.util.putMapList
import java.io.File
import java.text.SimpleDateFormat
import java.time.Duration
import java.util.*

public class WorkTimeCalculation {

    val timeZone = TimeZone.getTimeZone("CET");
    val calendar = Calendar.getInstance(timeZone);
    val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.US)

    constructor() {
        simpleDateFormat.setTimeZone(timeZone)
    }

    public fun readOneDayData() {

        val file = File("./data/Gurardx_0330.txt")
        val lines = file.readLines(charset("ISO-8859-1"))

        val counter = 15
        var datumIndex = 4
        var oraIndex = 5
        var enteringIndex = 8
        var nevIndex = 42
        val enterings = LinkedList<EnteringData>()
        val exits = LinkedList<EnteringData>()
        for (i in 6..lines.size - 5) {
            //println(lines[i].split(Regex("\\s")).size)
            val splittedLine = lines[i].split(Regex("\\s"))
            if (i < counter) {
                if ("".equals(splittedLine[oraIndex])) {

                }
                if ("kilépés".equals(splittedLine[enteringIndex])){
                    exits.add(EnteringData(parseDate(splittedLine[datumIndex],splittedLine[oraIndex]),splittedLine[enteringIndex],parseName(splittedLine[nevIndex],splittedLine[nevIndex+1])))
                } else  if ("belépés".equals(splittedLine[enteringIndex])){
                    enterings.add(EnteringData(parseDate(splittedLine[datumIndex],splittedLine[oraIndex]),splittedLine[enteringIndex],parseName(splittedLine[nevIndex],splittedLine[nevIndex+1])))
                } else {
                    println(lines[i])
                }

                //println(parseDate(splittedLine[datumIndex], splittedLine[oraIndex]))
                //println("${splittedLine[datumIndex]}\t${splittedLine[oraIndex]}\t${splittedLine[enteringIndex]}\t${splittedLine[nevIndex]}")
            } else {
                if (!"".equals(splittedLine[enteringIndex])) {
                    if ("kilépés".equals(splittedLine[enteringIndex])){
                        exits.add(EnteringData(parseDate(splittedLine[datumIndex-1],splittedLine[oraIndex]),splittedLine[enteringIndex],parseName(splittedLine[nevIndex],splittedLine[nevIndex+1])))
                    } else  if ("belépés".equals(splittedLine[enteringIndex])){
                        enterings.add(EnteringData(parseDate(splittedLine[datumIndex-1],splittedLine[oraIndex]),splittedLine[enteringIndex],parseName(splittedLine[nevIndex],splittedLine[nevIndex+1])))
                    } else {
                        println(lines[i])
                    }

                    //println("${splittedLine[datumIndex - 1]}\t${splittedLine[oraIndex]}\t${splittedLine[enteringIndex]}\t${splittedLine[nevIndex]}")
                } else {
                    if ("kilépés".equals(splittedLine[enteringIndex-1])){
                        exits.add(EnteringData(parseDate(splittedLine[datumIndex-1],splittedLine[oraIndex-1]),splittedLine[enteringIndex-1],parseName(splittedLine[nevIndex-1],splittedLine[nevIndex+1-1])))
                    } else  if ("belépés".equals(splittedLine[enteringIndex-1])){
                        enterings.add(EnteringData(parseDate(splittedLine[datumIndex-1],splittedLine[oraIndex-1]),splittedLine[enteringIndex-1],parseName(splittedLine[nevIndex-1],splittedLine[nevIndex+1-1])))
                    } else {
                        println(lines[i])
                    }
                    //println("${splittedLine[datumIndex - 1]}\t${splittedLine[oraIndex - 1]}\t${splittedLine[enteringIndex - 1]}\t${splittedLine[nevIndex - 1]}")
                }
            }

        }

        val enteringsByNames = HashMap<String,LinkedList<Date>>()
        val exitsByNames = HashMap<String, LinkedList<Date>>()

        enterings.forEach { entering ->
            putMapList(entering.userName,entering.date,enteringsByNames)
        }

        exits.forEach { exit ->
            putMapList(exit.userName,exit.date,exitsByNames)
        }

        println(enteringsByNames)
        println(exitsByNames)

        val hCsE = enteringsByNames.get("U:HORVATH CSABA")
        println(hCsE)
        val hCsEcit = exitsByNames.get("U:HORVATH CSABA")
        println(hCsEcit)

        enteringsByNames.forEach { name, dates ->
            dates.sortDescending()
            val exitDates = exitsByNames.get(name)
            if (exitDates != null){
                exitDates.sortDescending()
                if (dates.size == exitDates.size) {
                    var workMinutes = 0L
                    for (j in 0..dates.size - 1) {
                        workMinutes += getDiff(dates[j], exitDates[j])
                    }
                    println("${name}\t${workMinutes}")
                } else {
                    println("problem: $name")
                }
            } else {
                println("problem2: $name")
            }

        }


    }

    private fun parseDate(date: String, time: String): Date {
        val dateSplitted = date.split(Regex("\\."))
        val timeSplitted = time.split(Regex(":"))
        calendar.set(dateSplitted[2].toInt(), dateSplitted[1].toInt(), dateSplitted[0].toInt(), timeSplitted[0].toInt(), timeSplitted[1].toInt(),0)
        val tmpTime = calendar.time
        return calendar.time
    }

    private fun parseName(name1 : String,name2 : String) : String {
        return "${name1} ${name2}".trim()
    }

    private fun getDiff(enter : Date, exit : Date) : Long{
        val diff = Duration.between(enter.toInstant(),exit.toInstant())
        return diff.toMinutes()
    }






}

fun main(args: Array<String>) {
    val run = WorkTimeCalculation()
    run.readOneDayData()
}

