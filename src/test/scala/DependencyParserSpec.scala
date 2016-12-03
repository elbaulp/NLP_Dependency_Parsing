import com.elbauldelprogramador.nlp.parser.DependencyParser
import com.elbauldelprogramador.nlp.utils.DataParser
import org.specs2.Specification
import org.specs2.specification.script.{GWT, StandardRegexStepParsers}

/*
 * MIT License
 *
 * Copyright (c) 2016 Alejandro Alcalde
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
/**
  * Created by Alejandro Alcalde <contacto@elbauldelprogramador.com> on 12/2/16.
  */
class DependencyParserSpec extends Specification
  with GWT
  with StandardRegexStepParsers {def is = s2"""
  When training the model, set the following baselines  ${featuresBaseline.start}
    Given Train data set: es_ancora-converted-train1
    Given Test data set: es_ancora-converted-test1
    When Genenaring Vocabulary
    Then Dep. Acc should be at least: 70
    and Root Acc should be at least: 50
    and Comp. Acc should be at least: 11                ${featuresBaseline.end}
"""

  val aDataSet = readAs(".*: (.*)$").and((s: String) => s)
  val myD = readAs(".*: (\\d+)$").and((s: String) => s.toDouble)

  val featuresBaseline =
    Scenario("nFeatures").
      given(aDataSet).
      given(aDataSet).
      when(aString){case _ :: t :: tt :: _ =>
        val testSentences = DataParser.readDataSet(getClass.getResource(s"/data/spanish/$t").getPath)
        val trainSentences = DataParser.readDataSet(getClass.getResource(s"/data/spanish/$tt").getPath)
        val parser = new DependencyParser(trainSentences.get, testSentences.get)
        parser.getAccuracy
      }.
      andThen(myD){case baseline :: r :: _ => r.dependencyAccuracy*100 must be_>(baseline)}.
      andThen(myD){case baseline :: r :: _ => r.rootAccuracy*100 must be_>(baseline)}.
      andThen(myD){case baseline :: r :: _ => r.completeAccuracy*100 must be_>(baseline)}

}
