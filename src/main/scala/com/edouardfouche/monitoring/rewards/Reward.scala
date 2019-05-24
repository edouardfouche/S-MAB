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

/**
  * General trait for rewards.
  * Derives from an action (and, optionally, the last known value of this action) a reward
 */
trait Reward {
  val name: String

  /**
    * Derives the reward
    * @param newval outcome of the action
    * @param oldval previously known value of the action (note: not always used in practice)
    * @return a value, that corresponds to the reward (typically 1 or 0)
    */
  def getReward(newval: Double, oldval: Double): Double
}
