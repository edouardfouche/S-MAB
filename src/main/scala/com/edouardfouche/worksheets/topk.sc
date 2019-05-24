// Some idea to do better than sorting then take for finding the top-k elements?

import breeze.stats.distributions.Gaussian

def time[R](block: => R): R = {
  val t0 = System.nanoTime()
  val result = block    // call-by-name
  val t1 = System.nanoTime()
  println("Elapsed time: " + (t1 - t0) + "ns")
  result
}

// idea from https://stackoverflow.com/questions/5674741/simplest-way-to-get-the-top-n-elements-of-a-scala-iterable
implicit def iterExt[A](iter: Iterable[A]) = new {
  def top[B](n: Int, f: A => B)(implicit ord: Ordering[B]): List[A] = {
    def updateSofar (sofar: List [A], el: A): List [A] = {
      //println (el + " - " + sofar)

      if (ord.compare(f(el), f(sofar.head)) > 0)
        (el :: sofar.tail).sortBy (f)
      else sofar
    }

    val (sofar, rest) = iter.splitAt(n)
    (sofar.toList.sortBy (f) /: rest) (updateSofar (_, _)).reverse
  }
}

// Turns out that is not faster
val b = new Gaussian(mu = 0, sigma=1)
val a = (1 to 100).map(x => (x, b.draw()))

time{a.top(80, _._2)}
time{a.sortBy(-_._2).take(80)}

time{a.top(10, _._2)}
time{a.sortBy(-_._2).take(10)}