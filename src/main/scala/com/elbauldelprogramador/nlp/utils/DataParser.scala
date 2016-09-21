
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

  // TODO: issue #2 Use Either instead of Option to read train/test data

  def readDataSet(file: String,
                  isTrain: Boolean = true): Option[Vector[Sentence]] = {

    val filePath = getClass.getResource(file).getPath
    val EoS = if (isTrain) ("EOS", "EOS", -1) else ("EOS", "EOS", "EOS", -1)

    Manage(Source.fromFile(filePath)) { source =>

      val parsedTuples = source getLines() map (s => s.split("\t") match {
        case Array(l, posTag, dep) => Some((l, posTag, dep.toInt))
        case Array(l, posTag, goldTag, dep) => Some((l, posTag, goldTag, dep.toInt))
        case _ => Some(EoS) // End Of Sentence
      })

      val sentences = Vector.newBuilder[Sentence]
      val lex = Vector.newBuilder[String]
      val po = Vector.newBuilder[String]
      val gold = Vector.newBuilder[String]
      val d = Vector.newBuilder[Int]


      for (Some(t) <- parsedTuples) {
        t match {
          case EoS =>
            sentences += new Sentence(lex.result(), po.result(), d.result())
            lex.clear()
            po.clear()
            d.clear()
            if (isTrain) gold.clear()
          case _ if isTrain =>
            lex += t.asInstanceOf[(String, String, Int)]._1
            po += t.asInstanceOf[(String, String, Int)]._2
            d += t.asInstanceOf[(String, String, Int)]._3
          case _ if !isTrain =>
            lex += t.asInstanceOf[(String, String, String, Int)]._1
            po += t.asInstanceOf[(String, String, String, Int)]._2
            gold += t.asInstanceOf[(String, String, String, Int)]._3
            d += t.asInstanceOf[(String, String, String, Int)]._4
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