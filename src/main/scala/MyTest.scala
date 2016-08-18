import org.clulab.processors.Processor
import org.clulab.processors.corenlp.CoreNLPProcessor
import org.clulab.struct.DirectedGraphEdgeIterator

/*
 *     MyTest.scala is part of grado_informatica_tfg_naturallanguageprocessing (grado_informatica_TFG_NaturalLanguageProcessing).
 *
 *     grado_informatica_TFG_NaturalLanguageProcessing is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     grado_informatica_TFG_NaturalLanguageProcessing is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with grado_informatica_TFG_NaturalLanguageProcessing.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
  * Created by Alejandro Alcalde <contacto@elbauldelprogramador.com> on 8/18/16.
  */
object MyTest {
  def main(args: Array[String]) {
    // create the processor
    // any processor works here! Try FastNLPProcessor or BioNLPProcessor.
    val proc: Processor = new CoreNLPProcessor(withDiscourse = true)

    // the actual work is done here
    val doc = proc.annotate("John Smith went to China. He visited Beijing, on January 10th, 2013.")

    // you are basically done. the rest of this code simply prints out the annotations

    // let's print the sentence-level annotations
    var sentenceCount = 0
    for (sentence <- doc.sentences) {
      println("Sentence #" + sentenceCount + ":")
      println("Tokens: " + sentence.words.mkString(" "))
      println("Start character offsets: " + sentence.startOffsets.mkString(" "))
      println("End character offsets: " + sentence.endOffsets.mkString(" "))

      // these annotations are optional, so they are stored using Option objects, hence the foreach statement
      sentence.lemmas.foreach(lemmas => println(s"Lemmas: ${lemmas.mkString(" ")}"))
      sentence.tags.foreach(tags => println(s"POS tags: ${tags.mkString(" ")}"))
      sentence.chunks.foreach(chunks => println(s"Chunks: ${chunks.mkString(" ")}"))
      sentence.entities.foreach(entities => println(s"Named entities: ${entities.mkString(" ")}"))
      sentence.norms.foreach(norms => println(s"Normalized entities: ${norms.mkString(" ")}"))
      sentence.dependencies.foreach(dependencies => {
        println("Syntactic dependencies:")
        val iterator = new DirectedGraphEdgeIterator[String](dependencies)
        while (iterator.hasNext) {
          val dep = iterator.next
          // note that we use offsets starting at 0 (unlike CoreNLP, which uses offsets starting at 1)
          println(" head:" + dep._1 + " modifier:" + dep._2 + " label:" + dep._3)
        }
      })
      sentence.syntacticTree.foreach(tree => {
        println("Constituent tree: " + tree)
        // see the org.clulab.utils.Tree class for more information
        // on syntactic trees, including access to head phrases/words
      })

      sentenceCount += 1
      println("\n")
    }

    // let's print the coreference chains
    doc.coreferenceChains.foreach(chains => {
      for (chain <- chains.getChains) {
        println("Found one coreference chain containing the following mentions:")
        for (mention <- chain) {
          // note that all these offsets start at 0 too
          println("\tsentenceIndex:" + mention.sentenceIndex +
            " headIndex:" + mention.headIndex +
            " startTokenOffset:" + mention.startOffset +
            " endTokenOffset:" + mention.endOffset +
            " text: " + doc.sentences(mention.sentenceIndex).words.slice(mention.startOffset, mention.endOffset).mkString("[", " ", "]"))
        }
      }
    })

    // let's print the discourse tree
    doc.discourseTree.foreach(dt => {
      println("Document-wide discourse tree:")
      println(dt.toString())
    })
  }
}
