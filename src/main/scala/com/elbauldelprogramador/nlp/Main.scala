
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

import com.elbauldelprogramador.nlp.parser.SVMParser
import com.elbauldelprogramador.nlp.utils.{CommandArgs, DataParser}

/**
  * Created by Alejandro Alcalde <contacto@elbauldelprogramador.com> on 8/18/16.
  */
object Main extends App {
    val argz = CommandArgs.parseArgs(args)

    // Read and parse training data
    val trainSentences = DataParser.readDataSet(argz.trainingPath)
    val testSentences = DataParser.readDataSet(argz.testPath)

    val parser = new SVMParser
    parser.train(trainSentences.get)

    // Inference
    val inferredTree = parser.test(testSentences.get)
    // Evaluation
    val evaluation = parser.evaluate(inferredTree, testSentences.get)
}


