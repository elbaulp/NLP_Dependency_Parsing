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
//import com.elbauldelprogramador.nlp.svm.SVMTypes.{DblArray, DblMatrix}
//import com.elbauldelprogramador.nlp.utils.FileUtils
//import libsvm.svm_model
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
//
///**
//  * Define the model trait for classification and optimization algorithms.
//  * @author Patrick Nicolas
//  * @since 0.98.2 March 4, 2014 (0.98.2)
//  * @see Scala for Machine Learning Chapter 2 Hello World!
//  */
//trait Model {
//  /**
//    * Write the model parameters associated to this object into a file
//    * @param content to write into a file
//    * @return true if the write operation is successful, false otherwise
//    */
//  protected def write(content: String): Boolean  =
//    FileUtils.write(content, Model.RELATIVE_PATH, getClass.getSimpleName)
//
//  /**
//    * This operation or method has to be overwritten for a model to be saved into a file
//    * @return It returns true if the model has been properly saved, false otherwise
//    */
//  def >> : Boolean = false
//}
//
///**
//  * Companion singleton to the Model trait. It is used to define the simple read
//  * method to load the model parameters from file.
//  * @author Patrick Nicolas
//  * @since 0.98 March 4, 2014 0.98.2
//  * @see Scala for Machine Learning Chapter 2 Hello World!
//  */
//object Model {
//  private val RELATIVE_PATH = "models/"
//  /**
//    * Read this model parameters from a file defined as '''models/className'''
//    * @param className  file containing the model parameters
//    * @return Model parameters as a comma delimited string if successful, None otherwise
//    */
//  def read(className: String): Option[String] = FileUtils.read(RELATIVE_PATH, className)
//}