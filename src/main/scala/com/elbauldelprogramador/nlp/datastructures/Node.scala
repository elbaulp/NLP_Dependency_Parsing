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

/**
  * Node of a tree
  * Can contain any number of children
  * Left and Right represents the children created due to left and right dependencies, respectively
  *
  * Created by Alejandro Alcalde <contacto@elbauldelprogramador.com> on 8/19/16.
  */
class Node(val lex: String,
           val position: Int,
           val posTag: String, // TODO  create some data structure for this)
           var dependency: Int,
           var left: Vector[Node],
           var right: Vector[Node]) {

  def this(lex: String, position: Int, posTag: String, dependency: Int) =
    this(lex, position, posTag, dependency, Vector.empty, Vector.empty)

  def insertRight(child: Node): Unit = {
    child.dependency = position
    right = right :+ child
  }

  def insertLeft(child: Node): Unit = {
    child.dependency = position
    left = left :+ child
  }



//  /**
//    * Check if the tree is parsed correctly completely (Ignoring punctiation tags) against the Gold sentence tags
//    *
//    * @param goldSentence The sentences corretly annotated
//    * @return True if the tree is 100% correctly parsed
//    */
  //  def matchAll(goldSentence: String): Boolean = {
  //
  //  }
  override def toString: String = s"<LEX: $lex, TAG: $posTag, DEP: $dependency, POS: $position, LEFT: $left, RIGHT:  $right>"
}
