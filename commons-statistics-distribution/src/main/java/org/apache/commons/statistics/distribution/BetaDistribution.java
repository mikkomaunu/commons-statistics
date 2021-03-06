/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.statistics.distribution;

import org.apache.commons.numbers.gamma.RegularizedBeta;
import org.apache.commons.numbers.gamma.LogGamma;
import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.sampling.distribution.ChengBetaSampler;

/**
 * Implementation of the <a href="http://en.wikipedia.org/wiki/Beta_distribution">Beta distribution</a>.
 */
public class BetaDistribution extends AbstractContinuousDistribution {
    /** First shape parameter. */
    private final double alpha;
    /** Second shape parameter. */
    private final double beta;
    /** Normalizing factor used in density computations.*/
    private final double z;

    /**
     * Creates a new instance.
     *
     * @param alpha First shape parameter (must be positive).
     * @param beta Second shape parameter (must be positive).
     */
    public BetaDistribution(double alpha,
                            double beta) {
        this.alpha = alpha;
        this.beta = beta;
        z = LogGamma.value(alpha) + LogGamma.value(beta) - LogGamma.value(alpha + beta);
    }

    /**
     * Access the first shape parameter, {@code alpha}.
     *
     * @return the first shape parameter.
     */
    public double getAlpha() {
        return alpha;
    }

    /**
     * Access the second shape parameter, {@code beta}.
     *
     * @return the second shape parameter.
     */
    public double getBeta() {
        return beta;
    }

    /** {@inheritDoc} */
    @Override
    public double density(double x) {
        final double logDensity = logDensity(x);
        return logDensity == Double.NEGATIVE_INFINITY ? 0 : Math.exp(logDensity);
    }

    /** {@inheritDoc} **/
    @Override
    public double logDensity(double x) {
        if (x < 0 ||
            x > 1) {
            return Double.NEGATIVE_INFINITY;
        } else if (x == 0) {
            if (alpha < 1) {
                throw new DistributionException(DistributionException.TOO_SMALL,
                                                alpha, 1.0);
            }
            return Double.NEGATIVE_INFINITY;
        } else if (x == 1) {
            if (beta < 1) {
                throw new DistributionException(DistributionException.TOO_SMALL,
                                                beta, 1.0);
            }
            return Double.NEGATIVE_INFINITY;
        } else {
            final double logX = Math.log(x);
            final double log1mX = Math.log1p(-x);
            return (alpha - 1) * logX + (beta - 1) * log1mX - z;
        }
    }

    /** {@inheritDoc} */
    @Override
    public double cumulativeProbability(double x)  {
        if (x <= 0) {
            return 0;
        } else if (x >= 1) {
            return 1;
        } else {
            return RegularizedBeta.value(x, alpha, beta);
        }
    }

    /**
     * {@inheritDoc}
     *
     * For first shape parameter {@code alpha} and second shape parameter
     * {@code beta}, the mean is {@code alpha / (alpha + beta)}.
     */
    @Override
    public double getMean() {
        final double a = getAlpha();
        return a / (a + getBeta());
    }

    /**
     * {@inheritDoc}
     *
     * For first shape parameter {@code alpha} and second shape parameter
     * {@code beta}, the variance is
     * {@code (alpha * beta) / [(alpha + beta)^2 * (alpha + beta + 1)]}.
     */
    @Override
    public double getVariance() {
        final double a = getAlpha();
        final double b = getBeta();
        final double alphabetasum = a + b;
        return (a * b) / ((alphabetasum * alphabetasum) * (alphabetasum + 1));
    }

    /**
     * {@inheritDoc}
     *
     * The lower bound of the support is always 0 no matter the parameters.
     *
     * @return lower bound of the support (always 0)
     */
    @Override
    public double getSupportLowerBound() {
        return 0;
    }

    /**
     * {@inheritDoc}
     *
     * The upper bound of the support is always 1 no matter the parameters.
     *
     * @return upper bound of the support (always 1)
     */
    @Override
    public double getSupportUpperBound() {
        return 1;
    }

    /**
     * {@inheritDoc}
     *
     * The support of this distribution is connected.
     *
     * @return {@code true}
     */
    @Override
    public boolean isSupportConnected() {
        return true;
    }

    /**
     * {@inheritDoc}
     *
     * Sampling is performed using Cheng's algorithm:
     * <blockquote>
     * <pre>
     * R. C. H. Cheng,
     * "Generating beta variates with nonintegral shape parameters",
     * Communications of the ACM, 21, 317-322, 1978.
     * </pre>
     * </blockquote>
     */
    @Override
    public ContinuousDistribution.Sampler createSampler(final UniformRandomProvider rng) {
        // Beta distribution sampler.
        return new ChengBetaSampler(rng, alpha, beta)::sample;
    }
}
