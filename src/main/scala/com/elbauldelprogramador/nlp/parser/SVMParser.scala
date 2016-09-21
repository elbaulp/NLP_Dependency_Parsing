/*
 *     SVMParser.scala is part of grado_informatica_tfg_naturallanguageprocessing (grado_informatica_TFG_NaturalLanguageProcessing).
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

package com.elbauldelprogramador.nlp.parser

import com.elbauldelprogramador.nlp.datastructures.{Node, Sentence}
import com.elbauldelprogramador.nlp.utils.DataTypes.Counter

import scala.collection.mutable
import scala.collection.mutable.Map

/**
  * Created by Alejandro Alcalde <contacto@elbauldelprogramador.com> on 8/29/16.
  */
class SVMParser {
  val Left = 0
  val Shift = 1
  val Right = 2

  // TODO: try to make them vals
  var positionVocab = Map.empty[Int, Counter]
  var positionTag = Map.empty[Int, Counter]

  def train(sentences: Vector[Sentence]) = {
    for (s <- sentences) {
      var trees = s.tree
      var i = 0
      var noConstruction = false
      while (trees.nonEmpty && !noConstruction) {
        if (i == trees.size - 1) {
          noConstruction = true
          i = 0
        } else {
          // Build vocabulary
          buildVocabulary(trees, i, 2, 4)

          val y = estimateTrainAction(trees, i)
          val (newI, newTrees) = takeAction(trees, i, y)
          i = newI
          trees = newTrees
        }
      }
    }

    // Convert vocabulary to features
    //    positionVocab foreach println
    toFeatures(positionVocab)
    println("hola")
  }

  def takeAction(trees: Vector[Node], index: Int, action: Int /*TODO: issue #1 ACTION*/): (Int /*TODO: Action*/ ,
    Vector[Node]) = {
    val a = trees(index)
    val b = trees(index + 1)
    var i = index

    action match {
      case Right /*TODO: Action */ =>
        b.insertRight(a)
        // Update the tree and remove a
        val updatedTree = trees updated(index + 1, b) diff (Vector(a))
        if (i == 0)
          i = 1
        (i - 1, updatedTree)
      case Left /*TODO: Action */ =>
        a.insertLeft(b)
        val updatedTree = trees updated(index, a) diff (Vector(b))
        if (i == 0)
          i = 1
        (i - 1, updatedTree)
      case _ => //SHIFT
        (i + 1, trees)
    }
  }

  def estimateTrainAction(trees: Vector[Node], index: Int): Int /*TODO: Return an ACTION */ = {
    val a = trees(index)
    val b = trees(index + 1)

    if (a.dependency == b.position && isCompleteSubtree(trees, a))
      Right // TODO: RIGHT
    else if (b.dependency == a.position && isCompleteSubtree(trees, b))
      Left // TODO: LEFT
    else
      Shift // TODO: SHIFT
  }

  def isCompleteSubtree(trees: Vector[Node], child: Node): Boolean =
    !trees.exists(_.dependency == child.position)

  def buildVocabulary(trees: Vector[Node], i: Int, leftCtx: Int, rightCtx: Int) = {
    val range = (i - leftCtx to i + 1 + rightCtx).zipWithIndex
    //    var positionVocab = Map.empty[Int, MultiSet[String]]
    //    var positionTag = Map.empty[Int, Map[String, Int]]

    for ((w, k) <- range) {
      if (w >= 0 && w < trees.size) {
        val targetNode = trees(w)
        // Try to get the counter for this lex, if not, create one
        val word = positionVocab getOrElseUpdate(k, mutable.Map(targetNode.lex -> 0))
        // Increment the counter for this lex by 1, create one if not exists
        word += (targetNode.lex -> (word.getOrElseUpdate(targetNode.lex, 0) + 1))

        positionVocab += (k -> word)
      }
    }
  }

  def toFeatures(counter: Map[Int,  Counter]): Map[Int,  Counter] = {
    // Assign to each string key a counter, starting from 0 to the map size
    counter foreach {
      case (_, map) =>
        for ((lexKey, value) <- map.keys.zipWithIndex) map update(lexKey, value)
        map update("UNKNOWN", map.size) // For previously unknown features during training
    }

    counter
  }
}
