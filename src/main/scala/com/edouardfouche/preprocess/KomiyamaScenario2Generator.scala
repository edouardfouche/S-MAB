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

/**
  * Reproduce scenario 1 from "Optimal Regret Analysis of Thompson Sampling in Stochastic Multi-armed Bandit Problem with Multiple Plays" (Komiyama 2016)
  */
object KomiyamaScenario2Generator extends Scenario{
  val id = "scenario2"
  val d = 20
  /**
    * generate data
    * @return A 2-D Array of Double containing the values from the csv. (row oriented)
    */
  def generate(rand: RandBasis=
               new RandBasis(new ThreadLocalRandomGenerator(new MersenneTwister(scala.util.Random.nextInt)))): Array[Array[Double]] = {
    val means = Array(0.15, 0.12, 0.10) ++ (4 to 12).toArray.map(x => 0.05) ++ (13 to 20).map(x => 0.03)
    val n = 100000
    val cols: Array[Array[Double]] = means.map{x: Double =>
      val b = new Bernoulli(x)(rand)
      (1 to n).toArray.map(y => if(b.draw()) 1.0 else 0.0)
    }
    cols.transpose
  }
}
