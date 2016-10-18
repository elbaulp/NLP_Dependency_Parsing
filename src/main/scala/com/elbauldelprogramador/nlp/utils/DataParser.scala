
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

import com.elbauldelprogramador.nlp.datastructures.{LabeledSentence, Tokens}

import scala.io.Source
import scala.language.reflectiveCalls
import scala.util.control.NonFatal


/**
  * Read and parse the data sets
  *
  * Created by Alejandro Alcalde <contacto@elbauldelprogramador.com> on 8/20/16.
  */
object DataParser {
  // TODO: Better use some combinator? https://github.com/tpolecat/atto | http://www.lihaoyi.com/fastparse/
  // TODO: issue #5 Parse command line args like learning scala book, in section about Nothing Data Type
  def readDataSet(file: String): Option[Vector[LabeledSentence]] = {
    val filePath = getClass.getResource(file).getPath

    Manage(Source.fromFile(filePath)) { source =>

      val parsedTuples = source
        .getLines()
        .map(s => s.split("\t"))
        .map {
          case Array(a, b, c, d) => Tokens(a, b, c, d.toInt)
          case Array(a, b, d) => Tokens(a, b, "", d.toInt)
          case _ => Tokens() // Read end of sentence
        }./:((Tokens(), Vector.empty[LabeledSentence])) {
          // When reading an end of sentence, create a new Labeled sentence with tokens
          case ((z, l), t) if t.isEmpty => (Tokens(), l :+ LabeledSentence(z))
          // Accumulate tokens of the sentence
          case ((z, l), t) => (z append(z, t), l)
        }._2

      Some(parsedTuples)
    }
  }
}

/**
  * An implementation of a reusable application resource manager,
  *
  * Taken from the book “Programing scala, 2nd Edition”
  */

object Manage {
  //noinspection ScalaStyle
  def apply[R <: {def close() : Unit}, T](resource: => R)(f: R => Option[T]): Option[T] = {
    var res: Option[R] = None
    try {
      res = Some(resource) // Only reference "resource" once!!
      f(res.get)
    } catch {
      case NonFatal(ex) => {
        System.err.println(s"Non fatal exception! $ex")
        None
      }
    } finally {
      if (res.isDefined) {
        System.err.println(s"Closing resource...")
        res.get.close()
      }
    }
  }
}