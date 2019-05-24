package com.edouardfouche.monitoring.bandits.oracles

import com.edouardfouche.monitoring.bandits.Bandit
import com.edouardfouche.monitoring.rewards.Reward
import com.edouardfouche.monitoring.scalingstrategies.ScalingStrategy
import com.edouardfouche.streamsimulator.Simulator

/**
  * A Dynamic Oracle with Multiple Plays, which always choose the top-k arms (not "in expectation") at each round
  * This is like the "dynamic oracle" described in "Optimal Exploration-Exploitation in a Multi-Armed-Bandit Problem with Non-stationary Rewards" (Besbes14)
  *
  * @param stream a stream simulator on which we let this bandit run
  * @param reward the reward function which derives the gains for each action
  * @param scalingstrategy the scaling strategy, which decides how many arms to pull for the next step
  * @param k the initial number of pull per round
  */
case class OracleDynamic(stream: Simulator, reward: Reward, scalingstrategy: ScalingStrategy, var k: Int) extends Bandit {
  val name: String = "OD"


  def next: (Array[(Int, Int)], Array[Double], Double) = {
    val newMatrix = stream.nextAndCompute(combinations.zipWithIndex.map(_._2))
    if (newMatrix.isEmpty) return (Array[(Int, Int)](), Array[Double](), 0)

    // Find the top-k arms
    val diffMatrix = newMatrix.zip(currentMatrix.toArray).map(x => reward.getReward(x._1, x._2))

    // Update the current Matrix
    val topindexes = diffMatrix.zipWithIndex.sortBy(-_._1).map(_._2).take(k)
    val toparms = topindexes.map(combinations(_))
    topindexes.foreach(x => {
      currentMatrix(x) = newMatrix(x)
      counts(x) += 1.0
      sums(x) += diffMatrix(x)
    })

    // Sum up the gain of the top arms / top indexes
    val gains = topindexes.map(diffMatrix(_))

    t += 1
    k = scalingstrategy.scale(gains, topindexes, sums, counts, t)

    (toparms, gains, gains.sum)
  }

}
