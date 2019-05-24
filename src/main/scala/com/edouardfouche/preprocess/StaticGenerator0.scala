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

import breeze.stats.distributions.{Bernoulli, RandBasis, ThreadLocalRandomGenerator}
import org.apache.commons.math3.random.MersenneTwister

// StaticGenerator simulates a setting where the means of the arms linearly distributed between 0 and 1
// (as in the original publication). d is the number of arms.
/**
  * StaticGenerator simulates a setting where the means of the arms linearly distributed between 0 and 1
  * As in "Scaling Multi-Armed Bandit Algorithms" (Fouché 2019)
  * @param d the number of arms
  */
case class StaticGenerator0(d: Int = 100) extends Scenario{
  val id = s"StaticGenerator0-$d"
  val n = 100000
  /**
    * generate data
    * @return A 2-D Array of Double containing the values from the csv. (row oriented)
    */
  def generate(rand: RandBasis =
                 new RandBasis(new ThreadLocalRandomGenerator(new MersenneTwister(scala.util.Random.nextInt)))): Array[Array[Double]] = {
    val a = (1 to d).map(_/d.toDouble).toArray
    val means = a

    val cols: Array[Array[Double]] = means.map{x: Double =>
      val b = new Bernoulli(x)(rand)
      (1 to n).toArray.map(y => if(b.draw()) 1.0 else 0.0)
    }
    cols.transpose
  }
}
