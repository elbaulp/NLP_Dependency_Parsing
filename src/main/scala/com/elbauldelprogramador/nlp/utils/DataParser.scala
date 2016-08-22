/*
 *     Reader.scala is part of grado_informatica_tfg_naturallanguageprocessing (grado_informatica_TFG_NaturalLanguageProcessing).
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

package com.elbauldelprogramador.nlp.utils

import java.io.InputStream

import scala.io.Source


/**
  * Read and parse the data sets
  *
  * Created by Alejandro Alcalde <contacto@elbauldelprogramador.com> on 8/20/16.
  */
class DataParser(val fileName: String,
                 val isTraining: Boolean = true) {

  val document: InputStream = {
    getClass.getResourceAsStream(fileName)
  }

  def parseTrainingData = {
    val s = Source.fromInputStream(document).getLines().mkString("\\n")
    val x = s.foreach(s => print(s))
    print(s)
  }
}
