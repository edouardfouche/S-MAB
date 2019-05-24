package com.edouardfouche.monitoring.bandits.oracles

import breeze.linalg
import com.edouardfouche.monitoring.bandits.BanditAdwin
import com.edouardfouche.monitoring.resetstrategies.SharedAdwin
import com.edouardfouche.monitoring.rewards.Reward
import com.edouardfouche.monitoring.scalingstrategies.ScalingStrategy
import com.edouardfouche.streamsimulator.Simulator

/**
  * A Static Oracle with Multiple Plays, which always choose the top-k arms in expectation at each round
  * This is like the "static oracle" described in "Optimal Exploration-Exploitation in a Multi-Armed-Bandit Problem with Non-stationary Rewards" (Besbes14)
  * It is combined with ADWIN (in fact, just for the scaling part)
  *
  * @param delta the parameter for ADWIN (upper bound for the false positive rate)
  * @param stream a stream simulator on which we let this bandit run
  * @param reward the reward function which derives the gains for each action
  * @param scalingstrategy the scaling strategy, which decides how many arms to pull for the next step
  * @param k the initial number of pull per round
  */
case class OracleStatic_ADWIN(delta: Double)(val stream: Simulator, val reward: Reward, val scalingstrategy: ScalingStrategy, var k: Int) extends BanditAdwin {
  val name: String = s"OS-ADWIN-$delta"

  val cache: Array[Array[Double]] = stream.cache

  def get_top_indexes_hindsight: Array[Int] = {
    val hindsightmatrix = currentMatrix
    var totalrewards: linalg.Vector[Double] = linalg.Vector(cache(0).indices.map(x => 0.0).toArray)
    for {
      x <- cache.indices
    } {
      val rewards = cache(x).zipWithIndex.map { x =>
        val r = reward.getReward(x._1, hindsightmatrix(x._2))
        hindsightmatrix(x._2) = r
        r
      }
      totalrewards = totalrewards + linalg.Vector(rewards)
    }
    totalrewards.toArray.zipWithIndex.sortBy(-_._1).map(_._2)
  }
  val top_indexes_hindsight: Array[Int] = get_top_indexes_hindsight
  val top_arms_hindsight: Array[(Int,Int)] = top_indexes_hindsight.map(combinations(_))

  def next: (Array[(Int, Int)], Array[Double], Double) = {
    val top_arms = top_arms_hindsight.take(k)
    val top_indexes = top_indexes_hindsight.take(k)
    val newValues = stream.nextAndCompute(top_indexes)
    if (newValues.isEmpty) return (Array[(Int, Int)](), Array[Double](), 0)

    val updates = scala.collection.mutable.Map[Int, Double]()

    val gains = (top_indexes zip newValues).map(x => {
      val d = reward.getReward(x._2, currentMatrix(x._1))
      currentMatrix(x._1) = x._2 // replace
      counts(x._1) += 1.0
      sums(x._1) += d

      // Add into adwin and add the update into the map
      sharedAdwin.addElement(x._1, d)
      updates(x._1) = d
      d
    })

    history = history :+ updates
    t += 1
    k = scalingstrategy.scale(gains, top_indexes, sums, counts, t)

    // Here we, add up the size of the adwin (those are the number of pulls) and the number of unpulls, to get the
    // actual size of each pulled arm.
    val windows = (0 until narms).map(x => (x, sharedAdwin.getSingleSize(x) + (history.length-counts(x))))

    val smallest_window = windows.minBy(_._2) // this is the smallest window

    // Rolling back
    if(smallest_window._2.toInt < history.length) {
      for{
        x <- smallest_window._2.toInt until history.length
      } {
        val rollback = history.head
        history = history.tail
        for((key,value) <- rollback) {
          sums(key) = sums(key) - value
          counts(key) = counts(key) - 1
        }
      }
    }
    t = history.length // The time context is the same as the history, which is the same as the smallest window

    (top_arms, gains, gains.sum)
  }

}
