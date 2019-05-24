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
package com.edouardfouche.utils

import java.lang.management.ManagementFactory

/**
  * Created by fouchee on 17.07.17
  * Utility functions to measure time
  * We use it to measure WallTime or CPUTime in milliseconds
  */
object StopWatch {
  private val thread = ManagementFactory.getThreadMXBean
  private var startCPU: Long = 0
  private var startWall: Long = 0

  def start: Unit = {
    startCPU = thread.getCurrentThreadCpuTime
    startWall = System.nanoTime()
  }

  def convert(value: Long, unit: String = "ms"): Double = {
    unit match {
      case "s" => value/1000000000.0
      case "ms" => value/1000000.0
      case "μs" => value/1000.0
      case "ns" => value.toDouble
      case _ => throw new Error(s"Unknown unit $unit")
    }
  }

  // returns cpu and wall time
  def stop(unit: String = "ms"): (Double,Double) = {
    val cpu = thread.getCurrentThreadCpuTime - startCPU
    val wall = System.nanoTime() - startWall
    (convert(cpu, unit), convert(wall, unit))
  }

  def measureTime[R](block: => R, unit: String = "ms"): (Double,Double, R) = {
    val t01 = thread.getCurrentThreadCpuTime
    val t02 = System.nanoTime()
    val result = block
    val t11 = thread.getCurrentThreadCpuTime
    val t12 = System.nanoTime()
    (convert(t11 - t01, unit),convert(t12 - t02, unit), result)
  }


  def measureCPUTime[R](block: => R, unit: String = "ms"): (Double, R) = {
    val t0 = thread.getCurrentThreadCpuTime
    val result = block
    val t1 = thread.getCurrentThreadCpuTime
    (convert(t1 - t0, unit), result)
  }

  def measureWallTime[R](block: => R, unit: String = "ms"): (Double, R) = {
    val t0 = System.nanoTime()
    val result = block
    val t1 = System.nanoTime()
    (convert(t1 - t0, unit), result)
  }
}
