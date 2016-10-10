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

import java.io.File

import com.elbauldelprogramador.nlp.datastructures.{LabeledSentence, Node, Sentence}
import com.elbauldelprogramador.nlp.svm.SVMAdapter._
import com.elbauldelprogramador.nlp.svm.SVMConfig
import com.elbauldelprogramador.nlp.svm.SVMTypes.DblVector
import com.elbauldelprogramador.nlp.utils.Action.{Action, Left, Right, Shift}
import com.elbauldelprogramador.nlp.utils.Constants
import com.elbauldelprogramador.nlp.utils.DataTypes.Counter
import libsvm._

import scala.collection.mutable

/**
  * Created by Alejandro Alcalde <contacto@elbauldelprogramador.com> on 8/29/16.
  */
class SVMParser {

  val LeftCtx = 2
  val RightCtx = 4

  val Unknown = "UNKNOWN"

  // TODO: issue #10, try to make them vals
  // TODO: issue #11, Initialize with http://www.scala-lang.org/old/node/11944.html
  val positionVocab = mutable.Map.empty[Int, Counter]
  val positionTag = mutable.Map.empty[Int, Counter]
  val chLVocab = mutable.Map.empty[Int, Counter]
  val chLTag = mutable.Map.empty[Int, Counter]
  val chRVocab = mutable.Map.empty[Int, Counter]
  val chRTag = mutable.Map.empty[Int, Counter]
  val tagActions = mutable.Map[String, mutable.Map[Action, Int]]()
  var NFeatures = 0

  def train(sentences: Vector[LabeledSentence]) = {
    // TODO: Issue #12, Optimize this, may be tail rec?
    // FIXME: Need a deep refactor
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

    // TODO: Issue #13, Check if we have a previously trained model before try to train a new one

    val trainX = mutable.Map.empty[String, Vector[Vector[Int]]]
    val trainY = mutable.Map.empty[String, DblVector]

    //    val features = mutable.Map.empty[String, CRSMatrix]

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
          val actionCounter = tagActions getOrElseUpdate(posTag, mutable.Map(y -> 0)) getOrElseUpdate(y, 0)
          tagActions(posTag)(y) = actionCounter + 1

          // Fill features, if there is no feature stored for a tag, create empty vector and append feature
          trainX(posTag) = trainX.getOrElseUpdate(posTag, Vector.empty[Vector[Int]]) ++ Vector(extractedFeatures)
          trainY(posTag) = trainY.getOrElseUpdate(posTag, Vector.empty[Double]) :+ y.toDouble

          val (newI, newTrees) = takeAction(trees, i, y)
          i = newI
          trees = newTrees

          // Execute the action and modify the treese
          if (y != Shift)
            noConstruction = false
        }
      }
    }

    for (lp <- trainX.keys) {
      println(lp)
      println(s"Size of $lp: ${lp.length}")
      println(s"# features: $NFeatures")
      val classes = trainY.values.toSet.flatten

      // Train only if there are at least two classes
      if (classes.size > 1) {
        //        FileUtils.printToFile(new File(s"${Constants.ModelPath}$lp.p")) { p =>
        //          data.foreach(p.println)
        //        }
        // TODO: Check if the model already exists, if not, we need to train
        if (new File(s"${Constants.ModelPath}$lp.p").exists()) {
          println(s"Loaded ${Constants.ModelPath}$lp.p")
        } else {
        }
      }

      val svmProblem = new SVMProblem(trainY(lp).size, trainY(lp).toArray)

      // Create each row with its feature values Ex: (Only store the actual values, ignore zeros)
      //   x -> [ ] -> (2,0.1) (3,0.2) (-1,?)
      //        [ ] -> (2,0.1) (3,0.3) (4,-1.2) (-1,?)
      //        ......................................
      trainX(lp).zipWithIndex.foreach {
        case (x, i) =>
          val nodeCol = createNode(x)
        svmProblem.update(i, nodeCol)
      }
      // TODO #18: Check if there is error or not, and act in consequence
      val error = svm.svm_check_parameter(svmProblem.problem, SVMConfig.param)
      // TODO #19: Make SVMModel class to wrap this call
      val model = svm.svm_train(svmProblem.problem, SVMConfig.param)
      svm.svm_save_model(s"src/main/resources/models/svm.$lp.model", model)
    }
  }

  def countFeatures(feature: mutable.Map[Int, Counter]): Int = (feature map (_._2.size)).sum

  def extractTestFeatures(trees: Vector[Node], i: Int, leftCtx: Int, rightCtx: Int): Vector[Int] = {
    // Method to extract features for the given context window
    val range = (i - leftCtx to (i + 1 + rightCtx)).zipWithIndex
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

  // TODO #20: posTagFeature and lexFeature  very similar, look up a solution to reduce code
  def posTagFeature(position: Int, node: Node, offset: Int): Int = {
    val vocab = positionTag(position)

    vocab contains node.posTag match {
      case true => vocab(node.posTag) + offset
      case _ => vocab(Unknown) + offset
    }
  }

  def lexFeature(position: Int, node: Node, offset: Int): Int = {
    val vocab = positionVocab(position)

    vocab contains node.lex match {
      case true => vocab(node.lex) + offset
      case _ => vocab(Unknown) + offset
    }
  }

  // TODO: Issue #15 To recursive
  def childFeatures(position: Int, children: Vector[Node], offset: Int, family: Counter, featureType: Int):
  Vector[Int] = {
    var indices = Vector.empty[Int]
    for (child <- children) {
      indices = indices :+ childFeature(position, child, offset, family, featureType)
    }
    indices
  }

  def childFeature(position: Int, node: Node, offset: Int, family: Counter, featureType: Int): Int = {
    val vocab = family

    if (featureType == 0) /* EXTRACT_LEX */ {
      if (vocab contains node.lex)
        vocab(node.lex) + offset
      else {
        if (vocab contains node.posTag)
          vocab(node.posTag) + offset
      }
    }
    vocab(Unknown) + offset
  }

  def takeAction(trees: Vector[Node], index: Int, action: Action): (Int, Vector[Node]) = {
    val a = trees(index)
    val b = trees(index + 1)
    var i = index

    action match {
      case Right =>
        b.insertRight(a)
        // Update the tree and remove a
        val updatedTree = trees updated(index + 1, b) diff Vector(a)
        if (i == 0)
          i = 1
        (i - 1, updatedTree)
      case Left =>
        a.insertLeft(b)
        val updatedTree = trees updated(index, a) diff Vector(b)
        if (i == 0)
          i = 1
        (i - 1, updatedTree)
      case _ => //SHIFT
        (i + 1, trees)
    }
  }

  def estimateTrainAction(trees: Vector[Node], index: Int): Action = {
    val a = trees(index)
    val b = trees(index + 1)

    if (a.dependency == b.position && isCompleteSubtree(trees, a))
      Right
    else if (b.dependency == a.position && isCompleteSubtree(trees, b))
      Left
    else
      Shift
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
        tag += (targetNode.posTag -> (tag.getOrElseUpdate(targetNode.posTag, 0) + 1))

        positionVocab += (k -> word)
        positionTag += (k -> tag)

        // Check left and righ childs
        for (leftChild <- targetNode.left) {
          val word = chLVocab getOrElseUpdate(k, mutable.Map(leftChild.lex -> 0))
          val tag = chLTag getOrElseUpdate(k, mutable.Map(leftChild.posTag -> 0))

          word += (leftChild.lex -> (word.getOrElseUpdate(leftChild.lex, 0) + 1))
          tag += (leftChild.posTag -> (tag.getOrElseUpdate(leftChild.posTag, 0) + 1))

          chLVocab += (k -> word)
          chLTag += (k -> tag)
        }

        for (rightChild <- targetNode.right) {
          val word = chRVocab getOrElseUpdate(k, mutable.Map(rightChild.lex -> 0))
          val tag = chRTag getOrElseUpdate(k, mutable.Map(rightChild.posTag -> 0))

          word += (rightChild.lex -> (word.getOrElseUpdate(rightChild.lex, 0) + 1))
          tag += (rightChild.posTag -> (tag.getOrElseUpdate(rightChild.posTag, 0) + 1))

          chRVocab += (k -> word)
          chRTag += (k -> tag)
        }
      }
    }
  }

  def toFeatures(counter: mutable.Map[Int, Counter]): mutable.Map[Int, Counter] = {
    // Assign to each string key a counter, starting from 0 to the map size
    // TODO: Issue #16, Instead of update, generate new immutable map
    counter.foreach(a => a._2.zipWithIndex.foreach(r => a._2.update(r._1._1, r._2)))
    counter.foreach { case (i, c) => c(Unknown) = c.size }
    // When a counter does not have all context lenght, fill with Unkowns
    if (counter.size < 2 + LeftCtx + RightCtx) {
      for (index <- counter.size to (1 + LeftCtx + RightCtx)) {
        counter(index) = mutable.Map(Unknown -> 0)
      }
    }
    counter
  }

  def test(sentences: Vector[LabeledSentence]) = {
    val testSentences = for {
      s <- sentences
      testS = Sentence(s.words, s.tags)
    } yield testS

    for (s <- testSentences) {
      val trees = s.tree
      // TODO: Issue #17, Remove iterators like this, make them functional
      var i = 0
      var noConstruction = false
      while (trees.nonEmpty && !noConstruction) {
        if (i == trees.size - 1) {
          noConstruction = true
          i = 0
        } else {
          // extract features
          val extractedFeatures = extractTestFeatures(trees, i, LeftCtx, RightCtx)
          // estimate the action to be taken for i, i+ 1 target  nodes
          val y = estimateAction(trees, i, extractedFeatures)
          //
          //          // Update pos Action
          //          val actionCounter = tagActions getOrElseUpdate(posTag, mutable.Map(y -> 0)) getOrElseUpdate(y, 0)
          //          tagActions(posTag)(y) = actionCounter + 1
          //
          //          // Fill features, if there is no feature stored for a tag, create empty vector and append feature
          //          trainX(posTag) = trainX.getOrElseUpdate(posTag, Vector.empty[Vector[Int]]) ++ Vector(extractedFeatures)
          //          trainY(posTag) = trainY.getOrElseUpdate(posTag, Vector.empty[Double]) :+ y.toDouble
          //
          //          val (newI, newTrees) = takeAction(trees, i, y)
          //          i = newI
          //          trees = newTrees
          //
          //          // Execute the action and modify the trees
          //          if (y != Shift)
          //            noConstruction = false
        }
      }
    }
  }

  def estimateAction(trees: Vector[Node], position: Int, extractedFeatures: Vector[Int]): Int = {
    val treePosTag = trees(position).posTag
    // Params
    val svmParams = new svm_parameter
    svmParams.svm_type = svm_parameter.C_SVC
    svmParams.kernel_type = svm_parameter.POLY
    svmParams.degree = 2
    svmParams.gamma = 1
    svmParams.coef0 = 1
    svmParams.cache_size = 8000
    svmParams.eps = 0.1
    svmParams.C = 1

    val svmProblem = new svm_problem
    // feature fectors, will be in sparse form, size lxNFeatures (But sparse)
    //    svmProblem.x = new SVMNodes(svmProblem.l)
    //
    //    // Create each row with its feature values Ex: (Only store the actual values, ignore zeros)
    //    //   x -> [ ] -> (2,0.1) (3,0.2) (-1,?)
    //    //        [ ] -> (2,0.1) (3,0.3) (4,-1.2) (-1,?)
    //    //        [ ] -> (1,0.4) (-1,?)
    //    //        [ ] -> (2,0.1) (4,1.4) (5,0.5) (-1,?)
    //    //        [ ] -> (1,-0.1) (2,-0.2) (3,0.1) (4,1.1) (5,0.1) (-1,?)
    //    trainX(lp).zipWithIndex.foreach {
    //      case (x, i) =>
    //        val nodeCol = createNode(x)
    //        svmProblem.x(i) = nodeCol
    //    }
    //    val error = svm.svm_check_parameter(svmProblem, svmParams)
    //    val model = svm.svm_train(svmProblem, svmParams)
    //
    //    svm.svm_save_model(s"src/main/resources/models/svm.$lp.model", model)
    ////    val temFeatures =
    ???
  }
}
