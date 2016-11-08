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

import com.elbauldelprogramador.nlp.core.functional.Monoid

trait TestSentence {
  /** Actual tokens in this sentence */
  val words: Vector[String]
  /** POS tags for words */
  val tags: Vector[String]

  /** Constituent tree of this sentence; includes head words */
  // TODO: Would it be better to have a Structure like (root (leaf (leaf)) (leaf)...)
  var tree: Vector[Node]/* = {
    // In order to be able to access the index, zip with index
    val indexedWords = words.zipWithIndex

    // Iterate thought all three collections and create Nodes
    (indexedWords, tags).zipped.map {
      (w, t) => Node(w._1, w._2, t)
    }
  }*/

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
final case class LabeledSentence(t: Tokens) extends TestSentence
  with TrainSentence {

  /** Constituent tree of this sentence; includes head words */
  override val words: Vector[String] = t.lex
  override val tags: Vector[String] = t.pos
  override val dep: Vector[Int] = t.dep
  // TODO: Would it be better to have a Structure like (root (leaf (leaf)) (leaf)...)
  // TODO: Make immutable using QuickLenses
  override var tree: Vector[Node] = {
    // In order to be able to access the index, zip with index
    val indexedWords = words.zipWithIndex

    // Iterate thought all three collections and create Nodes
    (indexedWords, tags, dep).zipped.map {
      (w, t, d) => Node(w._1, w._2, t, d)
    }
  }
}

object LabeledSentence {
  def apply(l: LabeledSentence): LabeledSentence = new LabeledSentence(new Tokens(l.words, l.tags, Vector.empty, l.dep))
}

final case class Sentence(words: Vector[String],
                          tags: Vector[String]) extends TestSentence {
  override var tree: Vector[Node] = {
    // In order to be able to access the index, zip with index
    val indexedWords = words.zipWithIndex

    // Iterate thought all three collections and create Nodes
    (indexedWords, tags).zipped.map {
      (w, t) => Node(w._1, w._2, t)
    }
  }
}

final case class Tokens(lex: Vector[String],
                        pos: Vector[String],
                        gold: Vector[String],
                        dep: Vector[Int]) extends Monoid[Tokens] {

  override val identity: Tokens = this

  def isEmpty: Boolean = lex.isEmpty &&
    pos.isEmpty &&
    gold.isEmpty &&
    dep.isEmpty

  override def append(a: Tokens, b: Tokens): Tokens = a + b

  def +(b: Tokens): Tokens =
    Tokens(lex ++ b.lex,
      pos ++ b.pos,
      gold ++ b.gold,
      dep ++ b.dep)
}

object Tokens {
  def apply(a: String, b: String, c: String, d: Int): Tokens = new Tokens(Vector(a), Vector(b), Vector(c), Vector(d))

  def apply(): Tokens = new Tokens(Vector.empty, Vector.empty, Vector.empty, Vector.empty)
}