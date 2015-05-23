
package org.drip.spaces.tensor;

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
 * ContinuousRealMultidimensionalVector implements the normed/non-normed, bounded/unbounded Continuous
 * 	Multi-dimensional Real-valued R^d Vector Spaces.
 * 
 * The Reference we've used is:
 * 
 * 	- Carl, B., and I. Stephani (1990): Entropy, Compactness, and Approximation of Operators, Cambridge
 * 		University Press, Cambridge UK.
 *
 * @author Lakshmi Krishnamurthy
 */

public class ContinuousRealMultidimensionalVector extends
	org.drip.spaces.tensor.AggregatedMultidimensionalVectorSpace {

	/**
	 * Construct the Standard R^d ContinuousRealMultidimensionalVector Instance
	 * 
	 * @param iDimension The Space Dimension
	 * 
	 * @return The Standard R^d ContinuousRealMultidimensionalVector Instance
	 */

	public static final ContinuousRealMultidimensionalVector Standard (
		final int iDimension)
	{
		try {
			return 0 >= iDimension ? null : new ContinuousRealMultidimensionalVector (new
				org.drip.spaces.tensor.ContinuousRealUnidimensionalVector[iDimension]);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * ContinuousRealMultidimensionalVector Constructor
	 * 
	 * @param aCRUV Array of the Continuous Uni-dimensional Real Valued Vector Spaces
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are Invalid
	 */

	public ContinuousRealMultidimensionalVector (
		final org.drip.spaces.tensor.ContinuousRealUnidimensionalVector[] aCRUV)
		throws java.lang.Exception
	{
		super (aCRUV);
	}

	@Override public double[] leftDimensionEdge()
	{
		org.drip.spaces.tensor.GeneralizedUnidimensionalVectorSpace[] aGUVS = vectorSpaces();

		int iDimension = aGUVS.length;
		double[] adblLeftEdge = new double[iDimension];

		for (int i = 0; i < iDimension; ++i)
			adblLeftEdge[i] = ((org.drip.spaces.tensor.ContinuousRealUnidimensionalVector)
				aGUVS[i]).leftEdge();

		return adblLeftEdge;
	}

	@Override public double[] rightDimensionEdge()
	{
		org.drip.spaces.tensor.GeneralizedUnidimensionalVectorSpace[] aGUVS = vectorSpaces();

		int iDimension = aGUVS.length;
		double[] adblRightEdge = new double[iDimension];

		for (int i = 0; i < iDimension; ++i)
			adblRightEdge[i] = ((org.drip.spaces.tensor.ContinuousRealUnidimensionalVector)
				aGUVS[i]).rightEdge();

		return adblRightEdge;
	}

	@Override public org.drip.spaces.tensor.Cardinality cardinality()
	{
		return org.drip.spaces.tensor.Cardinality.UncountablyInfinite();
	}

	@Override public double leftEdge()
	{
		double[] adblLeftEdge = leftDimensionEdge();

		int iDimension = adblLeftEdge.length;
		double dblLeftEdge = adblLeftEdge[0];

		for (int i = 1; i < iDimension; ++i) {
			if (dblLeftEdge > adblLeftEdge[i]) dblLeftEdge = adblLeftEdge[i];
		}

		return dblLeftEdge;
	}

	@Override public double rightEdge()
	{
		double[] adblRightEdge = rightDimensionEdge();

		int iDimension = adblRightEdge.length;
		double dblRightEdge = adblRightEdge[0];

		for (int i = 1; i < iDimension; ++i) {
			if (dblRightEdge < adblRightEdge[i]) dblRightEdge = adblRightEdge[i];
		}

		return dblRightEdge;
	}

	@Override public double hyperVolume()
		throws java.lang.Exception
	{
		if (!isPredictorBounded())
			throw new java.lang.Exception
				("ContinuousRealMultidimensionalVector::hyperVolume => Space not Bounded");

		double[] adblLeftEdge = leftDimensionEdge();

		double dblHyperVolume = 1.;
		int iDimension = adblLeftEdge.length;

		double[] adblRightEdge = rightDimensionEdge();

		for (int i = 0; i < iDimension; ++i)
			dblHyperVolume *= (adblRightEdge[i] - adblLeftEdge[i]);

		return dblHyperVolume;
	}
}
