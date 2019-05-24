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

import com.edouardfouche.experiments.Data._
import com.edouardfouche.monitoring.actions.MI
import com.edouardfouche.preprocess.DataRef
import com.edouardfouche.streamsimulator.StreamSimulator

/**
  * Created by fouchee on 12.07.17.
  * This experiment pre-computes the bandit actions over the arms for our real-world experiment.
  * In the real-world experiment, "arms" = "pairs of attribute" and "action" = "compute MI"
  * In our case, we compute MI for pyrolisisbandit over a sliding window of size 1000 with step size 100
  */
object BanditCache extends BanditExperiment {
  val attributes = List("bandit","dataset","k","banditk","narms","gain","matrixdiff","cpuTime","wallTime","iteration","nrep")

  val data: Vector[DataRef] = Vector(bioliq_1wx20)

  val stepSize = 100
  val windowSize = 1000
  val action = MI

  def run(): Unit = {
    info(s"${formatter.format(java.util.Calendar.getInstance().getTime)} - Starting com.edouardfouche.experiments - ${this.getClass.getSimpleName}")

    for {
      d <- data
    } {
      info(s"Working with dataset ${d.id}")

      val initstream = StreamSimulator(d, action, windowSize, stepSize)

      info(s"Starting Precomputation: ${action.name}, $windowSize, $stepSize")
      val cache = initstream.cache
      info("Precomputation has ended!")

      val matrixPath = System.getProperty("user.dir") + "/data" + "/" + s"${initstream.id}.csv"
      info("Dumping results...")
      dump(matrixPath, cache.map(_ mkString ",").mkString("\n"))
      info("Dumping has ended!")
    }
    info(s"End of experiment ${this.getClass.getSimpleName} - ${formatter.format(java.util.Calendar.getInstance().getTime)}")
  }
}
