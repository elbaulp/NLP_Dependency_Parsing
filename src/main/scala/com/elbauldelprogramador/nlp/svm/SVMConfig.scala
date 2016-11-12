/*
 *     SVMParams.scala is part of grado_informatica_tfg_naturallanguageprocessing (grado_informatica_TFG_NaturalLanguageProcessing).
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

import libsvm.svm_parameter

/**
  *
  * Parameters for SVM
  *
  * Created by Alejandro Alcalde <contacto@elbauldelprogramador.com> on 10/10/16.
  */
object SVMConfig {
  val param = new svm_parameter

  param.svm_type = svm_parameter.C_SVC
  param.kernel_type = svm_parameter.POLY
  param.degree = 2
  param.gamma = 1.0
  param.coef0 = 1.0
  param.cache_size = 4000
  param.eps = 0.001
  param.C = 1.0
  param.shrinking = 1
}
