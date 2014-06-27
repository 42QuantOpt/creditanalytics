
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
 * CurveSpanConstructionInput contains the Parameters needed for the Curve Calibration/Estimation. It
 * 	contains the following:
 *  - Calibration Valuation Parameters
 *  - Calibration Quoting Parameters
 *  - Calibration Market Parameters
 *  - Calibration Pricing Parameters
 *  - Array of Calibration Stretch Representation
 *  - Map of Calibration Quotes
 *  - Map of Calibration Measures
 *  - Double Map of the Date/Index Fixings
 *
 * Additional functions provide for retrieval of the above and specific instrument quotes. Derived Classes
 *  implement Targeted Curve Calibrators.
 *  
 * @author Lakshmi Krishnamurthy
 */

public abstract class CurveSpanConstructionInput implements
	org.drip.analytics.definition.CurveConstructionInputSet
{
	private org.drip.param.pricer.PricerParams _pricerParam = null;
	private org.drip.param.valuation.ValuationParams _valParam = null;
	private org.drip.param.valuation.ValuationCustomizationParams _quotingParam = null;
	private org.drip.param.market.MarketParamSet _cmp = null;
	private org.drip.state.estimator.StretchRepresentationSpec[] _aSRS = null;
	private org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.String[]> _mapMeasures = null;
	private java.util.Map<org.drip.analytics.date.JulianDate,
		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>> _mmFixing = null;
	private org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>>
		_mapQuote = null;

	/**
	 * CurveSpanConstructionInput constructor
	 * 
	 * @param aSRS Array of Stretch Representation Set
	 * @param valParam Valuation Parameters
	 * @param pricerParam Pricer Parameters
	 * @param quotingParam Quoting Parameters
	 * @param cmp Component Market Parameters
	 * 
	 * @throws java.lang.Exception Thrown if Inputs are invalid
	 */

	public CurveSpanConstructionInput (
		final org.drip.state.estimator.StretchRepresentationSpec[] aSRS,
		final org.drip.param.valuation.ValuationParams valParam,
		final org.drip.param.pricer.PricerParams pricerParam,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParam,
		final org.drip.param.market.MarketParamSet cmp)
		throws java.lang.Exception
	{
		if (null == (_aSRS = aSRS) || 0 == _aSRS.length || null == (_valParam = valParam))
			throw new java.lang.Exception ("CurveSpanConstructionInput ctr: Invalid Inputs");

		_cmp = cmp;
		_pricerParam = pricerParam;
		_quotingParam = quotingParam;
	}

	@Override public org.drip.param.valuation.ValuationParams getValuationParameter()
	{
		return _valParam;
	}

	@Override public org.drip.param.valuation.ValuationCustomizationParams getQuotingParameter()
	{
		return _quotingParam;
	}

	@Override public org.drip.product.definition.CalibratableFixedIncomeComponent[] getComponent()
	{
		if (null == _aSRS || 0 == _aSRS.length) return null;

		java.util.List<org.drip.product.definition.CalibratableFixedIncomeComponent> lsCC = new
			java.util.ArrayList<org.drip.product.definition.CalibratableFixedIncomeComponent>();

		for (org.drip.state.estimator.StretchRepresentationSpec rbs : _aSRS) {
			if (null == rbs) continue;

			org.drip.product.definition.CalibratableFixedIncomeComponent[] aCC = rbs.getCalibComp();

			if (null == aCC) continue;

			int iNumComp = aCC.length;

			if (0 == iNumComp) continue;

			for (int i = 0; i < iNumComp; ++i)
				lsCC.add (aCC[i]);
		}

		int iNumComp = lsCC.size();

		if (0 == iNumComp) return null;

		org.drip.product.definition.CalibratableFixedIncomeComponent[] aCC = new
			org.drip.product.definition.CalibratableFixedIncomeComponent[iNumComp];

		for (int i = 0; i < iNumComp; ++i)
			aCC[i] = lsCC.get (i);

		return aCC;
	}

	@Override public
		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>>
			getQuote()
	{
		if (null != _mapQuote) return _mapQuote;

		if (null == _aSRS || 0 == _aSRS.length) return null;

		_mapQuote = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>>();

		for (org.drip.state.estimator.StretchRepresentationSpec rbs : _aSRS) {
			if (null == rbs) continue;

			org.drip.product.definition.CalibratableFixedIncomeComponent[] aCC = rbs.getCalibComp();

			if (null == aCC) continue;

			org.drip.state.representation.LatentStateMetricMeasure[] aLSMM = rbs.getLSMM();

			int iNumComp = aCC.length;

			if (0 == iNumComp || iNumComp != aLSMM.length) continue;

			for (int i = 0; i < iNumComp; ++i)
				_mapQuote.put (aCC[i].primaryCode(), aLSMM[i].quoteMap());
		}

		return _mapQuote;
	}

	@Override public org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.String[]> getMeasures()
	{
		if (null != _mapMeasures) return _mapMeasures;

		if (null == _aSRS || 0 == _aSRS.length) return null;

		_mapMeasures = new org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.String[]>();

		for (org.drip.state.estimator.StretchRepresentationSpec rbs : _aSRS) {
			if (null == rbs) continue;

			org.drip.product.definition.CalibratableFixedIncomeComponent[] aCC = rbs.getCalibComp();

			if (null == aCC) continue;

			org.drip.state.representation.LatentStateMetricMeasure[] aLSMM = rbs.getLSMM();

			int iNumComp = aCC.length;

			if (0 == iNumComp || iNumComp != aLSMM.length) continue;

			for (int i = 0; i < iNumComp; ++i)
				_mapMeasures.put (aCC[i].primaryCode(), aLSMM[i].manifestMeasures());
		}

		return _mapMeasures;
	}

	@Override public java.util.Map<org.drip.analytics.date.JulianDate,
		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>>
			getFixing()
	{
		return _mmFixing;
	}

	/**
	 * Retrieve the Pricer Parameters
	 * 
	 * @return The Pricer Parameters
	 */

	public org.drip.param.pricer.PricerParams getPricerParameter()
	{
		return _pricerParam;
	}

	/**
	 * Retrieve the Component Market Parameters
	 * 
	 * @return The Component Market Parameters
	 */

	public org.drip.param.market.MarketParamSet getCMP()
	{
		return _cmp;
	}

	/**
	 * Retrieve the Array of SRS
	 * 
	 * @return The Array of SRS
	 */

	public org.drip.state.estimator.StretchRepresentationSpec[] getSRS()
	{
		return _aSRS;
	}

	/**
	 * Retrieve the Linear Curve Calibrator
	 * 
	 * @return The Linear Curve Calibrator
	 */

	public abstract org.drip.state.estimator.LinearCurveCalibrator lcc();
}
