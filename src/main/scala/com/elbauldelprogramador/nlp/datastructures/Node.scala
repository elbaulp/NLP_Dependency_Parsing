/*
 *     Node.scala is part of grado_informatica_tfg_naturallanguageprocessing (grado_informatica_TFG_NaturalLanguageProcessing).
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

package com.elbauldelprogramador.nlp.datastructures

import com.elbauldelprogramador.nlp.utils.Constants

import scala.annotation.{switch, tailrec}

/**
  * Node of a tree
  * Can contain any number of children
  * Left and Right represents the children created due to left and right dependencies, respectively
  *
  * Created by Alejandro Alcalde <contacto@elbauldelprogramador.com> on 8/19/16.
  */
case class Node(lex: String,
                position: Int,
                posTag: String, // TODO  create some data structure for this)
                var dependency: Int = -1,
                var left: Vector[Node],
                var right: Vector[Node]) {

  def insertRight(child: Node): Unit = {
    child.dependency = position
    right = right :+ child
  }

  def insertLeft(child: Node): Unit = {
    child.dependency = position
    left = left :+ child
  }

  def matchNodes(goldSentence: LabeledSentence): Int = {
    @inline def condition(n: Node): Boolean =
      (goldSentence.dep(n.position) == n.dependency) || Constants.punctuationTags.contains(goldSentence.tags(n.position))

    @tailrec
    def match0(acc: Int, n: Node)(queue: Seq[Node]): Int = {
      val count = if (condition(n)) acc + 1 else acc

      (queue: @switch) match {
        case head +: tail =>
          match0(count, head)(head.left ++ head.right ++ tail)
        case Nil =>
          count
      }
    }

    match0(0, this)(left ++ right)
  }

//// TODO: Fix this method
//  def matchDep(goldSentence: LabeledSentence, depAcc: Map[String, Int], depAccBase: Map[String, Int])
//  : (Map[String, Int], Map[String, Int]) = {
//
//    @tailrec
//    def matchDep0(acc: Map[String, Int], acc2: Map[String, Int], n: Vector[Node])
//    : (Map[String, Int], Map[String, Int]) = {
//
//      @inline def condition(node:Node): Boolean =
//        node.dependency != -1 && !Constants.punctuationTags.contains(goldSentence.tags(node.position))
//      @inline def condition2(node:Node): Boolean =
//        goldSentence.dep(node.position) == node.dependency
//
//      (n: @switch) match {
//        case head +: tail =>
//          val w = goldSentence.words(head.position)
//          if (condition(head) && condition2(head)) {
//            // TODO: Optimize ifthenelse
//            matchDep0(acc + (w -> (acc.getOrElse(w, 0) + 1)),
//              acc2 + (w -> (acc2.getOrElse(w, 0) + 1)),
//              tail)
//          } else if (condition(head)) {
//            matchDep0(acc, acc2 + (w -> (acc2.getOrElse(w, 0) + 1)), tail)
//          } else {
//            (acc, acc2)
//          }
//        case _ => (acc, acc2)
//      }
//    }
//
//    val leftAcc = matchDep0(depAcc, depAccBase, left)
//    val rightAcc = matchDep0(depAcc, depAccBase, right)
//
//    (leftAcc._1 ++ rightAcc._1, leftAcc._2 ++ rightAcc._2)
//  }

//  // TODO: Repace this non-tail rec by the above
  def matchDep(goldSentence: LabeledSentence, depAcc: scala.collection.mutable.Map[String, Int], depAccBase: scala.collection.mutable.Map[String, Int])
  : Int = {
    var correctRoots = 0
    val position: Int = this.position
    val dep: Int = dependency
    val word = goldSentence.words(position)
    val tag = goldSentence.tags(position)

    if (dep != -1 && !Constants.punctuationTags.contains(tag)) {
      depAccBase(word) = depAccBase.getOrElseUpdate(word, 0) + 1
      if (goldSentence.dep(position) == dep) {
        correctRoots += 1
        depAcc(goldSentence.words(position)) = depAcc.getOrElseUpdate(goldSentence.words(position), 0) + 1
      }
    }

    if (right.nonEmpty)
      for (r <- right)
        correctRoots += r.matchDep(goldSentence, depAcc, depAccBase)

    if (left.nonEmpty)
      for (l <- left)
        correctRoots += l.matchDep(goldSentence, depAcc, depAccBase)

    correctRoots
  }

  /**
    * Check if the tree is parsed correctly completely (Ignoring punctuation tags)
    * against the Gold sentence tags
    *
    * @param goldSentence The sentences corretly annotated
    * @return True if the tree is 100% correctly parsed
    */
  def matchAll(goldSentence: LabeledSentence): Boolean = matchNodes(goldSentence) == goldSentence.words.size


  override def toString: String = s"<LEX: $lex, TAG: $posTag, DEP: $dependency, POS: $position, LEFT: $left, RIGHT:  $right>"
}


object Node {
  def apply(lex: String, position: Int, posTag: String, dependency: Int): Node =
    new Node(lex, position, posTag, dependency, Vector.empty, Vector.empty)

  def apply(lex: String, position: Int, posTag: String):Node =
    new Node(lex, position, posTag, -1, Vector.empty, Vector.empty)
}