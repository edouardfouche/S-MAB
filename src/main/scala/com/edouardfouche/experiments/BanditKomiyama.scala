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
import com.edouardfouche.monitoring.bandits.oracles.OracleStatic
import com.edouardfouche.monitoring.bandits.stationary._
import com.edouardfouche.monitoring.bandits.adversarial._
import com.edouardfouche.monitoring.rewards.AbsoluteThreshold
import com.edouardfouche.monitoring.scalingstrategies.{NoScaling, ScalingStrategy}
import com.edouardfouche.preprocess.{KomiyamaScenario1Generator, KomiyamaScenario2Generator, InternalDataRef}
import com.edouardfouche.streamsimulator.CachedStreamSimulator

/**
  * Created by fouchee on 12.07.17.
  * This experiment reproduces the experiments from "Optimal Regret Analysis of Thompson Sampling in Stochastic
  * Multi-armed Bandit Problem with Multiple Plays" Komiyama 2016.
  */
object BanditKomiyama extends BanditExperiment {
  val attributes = List("bandit","dataset","banditk","narms","gain","iteration")

  val scenarios = Array((KomiyamaScenario1Generator, 2),(KomiyamaScenario2Generator, 3))
  val nRep = 100

  val reward = AbsoluteThreshold(1)

  val banditConstructors = Vector(
    OracleStatic, // OracleDynamic,
    CUCB, CUCBm,
    MPKLUCB, MPKLUCBPLUS,
    //Exp3M(0.1)(_, _, _, _), Exp3M(0.3)(_, _, _, _), Exp3M(0.5)(_, _, _, _), Exp3M(0.7)(_, _, _, _), Exp3M.apply(0.9)(_, _, _, _),
    Exp3M,
    IMPTS, MPTS, MPOTS//, MPEGreedy(1)(_, _, _, _), MPEGreedy(0.95)(_, _, _, _), MPEGreedy(0.9)(_, _, _, _), MPEGreedy(0.8)(_, _, _, _), MPEGreedy(0.7)(_, _, _, _)
    )

  def run(): Unit = {
    info(s"${formatter.format(java.util.Calendar.getInstance().getTime)} - Starting com.edouardfouche.experiments - ${this.getClass.getSimpleName}")
    // display parameters
    info(s"Parameters:")

    for {
      scenario <- scenarios.par
    } {
      val id= scenario._1.id
      val generator= scenario._1
      val k = scenario._2
      val scalingstrategy: ScalingStrategy = NoScaling(k)

      info(s"Prepare simulators for $id")
      val simulators = (0 until nRep).map{x =>
        val d = generator.generate()
        val datasource = InternalDataRef(id, d, "cache")
        CachedStreamSimulator(datasource)
      }
      info(s"Simulators for $id are ready!")
      for{
        banditConstructor <- banditConstructors.par
      } {
        var allgains: linalg.Vector[Double] = linalg.Vector((1 to simulators(0).nbatches).map(x => 0.0).toArray)
        for {
          rep <- 0 until nRep
        } {
          val bandit = banditConstructor(simulators(rep).copy(), reward, scalingstrategy, k)
          if (rep % 10 == 0) info(s"Reached rep $rep with bandit ${bandit.name}, $id")
          val gains = fullrunner(bandit, Array[Double]())
          allgains = allgains +:+ (breeze.linalg.Vector(gains) *:* (1.0/nRep))
        }
        val bandit = banditConstructor(simulators(0).copy(), reward, scalingstrategy, k)

        for{
          step <- allgains.toArray.zipWithIndex
        }{
          val summary = ExperimentSummary(attributes)
          // this is the list of all the data possible we can record, "attributes" is usually a subset of it
          // val attributes = List("bandit","dataset","action","reward","scaling","windowSize","stepSize",
          // "delta","gamma","k","banditk","narms","gain","matrixdiff","cpuTime","wallTime","iteration","nrep")
          summary.add("bandit", bandit.name)
          summary.add("dataset", bandit.stream.dataset.id)
          /*
          summary.add("action", bandit.stream.action_name)
          summary.add("reward", bandit.reward.name)
          summary.add("scaling", bandit.scalingstrategy.name)
          summary.add("windowSize", bandit.stream.windowSize)
          summary.add("stepSize", bandit.stream.stepSize)
          summary.add("delta", bandit.scalingstrategy.delta)
          summary.add("gamma", bandit.scalingstrategy.gamma)
          */
          summary.add("banditk", bandit.k)
          summary.add("narms", bandit.narms)
          summary.add("gain", step._1)
          summary.add("iteration", step._2)
          summary.write(summaryPath)
        }
      }
    }
    info(s"End of experiment ${this.getClass.getSimpleName} - ${formatter.format(java.util.Calendar.getInstance().getTime)}")
  }
}
