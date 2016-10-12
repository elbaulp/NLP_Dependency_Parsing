
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

import com.elbauldelprogramador.nlp.datastructures.LabeledSentence

import scala.io.Source
import scala.language.reflectiveCalls
import scala.util.control.NonFatal


/**
  * Read and parse the data sets
  *
  * Created by Alejandro Alcalde <contacto@elbauldelprogramador.com> on 8/20/16.
  */
object DataParser {

  type Or[A, B] = Either[A,B]

  // TODO: issue #5 Parse command line args like learning scala book, in section about Nothing Data Type
  def readDataSet(file: String): Option[Vector[LabeledSentence]] = {

    def getSentenceType(s: Array[String]) =
      if (s.length == 3) {
        Left((s(0), s(1), s(2).toInt))
      } else if (s.length == 4) {
        Right((s(0), s(1), s(2), s(3).toInt))
      } else {
        Right(("EOS", "EOS", "EOS", -1))
      }

    val filePath = getClass.getResource(file).getPath

    Manage(Source.fromFile(filePath)) { source =>

      val parsedTuples = source getLines() map (s => s.split("\t"))

      val sentences = Vector.newBuilder[LabeledSentence]
      val lex = Vector.newBuilder[String]
      val po = Vector.newBuilder[String]
      val gold = Vector.newBuilder[String]
      val dep = Vector.newBuilder[Int]

      for (s <- parsedTuples) {
        getSentenceType(s) match {
          case Right(("EOS", "EOS", "EOS", -1)) =>
            sentences += new LabeledSentence(lex.result(), po.result(), dep.result())
            lex.clear()
            po.clear()
            dep.clear()
          //            if (isTrain) gold.clear()
          case Left(x) =>
            lex += x._1
            po += x._2
            dep += x._3
          case Right(x) =>
            lex += x._1
            po += x._2
            gold += x._3
            dep += x._4
        }
      }
      Some(sentences.result())
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