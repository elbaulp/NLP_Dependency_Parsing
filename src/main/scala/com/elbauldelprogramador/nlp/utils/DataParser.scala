
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

import com.elbauldelprogramador.nlp.datastructures.Sentence

import scala.io.Source
import scala.language.reflectiveCalls
import scala.util.control.NonFatal


/**
  * Read and parse the data sets
  *
  * Created by Alejandro Alcalde <contacto@elbauldelprogramador.com> on 8/20/16.
  */
object DataParser {

  def parseDataSet(file: String): Option[Vector[Sentence]] = {
    val filePath = getClass.getResource(file).getPath
    val EoS = ("EOS", "EOS", -1)

    Manage(Source.fromFile(filePath)) { source =>

      val parsedTuples = source getLines() map (s => s.split("\t") match {
        case Array(lex, posTag, dep) => Some((lex, posTag, dep.toInt))
        case _ => Some(EoS) // End Of Sentence
      })

      val sentences = Vector.newBuilder[Sentence]
      val lex = Vector.newBuilder[String]
      val po = Vector.newBuilder[String]
      val d = Vector.newBuilder[Int]

      for (Some(t) <- parsedTuples) {
        t match {
          case EoS =>
            sentences += new Sentence(lex.result(), po.result(), d.result())
            lex.clear()
            po.clear()
            d.clear()
          case _ =>
            lex += t._1
            po += t._2
            d += t._3
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