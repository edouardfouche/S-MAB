/*
 * Copyright (C) 2018 Edouard Fouché
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.edouardfouche.monitoring.resetstrategies

import abifet.ADWIN // We use the original implementation from Bifet

/**
  * This a "shared" ADWIN instance between k arms.
  * In fact, it holds k instances of ADWIN, one for each arm.
  * However, the actual window of the size of the smallest window
  *
  * @param k A number of arms
  * @param delta the parameter for ADWIN (upper bound for the false positive rate)
  */
class SharedAdwin(k: Int, val delta: Double = 0.1) {
  val adwins: scala.collection.immutable.Map[Int, ADWIN] = (0 until k).map(x =>
    x -> new ADWIN(delta)).toMap

  def addElement(x: Int, element: Double) = adwins(x).setInput(element)

  def getSize: Int = (0 until k).map(x => adwins(x).getWidth).min

  def getSingleSize(index: Int): Int = adwins(index).getWidth

  def getIndexAndSize: (Int,Int) = (0 until k).map(x => (x,adwins(x).getWidth)).minBy(_._2)

  def getIndexAndSizeFromArms(arms: Array[Int]): (Int,Int) = arms.map(x => (x,adwins(x).getWidth)).minBy(_._2)

  def getDetect: Boolean = (0 until k).exists(x => adwins(x).getDetect)
}
