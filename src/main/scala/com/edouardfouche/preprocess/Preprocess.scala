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

import java.io.FileInputStream
import java.util.zip.{ZipFile, ZipInputStream}

import scala.collection.parallel.ForkJoinTaskSupport

/**
  * Created by fouchee on 02.05.17.
  */

/**
  * Encapsulate a few preprocessing steps (open a CSV file, compute the rank com.edouardfouche.index structure).
  */
object Preprocess {
  /**
    * Helper function that redirects to openArff in case an arff is given else openCSV
    */
  def open(path: String, header: Int = 1, separator: String = ",", excludeIndex: Boolean = false, dropClass: Boolean = true, sample1000: Boolean = false): Array[Array[Double]] = {
    require(header >= 0, "header cannot be a negative number")
    require(separator.length == 1, "separator cannot be longer than 1")
    if (path.endsWith("arff")) openArff(path, dropClass, sample1000)
    //else if (path.endsWith("csv.zip")) openCSVZIP(path, header, separator, excludeIndex, dropClass, sample1000) // TODO: Would be a good idea to read directly from a zipfile
    else if (path.endsWith("csv")) openCSV(path, header, separator, excludeIndex, dropClass, sample1000)
    else throw new Error(s"Unknown extension in $path")
  }

  /**
    * Get the last column of a data file, assume it is the class and that it is numerical, even binary
    * @param path Path of the file in the system.
    * @param header Number of lines to discard (header), by default 1.
    * @param separator Number of lines to discard (header), by default 1.
    * @param excludeIndex Whether to exclude an com.edouardfouche.index (the first column) or not.
    * @return The "class" column, should be an Array of Double
    *
    * @note This is quick and dirty, open normally by keeping the class and only keep the last column
    */
  def getLabels(path: String, header: Int = 1, separator: String = ",", excludeIndex: Boolean = false): Array[Boolean] = {
    require(header >= 0, "header cannot be a negative number")
    require(separator.length == 1, "Data separator cannot be longer than 1")
    val lastcolumns = if(path.endsWith("arff")) {
      openArff(path, dropClass = false).last
    } else openCSV(path, header, separator, excludeIndex, dropClass = false).last
    lastcolumns.map(x => if(x > 0) true else false)  // This trick should be done for the arff data HiCS synthetic
    // That's because the class column has a binary encoding which then trick the auc computation
  }

  /**
    * Get the columns names of a data set. Assumes the names are placed in the first line and separated by a comma.
    * @param path Path of the file in the system.
    * @param header Number of lines to discard (header), by default 1.
    * @param separator Number of lines to discard (header), by default 1.
    * @return An array of strings, where each string is a column name. Names are in the original order.
    *
    * @note This is quick and dirty, open normally by keeping the class and only keep the last column
    */
  def getColumnNames(path: String, header: Int = 1, separator: String = ","): Array[String] = {
    require(header >= 0, "header cannot be a negative number")
    require(separator.length == 1, "Data separator cannot be longer than 1")
    val bufferedSource = scala.io.Source.fromFile(path)
    bufferedSource.getLines().drop(header-1).next.split(separator)
  }

  /**
    * Get the columns names of a data set in a map, assigning the position com.edouardfouche.index (integer) to the corresponding name (string)
    * @param path Path of the file in the system.
    * @param header Number of lines to discard (header), by default 1.
    * @param separator Number of lines to discard (header), by default 1.
    * @return An array of strings, where each string is a column name. Names are in the original order.
    *
    * @note This is quick and dirty, open normally by keeping the class and only keep the last column
    */
  def getColumnNamesMap(path: String, header: Int = 1, separator: String = ","): Map[Int,String] = {
    val names = getColumnNames(path, header, separator)
    names.zipWithIndex.map(x => (x._2,x._1)).toMap
  }

  /**
    * Open a csv file at a specified path. Currently, only handle numerical values.
    *
    * @param path      Path of the file in the system.
    * @param header    Number of lines to discard (header), by default 1.
    * @param separator Separator used, by default, comma.
    * @param excludeIndex Whether to exclude an com.edouardfouche.index (the first column) or not.
    * @param dropClass Whether to drop the "class" column if there is one. (assumes it is the last one)
    * @param max1000 cap the opened data to 1000 rows. If the original data has more rows, sample 1000 without replacement
    * @return A 2-D Array of Double containing the values from the csv.
    */
  def openCSV(path: String, header: Int = 1, separator: String = ",", excludeIndex: Boolean = false, dropClass: Boolean = true, max1000: Boolean = false): Array[Array[Double]] = {
    require(header >= 0, "header cannot be a negative number")
    require(separator.length == 1, "separator cannot be longer than 1")
    val bufferedSource = scala.io.Source.fromFile(path)
    //val result = bufferedSource.getLines.drop(header).map(x => x.split(separator).map(_.trim.toDouble)).toArray

    val result = if (!excludeIndex) {
      bufferedSource.getLines.filter(!_.isEmpty).drop(header).map(x => x.split(separator).map(_.trim)).toArray.transpose
    } else {
      val result = bufferedSource.getLines.filter(!_.isEmpty).drop(header).map(x => x.split(separator).map(_.trim)).toArray.transpose
      result.slice(1, result.length + 1)
    }
    bufferedSource.close()

    // This could be improved a bit, because toDouble is done twice

    val parser = result.map(x => Some(x.map(_.toDouble)))

    val data: Array[Array[Double]] = parser.collect{
      case Some(i) => i
    }

    //val data: Array[Array[Double]] = parser.flatten

    val droppedData = if(dropClass & header == 1) {
      val head = scala.io.Source.fromFile(path).getLines.next.split(" ")
      if(head.exists(_ contains "class")) data.init
      else data
    } else data

    val resultData = if(!max1000) droppedData
    else {
      if(droppedData(0).length < 1000) droppedData
      else {
        val indexes = scala.util.Random.shuffle(droppedData(0).indices.toList).take(1000).toArray
        data.map(x => indexes.map(x(_)))
      }
    }

    resultData.transpose
  }

  /**
    * Open an Arff file as a 2-D Array of Double
    * @param path Path to the file in the current filesystem
    * @param dropClass Whether to drop the "class" column if there is one
    * @param max1000 cap the opened data to 1000 rows. If the original data has more rows, sample 1000 without replacement
    * @return A 2-D Array of Double containing the values for each numerical columns
    *
    * @note This method is inspired from the work of Fabian Keller
    */
  def openArff(path: String, dropClass: Boolean = true, max1000: Boolean = false): Array[Array[Double]] = {
    val lines = scala.io.Source.fromFile(path).getLines.toArray
    val numAttr = lines.count(x => x.toLowerCase.startsWith("@attribute"))
    //val attrNames = lines.filter(x => x.startsWith("@attribute")).map(line => line.split(" ")(1))

    val linesData = lines.drop(lines.indexWhere(x => x.toLowerCase == "@data") + 1).filter(x => x.split(",").length == numAttr)
    val numInst = linesData.length

    val matrix = Array.ofDim[Double](numInst, numAttr)

    var i = 0
    for (line <- linesData) {
      val fields = line.split(",")
      var j = 0
      for (el <- fields) {
        if (el == "'no'") matrix(i)(j) = 0.0
        else if (el == "'yes'") matrix(i)(j) = 1.0
        else matrix(i)(j) = el.toFloat
        j += 1
      }
      i += 1
    }

    val data = matrix.transpose

    val droppedData = if(!dropClass) data
    else {
      val attributes = lines.filter(x => x.toLowerCase.startsWith("@attribute")).map(_.split(" ")(1))
      //print(s"attributes: ${attributes mkString ","}")
      if (attributes.exists(_.toLowerCase contains "class") | attributes.exists(_.toLowerCase contains "outlier")) data.init
      else data
    }

    val resultData = if(!max1000) droppedData
    else {
      if(droppedData(0).length < 1000) droppedData
      else {
        val indexes = scala.util.Random.shuffle(droppedData(0).indices.toList).take(1000).toArray
        droppedData.map(x => indexes.map(x(_)))
      }
    }

    resultData
  }

}
