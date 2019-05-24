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
package com.edouardfouche.preprocess

import breeze.stats.distributions.{RandBasis, ThreadLocalRandomGenerator}
import org.apache.commons.math3.random.MersenneTwister

/**
  * General trait for experimental scenarios
  * A scenario generate a data set with some particular properties
  */
trait Scenario{
  val id: String
  val d: Int
  /**
    * generate data
    * @return A 2-D Array of Double containing the values from the csv. (row oriented)
    */
  def generate(rand: RandBasis =
               new RandBasis(new ThreadLocalRandomGenerator(new MersenneTwister(scala.util.Random.nextInt)))): Array[Array[Double]]
}
