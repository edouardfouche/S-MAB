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
  * GradualGenerator simulates a setting where the means of the tops arms at "gradually" (i.e., successively) set to 0
  * and back. (as in "Scaling Mutli-Armed Bandit Algorithms" (Fouché 2019)).
  * @param d number of arms
  */
case class GradualGenerator(d: Int = 100) extends Scenario{
  val id = s"GradualGenerator-$d"
  val n = 100000
  /**
    * generate data
    * @return A 2-D Array of Double containing the values from the csv. (row oriented)
    */
  def generate(rand: RandBasis =
                 new RandBasis(new ThreadLocalRandomGenerator(new MersenneTwister(scala.util.Random.nextInt)))): Array[Array[Double]] = {
    val a = (1 to d).map(_/d.toDouble - 1/(3*d.toDouble)).toArray
    val means = a.reverse
    val nevents = 60
    val eventpoints = (1 to nevents).map(_*n/(nevents.toDouble+1)).toArray
    val stepstates = (0 to nevents/2) ++ (0 until nevents/2).reverse

    (0 until n).toArray.map{x =>
      val step = eventpoints.count(_ < x)
      means.zipWithIndex.map{y =>
        if(y._2 >= stepstates(step)) {
          val b = new Bernoulli(y._1)(rand)
          if(b.draw()) 1.0 else 0.0
        } else 0.0
      }
    }
  }
}
