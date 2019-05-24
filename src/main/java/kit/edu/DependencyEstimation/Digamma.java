/*
 * Copyright (C) 2017 Hendrik Braun
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
package kit.edu.DependencyEstimation;

public final class Digamma {
    //C is the Euler-Mascheronie Constant
    private static final double C = 0.57721566490153286060;
    private static final int OPTIMIZEBORDER = 913;
    private final double EPSILON;

    public Digamma(double eps) {
        EPSILON = eps;
    }

    /**
     * this function calculates the so called digamma function
     * it satisfies the recursion digamma(x+1) = digamma(x) + (1/x)
     *
     * @param x is the value of which we want to calculate the digamma function
     * @return the result of digamma(x)
     */
    public static double digamma(double x) {
        double digammaX = -C;
        for (double i = 1; i < x; i++) {
            digammaX += 1 / i;
        }
        return digammaX;
    }

    /**
     * this function might come in handy, as it takes quite a while to calculate
     * the digamma function, it is said, that digamma(x) ~ digammaEstimate(x) for large values
     *
     * @param x is the value of which we want to etimate the digamma function
     * @return the estimated result of digamma(x)
     */
    public static double digammaEstimate(double x) {
        return Math.log(x) - 1 / (2 * x);
    }

    public static double digammaOpt(int x) {
        if (x >= OPTIMIZEBORDER) {
            return digammaEstimate(x);
        } else {
            return digamma(x);
        }
    }

    public int digammaOptimizer() {
        int x = 0;
        while (Math.abs(digamma(x) - digammaEstimate(x)) > this.EPSILON) {
            x++;
        }
        return x;
    }
}
