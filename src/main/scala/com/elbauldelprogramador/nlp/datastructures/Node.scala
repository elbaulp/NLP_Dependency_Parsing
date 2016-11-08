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

  def matchDep(goldSentence: LabeledSentence, depAcc: Map[String, Int], depAccBase: Map[String, Int])
  : (Map[String, Int], Map[String, Int]) = {

    @tailrec
    def matchDep0(acc: Map[String, Int], acc2: Map[String, Int], n: Node)(queue: Seq[Node])
    : (Map[String, Int], Map[String, Int]) = {

      @inline def condition(node: Node): Boolean =
        node.dependency != -1 && !Constants.punctuationTags.contains(goldSentence.tags(node.position))
      @inline def condition2(node: Node): Boolean =
        goldSentence.dep(node.position) == node.dependency

      val w = goldSentence.words(n.position)
      val newAccs = if (condition(n) && condition2(n)) {
        (acc + (w -> (acc.getOrElse(w, 0) + 1)), acc2 + (w -> (acc2.getOrElse(w, 0) + 1)))
      } else if (condition(n)) {
        (acc, acc2 + (w -> (acc2.getOrElse(w, 0) + 1)))
      } else {
        (acc, acc2)
      }

      (queue: @switch) match {
        case head +: tail => matchDep0(newAccs._1, newAccs._2, head)(head.left ++ head.right ++ tail)
        case Nil => (newAccs._1, newAccs._2)
      }
    }

    matchDep0(depAcc, depAccBase, this)(left ++ right)
  }

  /**
    * Check if the tree is parsed correctly completely (Ignoring punctuation tags)
    * against the Gold sentence tags
    *
    * @param goldSentence The sentences corretly annotated
    * @return True if the tree is 100% correctly parsed
    */
  def matchAll(goldSentence: LabeledSentence): Boolean = matchNodes(goldSentence) == goldSentence.words.size

  def matchNodes(goldSentence: LabeledSentence): Int = {
    @inline def condition(n: Node): Boolean =
      (goldSentence.dep(n.position) == n.dependency) || Constants.punctuationTags.contains(goldSentence.tags(n.position))

    @tailrec
    def match0(acc: Int, n: Node)(queue: Seq[Node]): Int = {
      val count = if (condition(n)) acc + 1 else acc

      (queue: @switch) match {
        case head +: tail => match0(count, head)(head.left ++ head.right ++ tail)
        case Nil => count
      }
    }

    match0(0, this)(left ++ right)
  }

  override def toString: String = s"<LEX: $lex, TAG: $posTag, DEP: $dependency, POS: $position, LEFT: $left, RIGHT:  $right>"
}


object Node {
  def apply(lex: String, position: Int, posTag: String, dependency: Int): Node =
    new Node(lex, position, posTag, dependency, Vector.empty, Vector.empty)

  def apply(lex: String, position: Int, posTag: String): Node =
    new Node(lex, position, posTag, -1, Vector.empty, Vector.empty)
}