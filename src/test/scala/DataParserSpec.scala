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

class DataParserSpec extends Specification
  with GWT
  with StandardRegexStepParsers { def is = s2"""
  When no data given, do not launch algorithm   ${testResources.start}
    Given no train data
    Given no test data
    Then should do nothing                      ${testResources.end}

  When data given, launch algorithm             ${dataSet.start}
    Given Train file: es_ancora-converted-train1
    Given Test file: es_ancora-converted-test1
    When Launching
    Then should read Correctly                  ${dataSet.end}
"""

  val dataSetName = readAs(".*: (.*)$").and((s: String) => s)
  val noDataSet = readAs(".*").and((s:String) => "/tmp/aa")

  val testResources =
    Scenario("NoData").
      given(noDataSet).
      given(noDataSet).
      when() {case train :: test :: _ =>

        val trainResource = if (getClass.getResource(train) == null) "" else getClass.getResource(train).getPath
        val testResource = if (getClass.getResource(test) == null) "" else getClass.getResource(test).getPath

        val r1 = DataParser.readDataSet(trainResource)
        val r2 =  DataParser.readDataSet(testResource)

        (r1,r2)
      }.
      andThen() {case  _ :: r :: _ => (r._1 must beNone) && (r._2 must beNone) }

  val dataSet = Scenario("DATA").
      given(dataSetName).
      given(dataSetName).
      when(aString) {case _ :: t :: tt :: _ =>
        val r1 = DataParser.readDataSet(getClass.getResource(s"/data/spanish/$t").getPath)
        val r2 =  DataParser.readDataSet(getClass.getResource(s"/data/spanish/$tt").getPath)

        (r1,r2)
      }.
      andThen() {case _ :: r :: _ => (r._1 must beSome) && (r._2 must beSome)}

}

class GWTSpec extends Specification with GWT with StandardRegexStepParsers { def is = s2"""
 A given-when-then example for a calculator                       ${calculator1.start}
   Given the following number: 1.0
   And a second number: 2.0
   And a third number: 6.0
   When I use this operator: +
   Then I should get: 9.0
   And it should be >: 0.0                                          ${calculator1.end}
"""
  val anOperator = readAs(".*: (.)$").and((s: String) => s)

  val calculator1 =
    Scenario("calculator1")
      .given(aDouble)
      .given(aDouble)
      .given(aDouble)
      .when(anOperator) { case op :: i :: j :: k :: _ => if (op == "+") i+j+k else i*j*k }
      .andThen(aDouble)   { case expected :: sum :: _ => sum === expected }
      .andThen(aDouble)   { case expected :: sum :: _ => sum must be_>(expected) }
}