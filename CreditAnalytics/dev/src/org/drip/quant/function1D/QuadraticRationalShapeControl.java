
package org.drip.quant.function1D;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2013 Lakshmi Krishnamurthy
 * 
 * This file is part of CreditAnalytics, a free-software/open-source library for fixed income analysts and
 * 		developers - http://www.credit-trader.org
 * 
 * CreditAnalytics is a free, full featured, fixed income credit analytics library, developed with a special
 * 		focus towards the needs of the bonds and credit products community.
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
 * QuadraticRationalShapeControl implements the deterministic rational shape control functionality on top of
 *  the estimator basis splines inside - [0,...,1) - Globally [x_0,...,x_1):
 * 
 * 			y = 1 / [1 + lambda * x * (1-x)]
 * 
 *		where is the normalized ordinate mapped as
 * 
 * 			x => (x - x_i-1) / (x_i - x_i-1)
 * 
 * @author Lakshmi Krishnamurthy
 */

public class QuadraticRationalShapeControl extends org.drip.quant.function1D.AbstractUnivariate {
	private double _dblLambda = java.lang.Double.NaN;

	/**
	 * QuadraticRationalShapeControl constructor
	 * 
	 * @param dblLambda Tension Parameter
	 * 
	 * @throws java.lang.Exception Thrown if the inputs are invalid
	 */

	public QuadraticRationalShapeControl (
		final double dblLambda)
		throws java.lang.Exception
	{
		super (null);

		if (!org.drip.quant.common.NumberUtil.IsValid (_dblLambda = dblLambda))
			throw new java.lang.Exception ("QuadraticRationalShapeControl ctr: Invalid tension");
	}

	@Override public double evaluate (
		final double dblX)
		throws java.lang.Exception
	{
		return 1. / (1. + _dblLambda * dblX * (1. - dblX));
	}

	@Override public double calcDerivative (
		final double dblX,
		final int iOrder)
		throws java.lang.Exception
	{
		if (3 <= iOrder) return super.calcDerivative (dblX, iOrder);

		double dblD2BetaDX2 = -2. * _dblLambda;
		double dblDBetaDX = _dblLambda * (1. - 2. * dblX);
		double dblBeta = 1. + _dblLambda * dblX * (1. - dblX);
		return 1 == iOrder ? -1. * dblDBetaDX / (dblBeta * dblBeta) : (2. * dblDBetaDX - dblBeta *
			dblD2BetaDX2) / (dblBeta * dblBeta * dblBeta);
	}

	/**
	 * Retrieve the shape control coefficient
	 * 
	 * @return Shape control coefficient
	 */

	public double getShapeControlCoefficient()
	{
		return _dblLambda;
	}
}
