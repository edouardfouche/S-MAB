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
  * Return 1 if the difference between newval and oldval is >= t, else 0.
  * @param t the threshold above which we decide that the difference between newval and oldval yields a reward
  */
case class RelativeThreshold(t: Double) extends Reward {
  val name=s"Rel-$t"
  def getReward(newval: Double, oldval:Double): Double = if(abs(newval - oldval) > t) 1 else 0
}
