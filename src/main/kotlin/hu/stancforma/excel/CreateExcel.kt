package hu.stancforma.excel

import hu.stancforma.util.WorkTimeData
import hu.stancforma.util.getMultiply
import hu.stancforma.util.getMuszakType
import hu.stancforma.util.resultsRootDirectory
import org.apache.poi.hssf.usermodel.HSSFCell
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
        val morningColumn = 4
        firstRow.createCell(morningColumn).setCellValue("Reggel")
        val afternoonColumn = 5
        firstRow.createCell(afternoonColumn).setCellValue("Delutan")
        val holidayColumn = 6
        firstRow.createCell(holidayColumn).setCellValue("Hetvege")
        //firstRow.createCell(7).setCellValue("Ledolgozott Percek")
        //firstRow.createCell(8).setCellValue("Korrigalt Percek")

        var rownum = 1
        //keyset.forEach { key ->
        //var sumWorkTime = 0L
        var sumCorrigateWorktime = 0.0

        var delelott = 0.0
        var delutan = 0.0
        var hetvegen = 0.0

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
            //val workTimeCell = row.createCell(4)
            //val corrigateWorkTimeCell = row.createCell(5)

            if ("HETVEGE".equals(muszak)) {
                //hetvegen += timeData.workTimeMinutes
                row.createCell(holidayColumn).setCellValue(timeData.workTimeMinutes.toDouble() - 10)
            } else if ("REGGEL".equals(muszak)) {
                //delelott += timeData.workTimeMinutes
                row.createCell(morningColumn).setCellValue(timeData.workTimeMinutes.toDouble() - 10)
            } else if ("ESTI".equals(muszak)) {
                //delutan += timeData.workTimeMinutes
                row.createCell(afternoonColumn).setCellValue(timeData.workTimeMinutes.toDouble() - 10)
            }

            /*if (timeData.workTimeMinutes is Long) {
                workTimeCell.setCellValue(timeData.workTimeMinutes.toDouble())

                val corrigateWorkTime = (timeData.workTimeMinutes - 10) * getMultiply(muszak)
                sumCorrigateWorktime += corrigateWorkTime
                corrigateWorkTimeCell.setCellValue(corrigateWorkTime)
                sumWorkTime += timeData.workTimeMinutes
            }*/
        }
        val lastRow = rownum

        val sumRow = sheet.createRow(rownum++)


        val morningWorkTimeColumn = "E"
        val afternoonWorkTimeColumn = "F"
        val holidayWorkTimeColumn = "G"
        var sumMorningWorkTimeCell = sumRow.createCell(morningColumn)
        val sumFormulaWorkTime = "SUM(${morningWorkTimeColumn}2:${morningWorkTimeColumn}${lastRow})"
        sumMorningWorkTimeCell.setCellType(HSSFCell.CELL_TYPE_FORMULA);
        sumMorningWorkTimeCell.setCellFormula(sumFormulaWorkTime)

        var sumAfternoonWorkTimeCell = sumRow.createCell(afternoonColumn)
        //sumCorrigateWorkTimeCell.setCellValue(sumCorrigateWorktime)
        val sumFormulaCorrigateWorkTime = "SUM(${afternoonWorkTimeColumn}2:${afternoonWorkTimeColumn}${lastRow})"
        sumAfternoonWorkTimeCell.setCellType(HSSFCell.CELL_TYPE_FORMULA)
        sumAfternoonWorkTimeCell.setCellFormula(sumFormulaCorrigateWorkTime)

        var sumHolidayWorkTimeCell = sumRow.createCell(holidayColumn)
        //sumCorrigateWorkTimeCell.setCellValue(sumCorrigateWorktime)
        val sumFormulaHolidayWorkTime = "SUM(${holidayWorkTimeColumn}2:${holidayWorkTimeColumn}${lastRow})"
        sumHolidayWorkTimeCell.setCellType(HSSFCell.CELL_TYPE_FORMULA)
        sumHolidayWorkTimeCell.setCellFormula(sumFormulaHolidayWorkTime)

        val rowHour = sheet.createRow(rownum++)
        rowHour.createCell(3).setCellValue("Oraban:")
        val morningWorkTimeInHour = rowHour.createCell(morningColumn)
        morningWorkTimeInHour.cellType = HSSFCell.CELL_TYPE_FORMULA
        morningWorkTimeInHour.cellFormula = "${morningWorkTimeColumn}${sumRow.rowNum + 1}/60"

        val afternoonWorkTimeInHour = rowHour.createCell(afternoonColumn)
        afternoonWorkTimeInHour.cellType = HSSFCell.CELL_TYPE_FORMULA
        afternoonWorkTimeInHour.cellFormula = "${afternoonWorkTimeColumn}${sumRow.rowNum + 1}/60"


        val holidayWorkTimeInHour = rowHour.createCell(holidayColumn)
        holidayWorkTimeInHour.cellType = HSSFCell.CELL_TYPE_FORMULA
        holidayWorkTimeInHour.cellFormula = "${holidayWorkTimeColumn}${sumRow.rowNum + 1}/60"

        val rowBer = sheet.createRow(rownum++)
        rowBer.createCell(0).setCellValue("Ora ber:")
        rowBer.createCell(1).setCellValue(850.0)
        val oraBer = rowBer.getCell(1).numericCellValue
        val oraBerHiv = "B${rowBer.rowNum + 1}"


        val morningRow = sheet.createRow(rownum++)
        morningRow.createCell(0).setCellValue("Delelott:")
        val morningWorkTimaValue = morningRow.createCell(1)
        morningWorkTimaValue.cellType = HSSFCell.CELL_TYPE_FORMULA
        morningWorkTimaValue.cellFormula = "${morningWorkTimeColumn}${morningWorkTimeInHour.rowIndex + 1}"


        val morningFee = morningRow.createCell(4)
        morningFee.cellType = HSSFCell.CELL_TYPE_FORMULA
        morningFee.cellFormula = "B${morningRow.rowNum + 1} * ${oraBerHiv}"

        val afternoonRow = sheet.createRow(rownum++)
        afternoonRow.createCell(0).setCellValue("Delutan:")
        val afternoonWorkTimeValue = afternoonRow.createCell(1)
        afternoonWorkTimeValue.cellType = HSSFCell.CELL_TYPE_FORMULA
        afternoonWorkTimeValue.cellFormula = "${afternoonWorkTimeColumn}${afternoonWorkTimeInHour.rowIndex +1}"

        val afternoonFee = afternoonRow.createCell(morningColumn)
        afternoonFee.cellType = HSSFCell.CELL_TYPE_FORMULA
        afternoonFee.cellFormula = "B${afternoonRow.rowNum + 1} * ${oraBerHiv} * 1.1"

        val holidayRow = sheet.createRow(rownum++)
        holidayRow.createCell(0).setCellValue("Hetvegen:")
        val holidayWorkTimeValue = holidayRow.createCell(1)
        holidayWorkTimeValue.cellType = HSSFCell.CELL_TYPE_FORMULA
        holidayWorkTimeValue.cellFormula = "${holidayWorkTimeColumn}${holidayWorkTimeInHour.rowIndex +1}"

        val hetvegeBer = holidayRow.createCell(morningColumn)
        hetvegeBer.cellType = HSSFCell.CELL_TYPE_FORMULA
        hetvegeBer.cellFormula = "B${holidayRow.rowNum + 1} * ${oraBerHiv} * 1.34"

        val szabadsagRow = sheet.createRow(rownum++)
        szabadsagRow.createCell(0).setCellValue("Szabadsag:")
        szabadsagRow.createCell(1).setCellValue(0.0)
        val szabadsagBer = szabadsagRow.createCell(morningColumn)
        szabadsagBer.cellType = HSSFCell.CELL_TYPE_FORMULA
        szabadsagBer.cellFormula = "B${szabadsagRow.rowNum + 1}* ${oraBerHiv}"

        val betegsegRow = sheet.createRow(rownum++)
        betegsegRow.createCell(0).setCellValue("Betegseg:")
        betegsegRow.createCell(1).setCellValue(0.0)

        val unnepnapRow = sheet.createRow(rownum++)
        unnepnapRow.createCell(0).setCellValue("Unnepnapp:")
        unnepnapRow.createCell(1).setCellValue(0.0)
        val holidayBer = unnepnapRow.createCell(morningColumn)
        holidayBer.cellType = HSSFCell.CELL_TYPE_FORMULA
        holidayBer.cellFormula = "B${unnepnapRow.rowNum + 1} * ${oraBerHiv}"

        val extraHoursRow = sheet.createRow(rownum++)
        extraHoursRow.createCell(0).setCellValue("Tulorapotlek")


        val hoursRow = sheet.createRow(rownum++)
        hoursRow.createCell(0).setCellValue("Ledolgozando orak:")
        hoursRow.createCell(1).setCellValue(160.0)

        val extraHoursCell = extraHoursRow.createCell(1)
        extraHoursCell.cellType = HSSFCell.CELL_TYPE_FORMULA
        extraHoursCell.cellFormula = "IF(SUM(B${morningRow.rowNum + 1}:B${afternoonRow.rowNum + 1})>B${hoursRow.rowNum + 1}," +
                "SUM(B${morningRow.rowNum + 1}:B${afternoonRow.rowNum + 1})-B${hoursRow.rowNum + 1},0.0)"

        /*        val worktimeInAMounth = delelottRow.getCell(1).numericCellValue + delutanRow.getCell(1).numericCellValue
                if (worktimeInAMounth <= hoursRow.getCell(1).numericCellValue) {
                    val extraTimes = worktimeInAMounth - hoursRow.getCell(1).numericCellValue
                    extraHoursRow.createCell(1).setCellValue(extraTimes)
                } else {
                    extraHoursRow.createCell(1).setCellValue(0.0)
                }*/
        val extraHoursBer = extraHoursRow.createCell(4)
        extraHoursBer.cellType = HSSFCell.CELL_TYPE_FORMULA
        extraHoursBer.cellFormula = "B${extraHoursRow.rowNum + 1} * ${oraBerHiv} * 0.34"

        val fee = (sumCorrigateWorktime / 60) * 850

        val row3 = sheet.createRow(rownum++)
        row3.createCell(0).setCellValue("Kifizetett ber:")
        row3.createCell(1).setCellValue(fee)

        val sumAllFormula = "SUM(E${morningRow.rowNum + 1}:E${extraHoursRow.rowNum + 1})"
        val allFee = row3.createCell(4)
        allFee.setCellType(HSSFCell.CELL_TYPE_FORMULA)
        allFee.setCellFormula(sumAllFormula)

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

