/*
 *     Actions.scala is part of grado_informatica_tfg_naturallanguageprocessing (grado_informatica_TFG_NaturalLanguageProcessing).
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

package com.elbauldelprogramador.nlp.utils

/**
  * Encoding possible actions to take
  *
  * Created by Alejandro Alcalde <contacto@elbauldelprogramador.com> on 10/7/16.
  */
object Action {

  sealed trait Action

  /**
    * Implicit conversion to Double from Action, used when storing the Action Label for SVM
    *
    * @param action
    */
  implicit class ActionToDouble(action: Action) {
    def toDouble: Double = action match {
      case Left => Left.value.toDouble
      case Right => Right.value.toDouble
      case Shift => Shift.value.toDouble
    }
  }

  // TODO #21: See if it can be converted to implicit val instead of implicit class
  implicit class DoubleToAction(double: Double) {
    def toAction: Action = double match {
      case 0.0 => Left
      case 1.0 => Shift
      case 2.0 => Right
    }
  }

  case object Left extends Action {final def value: Int = 0}
  case object Shift extends Action {final def value: Int = 1}
  case object Right extends Action {final def value: Int = 2}
}