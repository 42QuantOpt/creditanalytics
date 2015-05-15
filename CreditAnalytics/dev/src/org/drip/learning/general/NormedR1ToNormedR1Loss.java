
package org.drip.learning.general;

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
 * NormedR1ToNormedR1Learner implements the Class that holds the Space of Normed R^1 -> Normed R^1 Learning
 * 	Functions. Class-Specific Asymptotic Sample, Covering-Number based Upper Probability Bounds and other
 * 	Parameters are also maintained.
 *  
 * The Reference are:
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


public abstract class NormedR1ToNormedR1Loss extends org.drip.spaces.functionclass.NormedR1ToNormedR1Class {
	private org.drip.learning.general.NormedR1ToNormedR1Learner _learnerClass = null;

	protected NormedR1ToNormedR1Loss (
		final org.drip.learning.general.NormedR1ToNormedR1Learner learnerClass)
		throws java.lang.Exception
	{
		super ((org.drip.spaces.RxToR1.NormedR1ToNormedR1[]) learnerClass.functionSpaces());

		_learnerClass = learnerClass;
	}

	/**
	 * Retrieve the Learner Class Instance
	 * 
	 * @return The Learner Class Instance
	 */

	public org.drip.learning.general.NormedR1ToNormedR1Learner learnerClass()
	{
		return _learnerClass;
	}
}
