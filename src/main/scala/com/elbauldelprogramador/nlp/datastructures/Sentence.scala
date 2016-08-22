/*
 *     Sentence.scala is part of grado_informatica_tfg_naturallanguageprocessing (grado_informatica_TFG_NaturalLanguageProcessing).
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
  * Represents an unlabeled sentence
  *
  * Created by Alejandro Alcalde <contacto@elbauldelprogramador.com> on 8/19/16.
  */
class Sentence(/** Actual tokens in this sentence */
               val words: Array[String],

               /** POS tags for words */
               val tags: Option[Array[String]]) {

  /** Constituent tree of this sentence; includes head words */
  private val tree: Vector[Node] = words.map(w => {
    val i = words.indexOf(w)
    new Node(w, i, tags.get(i), 0)
  }).toVector

  /**
    * Sentence's length
    *
    * @return
    */
  def size: Int = words.length
}
