package com.edouardfouche.monitoring.bandits.oracles

import com.edouardfouche.monitoring.bandits.Bandit
import com.edouardfouche.monitoring.rewards.Reward
import com.edouardfouche.monitoring.scalingstrategies.ScalingStrategy
import com.edouardfouche.streamsimulator.Simulator

import scala.util.Random

/**
  * A Random Oracle with Multiple Plays, simply choosing K arms at random for every round
  *
  * @param stream a stream simulator on which we let this bandit run
  * @param reward the reward function which derives the gains for each action
  * @param scalingstrategy the scaling strategy, which decides how many arms to pull for the next step
  * @param k the initial number of pull per round
  */
case class OracleRandom(stream: Simulator, reward: Reward, scalingstrategy: ScalingStrategy, var k: Int) extends Bandit {
  val name: String = "OR"

  def next: (Array[(Int, Int)], Array[Double], Double) = {
    // Random BanditK: Just draw arms at random
    val indexes = Random.shuffle(combinations.indices.toList).take(k).toArray
    val arms = indexes.map(combinations(_))
    val newValues = stream.nextAndCompute(indexes)
    if (newValues.isEmpty) return (Array[(Int, Int)](), Array[Double](), 0)

    val gains = (indexes zip newValues).map(x => {
      val d = reward.getReward(x._2, currentMatrix(x._1))
      currentMatrix(x._1) = x._2 // replace
      counts(x._1) += 1.0
      sums(x._1) += d
      d
    })

    t += 1
    k = scalingstrategy.scale(gains, indexes, sums, counts, t)

    // Sum up the gain of the top arms / top indexes
    val gain = gains.sum
    (arms, gains, gain)
  }
}
