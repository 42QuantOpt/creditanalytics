
package org.drip.learning.kernel;

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
 * EigenFunction holds the Eigen-vector Function and its corresponding Space of the R^x L2 -> R^x L2 Kernel
 *  Linear Integral Operator defined by:
 * 
 * 		T_k [f(.)] := Integral Over Input Space {k (., y) * f(y) * d[Prob(y)]}
 *  
 *  The References are:
 *  
 *  1) Ash, R. (1965): Information Theory, Inter-science New York.
 *  
 *  2) Konig, H. (1986): Eigenvalue Distribution of Compact Operators, Birkhauser, Basel, Switzerland. 
 *  
 *  3) Smola, A. J., A. Elisseff, B. Scholkopf, and R. C. Williamson (2000): Entropy Numbers for Convex
 *  	Combinations and mlps, in: Advances in Large Margin Classifiers, A. Smola, P. Bartlett, B. Scholkopf,
 *  	and D. Schuurmans - editors, MIT Press, Cambridge, MA.
 *
 * @author Lakshmi Krishnamurthy
 */

public abstract class EigenFunction extends org.drip.spaces.RxToR1.NormedRdToNormedR1 {
	private double _dblAgnosticUpperBound = java.lang.Double.NaN;

	protected EigenFunction (
		final org.drip.spaces.metric.RealMultidimensionalNormedSpace rmnsInput,
		final org.drip.spaces.metric.RealUnidimensionalNormedSpace runsOutput,
		final org.drip.function.deterministic.RdToR1 funcRdToR1,
		final double dblAgnosticUpperBound)
		throws java.lang.Exception
	{
		super (rmnsInput, runsOutput, funcRdToR1);

		if (!org.drip.quant.common.NumberUtil.IsValid (_dblAgnosticUpperBound = dblAgnosticUpperBound))
			throw new java.lang.Exception ("EigenFunction ctr: Invalid Inputs");
	}

	/**
	 * Retrieve the Agnostic Upper Bound of the Eigen-Function
	 * 
	 * @return The Agnostic Upper Bound of the Eigen-Function
	 */

	public double agnosticUpperBound()
	{
		return _dblAgnosticUpperBound;
	}
}
