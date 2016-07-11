package hu.stancforma.workTime

import hu.stancforma.excel.CreateExcel
import hu.stancforma.util.*
import org.joda.time.DateTime
import org.joda.time.Minutes
import java.io.File
import java.util.*

public class WorkTimeCalculation {


    companion object {
        val userDb = readUserDB()
        public val LOG = StringBuffer()
        @JvmStatic fun main(args: Array<String>) {
            val run = WorkTimeCalculation()
            //run.readOneDayData()
            if (args.size == 2) {
                args[0]
                run.readUsersData(args[0], args[1].toInt())
            } else {
                println("Helyes hasznalat a kovetkezo:")
                println("java -cp stancforma-time_main.jar hu.stancforma.workTime.WorkTimeCalculation ./data/txt/januar 160")
                println("Te ezeket a parametereket adtad meg most:\t${args.joinToString("\t")}")
            }
        }
    }

    public fun getLog() : String{
        return LOG.toString()
    }


    public fun readUsersData(rootDirectory: String, workHours: Int) {

        File(rootDirectory).listFiles().filter { file -> file.name.endsWith(".txt") }.forEach { file ->
            readUserData(file, workHours)
        }
    }

    public fun readUsersData(files: List<File>, workHours: Int) {

        files.filter { file -> file.name.endsWith(".txt") }.forEach { file ->
            readUserData(file, workHours)
        }

    }

    public fun readUserData(file: File, workHours: Int) {
        //val file = File("./data/Nagy_F_0316_0404.txt")
        val lines = file.readLines(charset("ISO-8859-1"))
        val userTimeDataByDay = HashMap<Int, EnteringData>()
        val enterings = HashMap<Int, LinkedList<DateTime>>()
        val exits = HashMap<Int, LinkedList<DateTime>>()
        for (i in 6..lines.size - 1) {

            //println(lines[i].split(Regex("\\s")).size)
            val splittedLine = lines[i].split(Regex("\\s"))
            if (getItemInList(1, splittedLine).contains(".")) {
                val date = parseDate(getItemInList(1, splittedLine), getItemInList(2, splittedLine))
                val day = date.dayOfMonth().get()
                val enteringType = getItemInList(5, splittedLine)

                if ("kilépés".equals(enteringType)) {
                    putMapList(day, date, exits)
                } else if ("belépés".equals(enteringType)) {
                    putMapList(day, date, enterings)
                } else {
                    println("something worng at : " + splittedLine)
                    LOG.append("something worng at : " + splittedLine)
                }
            }
            //println("$date   ${getItemInList(5, splittedLine)}")
            //println(parseDate(getItemInList(1, splittedLine), getItemInList(2, splittedLine)))
        }
        enterings.forEach { day, enterings ->
            //TODO getOrELsere cserelni
            val exits = exits.getOrElse(day, { LinkedList<DateTime>() })
            userTimeDataByDay.put(day, EnteringData(enterings, exits))
        }
        val result = getWorkTime(userTimeDataByDay)
        //println(result)
        //printResults(result)

        val userName = extractNameFromFileName(file)
        val userData = userDb.get(userName)

        if (userData != null) {
            val createExcel = CreateExcel()
            val fileName = file.name.split(Regex("/")).last().split(".")[0]
            val workbook = createExcel.createXlsToUserData(result, extractNameFromFileName(file), userData.oraBer, userData.bruttoBer, workHours)
            val directory = "$resultsRootDirectory/${getDirectory(file.path)}"
            if (!File(directory).exists()) {
                File(directory).mkdirs()
            }
            LOG.appendln(writeWorkBook(workbook, "$directory/$fileName.xlsx"))
        } else {
            println("probléma: $file\t$userName")
            LOG.appendln("probléma: $file\t$userName")
            //System.exit(1)
        }
    }

    private fun getWorkTime(userTimeDataByDay: HashMap<Int, EnteringData>): LinkedList<WorkTimeData> {
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
                LOG.appendln("ki/belepesi problema: ${enterings.entering.first()}")
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
        return Minutes.minutesBetween(enter, exit).minutes
        //val diff = Duration.between(enter.toInstant(), exit.toInstant())
        //return diff.toMinutes()
    }

    public fun printResults(result: LinkedList<WorkTimeData>) {
        result.forEach { entity ->
            val date = entity.begin
            println("${entity.date},${entity.workTimeMinutes}")
            //println("${year}.${mounth}.${entity.key},${entity.value}")
        }
    }


}

fun main(args: Array<String>) {
    val run = WorkTimeCalculation()
    //run.readOneDayData()
    if (args.size == 2) {
        //run.readUsersData("./data/txt/2016_majus", 160)
        run.readUsersData(args[0], args[1].toInt())
    } else {
        println(args.joinToString("\n"))
    }


}

