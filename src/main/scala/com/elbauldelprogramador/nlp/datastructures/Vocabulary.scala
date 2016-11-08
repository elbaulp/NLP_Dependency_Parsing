/*
 * Copyright 2016 Alejandro Alcalde
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.elbauldelprogramador.nlp.datastructures

import com.elbauldelprogramador.nlp.utils.DataTypes._

import scala.collection.immutable.Map

/**
  * Created by Alejandro Alcalde <contacto@elbauldelprogramador.com> on 11/8/16.
  */
final case class Vocabulary(positionVocab: Map[Int, Counter] = Map.empty.withDefaultValue(Map.empty.withDefaultValue(0)),
                            positionTag: Map[Int, Counter] = Map.empty.withDefaultValue(Map.empty.withDefaultValue(0)),
                            chLVocab: Map[Int, Counter] = Map.empty.withDefaultValue(Map.empty.withDefaultValue(0)),
                            chLTag: Map[Int, Counter] = Map.empty.withDefaultValue(Map.empty.withDefaultValue(0)),
                            chRVocab: Map[Int, Counter] = Map.empty.withDefaultValue(Map.empty.withDefaultValue(0)),
                            chRTag: Map[Int, Counter] = Map.empty.withDefaultValue(Map.empty.withDefaultValue(0))){

  def nFeatures:Int = positionVocab.values.map(_.size).sum +
    positionTag.values.map(_.size).sum +
    chLVocab.values.map(_.size).sum +
    chLTag.values.map(_.size).sum +
    chRVocab.values.map(_.size).sum +
    chRTag.values.map(_.size).sum
}
