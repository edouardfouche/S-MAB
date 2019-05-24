package com.edouardfouche.monitoring.bandits.oracles

import com.edouardfouche.monitoring.bandits.Bandit
import com.edouardfouche.monitoring.rewards.Reward
import com.edouardfouche.monitoring.scalingstrategies.ScalingStrategy
import com.edouardfouche.streamsimulator.Simulator

/**
  * A Sequential Oracle with Multiple Plays, simply choosing K arms sequential at each round
  *
  * @param stream a stream simulator on which we let this bandit run
  * @param reward the reward function which derives the gains for each action
  * @param scalingstrategy the scaling strategy, which decides how many arms to pull for the next step
  * @param k the initial number of pull per round
  */
case class OracleSequential(stream: Simulator, reward: Reward, scalingstrategy: ScalingStrategy, var k: Int) extends Bandit {
  val name: String = "OSeq"

  var position = 0

  override def reset: Unit = {
    super.reset
    position = 0
  }

  def next: (Array[(Int, Int)], Array[Double], Double) = {
    // Draw k arms one after the other
    val indexes = (position until position+k).map(_ % narms).toArray
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

    position = (position + k)% narms

    t += 1
    k = scalingstrategy.scale(gains, indexes, sums, counts, t)

    val gain = gains.sum
    (arms, gains, gain)
  }
}
