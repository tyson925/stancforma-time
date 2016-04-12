package hu.stancforma.excel

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
        writeWorkBook(workbook,"./data/test.xlsx")
    }

    public fun createXlsToUserData(timeDatas : Map<Int,Long>){
        //Blank workbook
        val workbook = XSSFWorkbook();

        //Create a blank sheet
        val sheet = workbook.createSheet("Employee Data");
        val data = TreeMap<DateTime, Long>();
        //data.put("1", arrayOf("Datum", "ledolgozott percerk"))
        timeDatas.forEach { timeData ->
            data.put(DateTime(2016,3,timeData.key,0,0), timeData.value)
        }
        val keyset = data.keys

        var rownum = 0
        keyset.forEach { key ->

            val row = sheet.createRow(rownum++)
            val workedMinutes = data.get(key)!!
            val dateCell = row.createCell(0);
            println(key.toDate())
            dateCell.setCellValue(key.toDate())
            val workTimeCell = row.createCell(1);
            if (workedMinutes is Long){
                workTimeCell.setCellValue(workedMinutes.toDouble())
            }
        }

        writeWorkBook(workbook,"./data/test.xlsx")

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

