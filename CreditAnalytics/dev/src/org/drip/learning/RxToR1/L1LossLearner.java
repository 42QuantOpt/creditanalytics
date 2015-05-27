
package org.drip.learning.RxToR1;

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
 * L1LossLearner implements the Learner Class that holds the Space of Normed R^x -> Normed R^1 Learning
 * 	Functions that employs L1 Empirical Loss Routine. Class-Specific Asymptotic Sample, Covering Number based
 *  Upper Probability Bounds and other Parameters are also maintained.
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
 *  5) Vapnik, V. N. (1998): Statistical Learning Theory, Wiley, New York.
 *
 * @author Lakshmi Krishnamurthy
 */

public class L1LossLearner extends org.drip.learning.RxToR1.GeneralizedLearner {
	private org.drip.learning.bound.MeasureConcentrationExpectationBound _cleb = null;

	/**
	 * L1LossLearner Constructor
	 * 
	 * @param funcClassRxToR1 R^x -> R^1 Function Class
	 * @param cdpb The Covering Number based Deviation Upper Probability Bound Generator
	 * @param cleb The Concentration of Measure based Loss Expectation Upper Bound Evaluator
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are Invalid
	 */

	public L1LossLearner (
		final org.drip.spaces.functionclass.NormedRxToNormedR1Finite funcClassRxToR1,
		final org.drip.learning.bound.CoveringNumberLossBound cdpb,
		final org.drip.learning.bound.MeasureConcentrationExpectationBound cleb)
		throws java.lang.Exception
	{
		super (funcClassRxToR1, cdpb);

		if (null == (_cleb = cleb)) throw new java.lang.Exception ("L1LossLearner ctr: Invalid Inputs");
	}

	/**
	 * Retrieve the Concentration of Measure based Loss Expectation Upper Bound Evaluator Instance
	 * 
	 * @return The Concentration of Measure based Loss Expectation Upper Bound Evaluator Instance
	 */

	public org.drip.learning.bound.MeasureConcentrationExpectationBound concentrationLossBoundEvaluator()
	{
		return _cleb;
	}

	@Override public double lossSampleCoveringNumber (
		final org.drip.spaces.instance.GeneralizedValidatedVectorInstance gvvi,
		final double dblEpsilon,
		final boolean bSupremum)
		throws java.lang.Exception
	{
		org.drip.spaces.functionclass.NormedRxToNormedR1Finite funcClassRxToR1 = functionClass();

		return bSupremum ? funcClassRxToR1.sampleSupremumCoveringNumber (gvvi, dblEpsilon) :
			funcClassRxToR1.sampleCoveringNumber (gvvi, dblEpsilon);
	}

	@Override public double empiricalLoss (
		final org.drip.function.deterministic.R1ToR1 funcLearnerR1ToR1,
		final double dblX,
		final double dblY)
		throws java.lang.Exception
	{
		if (null == funcLearnerR1ToR1 || !org.drip.quant.common.NumberUtil.IsValid (dblX) ||
			!org.drip.quant.common.NumberUtil.IsValid (dblY))
			throw new java.lang.Exception ("L1LossLearner::empiricalLoss => Invalid Inputs");

		return java.lang.Math.abs (funcLearnerR1ToR1.evaluate (dblX) - dblY);
	}

	@Override public double empiricalLoss (
		final org.drip.function.deterministic.RdToR1 funcLearnerRdToR1,
		final double[] adblX,
		final double dblY)
		throws java.lang.Exception
	{
		if (null == funcLearnerRdToR1 || null == adblX || 0 == adblX.length ||
			!org.drip.quant.common.NumberUtil.IsValid (dblY))
			throw new java.lang.Exception ("L1LossLearner::empiricalLoss => Invalid Inputs");

		return java.lang.Math.abs (funcLearnerRdToR1.evaluate (adblX) - dblY);
	}
}
