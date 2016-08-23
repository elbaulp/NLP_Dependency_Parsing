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

import com.elbauldelprogramador.nlp.utils.DataTypes.SentenceTokens

/**
  * Represents an unlabeled sentence
  *
  * Created by Alejandro Alcalde <contacto@elbauldelprogramador.com> on 8/19/16.
  */
class Sentence(/** Actual tokens in this sentence */
               val words: Vector[String],

               /** POS tags for words */
               val tags: Vector[String],

               /** Dependency order */
               val dep: Vector[Int]) {

  /** Constituent tree of this sentence; includes head words */
  private val tree: Vector[Node] = words.map(w => {
    val i = words.indexOf(w)
    new Node(w, i, tags.head, 0)
  })

  def this(sentence: SentenceTokens) = this(sentence._1, sentence._2, sentence._3)

  /**
    * Sentence's length
    *
    * @return
    */
  def size: Int = words.length
}