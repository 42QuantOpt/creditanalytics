
package org.drip.analytics.calibration;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2013 Lakshmi Krishnamurthy
 * Copyright (C) 2012 Lakshmi Krishnamurthy
 * Copyright (C) 2011 Lakshmi Krishnamurthy
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
 * This calls contains the credit calibration instruments to be used with the component calibrator to produce
 * 	scenario credit curves.
 * 
 * CreditCurveScenarioGenerator typically first constructs the actual curve calibrator instance to localize
 * 	the intelligence around curve construction. It then uses this curve calibrator instance to build
 *  individual curves or the sequence of node bumped scenario curves.
 *
 * @author Lakshmi Krishnamurthy
 */

public class CreditCurveScenarioGenerator {
	class TranslatedQuoteMeasure {
		java.lang.String _strMeasure = "";
		double _dblQuote = java.lang.Double.NaN;

		TranslatedQuoteMeasure (
			final java.lang.String strMeasure,
			final double dblQuote)
		{
			_dblQuote = dblQuote;
			_strMeasure = strMeasure;
		}
	}

	private final TranslatedQuoteMeasure translateQuoteMeasure (
		final org.drip.product.definition.CalibratableComponent comp,
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.analytics.definition.DiscountCurve dc,
		final org.drip.analytics.definition.CreditCurve cc,
		final java.lang.String strMeasure,
		final double dblQuote)
	{
		if (!(comp instanceof org.drip.product.definition.CreditDefaultSwap) ||
			(!"FlatSpread".equalsIgnoreCase (strMeasure) && !"QuotedSpread".equalsIgnoreCase (strMeasure)))
			return new TranslatedQuoteMeasure (strMeasure, dblQuote);

		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapQSMeasures =
			((org.drip.product.definition.CreditDefaultSwap) comp).valueFromQuotedSpread (valParams,
				pricerParams, org.drip.param.creator.ComponentMarketParamsBuilder.MakeCreditCMP (dc, cc),
					null, 0.01, dblQuote);

		return new TranslatedQuoteMeasure ("Upfront", null == mapQSMeasures ? null : mapQSMeasures.get
			("Upfront"));
	}

	private org.drip.product.definition.CalibratableComponent[] _aCalibInst = null;

	private org.drip.analytics.calibration.CurveCalibrator _compCalib = new
		org.drip.analytics.calibration.CurveCalibrator();

	/**
	 * Constructs a CreditCurveScenarioGenerator instance from the calibratable instrument array
	 * 
	 * @param aCalibInst Array of calibration instruments
	 * 
	 * @throws java.lang.Exception Thrown if inputs are invalid
	 */

	public CreditCurveScenarioGenerator (
		final org.drip.product.definition.CalibratableComponent[] aCalibInst)
		throws java.lang.Exception
	{
		if (null == (_aCalibInst = aCalibInst) || 0 == _aCalibInst.length)
			throw new java.lang.Exception ("CreditCurveScenarioGenerator ctr: Invalid calib instr!");
	}

	/**
	 * Returns an array of the calibration instruments
	 * 
	 * @return Array of the calibration instruments
	 */

	public org.drip.product.definition.Component[] getInstruments()
	{
		return _aCalibInst;
	}

	/**
	 * Calibrates a create curve
	 * 
	 * @param strName Credit Curve name
	 * @param valParams ValuationParams
	 * @param dc Base Discount Curve
	 * @param dcTSY Treasury Discount Curve
	 * @param dcEDSF EDSF Discount Curve
	 * @param adblQuotes Array of component quotes
	 * @param dblRecovery Component recovery
	 * @param astrCalibMeasure Array of the calibration measures
	 * @param mmFixings Map of fixings
	 * @param quotingParams Quoting Parameters
	 * @param bFlat Flat Calibration (True), or real bootstrapping (false)
	 * 
	 * @return CreditCurve
	 */

	public org.drip.analytics.definition.CreditCurve createCC (
		final java.lang.String strName,
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.analytics.definition.DiscountCurve dc,
		final org.drip.analytics.definition.DiscountCurve dcTSY,
		final org.drip.analytics.definition.DiscountCurve dcEDSF,
		final double[] adblQuotes,
		final double dblRecovery,
		final java.lang.String[] astrCalibMeasure,
		final java.util.Map<org.drip.analytics.date.JulianDate,
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>> mmFixings,
		final org.drip.param.valuation.QuotingParams quotingParams,
		final boolean bFlat)
	{
		if (null == strName || null == adblQuotes || null == astrCalibMeasure || adblQuotes.length !=
			astrCalibMeasure.length || _aCalibInst.length != astrCalibMeasure.length || null == valParams ||
				null == dc)
			return null;

		int iNumInstr = adblQuotes.length;
		double adblDate[] = new double[iNumInstr];
		double adblHazardRate[] = new double[iNumInstr];
		org.drip.analytics.definition.CreditCurve cc = null;

		if (0 == iNumInstr || iNumInstr != astrCalibMeasure.length || iNumInstr != astrCalibMeasure.length)
			return null;

		for (int i = 0; i < iNumInstr; ++i) {
			adblHazardRate[i] = java.lang.Double.NaN;

			adblDate[i] = _aCalibInst[i].getMaturityDate().getJulian();
		}

		try {
			cc = org.drip.analytics.creator.CreditCurveBuilder.CreateCreditCurve (new
				org.drip.analytics.date.JulianDate (valParams._dblValue), strName, adblDate, adblHazardRate,
					dblRecovery);
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		org.drip.param.pricer.PricerParams pricerParams = new org.drip.param.pricer.PricerParams (7, null,
			false, org.drip.param.pricer.PricerParams.PERIOD_DISCRETIZATION_DAY_STEP);

		for (int i = 0; i < iNumInstr; ++i) {
			TranslatedQuoteMeasure tqm = translateQuoteMeasure (_aCalibInst[i], valParams, pricerParams, dc,
				cc, astrCalibMeasure[i], adblQuotes[i]);

			if (null == tqm) return null;

			if (!_compCalib.bootstrapHazardRate (cc, _aCalibInst[i], i, valParams, dc, dcTSY, dcEDSF,
				pricerParams, tqm._strMeasure, tqm._dblQuote, mmFixings, quotingParams, bFlat))
				return null;
		}

		cc.setInstrCalibInputs (valParams, bFlat, dc, dcTSY, dcEDSF, pricerParams, _aCalibInst, adblQuotes,
			astrCalibMeasure, mmFixings, quotingParams);

		return cc;
	}

	/**
	 * Creates an array of tenor bumped credit curves
	 * 
	 * @param strName Credit Curve Name
	 * @param valParams ValuationParams
	 * @param dc Base Discount Curve
	 * @param dcTSY Treasury Discount Curve
	 * @param dcEDSF EDSF Discount Curve
	 * @param adblQuotes Array of component quotes
	 * @param dblBump Amount of bump applied to the tenor
	 * @param dblRecovery Component recovery
	 * @param astrCalibMeasure Array of the calibration measures
	 * @param mmFixings Map of fixings
	 * @param quotingParams Quoting Parameters
	 * @param bFlat Flat Calibration (True), or real bootstrapping (false)
	 * 
	 * @return Array of CreditCurves
	 */

	public org.drip.analytics.definition.CreditCurve[] createTenorCC (
		final java.lang.String strName,
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.analytics.definition.DiscountCurve dc,
		final org.drip.analytics.definition.DiscountCurve dcTSY,
		final org.drip.analytics.definition.DiscountCurve dcEDSF,
		final double[] adblQuotes,
		final double dblBump,
		final double dblRecovery,
		final java.lang.String[] astrCalibMeasure,
		final java.util.Map<org.drip.analytics.date.JulianDate,
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>> mmFixings,
		final org.drip.param.valuation.QuotingParams quotingParams,
		final boolean bFlat)
	{
		if (null == strName || null == adblQuotes || null == astrCalibMeasure || null == valParams || null ==
			dc || adblQuotes.length != astrCalibMeasure.length || _aCalibInst.length !=
				astrCalibMeasure.length || !org.drip.math.common.NumberUtil.IsValid (dblRecovery))
			return null;

		org.drip.analytics.definition.CreditCurve[] aCC = new
			org.drip.analytics.definition.CreditCurve[_aCalibInst.length];

		for (int i = 0; i < aCC.length; ++i) {
			double[] adblTenorQuotes = new double [aCC.length];

			for (int j = 0; j < aCC.length; ++j) {
				adblTenorQuotes[j] = adblQuotes[j];

				if (j == i) adblTenorQuotes[j] += dblBump;
			}

			if (null == (aCC[i] = createCC (strName, valParams, dc, dcTSY, dcEDSF, adblTenorQuotes,
				dblRecovery, astrCalibMeasure, mmFixings, quotingParams, bFlat)))
				return null;
		}

		return aCC;
	}

	/**
	 * Creates an tenor named map of tenor bumped credit curves
	 * 
	 * @param strName Credit Curve name
	 * @param valParams ValuationParams
	 * @param dc Base Discount Curve
	 * @param dcTSY Treasury Discount Curve
	 * @param dcEDSF EDSF Discount Curve
	 * @param adblQuotes Array of component quotes
	 * @param dblBump Amount of bump applied to the tenor
	 * @param dblRecovery Component recovery
	 * @param astrCalibMeasure Array of the calibration measures
	 * @param mmFixings Map of fixings
	 * @param quotingParams Quoting Parameters
	 * @param bFlat Flat Calibration (True), or real bootstrapping (false)
	 * 
	 * @return Tenor named map of tenor bumped credit curves
	 */

	public org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.definition.CreditCurve>
		createTenorCCMap (
			final java.lang.String strName,
			final org.drip.param.valuation.ValuationParams valParams,
			final org.drip.analytics.definition.DiscountCurve dc,
			final org.drip.analytics.definition.DiscountCurve dcTSY,
			final org.drip.analytics.definition.DiscountCurve dcEDSF,
			final double[] adblQuotes,
			final double dblBump,
			final double dblRecovery,
			final java.lang.String[] astrCalibMeasure,
			final java.util.Map<org.drip.analytics.date.JulianDate,
				org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>> mmFixings,
			final org.drip.param.valuation.QuotingParams quotingParams,
			final boolean bFlat)
	{
		if (null == strName || null == valParams || null == dc || null == adblQuotes || null ==
			astrCalibMeasure || adblQuotes.length != astrCalibMeasure.length || _aCalibInst.length !=
				astrCalibMeasure.length || !org.drip.math.common.NumberUtil.IsValid (dblRecovery))
			return null;

		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.definition.CreditCurve> mapTenorCC =
			new org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.definition.CreditCurve>();

		for (int i = 0; i < _aCalibInst.length; ++i) {
			org.drip.analytics.definition.CreditCurve cc = null;
			double[] adblTenorQuotes = new double[_aCalibInst.length];

			for (int j = 0; j < _aCalibInst.length; ++j) {
				if (j == i)
					adblTenorQuotes[j] = adblQuotes[j] + dblBump;
				else
					adblTenorQuotes[j] = adblQuotes[j];
			}

			if (null == (cc = createCC (strName, valParams, dc, dcTSY, dcEDSF, adblTenorQuotes, dblRecovery,
				astrCalibMeasure, mmFixings, quotingParams, bFlat)))
				return null;

			mapTenorCC.put (org.drip.analytics.date.JulianDate.fromJulian
				(_aCalibInst[i].getMaturityDate().getJulian()), cc);
		}

		return mapTenorCC;
	}
}
