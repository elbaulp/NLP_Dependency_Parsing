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

import com.elbauldelprogramador.nlp.datastructures.{LabeledSentence, Node, Sentence, Vocabulary}
import com.elbauldelprogramador.nlp.svm.SVMAdapter._
import com.elbauldelprogramador.nlp.svm.SVMConfig
import com.elbauldelprogramador.nlp.svm.SVMTypes._
import com.elbauldelprogramador.nlp.utils.Action.{Action, DoubleToAction, Left, Right, Shift}
import com.elbauldelprogramador.nlp.utils.Constants
import com.elbauldelprogramador.nlp.utils.DataTypes._
import com.softwaremill.quicklens._
import libsvm.{svm, svm_model}
import org.log4s._

import scala.annotation.{switch, tailrec}
import scala.collection.immutable.Map

/**
  * Created by Alejandro Alcalde <contacto@elbauldelprogramador.com> on 8/29/16.
  */
class DependencyParser(val trainSentences: Vector[LabeledSentence],
                       val testSentences: Vector[LabeledSentence]) {

  case class Accuracy(private[DependencyParser] val rootAcc: Map[String, Int] = Map.empty,
                      private[DependencyParser] val depNAcc: Map[String, Int] = Map.empty,
                      private[DependencyParser] val depDAcc: Map[String, Int] = Map.empty,
                      private[DependencyParser] val completeD: Int = 0,
                      private[DependencyParser] val completeN: Int = 0){

    def rootAccuracy: Double = rootAcc.values.sum / testSentences.size.toDouble
    def dependencyAccuracy: Double = depNAcc.values.sum / depDAcc.values.sum.toDouble
    def completeAccuracy: Double = completeN / completeD.toDouble
  }

  private[this] val logger = getLogger
  private[this] val sentences2 = trainSentences.map(l => LabeledSentence(l))

  /**
    * Train
    */
  // 1.1 - Build Vocabulary
  private[this] val vocabulary = generateVocabulary(trainSentences)
  // 1.2 - Extract Features
  private[this] val features = extractFeatures(sentences2)
  // 1.3 - Train models
  private[this] val models = train(features._1, features._2)

  /**
    * Test with unseen data
    */
  private[this] val inferredTree = test(testSentences)

  private[this] def generateVocabulary(sentences: Vector[LabeledSentence]): Vocabulary = {
    // 1.1 - Build vocab
    logger.info("Building vocabulary fro training...")

    @tailrec
    def train0(v: Vocabulary, s: Seq[LabeledSentence]): Vocabulary = (s: @switch) match {
      case head +: tail =>
        var trees = head.tree
        var i = 0
        var noConstruction = false
        var exit = false
        var updatedVocab = v

        while (trees.nonEmpty && !exit) {
          if (i == trees.size - 1) {
            if (noConstruction) exit = true
            noConstruction = true
            i = 0
          } else {
            // Build vocabulary
            updatedVocab = buildVocabulary(trees, updatedVocab, i, Config.LeftCtx, Config.RightCtx)

            val y = estimateTrainAction(trees, i)

            val (newI, newTrees) = takeAction(trees, i, y)

            i = newI
            trees = newTrees

            // Execute the action and modify the trees
            if (y != Shift)
              noConstruction = false
          }
        }
        train0(updatedVocab, tail)
      case Nil => v
    }

    def toFeatures(v: Vocabulary): Vocabulary =
    // Assign to each string key a counter, starting from 0 to the map size
      v.modifyAll(_.positionVocab, _.positionTag, _.chLTag, _.chLVocab, _.chRTag, _.chRVocab)
        .using(_.map(x => x._1 -> (x._2.zipWithIndex.map(y => y._1._1 -> y._2) + (Config.Unknown -> x._2.size))))

    // 1- build vocab
    val vocabulary = train0(Vocabulary(), sentences)
    toFeatures(vocabulary)
  }

  // 1.2 - Extract features
  private[this] def extractFeatures(sentences: Seq[LabeledSentence]): (Map[String, Vector[Vector[Int]]], Map[String, DblVector]) = {
    logger.info("Extracting features...")

    @tailrec
    def eF(X: Map[String, Vector[Vector[Int]]],
           Y: Map[String, DblVector],
           s: Seq[LabeledSentence]): (Map[String, Vector[Vector[Int]]], Map[String,
      DblVector]) = (s: @switch) match {
      case head +: tail =>
        var trees = head.tree
        var i = 0
        var noConstruction = false
        var exit = false
        var updatedX = X
        var updatedY = Y

        while (trees.nonEmpty && !exit) {
          if (i == trees.size - 1) {
            if (noConstruction) exit = true
            noConstruction = true
            i = 0
          } else {
            val posTag = trees(i).posTag

            // extract features
            val extractedFeatures = extractTestFeatures(trees, i, Config.LeftCtx, Config.RightCtx)
            val y = estimateTrainAction(trees, i)

            // Fill features, if there is no feature stored for a tag, create empty vector and append feature
            updatedX = updatedX + (posTag -> (updatedX(posTag) ++ Vector(extractedFeatures)))
            updatedY = updatedY + (posTag -> (updatedY(posTag) :+ y.toDouble))

            val (newI, newTrees) = takeAction(trees, i, y)
            i = newI
            trees = newTrees

            // Execute the action and modify the treese
            if (y != Shift)
              noConstruction = false
          }
        }
        eF(X ++ updatedX, Y ++ updatedY, tail)
      case Nil => (X, Y)
    }
    // TODO: Issue #13, Check if we have a previously trained model before try to train a new one, pickle models to
    // disk an read it back
    eF(Map.empty[String, Vector[Vector[Int]]].withDefaultValue(Vector.empty[Vector[Int]]),
      Map.empty[String, DblVector].withDefaultValue(Vector.empty[Double]),
      sentences)
  }

  // 1.3 - Train models
  def train(X: Map[String, Vector[Vector[Int]]], Y: Map[String, DblVector]): Map[String, svm_model] = {
    logger.info("Training models...")

    val nFeatures = vocabulary.nFeatures

    @tailrec
    def train0(XKey: Iterable[String], modelsAcc: Map[String, svm_model]): Map[String, svm_model] = {

      logger.info(s"\t\tPosTags left: $XKey")
      logger.info(s"\t\t# features: $nFeatures")

      (XKey.toSeq: @switch) match {
        case head +: tail =>
          (new File(s"${Constants.ModelPath}/svm.$head.model").exists(): @switch) match {
            case true =>
              logger.info(s"Loaded model: ${Constants.ModelPath}/svm.$head.model")
              // Load Models

              train0(tail, modelsAcc + (head -> svm.svm_load_model(s"${Constants.ModelPath}/svm.$head.model")))
            case false =>
              val svmProblem = new SVMProblem(Y(head).size, Y(head).toArray)
              // Create each row with its feature values Ex: (Only store the actual values, ignore zeros)
              //   x -> [ ] -> (2,0.1) (3,0.2) (-1,?)
              //        [ ] -> (2,0.1) (3,0.3) (4,-1.2) (-1,?)
              //        ......................................
              X(head).zipWithIndex.foreach {
                case (x, i) =>
                  val nodeCol = createNode(x)
                  svmProblem.update(i, nodeCol)
              }
              val error = svm.svm_check_parameter(svmProblem.problem, SVMConfig.param)
              require(error == null, f"${logger.error(s"Errors in SVM parameters:\n$error")}")

              // TODO #19: Make SVMModel class to wrap this call
              val m = modelsAcc + (head -> trainSVM(svmProblem, SVMConfig.param))
              svm.svm_save_model(s"${Constants.ModelPath}/svm.$head.model", m(head))

              train0(tail, m)
          }
        case Nil => modelsAcc
      }
    }
    train0(X.keys, Map.empty)
  }

  private[this] def test(sentences: Vector[LabeledSentence]): Vector[Vector[Node]] = {
    logger.info("Testing with unseen data...")

    val testSentences = for {
      s <- sentences
      testS = Sentence(s.words, s.tags)
    } yield testS

    @tailrec
    def test0(s: Seq[Sentence], inferredTree: Vector[Vector[Node]]): Vector[Vector[Node]] = (s: @switch) match {
      case head +: tail =>
        var i = 0
        var noConstruction = false
        var exit = false
        while (head.tree.nonEmpty && !exit) {
          if (i == head.tree.size - 1) {
            if (noConstruction) exit = true
            noConstruction = true
            i = 0
          } else {
            // extract features
            val extractedFeatures = extractTestFeatures(head.tree, i, Config.LeftCtx, Config.RightCtx)
            // estimate the action to be taken for i, i+ 1 target  nodes
            val y = estimateAction(head.tree, i, extractedFeatures)
            val (newI, newTrees) = takeAction(head.tree, i, y)
            i = newI
            head.tree = newTrees
            // Execute the action and modify the trees
            if (y != Shift)
              noConstruction = false
          }
        }
        test0(tail, inferredTree ++ Vector(head.tree))
      case Nil => inferredTree
    }
    test0(testSentences, Vector.empty)
  }

  def getAccuracy: Accuracy = {
    inferredTree.zipWithIndex./:(Accuracy()) {
      case (e, (v, i)) =>
        val goldS = testSentences(i)
        val current = v(0)
        (v.size: @switch) match {
          case 1 =>
            val updatedN = if (current.matchAll(goldS)) e.completeN + 1 else e.completeN
            val updatedRoot = if (!Constants.punctuationTags.contains(current.posTag)
              && goldS.dep(current.position) == -1)
              e.rootAcc + (current.lex -> (e.rootAcc.getOrElse(current.lex, 0) + 1))
            else e.rootAcc

            Accuracy(updatedRoot, e.depNAcc, e.depDAcc,
              e.completeD + 1, updatedN)
          case _ =>
            // Count how many nodes have correct parents ignoring punctiation
            val depAcc = v./:(e.depNAcc, e.depDAcc) {
              case ((depN, depD), n) =>
                val result = n.matchDep(goldS, depN, depD)
                (depN ++ result._1, depD ++ result._2)
            }
            // Update root accuracy counter
            Accuracy(e.rootAcc ++
              v.filter(i => !Constants.punctuationTags.contains(i.posTag) && goldS.dep(i.position) == -1)
                // Update the map, increment by 1 the lex counter
                ./:(e.rootAcc)((r, i) => r + (i.lex -> (r.getOrElse(i.lex, 0) + 1))),
              e.depNAcc ++ depAcc._1,
              e.depDAcc ++ depAcc._2,
              e.completeD,
              e.completeN)
        }
    }
  }

  private[this] def buildVocabulary(trees: Vector[Node], vocab: Vocabulary, i: Int, leftCtx: Int, rightCtx: Int):
  Vocabulary = {
    val range = (i - leftCtx to i + 1 + rightCtx).zipWithIndex.filter(r => r._1 >= 0 && r._1 < trees.size)

    def build0(rangeIterator: Seq[(Int, Int)], acc: Vocabulary): Vocabulary = (rangeIterator: @switch) match {
      case (w, k) +: tail =>
        val node = trees(w)
        val lexKey = trees(w).lex
        val tagKey = trees(w).posTag

        val pV = acc.positionVocab + (k -> (acc.positionVocab(k) ++ Map(lexKey -> (acc.positionVocab(k)(lexKey) + 1))))
        val pT = acc.positionTag + (k -> (acc.positionTag(k) ++ Map(tagKey -> (acc.positionTag(k)(tagKey) + 1))))

        val leftChild = node.left./:(acc.chLVocab, acc.chLTag) {
          case ((lvocab, ltag), n) =>
            val lV = lvocab +
              (k -> (lvocab(k) ++ Map(n.lex -> (lvocab(k)(n.lex) + 1))))
            val lT = ltag +
              (k -> (ltag(k) ++ Map(n.posTag -> (ltag(k)(n.posTag) + 1))))

            (lV, lT)
        }

        val rightChild = node.right./:(acc.chRVocab, acc.chRTag) {
          case ((rvocab, rtag), n) =>
            val rV = rvocab +
              (k -> (rvocab(k) ++ Map(n.lex -> (rvocab(k)(n.lex) + 1))))
            val rT = rtag +
              (k -> (rtag(k) ++ Map(n.posTag -> (rtag(k)(n.posTag) + 1))))

            (rV, rT)
        }

        val updated = Vocabulary(pV,
          pT,
          leftChild._1,
          leftChild._2,
          rightChild._1,
          rightChild._2)

        build0(tail, updated)
      case Nil => acc
    }
    build0(range, vocab)
  }

  private[this] def takeAction(trees: Vector[Node], index: Int, action: Action): (Int, Vector[Node]) = {
    val a = trees(index)
    val b = trees(index + 1)
    var i = index

    (action: @switch) match {
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

  private[this] def estimateTrainAction(trees: Vector[Node], index: Int): Action = {
    val a = trees(index)
    val b = trees(index + 1)

    if (a.dependency == b.position && isCompleteSubtree(trees, a))
      Right
    else if (b.dependency == a.position && isCompleteSubtree(trees, b))
      Left
    else
      Shift
  }

  private[this] def isCompleteSubtree(trees: Vector[Node], child: Node): Boolean =
    !trees.exists(_.dependency == child.position)

  private[this] def extractTestFeatures(trees: Vector[Node], i: Int, leftCtx: Int, rightCtx: Int): Vector[Int] = {
    // Method to extract features for the given context window
    val range = (i - leftCtx to i + 1 + rightCtx).zipWithIndex.filter(r => r._1 >= 0 && r._1 < trees.size)

    @tailrec
    def testFeat(rangeIterator: Seq[(Int, Int)], acc: Vector[Int], offset: Int): Vector[Int] =
      (rangeIterator: @switch) match {
        case (w, k) +: tail =>
          val targetNode = trees(w)

          val lexTemp = lexFeature(k, targetNode, offset) +: Vector.empty
          var tempOff = offset + vocabulary.positionVocab(k).size
          val tagTemp = posTagFeature(k, targetNode, tempOff) +: Vector.empty
          tempOff += vocabulary.positionTag(k).size
          val chLLexTemp = childFeatures(k, targetNode.left, tempOff, vocabulary.chLVocab getOrElse(k, Map(Config.Unknown -> 0)), 0)
          tempOff += vocabulary.chLVocab(k).size
          val chLTagTemp = childFeatures(k, targetNode.left, tempOff, vocabulary.chLTag getOrElse(k, Map(Config.Unknown -> 0)), 1)
          tempOff += vocabulary.chLTag(k).size
          val chRLexTemp = childFeatures(k, targetNode.right, tempOff, vocabulary.chRVocab getOrElse(k, Map(Config.Unknown -> 0)), 0)
          tempOff += vocabulary.chRVocab(k).size
          val chRTagTemp = childFeatures(k, targetNode.right, tempOff, vocabulary.chRTag getOrElse(k, Map(Config.Unknown -> 0)), 1)
          tempOff += vocabulary.chRTag(k).size

          testFeat(tail,
            acc ++ lexTemp ++ tagTemp ++ chLLexTemp ++ chLTagTemp ++ chRLexTemp ++ chRTagTemp,
            tempOff)
        case Nil => acc
      }
    testFeat(range, Vector.empty[Int], 0)
  }

  // TODO #20: posTagFeature and lexFeature  very similar, look up a solution to reduce code
  private[this] def posTagFeature(position: Int, node: Node, offset: Int): Int = {
    val vocab = vocabulary.positionTag(position)

    (vocab contains node.posTag: @switch) match {
      case true => vocab(node.posTag) + offset
      case _ => vocab(Config.Unknown) + offset
    }
  }

  private[this] def lexFeature(position: Int, node: Node, offset: Int): Int = {
    val vocab = vocabulary.positionVocab(position)

    (vocab contains node.lex: @switch) match {
      case true => vocab(node.lex) + offset
      case _ => vocab(Config.Unknown) + offset
    }
  }

  private[this] def childFeatures(position: Int, children: Vector[Node], offset: Int,
                                  family: Counter, featureType: Int): Vector[Int] =
    children./:(Vector.empty[Int])((v, n) => v :+ childFeature(position, n, offset, family, featureType))

  private[this] def childFeature(position: Int, node: Node, offset: Int, family: Counter, featureType: Int): Int = {
    val vocab = family

    if (featureType == 0) /* EXTRACT_LEX */ {
      if (vocab contains node.lex)
        vocab(node.lex) + offset
      else {
        if (vocab contains node.posTag)
          vocab(node.posTag) + offset
      }
    }
    vocab(Config.Unknown) + offset
  }

  private[this] def estimateAction(trees: Vector[Node], position: Int, extractedFeatures: Vector[Int]): Action = {
    val treePosTag = trees(position).posTag

    val action = if (models contains treePosTag) {
      val model = models(treePosTag)
      predictSVM(model, extractedFeatures).toAction
    }
    else
      Shift

    action
  }

  private[this] object Config {
    val LeftCtx = 2
    val RightCtx = 4
    val Unknown = "UNKNOWN"
  }

}

