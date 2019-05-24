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
package com.edouardfouche.monitoring.scalingstrategies

/**
  * General trait for scaling strategies
  * A scaling strategy is basically just a function which determine the number of pulls for the next round, based on various information
  */
trait ScalingStrategy {
  val name: String
  val delta: Double
  val gamma: Double
  var confidence: Double
  var k: Int
  val lmin: Int
  val lmax: Int
  def scale(rewards: Array[Double], indexes: Array[Int], sums: Array[Double], counts: Array[Double], t:Double): Int
}
