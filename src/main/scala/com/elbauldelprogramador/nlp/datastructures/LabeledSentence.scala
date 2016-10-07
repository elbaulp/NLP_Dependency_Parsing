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

trait TestSentence {
  /** Actual tokens in this sentence */
  val words: Vector[String]
  /** POS tags for words */
  val tags: Vector[String]


  /** Constituent tree of this sentence; includes head words */
  // TODO: Would it be better to have a Structure like (root (leaf (leaf)) (leaf)...)
  val tree: Vector[Node] = {
    // In order to be able to access the index, zip with index
    val indexedWords = words.zipWithIndex

    // Iterate thought all three collections and create Nodes
    (indexedWords, tags).zipped.map {
      (w, t) => new Node(w._1, w._2, t)
    }
  }

  /**
    * Sentence's length
    *
    * @return
    */
  def size: Int = words.length
}

trait TrainSentence {
  /** Dependency order */
  val dep: Vector[Int]
}

/**
  * Represents a labeled sentence (for training)
  *
  * Created by Alejandro Alcalde <contacto@elbauldelprogramador.com> on 8/19/16.
  */
final case class LabeledSentence(words: Vector[String],
                                 tags: Vector[String],
                                 dep: Vector[Int]) extends TestSentence with TrainSentence {
  /** Constituent tree of this sentence; includes head words */
  // TODO: Would it be better to have a Structure like (root (leaf (leaf)) (leaf)...)
  override val tree: Vector[Node] = {
    // In order to be able to access the index, zip with index
    val indexedWords = words.zipWithIndex

    // Iterate thought all three collections and create Nodes
    (indexedWords, tags, dep).zipped.map {
      (w, t, d) => new Node(w._1, w._2, t, d)
    }
  }
}

//  def this(sentence: SentenceTokens) = this(sentence._1, sentence._2, sentence._3)


final case class Sentence(words: Vector[String],
                          tags: Vector[String]) extends TestSentence