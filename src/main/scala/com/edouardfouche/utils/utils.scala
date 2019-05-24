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
package com.edouardfouche

import java.io.{BufferedWriter, File, FileWriter}
import com.typesafe.scalalogging.LazyLogging

/**
  * Created by fouchee on 11.07.17.
  * A bunch of utility function, self-explaining
  */
package object utils extends LazyLogging {
  def time[A](f: => A) = {
    val s = System.nanoTime
    val ret = f
    println("Time: " + (System.nanoTime - s) / 1e6 + "ms")
    ret
  }

  // expect rows
  def saveDataSet[T](res: Array[Array[T]], path: String): Unit = {
    val file = new File(path)
    val bw = new BufferedWriter(new FileWriter(file))
    bw.write(s"${(1 to res(0).length) mkString ","} \n") // a little header
    res.foreach(x => bw.write(s"${x mkString ","} \n"))
    bw.close()
  }


  def save[T](res: Array[T], path: String): Unit = {
    val file = new File(path)
    val bw = new BufferedWriter(new FileWriter(file))
    bw.write(res mkString ",")
    bw.close()
  }

  def createFolderIfNotExisting(path: String): Unit = {
    val directory = new File(path)
    if (!directory.exists()) {
      directory.mkdir()
    }
  }

  def initiateSummaryCSV(path: String, header: String): FileWriter = {
    val fileA = new File(path)
    val fwA = new FileWriter(fileA, true) // append set to true
    fwA.write(header) // this is the header
    fwA
  }

  def extractFieldNames[T <: Product](implicit m: Manifest[T]) = m.runtimeClass.getDeclaredFields.map(_.getName)
}
