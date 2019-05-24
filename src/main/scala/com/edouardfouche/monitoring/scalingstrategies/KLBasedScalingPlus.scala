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

//
/**
  * Scaling Strategy based on KL-UCB++
  * @param lmin the minimum number of pulls (in fact, typically 1)
  * @param lmax the maximum number of pulls (in fact, typically the number of arms)
  * @param delta the confidence threshold for scaling decisions
  *
  * @note the difference with KLBasedScaling is that it is using scala.math.log(t/ counts(arm)) / counts(arm)
  */
case class KLBasedScalingPlus(lmin: Int, lmax: Int, delta: Double) extends ScalingStrategy {
  val gamma = 0.0
  val name=s"KLP-$lmin-$lmax-$delta"
  var confidence=1.0

  val d = scala.math.pow(10,-8)
  val eps = scala.math.pow(10,-12)

  val maxiter = 20

  var k = lmax

  // calculate the kl-divergence
  def kl(p: Double, q: Double): Double = {
    p * scala.math.log(p/q) + (1-p)*scala.math.log((1-p)/(1-q))
  }
  // calculate the derivative kl-divergence
  def dkl(p: Double , q: Double): Double = {
    (q-p) / (q * (1.0 - q))
  }

  def scale(rewards: Array[Double], indexes: Array[Int], sums: Array[Double], counts: Array[Double], t:Double): Int = {
    require(rewards.length == indexes.length)
    require(sums.length == counts.length)
    val Lt: Double = indexes.length.toDouble
    val eta: Double = indexes.map(x => sums(x)/counts(x)).sum/Lt

    // use Newton's method
    def getKLUCBupper(arm: Int, t: Double): Double = {
      val logndn = scala.math.log(t/ counts(arm)) / counts(arm)
      val p: Double = (sums(arm)/counts(arm)).max(d)
      if(p >= 1.0) return 1.0

      var q = p + d
      for(i <- 1 to maxiter) {
        val f = logndn - kl(p,q)
        val df = -dkl(p,q)
        if(f*f < eps) return q // newton's method has converged
        q = (1.0-d).min((q - f/df).max(p+d))
      }
      q
    }

    if(eta <= delta) {
      if(Lt <= lmin) {
        k = Lt.toInt // Already min, can't scale down
        k
      } else {
        k = (Lt - 1.0).toInt // Scale Down
        k
      }
    } else {
      if(Lt >= lmax) { // Already max, can't scale up
        k = Lt.toInt
        k
      } else {
        val klindices:Array[(Int,Double)] = sums.indices.map(x => if(t==0.0 | counts(x) == 0.0) (x,1.0) else (x,getKLUCBupper(x,t))).toArray
        val sortedindices = klindices.sortBy(-_._2)
        val b = sortedindices((Lt.toInt-1) + 1)._2
        val Bt = (Lt / (Lt+1))*eta + (1/(Lt+1)) * b
        confidence = Bt
        if(Bt > delta) {
          k = (Lt + 1.0).toInt
          k
        } // Scale Up
        else {
          k = Lt.toInt
          k
        } // Don't Scale
      }
    }
  }
}
