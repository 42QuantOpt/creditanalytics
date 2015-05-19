
package org.drip.learning.lossFamily;

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
 * NormedR1NormedR1ApproximateLipschitz implements the Learner Class that holds the Space of Normed R^1 ->
 *  Normed R^1 Learning Functions for the Family of Loss Functions that are "approximately" Lipschitz, i.e.,
 * 
 * 				loss (ep) - loss (ep') <= max (C * |ep-ep'|, C')
 *  
 * The References are:
 *  
 *  1) Alon, N., S. Ben-David, N. Cesa Bianchi, and D. Haussler (1997): Scale-sensitive Dimensions, Uniform
 *  	Convergence, and Learnability, Journal of Association of Computational Machinery, 44 (4) 615-631.
 * 
 *  2) Anthony, M., and P. L. Bartlett (1999): Artificial Neural Network Learning - Theoretical Foundations,
 *  	Cambridge University Press, Cambridge, UK.
 *  
 *  3) Kearns, M. J., R. E. Schapire, and L. M. Sellie (1994): Towards Efficient Agnostic Learning, Machine
 *  	Learning, 17 (2) 115-141.
 *  
 *  4) Lee, W. S., P. L. Bartlett, and R. C. Williamson (1998): The Importance of Convexity in Learning with
 *  	Squared Loss, IEEE Transactions on Information Theory, 44 1974-1980.
 * 
 *  5) Vapnik, V. N. (1998): Statistical learning Theory, Wiley, New York.
 *
 * @author Lakshmi Krishnamurthy
 */

public abstract class NormedR1NormedR1ApproximateLipschitz extends
	org.drip.learning.lossFamily.NormedR1NormedR1Lipschitz {
	private double _dblLipschitzFloor = java.lang.Double.NaN;

	/**
	 * NormedR1NormedR1ApproximateLipschitz Constructor
	 * 
	 * @param aR1ToR1Learner Array of Candidate Learning Functions belonging to the Function Class
	 * @param cdpb The Covering Number based Deviation Upper Probability Bound Generator
	 * @param cleb The Concentration of Measure based Loss Expectation Upper Bound Evaluator
	 * @param dblLipschitzSlope The Lipschitz Slope Bound
	 * @param dblLipschitzFloor The Lipschitz Floor Bound
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are Invalid
	 */

	public NormedR1NormedR1ApproximateLipschitz (
		final org.drip.spaces.RxToR1.NormedR1ToNormedR1[] aR1ToR1Learner,
		final org.drip.learning.loss.CoveringNumberProbabilityBound cdpb,
		final org.drip.learning.loss.MeasureConcentrationExpectationBound cleb,
		final double dblLipschitzSlope,
		final double dblLipschitzFloor)
		throws java.lang.Exception
	{
		super (aR1ToR1Learner, cdpb, cleb, dblLipschitzSlope);

		if (!org.drip.quant.common.NumberUtil.IsValid (_dblLipschitzFloor = dblLipschitzFloor))
			throw new java.lang.Exception ("NormedR1NormedR1ApproximateLipschitz ctr: Invalid Inputs");
	}

	/**
	 * Retrieve the Lipschitz Floor
	 * 
	 * @return The Lipschitz Floor
	 */

	public double lipschitzFloor()
	{
		return _dblLipschitzFloor;
	}

	@Override public double lossSampleCoveringNumber (
		final org.drip.spaces.instance.GeneralizedValidatedVectorInstance gvvi,
		final double dblEpsilon,
		final boolean bSupremum)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblEpsilon) || dblEpsilon <= (_dblLipschitzFloor /
			lipschitzSlope()))
			throw new java.lang.Exception
				("NormedR1NormedR1ApproximateLipschitz::lossSampleCoveringNumber => Invalid Inputs");

		return super.lossSampleCoveringNumber (gvvi, dblEpsilon, bSupremum);
	}
}
