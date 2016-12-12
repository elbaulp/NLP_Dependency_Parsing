/*
 *     FileUtils.scala is part of grado_informatica_tfg_naturallanguageprocessing (grado_informatica_TFG_NaturalLanguageProcessing).
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

import java.io.{FileOutputStream, ObjectInputStream, ObjectOutputStream}

/**
  * Created by Alejandro Alcalde <contacto@elbauldelprogramador.com> on 9/28/16.
  */
object FileUtils {
  def printToFile(f: java.io.File)(op: java.io.PrintWriter => Unit): Unit = {
    val p = new java.io.PrintWriter(f)
    try {
      op(p)
    } finally {
      p.close()
    }
  }

  def saveOject(o: Any): Unit = {
    val oos = new ObjectOutputStream(new FileOutputStream("src/main/resources/XY"))
    try {
      oos.writeObject(o)
    } finally {
      oos.close()
    }
  }

  def getObject[T]:T = {
    val ois = new ObjectInputStream(getClass.getResource("/XY").openStream())
    try {
      val r = ois.readObject.asInstanceOf[T]
      r
    } finally {
      ois.close()
    }
  }
}
