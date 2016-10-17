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
           var dependency: Int,
           var left: Vector[Node],
           var right: Vector[Node]) {

  // TODO: #24 Remove secondary constructors, override apply in companion object
  def this(lex: String, position: Int, posTag: String, dependency: Int) =
    this(lex, position, posTag, dependency, Vector.empty, Vector.empty)

  def this(lex: String, position: Int, posTag: String) =
    this(lex, position, posTag, 0, Vector.empty, Vector.empty)

  def insertRight(child: Node): Unit = {
    child.dependency = position
    right = right :+ child
  }

  def insertLeft(child: Node): Unit = {
    child.dependency = position
    left = left :+ child
  }

  def matchNodes(goldSentence: LabeledSentence): Int = {

    def condition(n:Node): Boolean =
      (goldSentence.dep(n.position) == n.dependency) || Constants.punctuationTags.contains(n.posTag)

    def match0(acc:Int, n:Seq[Node]):Int = {
      n match {
        case head :: tail => {
          if (condition(head)){
            match0(acc + 1, tail)
          } else {
            acc
          }
        }
        case node :: Nil => if (condition(node)) acc + 1 else acc
        case _ =>  acc
      }
    }

    val counter = match0(0, left) + match0(0, right)

    counter
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
