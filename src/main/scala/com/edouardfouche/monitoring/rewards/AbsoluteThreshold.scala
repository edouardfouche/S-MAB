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
package com.edouardfouche.monitoring.rewards

import breeze.numerics.abs

/**
  * Return 1 if the value is >= t, else 0. (as in "Scaling Multi-Armed Bandit Algorithms" (Fouché 2019))
  * @param t the threshold above which we decide that the action (newval) yields a reward
  */
case class AbsoluteThreshold(t: Double) extends Reward {
  val name=s"Abs-$t"
  def getReward(newval: Double, oldval:Double): Double = if(abs(newval) >= t) 1 else 0
}
