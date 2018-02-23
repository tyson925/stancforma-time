package hu.stancforma.workTime


import hu.stancforma.excel.CreateExcel
import hu.stancforma.util.*
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.joda.time.DateTime
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.util.*


class ApachePOIExcelRead {


    companion object {
        private val file = File("./data/new/Muk_Bernadett_január_2.xlsx")

        private fun parseDate(date: String): DateTime {
            val splittedDate = date.split(".")
            val hours = splittedDate.last().trim().split(":")
            val time = DateTime(splittedDate[0].toInt(), splittedDate[1].trim().toInt(), splittedDate[2].trim().toInt(), hours[0].toInt(), hours[1].toInt())
            return time
        }


        @JvmStatic
        fun main(args: Array<String>) {

            File("./data/new/").listFiles().filter({ file -> file.name.endsWith("xlsx") }).forEach { file ->
                try {

                    val excelFile = FileInputStream(file)
                    val workbook = XSSFWorkbook(excelFile)
                    val datatypeSheet = workbook.getSheetAt(0)

                    val iterator = datatypeSheet.iterator()

                    val userTimeDataByDay = HashMap<Int, EnteringData>()
                    val enterings = HashMap<Int, LinkedList<DateTime>>()
                    val exits = HashMap<Int, LinkedList<DateTime>>()
                    iterator.next()


                    while (iterator.hasNext()) {
                        val currentRow = iterator.next()

                        val dateCell = currentRow.getCell(5)
                        val dateValue = dateCell.stringCellValue

                        //print(dateValue)
                        if (dateValue.isNotEmpty()) {
                            val date = parseDate(dateValue)
                            val day = date.dayOfMonth().get()
                            val cell = currentRow.getCell(6)
                            val cellValue = cell.stringCellValue

                            if (cellValue.contains(" Kilép")) {
                                if (cellValue.contains("étkező Kilép") || cellValue.contains("étkető Kilép")) {
                                    putMapList(day, date, enterings)
                                } else {
                                    putMapList(day, date, exits)
                                }
                            } else if (cellValue.contains(" Be]")) {
                                if (cellValue.contains("étkező Be]")){
                                    putMapList(day, date, exits)
                                } else {
                                putMapList(day, date, enterings)
                                }
                            } else {
                                println("OTHER: \t $cellValue")
                            }
                        }
                        //println(cellValue)
                    }

                    enterings.forEach { day, enterings ->
                        val exits = exits.getOrElse(day, { LinkedList<DateTime>() })
                        userTimeDataByDay.put(day, EnteringData(enterings, exits))
                    }
                    val result = getWorkTime(userTimeDataByDay)

                    println(result)

                    val userName = extractNameFromFileName(file.name.split("/").last())
                    println(WorkTimeCalculation.userDb)
                    val userData = WorkTimeCalculation.userDb[userName]

                    if (userData != null) {
                        val createExcel = CreateExcel()
                        val fileName = file.name.split(Regex("/")).last().split(".")[0]
                        val workbook = createExcel.createXlsToUserData(result, extractNameFromFileName(file), userData.oraBer, userData.bruttoBer, 164)
                        val directory = "${resultsRootDirectory}/${getDirectory(file.path)}"
                        if (!File(directory).exists()) {
                            File(directory).mkdirs()
                        }
                        WorkTimeCalculation.LOG.appendln(writeWorkBook(workbook, "$directory/$fileName.xlsx"))
                    } else {
                        println("probléma: $file\t$userName")
                        WorkTimeCalculation.LOG.appendln("probléma: $file\t$userName")
                        //System.exit(1)
                    }
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }

        }



    }
}