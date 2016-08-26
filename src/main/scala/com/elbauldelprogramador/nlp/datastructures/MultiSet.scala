/*
 *     MultiSet.scala is part of grado_informatica_tfg_naturallanguageprocessing (grado_informatica_TFG_NaturalLanguageProcessing).
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
  * Counts how many times an element appear in the Set.
  *
  * Similar to Counter() in python
  *
  * Created by Alejandro Alcalde <contacto@elbauldelprogramador.com> on 8/26/16.
  */
class MultiSet[T] {
  var counter: Map[T, Int] = Map.empty.withDefaultValue(0)

  def add(key: T): Unit = counter get key match {
    case Some(value) =>
      counter += (key -> (value + 1))
    case _ =>
      counter += (key -> 1)
  }



  //  private def count[T](s: Seq[T]) = s.groupBy(identity).mapValues(_.length)
  override def toString: String = counter.toString()
}

