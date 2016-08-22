/*
 *     Main.scala is part of grado_informatica_tfg_naturallanguageprocessing (grado_informatica_TFG_NaturalLanguageProcessing).
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

package com.elbauldelprogramador.nlp

import java.io.InputStream

import com.elbauldelprogramador.nlp.datastructures.Node

/*
 *     Main.scala is part of grado_informatica_tfg_naturallanguageprocessing (grado_informatica_TFG_NaturalLanguageProcessing).
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

/*
 *     Main.scala is part of grado_informatica_tfg_naturallanguageprocessing (grado_informatica_TFG_NaturalLanguageProcessing).
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
object Main extends App {
  val DataSourcePath = "/data/spanish"
  val ModelsPath = "/models"
  val TrainSentencesFile = "/es_ancora-converted-train"
  val TestSentencesFile = "/ancora-test-one-sentence"

  val TrainSet = getClass.getResourceAsStream(DataSourcePath + TrainSentencesFile)
  val TestSet = getClass.getResourceAsStream(DataSourcePath + TestSentencesFile)

  val TrainSetLines = scala.io.Source.fromInputStream(TrainSet).getLines
  val TestSetLines = scala.io.Source.fromInputStream(TestSet).getLines

  TrainSetLines.map(_.split("\\t")).foreach(r => println(r))

  TestSetLines.foreach(System.out.println)

  val stream: InputStream = getClass.getResourceAsStream(DataSourcePath + TestSentencesFile)
  val lines = scala.io.Source.fromInputStream(stream).getLines

  //  val trainingData = new DataParser(DataSourcePath + TestSentencesFile)
  //
  //  System.out.println(trainingData.parseTrainingData)

  //  lines.foreach(System.out.println)


  val lista = new Node("UNO", 2, "fd", 2)

  println(lista)

  //  val s = new Sentence()
}


