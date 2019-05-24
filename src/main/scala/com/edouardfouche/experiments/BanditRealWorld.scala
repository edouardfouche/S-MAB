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
package com.edouardfouche.experiments

import breeze.linalg
import com.edouardfouche.experiments.Data._
import com.edouardfouche.monitoring.bandits.nonstationary.{CUCB_ADWIN, MPKLUCB_ADWIN, MPTS_ADWIN}
import com.edouardfouche.monitoring.bandits.adversarial._
import com.edouardfouche.monitoring.bandits.oracles._
import com.edouardfouche.monitoring.bandits.stationary._
import com.edouardfouche.monitoring.rewards.AbsoluteThreshold
import com.edouardfouche.monitoring.scalingstrategies.{KLBasedScaling, NoScaling, ScalingStrategy}
import com.edouardfouche.preprocess.DataRef
import com.edouardfouche.streamsimulator.CachedStreamSimulator

/**
  * Created by fouchee on 12.07.17.
  * This experiment compares the behavior of various bandits against real-world data (see Paper)
  */
object BanditRealWorld extends BanditExperiment {
  val attributes = List("bandit","dataset","scalingstrategy","k","gain","cputime", "iteration")

  val data: DataRef = bioliq_1wx20_MI_1000_100
  val streamsimulator = CachedStreamSimulator(data)

  val reward = AbsoluteThreshold(2)

  val lmin = 1
  val lmax = streamsimulator.npairs

  val nRep = 100
  //val nRep = 1

  val scalingstrategies: Array[ScalingStrategy] = Array(
    KLBasedScaling(lmin, lmax, 0.1),
    KLBasedScaling(lmin, lmax, 0.2),
    KLBasedScaling(lmin, lmax, 0.3),
    KLBasedScaling(lmin, lmax, 0.4),
    KLBasedScaling(lmin, lmax, 0.5),
    KLBasedScaling(lmin, lmax, 0.6),
    KLBasedScaling(lmin, lmax, 0.7),
    KLBasedScaling(lmin, lmax, 0.8),
    KLBasedScaling(lmin, lmax, 0.9),
    NoScaling((streamsimulator.npairs*(1.0/10.0)).toInt),
    NoScaling((streamsimulator.npairs*(1.0/5.0)).toInt),
    NoScaling((streamsimulator.npairs*(1.0/4.0)).toInt),
    NoScaling((streamsimulator.npairs*(1.0/3.0)).toInt),
    NoScaling((streamsimulator.npairs*(1.0/2.0)).toInt),
    NoScaling(streamsimulator.npairs.toInt)
  )

  val banditConstructors = Vector(
    OracleDynamic,
    OracleStatic,
    OracleRandom,
    //OracleSequential,
    CUCB, CUCBm,
    MPKLUCB, MPKLUCBPLUS,
    Exp3M,
    MPTS, IMPTS, MPOTS,
    //MPDTS(0.7)(_,_,_,_), MPDTS(0.8)(_,_,_,_), MPDTS(0.9)(_,_,_,_), MPDTS(0.99)(_,_,_,_),
    //MPEGreedy(0.7)(_, _, _, _), MPEGreedy(0.8)(_, _, _, _), MPEGreedy(0.9)(_, _, _, _), MPEGreedy(0.99)(_, _, _, _),
    //MPSWUCB(50)(_, _, _, _), MPSWUCB(100)(_, _, _, _), MPSWUCB(500)(_, _, _, _), MPSWUCB(1000)(_, _, _, _),

    OracleStatic_ADWIN(0.1)(_,_,_,_), OracleDynamic_ADWIN(0.1)(_,_,_,_), OracleRandom_ADWIN(0.1)(_,_,_,_),
    //OracleSequential_ADWIN(0.1)(_,_,_,_),

    CUCB_ADWIN(0.1)(_,_,_,_),
    MPKLUCB_ADWIN(0.1)(_,_,_,_),
    Exp3M_ADWIN(0.1)(_,_,_,_),
    MPTS_ADWIN(0.1)(_,_,_,_)

    //MPTS_ADWIN(0.5)(_,_,_,_),
    //MPTS_ADWIN(0.3)(_,_,_,_),
    //MPTS_ADWIN(1)(_,_,_,_),
    //MPTS_ADWIN(0.01)(_,_,_,_),
    //MPTS_ADWIN(0.001)(_,_,_,_)
  )

  def run(): Unit = {
    info(s"${formatter.format(java.util.Calendar.getInstance().getTime)} - Starting com.edouardfouche.experiments - ${this.getClass.getSimpleName}")
    // display parameters
    info(s"Parameters:")
    info(s"lmin:$lmin, lmax: $lmax")
    info(s"data:${data.id}")
    info(s"scalingstrategies: ${scalingstrategies.map(_.name) mkString ", "}")
    info(s"reward: ${reward.name}")
    info(s"nRep: ${nRep}")

    for {
      scalingstrategy <- scalingstrategies.par
    } {
      for{
        banditConstructor <- banditConstructors.par
      } {
        var allgains: linalg.Vector[Double] = linalg.Vector((1 to streamsimulator.nbatches).map(x => 0.0).toArray)
        var allks: linalg.Vector[Double] = linalg.Vector((1 to streamsimulator.nbatches).map(x => 0.0).toArray)
        var allcpu: linalg.Vector[Double] = linalg.Vector((1 to streamsimulator.nbatches).map(x => 0.0).toArray)

        for {
          rep <- 0 until nRep
        } {
          //info(s"Starting com.edouardfouche.experiments with data: ${d.id}, configuration k: ${kratio}, rep=$rep")
          val bandit = banditConstructor(streamsimulator.copy(), reward, scalingstrategy, scalingstrategy.k)
          if (rep % 10 == 0) info(s"Reached rep $rep with bandit ${bandit.name}, ${scalingstrategy.name}")
          val (gains, ks, cpu) = fullrunnerGainsKsCPU(bandit, Array[Double](), Array[Int](), Array[Double]())
          allgains = allgains +:+ (breeze.linalg.Vector(gains) *:* (1.0/nRep))
          allks = allks +:+ (breeze.linalg.Vector(ks.map(_.toDouble)) *:* (1.0/nRep))
          allcpu = allcpu +:+ (breeze.linalg.Vector(cpu.map(_.toDouble)) *:* (1.0/nRep))
        }

        val bandit = banditConstructor(streamsimulator.copy(), reward, scalingstrategy, scalingstrategy.k)
        for{
          step <- 0 until allgains.length
        }{
          val summary = ExperimentSummary(attributes)
          // this is the list of all the data possible we can record, "attributes" is usually a subset of it
          // val attributes = List("bandit","dataset","action","reward","scaling","windowSize","stepSize",
          // "delta","gamma","k","banditk","narms","gain","matrixdiff","cpuTime","wallTime","iteration","nrep")
          summary.add("bandit", bandit.name)
          summary.add("dataset", bandit.stream.dataset.id)
          summary.add("scalingstrategy", bandit.scalingstrategy.name)
          summary.add("k",  "%.2f".format(allks(step)))
          summary.add("gain",  "%.2f".format(allgains(step)))
          //summary.add("confidence", allconfs(step))
          //summary.add("gamma", bandit.scalingstrategy.gamma)
          summary.add("cputime", "%.4f".format(allcpu(step)))
          summary.add("iteration", step)
          summary.write(summaryPath)
        }
      }

      }



    info(s"End of experiment ${this.getClass.getSimpleName} - ${formatter.format(java.util.Calendar.getInstance().getTime)}")
  }
}
