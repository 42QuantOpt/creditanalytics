
package org.drip.analytics.definition;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2014 Lakshmi Krishnamurthy
 * Copyright (C) 2013 Lakshmi Krishnamurthy
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
 * ShapePreservingCCIS extends the CurveSpanConstructionInput Instance. Additionally, it exposes the Shape
 *  Preserving Linear Curv Calibrator.
 *
 * @author Lakshmi Krishnamurthy
 */

public class ShapePreservingCCIS extends org.drip.analytics.definition.CurveSpanConstructionInput {
	private org.drip.state.estimator.LinearCurveCalibrator _lccShapePreserving = null;

	/**
	 * ShapePreservingCCIS constructor
	 * 
	 * @param lccShapePreserving Shape Preserving LinearCurveCalibrator instance
	 * @param aSRS Array of Stretch Representations
	 * @param valParam Valuation Parameters
	 * @param pricerParam Pricer Parameters
	 * @param quotingParam Quoting Parameters
	 * @param cmp Component Market Parameters
	 * 
	 * @throws java.lang.Exception Thrown if Inputs are invalid
	 */

	public ShapePreservingCCIS (
		final org.drip.state.estimator.LinearCurveCalibrator lccShapePreserving,
		final org.drip.state.estimator.StretchRepresentationSpec[] aSRS,
		final org.drip.param.valuation.ValuationParams valParam,
		final org.drip.param.pricer.PricerParams pricerParam,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParam,
		final org.drip.param.definition.ComponentMarketParams cmp)
		throws java.lang.Exception
	{
		super (aSRS, valParam, pricerParam, quotingParam, cmp);

		if (null == (_lccShapePreserving = lccShapePreserving))
			throw new java.lang.Exception ("ShapePreservingCCIS ctr: Invalid Inputs");
	}

	@Override public org.drip.state.estimator.LinearCurveCalibrator lcc()
	{
		return _lccShapePreserving;
	}
}
