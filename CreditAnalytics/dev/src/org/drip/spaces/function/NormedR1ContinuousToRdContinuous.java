
package org.drip.spaces.function;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2015 Lakshmi Krishnamurthy
 * 
 *  This file is part of DRIP, a free-software/open-source library for fixed income analysts and developers -
 * 		http://www.credit-trader.org/Begin.html
 * 
 *  DRIP is a free, full featured, fixed income rates, credit, and FX analytics library with a focus towards
 *  	pricing/valuation, risk, and market making.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *   	you may not use this file except in compliance with the License.
 *   
 *  You may obtain a copy of the License at
 *  	http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  	distributed under the License is distributed on an "AS IS" BASIS,
 *  	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  
 *  See the License for the specific language governing permissions and
 *  	limitations under the License.
 */

/**
 * NormedRdContinuousToRdContinuous implements the f : Validated R^d Continuous -> Validated R^d Continuous
 *  Normed Function Spaces.
 * 
 * The Reference we've used is:
 * 
 * 	- Carl, B., and I. Stephani (1990): Entropy, Compactness, and Approximation of Operators, Cambridge
 * 		University Press, Cambridge UK.
 *
 * @author Lakshmi Krishnamurthy
 */

public class NormedR1ContinuousToRdContinuous extends org.drip.spaces.function.NormedR1ToRd {

	/**
	 * NormedR1ContinuousToRdContinuous Function Space Constructor
	 * 
	 * @param funcR1ToRd The R1ToRd Function
	 * @param cruvInput The R^1 Input Vector Space (may/may not be Normed)
	 * @param crmvOutput The R^d Output Vector Space (may/may not be Normed)
	 * @param iPNorm The Function-level Norm
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are Invalid
	 */

	public NormedR1ContinuousToRdContinuous (
		final org.drip.function.deterministic.R1ToRd funcR1ToRd,
		final org.drip.spaces.tensor.ContinuousRealUnidimensionalVector cruvInput,
		final org.drip.spaces.tensor.ContinuousRealMultidimensionalVector crmvOutput,
		final int iPNorm)
		throws java.lang.Exception
	{
		super (cruvInput, crmvOutput, funcR1ToRd, iPNorm);
	}

	@Override public double[] populationRdMetricNorm()
	{
		org.drip.spaces.tensor.GeneralizedUnidimensionalVectorSpace guvsInput = input();

		if (!(guvsInput instanceof org.drip.spaces.metric.ContinuousRealUnidimensional)) return null;

		org.drip.spaces.metric.ContinuousRealUnidimensional cru =
			(org.drip.spaces.metric.ContinuousRealUnidimensional) guvsInput;

		final org.drip.measure.continuous.UnivariateDistribution uniDist = cru.borelSigmaMeasure();

		final org.drip.function.deterministic.R1ToRd funcR1ToRd = function();

		if (null == uniDist) return null;

		final int iPNorm = pNorm();

		org.drip.function.deterministic.R1ToRd funcR1ToRdPointNorm = new
			org.drip.function.deterministic.R1ToRd (null) {
			@Override public double[] evaluate (
				final double dblX)
			{
				double[] adblNorm = funcR1ToRd.evaluate (dblX);

				if (null == adblNorm) return null;

				int iOutputDimension = adblNorm.length;
				double dblProbabilityDensity = java.lang.Double.NaN;

				if (0 == iOutputDimension) return null;

				try {
					dblProbabilityDensity = uniDist.density (dblX);
				} catch (java.lang.Exception e) {
					e.printStackTrace();

					return null;
				}

				for (int j = 0; j < iOutputDimension; ++j)
					adblNorm[j] = dblProbabilityDensity * java.lang.Math.pow (java.lang.Math.abs
						(adblNorm[j]), iPNorm);

				return adblNorm;
			}
		};

		double[] adblPopulationRdMetricNorm = funcR1ToRdPointNorm.integrate (cru.leftEdge(),
			cru.rightEdge());

		if (null == adblPopulationRdMetricNorm) return null;

		int iOutputDimension = adblPopulationRdMetricNorm.length;

		if (0 == iOutputDimension) return null;

		for (int i = 0; i < iOutputDimension; ++i)
			adblPopulationRdMetricNorm[i] = java.lang.Math.pow (adblPopulationRdMetricNorm[i], 1. / iPNorm);

		return adblPopulationRdMetricNorm;
	}
}
