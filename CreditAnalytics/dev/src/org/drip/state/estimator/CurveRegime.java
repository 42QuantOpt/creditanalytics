
package org.drip.state.estimator;

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
 * CurveRegime expands the normal Multi-Segment Regime by implementing the calibrated Regime for
 *  Boot-strapped Instruments. 
 *
 * @author Lakshmi Krishnamurthy
 */

public class CurveRegime extends org.drip.spline.regime.CalibratableMultiSegmentSequence {
	private double _dblBuiltPredictorOrdinateRight = java.lang.Double.NaN;

	/**
	 * CurveRegime constructor - Construct a sequence of Basis Spline Segments
	 * 
	 * @param strName Name of the Regime
	 * @param aPR Array of Segments
	 * @param aPRBP Array of Segment Builder Parameters
	 * 
	 * @throws java.lang.Exception Thrown if the inputs are invalid
	 */

	public CurveRegime (
		final java.lang.String strName,
		final org.drip.spline.segment.ElasticConstitutiveState[] aPR,
		final org.drip.spline.params.SegmentCustomBuilderControl[] aPRBP)
		throws java.lang.Exception
	{
		super (strName, aPR, aPRBP);

		_dblBuiltPredictorOrdinateRight = getLeftPredictorOrdinateEdge();
	}

	/**
	 * Mark the Range of the "built" Segments
	 * 
	 * @param iSegment The Current Segment Range Built
	 * 
	 * @return TRUE => Range successfully marked as "built"
	 */

	public boolean setSegmentBuilt (
		final int iSegment)
	{
		org.drip.spline.segment.ElasticConstitutiveState[] aSegment = segments();

		if (iSegment >= aSegment.length) return false;

		_dblBuiltPredictorOrdinateRight = aSegment[iSegment].right();

		return true;
	}

	/**
	 * Clear the built range mark to signal the start of a fresh calibration run
	 * 
	 * @return TRUE => Built Range successfully cleared
	 */

	public boolean setClearBuiltRange()
	{
		_dblBuiltPredictorOrdinateRight = getLeftPredictorOrdinateEdge();

		return true;
	}

	/**
	 * Indicate if the specified Predictor Ordinate is inside the "Built" Range
	 * 
	 * @param dblPredictorOrdinate The Predictor Ordinate
	 * 
	 * @return TRUE => The specified Predictor Ordinate is inside the "Built" Range
	 * 
	 * @throws java.lang.Exception Thrown if inputs are invalid
	 */

	public boolean inBuiltRange (
		final double dblPredictorOrdinate)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblPredictorOrdinate))
			throw new java.lang.Exception ("CurveRegime.inBuiltRange => Invalid Inputs");

		return dblPredictorOrdinate >= getLeftPredictorOrdinateEdge() && dblPredictorOrdinate <=
			_dblBuiltPredictorOrdinateRight;
	}
}
