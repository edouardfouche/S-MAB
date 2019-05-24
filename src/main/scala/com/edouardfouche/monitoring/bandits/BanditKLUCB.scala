package com.edouardfouche.monitoring.bandits

/**
  * General trait for bandits based on KL-UCB
  */
trait BanditKLUCB extends Bandit {
  val Ndelta: Double = scala.math.pow(10,-8)
  val eps: Double = scala.math.pow(10,-12)
  val maxiter = 20

  // Note to future self: this was a big mistake, as operator ^ in scala does not mean at all "power
  //val delta = 1*10^(-8) // -> give -14
  //val eps = 1*10^(-12)
  // calculate the kl-divergence
  def kl(p: Double, q: Double): Double = {
    p * scala.math.log(p/q) + (1-p)*scala.math.log((1-p)/(1-q))
  }
  // calculate the derivative kl-divergence
  def dkl(p: Double , q: Double): Double = {
    (q-p) / (q * (1.0 - q))
  }
  // use Newton's method
  def getKLUCBupper(arm: Int, t: Double): Double = {
    val logndn = scala.math.log(t) / counts(arm) // alternative: KL-UCB+ scala.math.log(t/ counts(arm)) / counts(arm)
    val p: Double = (sums(arm)/counts(arm)).max(Ndelta)
    if(p >= 1.0) return 1.0

    var q = p + Ndelta
    for(i <- 1 to maxiter) {
      val f = logndn - kl(p,q)
      val df = -dkl(p,q)
      if(f*f < eps) return q // newton's method has converged
      q = (1.0-Ndelta).min((q - f/df).max(p+Ndelta))
    }
    q
  }

}
