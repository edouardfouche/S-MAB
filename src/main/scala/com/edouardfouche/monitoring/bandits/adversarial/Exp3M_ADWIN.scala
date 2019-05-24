package com.edouardfouche.monitoring.bandits.adversarial

import com.edouardfouche.monitoring.bandits.BanditAdwin
import com.edouardfouche.monitoring.rewards.Reward
import com.edouardfouche.monitoring.scalingstrategies.ScalingStrategy
import com.edouardfouche.streamsimulator.Simulator

import scala.language.implicitConversions
import scala.util.Random

/**
  * Exp3.M, as described in "Algorithms for Adversarial BanditK Problems with Multiple Plays" (Uchiya, 2010)
  * This version is combined with ADWIN
  *
  * @param delta the parameter for ADWIN (upper bound for the false positive rate)
  * @param stream a stream simulator on which we let this bandit run
  * @param reward the reward function which derives the gains for each action
  * @param scalingstrategy the scaling strategy, which decides how many arms to pull for the next step
  * @param k the initial number of pull per round
  *
  * @note the parameter gamma is set automatically as suggested by the corollary in Uchiya2010
  */
case class Exp3M_ADWIN(delta: Double)(val stream: Simulator, val reward: Reward, val scalingstrategy: ScalingStrategy, var k: Int) extends BanditAdwin {


  val name = s"Exp3.M-ADWIN-$delta"
  //println(s"Initialized $name= ncols: $ncols, nrows: $nrows, narms: $narms, npulls: $k")

  // As the weights of Exp3M can grow very large, it needs it own weight array made of BigDecimal
  // Otherwise, at somepoint the weights become Infinity for large experiments
  var Exp3Mweights: Array[BigDecimal] = (0 until narms).map(x => BigDecimal(1.0)).toArray // initialize weights to 1
  var nperformed: Int = 0
  var gamma: Double = (0.999).min(
    scala.math.sqrt(narms * scala.math.log(narms/k) / ((scala.math.E -1)*k*(stream.nbatches-nperformed))))

  override def reset: Unit = {
    super.reset
    nperformed = 0
    gamma = (0.999).min(
      scala.math.sqrt(narms * scala.math.log(narms/k) / ((scala.math.E -1)*k*(stream.nbatches-nperformed))))
    Exp3Mweights = (0 until narms).map(x => BigDecimal(1.0)).toArray // initialize weights to 1
  }

  def next: (Array[(Int, Int)], Array[Double], Double) = {
    //val newWindow = stream.next.transpose // you have to transpose to make it columns oriented !!
    //if(newWindow.isEmpty) return (Array[(Int,Int)](), Array[Double](), 0) // return empty array and 0 gain if the stream is depleted

    // Step 1 and 2 at the same time
    def step1and2(weights: Array[BigDecimal], k: Int, gamma:Double): (Array[BigDecimal], Array[Int]) =  {
      val teta = (1.0/k - gamma/weights.size)/(1.0-gamma)

      val weightsum = weights.sum
      val maxweight = weights.max

      if(maxweight >= BigDecimal(teta)*weightsum) {
      def find_threshold(remainingweights: List[(BigDecimal, Int)], i:Int, S0: Array[Int], weightsum:BigDecimal): (BigDecimal, Array[Int]) = {
        val alphatest = if(1-i*teta == 0) BigDecimal(0) else (weightsum*BigDecimal(teta))/BigDecimal(1-i*teta)
        if(remainingweights.nonEmpty && alphatest < remainingweights.head._1) {
          find_threshold(remainingweights.tail, i+1, S0 :+ remainingweights.head._2, weightsum - remainingweights.head._1)
        } else {
          (alphatest, S0)
        }
      }
      val sortedweights = weights.zipWithIndex.sortBy(- _._1).toList // the larger weights first
      //println(s"Weights need correction, max weight = ${sortedweights(0)._1}, teta= $teta, weightsum=$weightsum")
      val thresholdfinding = find_threshold(sortedweights.tail, 1, Array[Int](sortedweights.head._2), weightsum-sortedweights.head._1)
      val alpha = thresholdfinding._1
      val S0 = thresholdfinding._2
      //println(s"I've set alpha = $alpha, S0 : ${S0 mkString ","}")
      (weights.map(x => if(x >= alpha) alpha else x), S0)
    } else {
      (weights,Array[Int]())
    }
    }

    val step1and2_result = step1and2(Exp3Mweights, k, gamma)

    // Step 3
    val temp_weights = step1and2_result._1
    val S0 = step1and2_result._2
    val temp_weightsum = temp_weights.sum
    val probabilities = temp_weights.map(x => {
      val p = if(x==0.0 || temp_weightsum==0.0) 0.0 else ((1-gamma)*(x/temp_weightsum)).toDouble
      k*(p + gamma/narms)
    })
    //println(s"Sum of probabilities: ${probabilities.sum} (should be equal to $k)")
    //println(s"probs: ${probabilities mkString ","}")

    // As suggested in https://stackoverflow.com/questions/8385458/how-to-compare-floating-point-values-in-scala
    case class Precision(val p:Double)
    implicit val precision = Precision(0.001)

    class withAlmostEquals(d:Double) {
      def ~=(d2:Double)(implicit p:Precision) = (d-d2).abs <= p.p
    }
    implicit def add_~=(d:Double) = new withAlmostEquals(d)

    // Step 4
    // draw the arms according to DepRound
    def DepRound(k: Int, prob:Array[Double]): Array[Int] = {
      if(prob.exists(x => x > 0.00001 & x < 0.99999)) {
        val draw = Random.shuffle(prob.zipWithIndex.filter(x => x._1 > 0.00001 & x._1 < 0.99999).toList).take(2)
        val pi = draw.head
        val pj = draw.last
        val alpha = (1-pi._1).min(pj._1)
        val beta = pi._1.min(1 - pj._1)
        if(math.random < beta/(alpha+beta)) {
          prob(pi._2) = pi._1 + alpha
          prob(pj._2) = pj._1 - alpha
        } else {
          prob(pi._2) = pi._1 - beta
          prob(pj._2) = pj._1 + beta
        }
        DepRound(k, prob)
      } else {
        probabilities.zipWithIndex.filter(_._1 ~= 1.0).map(_._2)
      }
    } //TODO: There is a bug in this function, and it does not always return k elements (should be fixed now)
    // Note: In case k==narms, using Depround does not make much sense (also, it will fail to return every arms)
    val indexes = if(k==narms) probabilities.indices.toArray else DepRound(k, probabilities)
    val arms = indexes.map(combinations(_))

    if(indexes.length != k) logger.info(s"$name: wrong number of pull. Expected: $k, Actual: ${indexes.length}")


    //Step 5
    //val newValues = arms.map(x => abs(action.compute(newWindow(x._1), newWindow(x._2))))
    val newValues = stream.nextAndCompute(indexes)
    if (newValues.isEmpty) return (Array[(Int, Int)](), Array[Double](), 0)

    val updates = scala.collection.mutable.Map[Int, Double]()

    // Update the current Matrix and compute the diff at the same time
    val gains = (indexes zip newValues).map(x => {
      //val d = abs(currentMatrix(x._1) - x._2) // compute absolute difference
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

    k = scalingstrategy.scale(gains, indexes, sums, counts, t)


    // Compute the rewards
    val rewards: Array[Double] = (0 until narms).map(x => 0.0).toArray
    (gains zip indexes).foreach{
      x => rewards(x._2) = x._1 / probabilities(x._2)
    }

    (0 until narms).foreach{x =>
      // Note: If I am not careful here I may end up with infinite value here. (may not happen in the case of auto gamma)
      if(!S0.contains(x)) Exp3Mweights(x) = Exp3Mweights(x)*math.exp(k*gamma*rewards(x)/narms)
    }
    //println(s"weights: ${weights mkString ","}")

    nperformed += 1
    if(stream.nbatches != nperformed) { // update gamma only if there is still at least one iteration !
      gamma = (0.999).min(
        scala.math.sqrt(narms * scala.math.log(narms/k) / ((scala.math.E -1)*k*(stream.nbatches-nperformed))))
    }

    // Here we, add up the size of the adwin (those are the number of pulls) and the number of unpulls, to get the
    // actual size of each pulled arm.
    val windows = (0 until narms).map(x => (x, sharedAdwin.getSingleSize(x) + (history.length-counts(x))))

    val smallest_window = windows.minBy(_._2) // this is the smallest window

    // Rolling back
    if(smallest_window._2.toInt < history.length) {
      //println(s"arm0, counts:${counts(0)}, sums:${sums(0)}")
      //println(s"arm99, counts:${counts(99)}, sums:${sums(99)}")
      //println("ROLLING BACK")
      for{
        x <- smallest_window._2.toInt until history.length
      } {
        val rollback = history.head
        history = history.tail
        for((key,value) <- rollback) {
          // weights(key) = weights(key) - value._3 // I am not sure that this is right to do it this way
          sums(key) = sums(key) - value // if(counts(key) == 1.0) 1.0 else weights(key) - (1.0/(counts(key)-1.0))*(value._1 - weights(key))
          counts(key) = counts(key) - 1 //- value._2
        }
      }
      //println(s"arm0, counts:${counts(0)}, sums:${sums(0)}")
      //println(s"arm99, counts:${counts(99)}, sums:${sums(99)}")
    }
    t = history.length // The time context is the same as the history, which is the same as the smallest window

    (arms, gains, gains.sum)
  }


}
