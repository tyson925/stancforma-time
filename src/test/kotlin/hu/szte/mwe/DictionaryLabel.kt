package hu.szte.mwe

import java.io.*
import java.util.*

data class PredictedData(val token : String,val prediction : String)

class DictionaryLabel {

    fun readCorpusData(fileName : String) : List<List<PredictedData>>{
        val corpus = LinkedList<List<PredictedData>>()
        var sentetence = LinkedList<PredictedData>()
        File(fileName).forEachLine { line ->
            if ("".equals(line.trim())){
                corpus.add(sentetence)
                sentetence = LinkedList<PredictedData>()
            } else{
                val splittedLine = line.split("\t")
                println(line)
                sentetence.add(PredictedData(splittedLine[0],splittedLine[1]))
            }
        }
        return corpus
    }

    fun printLabelStat(){

        val reader = BufferedReader(InputStreamReader(FileInputStream("./data/collocation_10000.iob")))
        var line : String? = reader.readLine()
        val list = LinkedList<String>()
        while (line != null){
            line = reader.readLine()
            if (line != null) {
                var lineSplit = line.split(" ")
                if (lineSplit.size > 1 && "B-COL".equals(lineSplit[1]) && "O".equals(lineSplit[2])) {
                    var collocation = lineSplit[0]
                    line = reader.readLine()
                    lineSplit = line.split(" ")
                    while (line != null && lineSplit.size > 1 && "I-COL".equals(lineSplit[1]) && "O".equals(lineSplit[2])) {
                        collocation += " " + lineSplit[0]
                        line = reader.readLine()
                        if (line != null) {
                            lineSplit = line.split(" ")
                        }
                    }
                    list.add(collocation)
                    //println(collocation)
                }
            }
        }
        val res = list.groupBy { item -> item }.map { item -> Pair(item.key,item.value.size) }.sortedByDescending { item -> item.second }.joinToString("\n")
        File("./data/error.txt").writeText(res)
        println(res)
    }

    fun readDictRawStringList(fileName : String, take : Int) : List<String>{
        return File(fileName).readLines().map { line -> line.toLowerCase() }.take(take)

    }

    fun dictionaryLabel(dictionaryName: String, corpus: List<List<PredictedData>>, tag: String) {
        val take = 20000
        val dictionary = readDictRawStringList(dictionaryName,take).map { collocation -> collocation.toLowerCase().trim() }.toSet()

        println("APART\t" + dictionary.contains("apart from"))
        val RET = LinkedList<Array<String>>()
        val writer = BufferedWriter(OutputStreamWriter(FileOutputStream("./data/collocation_$take.iob")))
        for (sentence in corpus) {
            val prediction = Array<String>(sentence.size) { "O" }
            for (collokation in dictionary) {
                val collokationArray = collokation.toLowerCase().split(" ")


                sentence.forEachIndexed {i, token ->
                    if ("apart".equals(token.token.toLowerCase())){
                      //  println(token)
                    }
                    if (token.token.toLowerCase().equals(collokationArray[0])) {
                        var j = i
                        var collocationIndex = 0

                        while (j < sentence.size && collocationIndex < collokationArray.size && sentence.get(j).token.toLowerCase().equals(collokationArray[collocationIndex])) {
                            collocationIndex++
                            j++
                        }
                        if (collokationArray.size == collocationIndex) {
                            prediction[i] = "B-${tag}"
                            i.inc()
                            //j.dec()
                            for (tmpI in i + 1..j - 1) {
                                prediction[tmpI] = "I-${tag}"
                                i.inc()
                            }
                        }
                    }
                }

            }
            writeIOB(sentence, prediction, writer, tag)
            RET.add(prediction)
            writer.write("\n")
        }
        writer.flush()
        writer.close()
    }

    private fun writeIOB(sentence: List<PredictedData>, pred: Array<String>, writer: BufferedWriter, tag: String) {
        for (i in 0..sentence.size - 1) {
            var label: String
            /*if (sentence.getMweEtalonLabel().get(i).contains("IDIOM")){
                label = sentence.getMweEtalonLabel().get(i).replace("IDIOM","${tag}").replace("E-","I-")
            } else*/
            if (sentence.get(i).prediction.contains(tag)) {
                label = sentence.get(i).prediction.replace("E-", "I-")
            } else {
                label = "O"
            }
            writer.write("${sentence.get(i).token} ${label} ${pred.get(i)}\n")
        }
    }


}

fun main(args: Array<String>) {
 val dicLabel = DictionaryLabel()
    val corpus = dicLabel.readCorpusData("./../MWE/data/sancl/schneider.txt")
    dicLabel.dictionaryLabel("./../MLyBigData/CoreNlp/googleNgramsList.txt",corpus,"COL")
    //dicLabel.printLabelStat()
}

