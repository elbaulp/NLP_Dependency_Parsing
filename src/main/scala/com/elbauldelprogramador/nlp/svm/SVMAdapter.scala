/*
 *     SVMAdapter.scala is part of grado_informatica_tfg_naturallanguageprocessing (grado_informatica_TFG_NaturalLanguageProcessing).
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

package com.elbauldelprogramador.nlp.svm

import com.elbauldelprogramador.nlp.svm.SVMTypes.DblArray
import libsvm._

/**
  * Partially borrowed from https://github.com/prnicolas/ScalaMl/blob/master/src/main/scala/org/scalaml/libraries/libsvm/SVMAdapter.scala
  *
  */
object SVMAdapter {

  type SVMNodes = Array[Array[svm_node]]

  def createNode(features: Vector[Int]): Array[svm_node] = {
    // Create a new row for SVM, with the format x -> [ ] -> (2,0.1) (3,0.2) (-1,?)
    // Where each tuple correspont with the feature number and its value,
    val newNode = new Array[svm_node](features.size)
    features.zipWithIndex.foreach { case (f, i) =>
      val node = new svm_node
      node.index = f
      node.value = 1.0
      newNode(i) = node
    }
    newNode
  }

  def trainSVM(problem: SVMProblem, param: svm_parameter): svm_model =
    svm.svm_train(problem.problem, param)

  class SVMProblem(numObs: Int, labels: DblArray) {
    val problem = new svm_problem
    // Size of the problem (How many rows of data we have
    problem.l = numObs
    // Values of each class for the rows of data
    problem.y = labels
    // feature fectors, will be in sparse form, size lxNFeatures (But sparse)
    problem.x = new SVMNodes(numObs)

    def update(n: Int, node: Array[svm_node]): Unit = {
      problem.x(n) = node
    }
  }
}
