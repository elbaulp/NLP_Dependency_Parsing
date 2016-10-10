///*
// *     SVMModel.scala is part of grado_informatica_tfg_naturallanguageprocessing (grado_informatica_TFG_NaturalLanguageProcessing).
// *
// *     grado_informatica_TFG_NaturalLanguageProcessing is free software: you can redistribute it and/or modify
// *     it under the terms of the GNU General Public License as published by
// *     the Free Software Foundation, either version 3 of the License, or
// *     (at your option) any later version.
// *
// *     grado_informatica_TFG_NaturalLanguageProcessing is distributed in the hope that it will be useful,
// *     but WITHOUT ANY WARRANTY; without even the implied warranty of
// *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// *     GNU General Public License for more details.
// *
// *     You should have received a copy of the GNU General Public License
// *     along with grado_informatica_TFG_NaturalLanguageProcessing.  If not, see <http://www.gnu.org/licenses/>.
// */
//
//package com.elbauldelprogramador.nlp.svm
//
//import com.elbauldelprogramador.nlp.svm.SVMTypes.DblArray
//import libsvm.svm_model
//import org.scalaml.core.Design.Model
//import org.scalaml.core.Types.ScalaMl._
//import org.scalaml.core.Types._
//import org.scalaml.util.FormatUtils.{SHORT, _}
//
///**
//  * Defined a model for support vector machine. The model is composed of the svm_model
//  * parameters of '''LIBSVM''' library and the accuracy computed during training.
//  *
//  * This implementation uses the LIBSVM library: ''http://www.csie.ntu.edu.tw/~cjlin/libsvm/''
//  *
//  * @constructor Create a SVMModel with a given LIBSVM model and accuracy
//  * @see LIBSVM
//  * @see org.scalaml.core.Design.Model
//  * @throws IllegalArgumentException if LIBSVM model is undefined.
//  * @param svmmodel Model parameters as defined in '''LIBSVM'''
//  * @param accuracy	Accuracy of the model from the training process
//  * @author Patrick Nicolas
//  * @since April 19, 2014
//  * @note Scala for Machine Learning  Chapter 8 Kernel models and support vector machines
//  */
//final class SVMModel(val svmmodel: svm_model, val accuracy: Double) extends Model {
//  require(svmmodel != null, "SVMModel LIBSVM model smmodel is undefined")
//
//  lazy val residuals: DblArray = svmmodel.sv_coef(0)
//
//  /**
//    * Textual representation of the SVM model. The method converts LIBSVM nodes values
//    * and the SVM weights (or coefficients) into characters string
//    *
//    * @return Description of the model for debugging purpose
//    */
//  override def toString: String = {
//    val description = new StringBuilder("SVM model\n")
//
//    // Stringize LIBSVM nodes
//    val nodes = svmmodel.SV
//    if( nodes.nonEmpty) {
//      description.append("SVM nodes:\n")
//      val _nodes: DblMatrix = Array.tabulate(nodes.size)(n => {
//        val row = nodes(n)
//        row.map(r => r.value)
//      })
//      description.append(format(_nodes, SHORT))
//    }
//    // Stringize the LIBSVM coefficients
//    val coefs = svmmodel.sv_coef
//    if( coefs.nonEmpty ) {
//      description.append("\nSVM basis functions:\n")
//      coefs.foreach(w => {
//        w.foreach(c => description.append(s"${format(c, emptyString, SHORT)}\n"))
//        description.append("\n")
//      })
//    }
//    // Aggregate the description
//    description.append(s"Accuracy: ${format(accuracy, emptyString, SHORT)}\n")
//      .toString()
//  }
//}