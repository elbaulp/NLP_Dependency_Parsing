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

/**
  * Created by Alejandro Alcalde <contacto@elbauldelprogramador.com> on 8/29/16.
  */
class SVMParser {
  val Left = 0
  val Shift = 1
  val Right = 2
  val LeftCtx = 2
  val RightCtx = 4

  val Unknown = "UNKNOWN"

  // TODO: try to make them vals
  val positionVocab = mutable.Map.empty[Int, Counter]
  val positionTag = mutable.Map.empty[Int, Counter]
  val chLVocab = mutable.Map.empty[Int, Counter]
  val chLTag = mutable.Map.empty[Int, Counter]
  val chRVocab = mutable.Map.empty[Int, Counter]
  val chRTag = mutable.Map.empty[Int, Counter]
  val tagActions = mutable.Map[String, mutable.Map[Int,Int]]()
  var NFeatures = 0

  def train(sentences: Vector[Sentence]) = {
    // TODO: Optimize this, may be tail rec?
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
          buildVocabulary(trees, i, LeftCtx, RightCtx)

          val y = estimateTrainAction(trees, i)
          val (newI, newTrees) = takeAction(trees, i, y)
          i = newI
          trees = newTrees

          // Execute the action and modify the trees
          if (y != Shift)
            noConstruction = false
        }
      }
    }

    // Convert vocabulary to features
    toFeatures(positionTag)
    toFeatures(positionVocab)
    toFeatures(chLTag)
    toFeatures(chRTag)
    toFeatures(chLVocab)
    toFeatures(chRVocab)

    // Set the total number of features
    NFeatures = countFeatures(positionVocab) +
      countFeatures(positionTag) +
      countFeatures(chLTag) +
      countFeatures(chRTag) +
      countFeatures(chLVocab) +
      countFeatures(chRVocab)

    // TODO Check if we have a previously trained model before try to train a new one

    val trainX = mutable.Map.empty[String, Vector[Int]]
    val trainY = mutable.Map.empty[String, Vector[Int]]

    for (s <- sentences) {
      var trees = s.tree
      var i = 0
      var noConstruction = false
      while (trees.nonEmpty && !noConstruction) {
        if (i == trees.size - 1) {
          noConstruction = true
          i = 0
        } else {
          val posTag = trees(i).posTag

          // extract features
          val extractedFeatures = extractTestFeatures(trees, i, LeftCtx, RightCtx)

          val y = estimateTrainAction(trees, i)

          // Update pos Action
          val actionCounter = tagActions getOrElseUpdate (posTag, mutable.Map(y -> 0)) getOrElseUpdate (y, 0)
          tagActions(posTag)(y) = actionCounter + 1

          trainX getOrElseUpdate(posTag, extractedFeatures ++ Vector.empty)
          trainY getOrElseUpdate(posTag, y +: Vector.empty)

          val (newI, newTrees) = takeAction(trees, i, y)
          i = newI
          trees = newTrees

          // Execute the action and modify the trees
          if (y != Shift)
            noConstruction = false
        }
      }
    }

    for (lp <- trainX.keys){
      println(lp)
      println(s"Size of $lp: ${lp.size}")
      println(s"# features: $NFeatures")

      val classes = trainY.values.toSet.flatten
      println(classes)
    }
  }

  def countFeatures(feature: mutable.Map[Int, Counter]): Int = (feature map (_._2.size)).sum

  def extractTestFeatures(trees: Vector[Node], i: Int, leftCtx: Int, rightCtx: Int):Vector[Int] = {
    // Method to extract features for the given context window
    val range = (i - leftCtx to i + 1 + rightCtx).zipWithIndex
    var offset = 0
    var features = Vector.empty[Int]

    for ((w, k) <- range) {
      if (w >= 0 && w < trees.size) {
        val targetNode = trees(w)

        val lexTemp = lexFeature(k, targetNode, offset) +: Vector.empty
        offset += positionVocab(k).size
        val tagTemp = posTagFeature(k, targetNode, offset) +: Vector.empty
        offset += positionTag(k).size
        val chLLexTemp = childFeatures(k, targetNode.left,
          offset, chLVocab getOrElseUpdate(k, mutable.Map(Unknown -> 0)), 0)
        offset += chLVocab(k).size
        val chLTagTemp = childFeatures(k, targetNode.left, offset, chLTag getOrElseUpdate(k, mutable.Map(Unknown ->
          0)), 1)
        offset += chLTag(k).size
        val chRLexTemp = childFeatures(k, targetNode.right, offset, chRVocab getOrElseUpdate(k, mutable.Map(Unknown -> 0)), 0)
        offset += chRVocab(k).size
        val chRTagTemp = childFeatures(k, targetNode.right, offset, chRTag getOrElseUpdate(k, mutable.Map(Unknown -> 0)), 0)
        offset += chRTag(k).size

        features = features ++ lexTemp ++ tagTemp ++ chLLexTemp ++ chLTagTemp ++ chRLexTemp ++ chRTagTemp
      }
    }

    features
  }

  def posTagFeature(position: Int, node: Node, offset: Int): Int = {
    val vocab = positionTag(position) // TODO: Match better

    if (vocab.contains(node.posTag))
      vocab(node.posTag) + offset
    else
      vocab(Unknown) + offset
  }

  def lexFeature(position: Int, node: Node, offset: Int): Int = {
    val vocab = positionVocab(position) // TODO: Match better

    if (vocab.contains(node.lex))
      vocab(node.lex) + offset
    else
      vocab(Unknown) + offset
  }

  // TODO: To recursive
  def childFeatures(position: Int, children: Vector[Node], offset: Int, family: Counter, featureType: Int):
  Vector[Int] =  {
    var indices = Vector.empty[Int]
    for (child <- children){
      indices = indices :+ childFeature(position, child, offset, family, featureType)
    }
    indices
  }

  def childFeature(position: Int, node: Node, offset: Int, family: Counter, featureType: Int):Int = {
    val vocab = family

    if (featureType == 0) /* EXTRACT_LEX */ {
      if(vocab contains node.lex)
        vocab(node.lex) + offset
      else {
        if (vocab contains node.posTag)
          vocab(node.posTag) + offset
      }
    }
    vocab(Unknown) + offset
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
        val updatedTree = trees updated(index + 1, b) diff Vector(a)
        if (i == 0)
          i = 1
        (i - 1, updatedTree)
      case Left /*TODO: Action */ =>
        a.insertLeft(b)
        val updatedTree = trees updated(index, a) diff Vector(b)
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

  /**
    *
    * TODO: Issue #3 Refactor this function
    */
  def buildVocabulary(trees: Vector[Node], i: Int, leftCtx: Int, rightCtx: Int) = {
    val range = (i - leftCtx to i + 1 + rightCtx).zipWithIndex

    for ((w, k) <- range) {
      if (w >= 0 && w < trees.size) {
        val targetNode = trees(w)
        // Try to get the counter for this lex, if not, create one
        val word = positionVocab getOrElseUpdate(k, mutable.Map(targetNode.lex -> 0))
        // Same for POS Tag
        val tag = positionTag getOrElseUpdate(k, mutable.Map(targetNode.posTag -> 0))

        // Increment the counter for this lex by 1, create one if not exists
        word += (targetNode.lex -> (word.getOrElseUpdate(targetNode.lex, 0) + 1))
        tag += (targetNode.posTag -> (word.getOrElseUpdate(targetNode.posTag, 0) + 1))

        positionVocab += (k -> word)
        positionTag += (k -> tag)

        // Check left and righ childs
        for (leftChild <- targetNode.left) {
          val word = chLVocab getOrElseUpdate(k, mutable.Map(leftChild.lex -> 0))
          val tag = chLTag getOrElseUpdate(k, mutable.Map(leftChild.posTag -> 0))

          word += (leftChild.lex -> (word.getOrElseUpdate(leftChild.lex, 0) + 1))
          tag += (leftChild.posTag -> (word.getOrElseUpdate(leftChild.posTag, 0) + 1))

          chLVocab += (k -> word)
          chLTag += (k -> tag)
        }

        for (rightChild <- targetNode.right) {
          val word = chRVocab getOrElseUpdate(k, mutable.Map(rightChild.lex -> 0))
          val tag = chRTag getOrElseUpdate(k, mutable.Map(rightChild.posTag -> 0))

          word += (rightChild.lex -> (word.getOrElseUpdate(rightChild.lex, 0) + 1))
          tag += (rightChild.posTag -> (word.getOrElseUpdate(rightChild.posTag, 0) + 1))

          chRVocab += (k -> word)
          chRTag += (k -> tag)
        }
      }
    }
  }

  def toFeatures(counter: mutable.Map[Int, Counter]): mutable.Map[Int, Counter] = {
    //    counter map (_._2.size) zipWithIndex ()
    // Assign to each string key a counter, starting from 0 to the map size
    counter foreach {
      case (_, map) =>
        for ((lexKey, value) <- map.keys.zipWithIndex) map update(lexKey, value)
        map update(Unknown, map.size) // For previously unknown features during training
    }
    counter
  }
}
