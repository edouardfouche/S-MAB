package com.edouardfouche.monitoring.bandits.stationary

import breeze.stats.distributions.Beta
import com.edouardfouche.monitoring.bandits.BanditTS
import com.edouardfouche.monitoring.rewards.Reward
import com.edouardfouche.monitoring.scalingstrategies.ScalingStrategy
import com.edouardfouche.streamsimulator.Simulator

/**
  * "Improved" Multiple Play Thompson Sampling
  * The idea of MP-TS/IMP-TS comes from "Optimal Regret Analysis of Thompson Sampling in Stochastic Multi-armed BanditK Problem with Multiple Plays" (Komiyama 2016)
  * In "Scaling Multi-Armed Bandit Algorithms" (Fouché 2019), MP-TS is referred to as S-TS
  *
  * @param stream a stream simulator on which we let this bandit run
  * @param reward the reward function which derives the gains for each action
  * @param scalingstrategy the scaling strategy, which decides how many arms to pull for the next step
  * @param k the initial number of pull per round
  *
  * @note IMP-TS differs from MP-TS as k-1 arm are selected by purely exploiting knowledge and the last one via TS
  */
case class IMPTS(stream: Simulator, reward: Reward, scalingstrategy: ScalingStrategy, var k: Int) extends BanditTS {
  val name = "IMP-TS"

  def next: (Array[(Int, Int)], Array[Double], Double) = {
    // The difference with MPTS is that only one arm is used for exploration, the rest is exploitation.
    val draws = beta_params.zipWithIndex.map(x => (x._2, x._1._1 / (x._1._1 + x._1._2))).sortBy(- _._2) // get the empirical average from distributions
    val exploitdraws = draws.take(k-1)
    val exploredraw = draws.drop(k-1).map(x =>
       (x._1, new Beta(beta_params(x._1)._1, beta_params(x._1)._2).draw())
    ).maxBy(_._2)

    val indexes = (exploitdraws :+ exploredraw).map(_._1)
    val arms = indexes.map(combinations(_))

    val newValues = stream.nextAndCompute(indexes)
    if (newValues.isEmpty) return (Array[(Int, Int)](), Array[Double](), 0)

    // Update the current Matrix and compute the diff at the same time
    val gains = (indexes zip newValues).map(x => {
      val d = reward.getReward(x._2,currentMatrix(x._1))
      beta_params(x._1) = (beta_params(x._1)._1+d, beta_params(x._1)._2+(1.0-d))
      currentMatrix(x._1) = x._2 // replace
      counts(x._1) += 1.0
      sums(x._1) += d // (http://www.cs.cmu.edu/~rsalakhu/10703/Lecture_Exploration.pdf slide 12)
      d
    })

    t += 1
    k = scalingstrategy.scale(gains, indexes, sums, counts, t)

    (arms, gains, gains.sum)
  }

}
