package hu.stancforma.excel

import hu.stancforma.util.WorkTimeData
import hu.stancforma.util.getMultiply
import hu.stancforma.util.getMuszakType
import hu.stancforma.util.resultsRootDirectory
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.joda.time.DateTime
import java.io.FileOutputStream
import java.io.File
import java.util.*


public class CreateExcel() {

    public fun creatXls() {
        //Blank workbook
        val workbook = XSSFWorkbook();

        //Create a blank sheet
        val sheet = workbook.createSheet("Employee Data");
        val data = TreeMap<String, Array<Any>>();
        data.put("1", arrayOf("ID", "NAME", "LASTNAME"))
        data.put("2", arrayOf(1, "Amit", "Shukla"))
        data.put("3", arrayOf(2, "Lokesh", "Gupta"))
        data.put("4", arrayOf(3, "John", "Adwards"))
        data.put("5", arrayOf(4, "Brian", "Schultz"))

        val keyset = data.keys

        var rownum = 0
        keyset.forEach { key ->

            val row = sheet.createRow(rownum++)
            val objArr = data.get(key)!!
            var cellnum = 0;
            objArr.forEach { obj ->

                val cell = row.createCell(cellnum++);
                if (obj is String) {
                    cell.setCellValue(obj)
                } else if (obj is Integer) {
                    cell.setCellValue(obj.toDouble())
                }

            }

        }
        println(sheet)
        //XSSFWorkbook(sheet)
        writeWorkBook(workbook, "./data/test.xlsx")
    }

    public fun createXlsToUserData(timeDatas: LinkedList<WorkTimeData>, fileName: String) {
        //Blank workbook
        val workbook = XSSFWorkbook();
        val createHelper = workbook.getCreationHelper();

        //Create a blank sheet
        val sheet = workbook.createSheet("Employee Data");

        val cellStyle = workbook.createCellStyle()
        cellStyle.dataFormat = createHelper.createDataFormat().getFormat("yyyy/m/d")

        val cellStyle2 = workbook.createCellStyle()
        cellStyle2.dataFormat = createHelper.createDataFormat().getFormat("h:mm")


        val firstRow = sheet.createRow(0)
        val cell1 = firstRow.createCell(0)
        cell1.setCellValue("Datum")

        val cell2 = firstRow.createCell(1)
        cell2.setCellValue("Muszak")
        val cell3 = firstRow.createCell(2)
        cell3.setCellValue("Erkezes")
        val cell4 = firstRow.createCell(3)
        cell4.setCellValue("Tavozas")
        val cell5 = firstRow.createCell(4)
        cell5.setCellValue("Ledolgozott Percek")
        val cell6 = firstRow.createCell(4)
        cell6.setCellValue("Korrigalt Percek")

        var rownum = 1
        //keyset.forEach { key ->
        var sumWorkTime = 0L
        var sumCorrigateWorktime = 0.0
        timeDatas.forEach { timeData ->

            val row = sheet.createRow(rownum++)

            val dateCell = row.createCell(0);
            println(timeData.date.toDate())
            dateCell.setCellValue(timeData.date.toDate())
            dateCell.setCellStyle(cellStyle)
            val muszakCell = row.createCell(1)
            val muszak = getMuszakType(timeData.begin, timeData.end)
            muszakCell.setCellValue(muszak)

            val beginTimeCell = row.createCell(2)
            beginTimeCell.setCellValue(timeData.begin.toDate())
            beginTimeCell.setCellStyle(cellStyle2)
            val endTimeCell = row.createCell(3)
            endTimeCell.setCellValue(timeData.end.toDate())
            endTimeCell.setCellStyle(cellStyle2)
            val workTimeCell = row.createCell(4)
            val corrigateWorkTimeCell = row.createCell(5)
            if (timeData.workTimeMinutes is Long) {
                workTimeCell.setCellValue(timeData.workTimeMinutes.toDouble())
                val corrigateWorkTime = (timeData.workTimeMinutes - 10) * getMultiply(muszak)
                sumCorrigateWorktime += corrigateWorkTime
                corrigateWorkTimeCell.setCellValue(corrigateWorkTime)
                sumWorkTime += timeData.workTimeMinutes
            }
        }
        val row = sheet.createRow(rownum++)
        var sumWorkTimeCell = row.createCell(4)
        sumWorkTimeCell.setCellValue(sumWorkTime.toDouble())
        var sumCorrigateWorkTimeCell = row.createCell(5)
        sumCorrigateWorkTimeCell.setCellValue(sumCorrigateWorktime)

        val row2 = sheet.createRow(rownum++)

        row2.createCell(0).setCellValue("Ora ber:")
        row2.createCell(1).setCellValue(850.0)

        val fee = (sumCorrigateWorktime / 60) * 850

        val row3 = sheet.createRow(rownum++)
        row3.createCell(0).setCellValue("Kifizetett ber:")
        row3.createCell(1).setCellValue(fee)

        writeWorkBook(workbook, "$resultsRootDirectory/$fileName.xlsx")

    }

    public fun writeWorkBook(workbook: XSSFWorkbook, fileName: String) {
        val out = FileOutputStream(File(fileName))
        workbook.write(out)
        out.close()
        println("$fileName written successfully on disk.")


    }


}

fun main(args: Array<String>) {
    val xls = CreateExcel()
    xls.creatXls()
    //xls.writeWorkBook()
}

