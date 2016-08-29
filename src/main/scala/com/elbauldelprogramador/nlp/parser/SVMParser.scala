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

import com.elbauldelprogramador.nlp.datastructures.{MultiSet, Node, Sentence}

/**
  * Created by Alejandro Alcalde <contacto@elbauldelprogramador.com> on 8/29/16.
  */
class SVMParser {
  val Left = 0
  val Shift = 1
  val Right = 2

  var positionVocab: Map[Int, MultiSet[String]] = Map.empty

  def train(sentences: Vector[Sentence]) = {
    for (s <- sentences) {
      var trees = s.tree
      var i = 0
      var noConstruction = false
      while (trees.size > 0 && !noConstruction) {
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
    positionVocab foreach println
  }

  def takeAction(trees: Vector[Node], index: Int, action: Int /*TODO: ACTION*/): (Int /*TODO: Action*/ ,
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
    var positionTag = Map.empty[Int, MultiSet[String]]

    for ((w, k) <- range) {
      if (w >= 0 && w < trees.size) {
        val targetNode = trees(w)
        val word = positionVocab.getOrElse(k, new MultiSet[String])
        word.add(targetNode.lex)
        positionVocab += (k -> word)
      }
    }
  }
}
