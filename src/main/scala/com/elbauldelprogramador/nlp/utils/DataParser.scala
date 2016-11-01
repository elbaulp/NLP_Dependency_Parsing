
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
  def readDataSet(file: String): Option[Vector[LabeledSentence]] = {

    Manage(Source.fromFile(file)) { source =>

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

/**
  * Simple command-line parsing, thanks to Programming scala, 2nd edition: http://amzn.to/2eN8IRl
  */
object CommandArgs {
  val help =
    """
      |usage: java ... objectsystem.CommandArgs arguments
      |where the allowed arguments are:
      | -h | --help          Show this help
      | -d | --train path    Path of training data
      | -t | --test path     Path of test data
      | """.stripMargin

  def parseArgs(args: Array[String]): Args = {
    def pa(args2: List[String], result: Args): Args = args2 match {
      case Nil => result
      case ("-h" | "--help") :: Nil => quit("", 0)
      case ("-d" | "--train") :: path :: tail =>
        pa(tail, result copy (trainingPath = path))
      case ("-t" | "--test") :: path :: tail =>
        pa(tail, result copy (testPath = path))
      case _ => quit(s"Unrecognized argument ${args2.head}", 1)
    }

    val argz = pa(args.toList, Args("", ""))
    if (argz.trainingPath == "" || argz.testPath == "")
      quit("Must specify input and output paths.", 1)
    argz
  }

  def quit(message: String, status: Int): Nothing = {
    if (message.length > 0) println(message)
    println(help)
    sys.exit(status)
  }

  case class Args(trainingPath: String, testPath: String)
}
