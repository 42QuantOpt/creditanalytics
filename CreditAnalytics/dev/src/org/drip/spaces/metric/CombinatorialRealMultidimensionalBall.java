
package org.drip.spaces.metric;

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
 * CombinatorialRealMultidimensionalBall extends the Combinatorial R^d Banach Space by enforcing the Closed
 *  Bounded Metric.
 * 
 * The Reference we've used is:
 * 
 * 	- Carl, B., and I. Stephani (1990): Entropy, Compactness, and Approximation of Operators, Cambridge
 * 		University Press, Cambridge UK.
 *
 * @author Lakshmi Krishnamurthy
 */

public class CombinatorialRealMultidimensionalBall extends
	org.drip.spaces.metric.CombinatorialRealMultidimensionalBanach {
	private double _dblNormRadius = java.lang.Double.NaN;

	/**
	 * Construct a CombinatorialRealMultidimensionalBall Instance of Unit Radius
	 * 
	 * @param aCRU Array of Combinatorial Real Valued Multidimensional Vector Spaces
	 * @param multiDist The Multivariate Borel Sigma Measure
	 * @param iPNorm The p-norm of the Space
	 * 
	 * @return CombinatorialRealMultidimensionalBall Instance of Unit Radius
	 */

	public static final CombinatorialRealMultidimensionalBall ClosedUnit (
		final org.drip.spaces.tensor.CombinatorialRealUnidimensionalVector[] aCRU,
		final org.drip.measure.continuous.Rd multiDist,
		final int iPNorm)
	{
		try {
			return new CombinatorialRealMultidimensionalBall (aCRU, multiDist, iPNorm, 1.);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * CombinatorialRealMultidimensionalBall Constructor
	 * 
	 * @param aCRU Array of Combinatorial Real Valued Unidimensional Vector Spaces
	 * @param multiDist The Multivariate Borel Sigma Measure
	 * @param iPNorm The p-norm of the Space
	 * @param dblNormRadius Radius Norm of the Unit Ball
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are Invalid
	 */

	public CombinatorialRealMultidimensionalBall (
		final org.drip.spaces.tensor.CombinatorialRealUnidimensionalVector[] aCRU,
		final org.drip.measure.continuous.Rd multiDist,
		final int iPNorm,
		final double dblNormRadius)
		throws java.lang.Exception
	{
		super (aCRU, multiDist, iPNorm);

		if (!org.drip.quant.common.NumberUtil.IsValid (_dblNormRadius = dblNormRadius) || 0. >=
			_dblNormRadius)
			throw new java.lang.Exception ("CombinatorialRealMultidimensionalBall Constructor: Invalid Inputs");
	}

	/**
	 * Retrieve the Radius Norm
	 * 
	 * @return The Radius Norm
	 */

	public double normRadius()
	{
		return _dblNormRadius;
	}

	@Override public boolean validateInstance (
		final double[] adblInstance)
	{
		try {
			return super.validateInstance (adblInstance) && _dblNormRadius <= sampleMetricNorm
				(adblInstance);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return false;
	}
}
