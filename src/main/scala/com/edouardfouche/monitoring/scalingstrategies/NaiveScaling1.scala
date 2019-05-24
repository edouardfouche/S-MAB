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
  * Naive Scaling.
  * Decrement L_t by a proportion gamma if the observed efficiency is lower than delta, otherwise increment it
  *
  * @param lmin the minimum number of pulls (in fact, typically 1)
  * @param lmax the maximum number of pulls (in fact, typically the number of arms)
  * @param gamma "dynamic" of the scaling. Proportion used to increment and decrement
  * @param delta efficiency threshold
  */
case class NaiveScaling1(lmin: Int, lmax: Int, gamma: Double, delta: Double) extends ScalingStrategy {
  val name=s"Naive1-$lmin-$lmax-$gamma-$delta"
  var confidence=1.0
  var k = lmax

  def scale(rewards: Array[Double], indexes: Array[Int], sums: Array[Double], counts: Array[Double], t:Double): Int = {
    require(rewards.length == indexes.length)
    require(sums.length == counts.length)
    val selected_weights = indexes.map(x => sums(x)/counts(x))
    val Lt = selected_weights.length.toDouble
    if(rewards.sum / Lt < delta) {
      k = lmin.max(math.floor((1.0-gamma)*Lt).toInt)
      k
    } // Scale down
    else {
      k = lmax.min(math.ceil((1.0+gamma)*Lt).toInt)
      k
    } // Scale up
  }
}
