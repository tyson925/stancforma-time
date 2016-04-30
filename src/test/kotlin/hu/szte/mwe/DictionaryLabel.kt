package hu.szte.mwe

import java.io.BufferedWriter
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.util.*
import java.io.File

public data class PredictedData(val token : String,val prediction : String)

public class DictionaryLabel(){

    public fun readCorpusData(fileName : String) : List<List<PredictedData>>{
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

    public fun readDictRawStringList(fileName : String, take : Int) : List<String>{
        return File(fileName).readLines().map { line -> line.toLowerCase() }.take(take)

    }

    public fun dictionaryLabel(dictionaryName: String, corpus: List<List<PredictedData>>, tag: String) {
        val take = 100000
        val dictionary = readDictRawStringList(dictionaryName,take)
        val RET = LinkedList<Array<String>>()
        val writer = BufferedWriter(OutputStreamWriter(FileOutputStream("./data/collocation_$take.iob")))
        for (sentence in corpus) {
            val prediction = Array<String>(sentence.size) { "O" }
            for (collokation in dictionary) {
                val collokationArray = collokation.split(" ")


                sentence.forEachIndexed {i, token ->
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
    dicLabel.dictionaryLabel("./../MLyBigData/CoreNlp/googleNgramsList_3.txt",corpus,"COL");
}

