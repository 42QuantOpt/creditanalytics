
package org.drip.state.manager;

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
 * NonlinearDiscountFactorDiscountCurve manages the Discounting Latent State, using the Discount Factor as
 *  the State Response Representation. The class constructs the discount curve using generic polynomial
 *  splines (arbitrary degree, variable shape control, custom segment knot constraints, user specified
 *  variational penalty optimization, and segment tension). It exports the following functionality:
 *  - Calculate discount factor / discount factor Jacobian
 *  - Calculate implied forward rate / implied forward rate Jacobian
 *  - Construct tweaked curve instances (parallel/tenor/custom tweaks)
 *  - Optionally provide the calibration instruments and quotes used to build the curve.
 *
 * @author Lakshmi Krishnamurthy
 */

public class NonlinearDiscountFactorDiscountCurve extends org.drip.analytics.definition.DiscountCurve {
	private static final int NUM_DF_QUADRATURES = 5;

	private double[] _adblDate = null;
	private double[] _adblCalibQuote = null;
	private java.lang.String _strCurrency = "";
	private double _dblStartDate = java.lang.Double.NaN;
	private java.lang.String[] _astrCalibMeasure = null;
	private double _dblLeftNodeDF = java.lang.Double.NaN;
	private org.drip.math.regime.MultiSegmentRegime _csi = null;
	private double _dblLeftNodeDFSlope = java.lang.Double.NaN;
	private double _dblLeftFlatForwardRate = java.lang.Double.NaN;
	private double _dblRightFlatForwardRate = java.lang.Double.NaN;
	private org.drip.param.valuation.ValuationParams _valParam = null;
	private org.drip.param.valuation.QuotingParams _quotingParams = null;
	private org.drip.product.definition.CalibratableComponent[] _aCalibInst = null;
	private org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> _mapQuote = null;
	private org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.String> _mapMeasure = null;
	private java.util.Map<org.drip.analytics.date.JulianDate,
		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>> _mmFixing = null;

	private NonlinearDiscountFactorDiscountCurve (
		final org.drip.analytics.date.JulianDate dtStart,
		final java.lang.String strCurrency,
		final double[] adblDate,
		final org.drip.math.regime.MultiSegmentRegime csi)
		throws java.lang.Exception
	{
		_csi = csi;
		_strCurrency = strCurrency;
		_adblDate = new double[adblDate.length];

		for (int i = 0; i < _adblDate.length; ++i)
			_adblDate[i] = adblDate[i];
	}

	/**
	 * Constructs NonlinearDiscountFactorDiscountCurve instance from an array of dates and forward rates
	 * 
	 * @param dtStart Epoch Date
	 * @param strCurrency Currency
	 * @param adblDate Array of Dates
	 * @param adblRate Array of Forward Rates
	 * 
	 * @throws java.lang.Exception Thrown if the curve cannot be created
	 */

	public NonlinearDiscountFactorDiscountCurve (
		final org.drip.analytics.date.JulianDate dtStart,
		final java.lang.String strCurrency,
		final double[] adblDate,
		final double[] adblRate)
		throws java.lang.Exception
	{
		if (null == adblDate || 0 == adblDate.length || null == adblRate || adblDate.length !=
			adblRate.length || null == dtStart || null == (_strCurrency = strCurrency) ||
				_strCurrency.isEmpty())
			throw new java.lang.Exception ("NonlinearDiscountFactorDiscountCurve ctr: Invalid inputs");

		_dblStartDate = dtStart.getJulian();

		org.drip.math.segment.PredictorResponseBuilderParams sbp = new
			org.drip.math.segment.PredictorResponseBuilderParams
				(org.drip.math.regime.RegimeBuilder.BASIS_SPLINE_POLYNOMIAL, new
					org.drip.math.spline.PolynomialBasisSetParams (2), new
						org.drip.math.segment.DesignInelasticParams (0, 2), null);

		_adblDate = new double[adblDate.length];
		double[] adblDF = new double[adblDate.length];
		org.drip.math.segment.PredictorResponseBuilderParams[] aSBP = new
			org.drip.math.segment.PredictorResponseBuilderParams[adblDate.length - 1];

		for (int i = 0; i < _adblDate.length; ++i) {
			_adblDate[i] = adblDate[i];

			if (0 == i)
				adblDF[0] = java.lang.Math.exp (adblRate[0] * (_dblStartDate - _adblDate[0]) / 365.25);
			else {
				aSBP[i - 1] = sbp;

				adblDF[i] = java.lang.Math.exp (adblRate[i] * (_adblDate[i - 1] - _adblDate[i]) / 365.25) *
					adblDF[i - 1];
			}
		}

		_dblLeftFlatForwardRate = -365.25 * java.lang.Math.log (adblDF[0]) / (_adblDate[0] - _dblStartDate);

		_dblRightFlatForwardRate = -365.25 * java.lang.Math.log (adblDF[adblDF.length - 1]) /
			(_adblDate[_adblDate.length - 1] - _dblStartDate);

		_csi = org.drip.math.regime.RegimeBuilder.CreateCalibratedRegimeEstimator ("POLY_SPLINE_DF_REGIME",
			adblDate, adblDF, aSBP, org.drip.math.regime.MultiSegmentRegime.BOUNDARY_CONDITION_NATURAL,
				org.drip.math.regime.MultiSegmentRegime.CALIBRATE);
	}

	/**
	 * NonlinearDiscountFactorDiscountCurve de-serialization from input byte array
	 * 
	 * @param ab Byte Array
	 * 
	 * @throws java.lang.Exception Thrown if NonlinearDiscountFactorDiscountCurve cannot be properly
	 * 	de-serialized
	 */

	public NonlinearDiscountFactorDiscountCurve (
		final byte[] ab)
		throws java.lang.Exception
	{
		if (null == ab || 0 == ab.length)
			throw new java.lang.Exception
				("NonlinearDiscountFactorDiscountCurve de-serializer: Invalid input Byte array");

		java.lang.String strRawString = new java.lang.String (ab);

		if (null == strRawString || strRawString.isEmpty())
			throw new java.lang.Exception
				("NonlinearDiscountFactorDiscountCurve de-serializer: Empty state");

		java.lang.String strSerializedPolynomialSplineDF = strRawString.substring (0, strRawString.indexOf
			(getObjectTrailer()));

		if (null == strSerializedPolynomialSplineDF || strSerializedPolynomialSplineDF.isEmpty())
			throw new java.lang.Exception
				("NonlinearDiscountFactorDiscountCurve de-serializer: Cannot locate state");

		java.lang.String[] astrField = org.drip.math.common.StringUtil.Split
			(strSerializedPolynomialSplineDF, getFieldDelimiter());

		if (null == astrField || 4 > astrField.length)
			throw new java.lang.Exception
				("NonlinearDiscountFactorDiscountCurve de-serializer: Invalid reqd field set");

		// double dblVersion = new java.lang.Double (astrField[0]);

		if (null == astrField[1] || astrField[1].isEmpty() ||
			org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[1]))
			throw new java.lang.Exception
				("NonlinearDiscountFactorDiscountCurve de-serializer: Cannot locate start state");

		_dblStartDate = new java.lang.Double (astrField[1]);

		if (null == astrField[2] || astrField[2].isEmpty() ||
			org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[2]))
			throw new java.lang.Exception
				("NonlinearDiscountFactorDiscountCurve de-serializer: Cannot locate currency");

		_strCurrency = astrField[2];

		java.util.List<java.lang.Double> lsdblDate = new java.util.ArrayList<java.lang.Double>();

		java.util.List<java.lang.Double> lsdblRate = new java.util.ArrayList<java.lang.Double>();

		if (null == astrField[3] || astrField[3].isEmpty() ||
			org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[3]))
			throw new java.lang.Exception
				("NonlinearDiscountFactorDiscountCurve de-serializer: Cannot decode state");

		if (!org.drip.math.common.StringUtil.KeyValueListFromStringArray (lsdblDate, lsdblRate, astrField[3],
			getCollectionRecordDelimiter(), getCollectionKeyValueDelimiter()))
			throw new java.lang.Exception
				("NonlinearDiscountFactorDiscountCurve de-serializer: Cannot decode state");

		if (0 == lsdblDate.size() || 0 == lsdblRate.size() || lsdblDate.size() != lsdblRate.size())
			throw new java.lang.Exception
				("NonlinearDiscountFactorDiscountCurve de-serializer: Cannot decode state");

		_adblDate = new double[lsdblDate.size()];

		// _adblEndRate = new double[lsdblRate.size()];

		for (int i = 0; i < _adblDate.length; ++i) {
			_adblDate[i] = lsdblDate.get (i);

			// _adblEndRate[i] = lsdblRate.get (i);
		}
	}

	public boolean initializeCalibrationRun (
		final double dblLeftSlope)
	{
		return org.drip.math.common.NumberUtil.IsValid (_dblLeftNodeDFSlope = dblLeftSlope);
	}

	@Override public java.util.Map<org.drip.analytics.date.JulianDate,
		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>>
			getCalibFixings()
	{
		return _mmFixing;
	}

	/**
	 * Calculate the calibration metric for the node
	 * 
	 * @return Calibration Metric
	 * 
	 * @throws java.lang.Exception
	 */

	public double getCalibrationMetric()
		throws java.lang.Exception
	{
		return _csi.calcRightEdgeDerivative (2);
	}

	@Override public void setInstrCalibInputs (
		final org.drip.param.valuation.ValuationParams valParam,
		final org.drip.product.definition.CalibratableComponent[] aCalibInst,
		final double[] adblCalibQuote,
		final java.lang.String[] astrCalibMeasure, final java.util.Map<org.drip.analytics.date.JulianDate,
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>> mmFixing,
		final org.drip.param.valuation.QuotingParams quotingParams)
	{
		_valParam = valParam;
		_mmFixing = mmFixing;
		_quotingParams = quotingParams;
		_adblCalibQuote = adblCalibQuote;
		_astrCalibMeasure = astrCalibMeasure;

		if (null == (_aCalibInst = aCalibInst) || 0 == _aCalibInst.length) return;

		_mapQuote = new org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>();

		_mapMeasure = new org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.String>();

		for (int i = 0; i < _aCalibInst.length; ++i) {
			_mapMeasure.put (_aCalibInst[i].getPrimaryCode(), astrCalibMeasure[i]);

			_mapQuote.put (_aCalibInst[i].getPrimaryCode(), adblCalibQuote[i]);

			java.lang.String[] astrSecCode = _aCalibInst[i].getSecondaryCode();

			if (null != astrSecCode) {
				for (int j = 0; j < astrSecCode.length; ++j)
					_mapQuote.put (astrSecCode[j], adblCalibQuote[i]);
			}
		}
	}

	@Override public org.drip.product.definition.CalibratableComponent[] getCalibComponents()
	{
		return _aCalibInst;
	}

	@Override public double[] getCompQuotes()
	{
		return _adblCalibQuote;
	}

	@Override public double getQuote (
		final java.lang.String strInstr)
		throws java.lang.Exception
	{
		if (null == _mapQuote || 0 == _mapQuote.size() || null == strInstr || strInstr.isEmpty() ||
			!_mapQuote.containsKey (strInstr))
			throw new java.lang.Exception ("Cannot get " + strInstr);

		return _mapQuote.get (strInstr);
	}

	@Override public NonlinearDiscountFactorDiscountCurve createParallelRateShiftedCurve (
		final double dblShift)
	{
		return null;
	}

	@Override public NonlinearDiscountFactorDiscountCurve createParallelShiftedCurve (
		final double dblShift)
	{
		if (!org.drip.math.common.NumberUtil.IsValid (dblShift)) return null;

		if (null == _valParam || null == _aCalibInst || 0 == _aCalibInst.length || null == _adblCalibQuote ||
			0 == _adblCalibQuote.length || null == _astrCalibMeasure || 0 == _astrCalibMeasure.length ||
				_astrCalibMeasure.length != _adblCalibQuote.length || _adblCalibQuote.length !=
					_aCalibInst.length)
			return createParallelRateShiftedCurve (dblShift);

		NonlinearDiscountFactorDiscountCurve dc = null;

		try {
			dc = new NonlinearDiscountFactorDiscountCurve (new org.drip.analytics.date.JulianDate (_dblStartDate),
				_strCurrency, _adblDate, _csi);
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		double[] adblCalibQuoteShifted = new double[_adblCalibQuote.length];

		try {
			org.drip.state.estimator.NonlinearCurveCalibrator calibrator = new
				org.drip.state.estimator.NonlinearCurveCalibrator();

			for (int i = 0; i < _adblCalibQuote.length; ++i)
				calibrator.calibrateIRNode (dc, null, null, _aCalibInst[i], i, _valParam,
					_astrCalibMeasure[i], adblCalibQuoteShifted[i] = _adblCalibQuote[i] + dblShift,
						_mmFixing, _quotingParams, false, java.lang.Double.NaN);
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		dc.setInstrCalibInputs (_valParam, _aCalibInst, adblCalibQuoteShifted, _astrCalibMeasure, _mmFixing,
			_quotingParams);

		return dc;
	}

	@Override public NonlinearDiscountFactorDiscountCurve createBasisRateShiftedCurve (
		final double[] adblDate,
		final double[] adblBasis)
	{
		if (null == adblDate || 0 == adblDate.length || null == adblBasis || 0 == adblBasis.length ||
			adblDate.length != adblBasis.length)
			return null;

		try {
			double[] adblCDFRate = new double[adblBasis.length];

			for (int i = 0; i < adblDate.length; ++i)
				adblCDFRate[i] = calcImpliedRate (adblDate[i]) + adblBasis[i];

			return new NonlinearDiscountFactorDiscountCurve (new org.drip.analytics.date.JulianDate (_dblStartDate),
				_strCurrency, adblDate, adblCDFRate);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override public org.drip.analytics.definition.DiscountCurve createTweakedCurve (
		final org.drip.param.definition.NodeTweakParams ntp)
	{
		if (null == ntp) return null;

		double[] adblEndRate = new double[_adblDate.length];

		for (int i = 0; i < adblEndRate.length; ++i) {
			try {
				adblEndRate[i] = calcImpliedRate (_adblDate[i]);
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}
		}

		double[] adblCDFBumped = org.drip.analytics.support.AnalyticsHelper.BumpNTPNode (adblEndRate, ntp);

		if (null == adblCDFBumped || 0 == adblCDFBumped.length) return null;

		try {
			return new  org.drip.state.manager.ForwardRateDiscountCurve (new
				org.drip.analytics.date.JulianDate (_dblStartDate), _strCurrency, _adblDate, adblCDFBumped);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override public java.lang.String getName()
	{
		return _strCurrency;
	}

	@Override public java.lang.String getCurrency()
	{
		return _strCurrency;
	}

	@Override public double getDF (
		final double dblDate)
		throws java.lang.Exception
	{
		if (!org.drip.math.common.NumberUtil.IsValid (dblDate))
			throw new java.lang.Exception ("NonlinearDiscountFactorDiscountCurve::getDF => Invalid Inputs");

		if (dblDate <= _dblStartDate) return 1.;

		if (dblDate <= _adblDate[0])
			return java.lang.Math.exp (-1. * _dblLeftFlatForwardRate * (dblDate - _dblStartDate) / 365.25);

		return dblDate <= _adblDate[_adblDate.length - 1] ? _csi.response (dblDate) : java.lang.Math.exp
			(-1. * _dblRightFlatForwardRate * (dblDate - _dblStartDate) / 365.25);
	}

	@Override public org.drip.math.calculus.WengertJacobian getDFJacobian (
		final double dblDate)
	{
		if (!org.drip.math.common.NumberUtil.IsValid (dblDate)) return null;

		org.drip.math.calculus.WengertJacobian wj = null;

		try {
			wj = new org.drip.math.calculus.WengertJacobian (1, _adblDate.length);
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		for (int i = 0; i < _adblDate.length; ++i) {
			if (!wj.accumulatePartialFirstDerivative (0, i, 0.)) return null;
		}

		if (dblDate <= _dblStartDate) return wj;

		if (dblDate <= _adblDate[0]) {
			try {
				return wj.accumulatePartialFirstDerivative (0, 0, (dblDate - _dblStartDate) / (_adblDate[0] -
					_dblStartDate) * java.lang.Math.exp (_dblLeftFlatForwardRate * (_adblDate[0] - dblDate) /
						365.25)) ? wj : null;
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}
		}

		if (dblDate <= _adblDate[_adblDate.length - 1]) return _csi.jackDResponseDResponseInput (dblDate);

		try {
			return wj.accumulatePartialFirstDerivative (0, _adblDate.length - 1, (dblDate - _dblStartDate) /
				(_adblDate[_adblDate.length - 1] - _dblStartDate) * java.lang.Math.exp
					(_dblRightFlatForwardRate * (_adblDate[_adblDate.length - 1] - dblDate) / 365.25)) ? wj :
						null;
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override public double getEffectiveDF (
		final double dblDate1,
		final double dblDate2)
		throws java.lang.Exception
	{
		if (dblDate1 == dblDate2) return getDF (dblDate1);

		int iNumQuadratures = 0;
		double dblEffectiveDF = 0.;
		double dblQuadratureWidth = (dblDate2 - dblDate1) / NUM_DF_QUADRATURES;

		for (double dblDate = dblDate1; dblDate <= dblDate2; dblDate += dblQuadratureWidth) {
			++iNumQuadratures;

			dblEffectiveDF += (getDF (dblDate) + getDF (dblDate + dblQuadratureWidth));
		}

		return dblEffectiveDF / (2. * iNumQuadratures);
	}

	@Override public double getEffectiveDF (
		final org.drip.analytics.date.JulianDate dt1,
		final org.drip.analytics.date.JulianDate dt2)
		throws java.lang.Exception
	{
		if (null == dt1 || null == dt2)
			throw new java.lang.Exception
				("NonlinearDiscountFactorDiscountCurve::getEffectiveDF => Invalid Input");

		return getEffectiveDF (dt1.getJulian(), dt2.getJulian());
	}

	@Override public double getEffectiveDF (
		final java.lang.String strTenor1,
		final java.lang.String strTenor2)
		throws java.lang.Exception
	{
		if (null == strTenor1 || strTenor1.isEmpty() || null == strTenor2 || strTenor2.isEmpty())
			throw new java.lang.Exception
				("NonlinearDiscountFactorDiscountCurve::getEffectiveDF => Invalid Input");

		return getEffectiveDF (new org.drip.analytics.date.JulianDate (_dblStartDate).addTenor (strTenor1),
			new org.drip.analytics.date.JulianDate (_dblStartDate).addTenor (strTenor2));
	}

	@Override public boolean setNodeValue (
		final int iNodeIndex,
		final double dblNodeDF)
	{
		if (!org.drip.math.common.NumberUtil.IsValid (dblNodeDF) || iNodeIndex > _adblDate.length)
			return false;

		if (0 == iNodeIndex) {
			_dblLeftFlatForwardRate = -365.25 * java.lang.Math.log (_dblLeftNodeDF = dblNodeDF) /
				(_adblDate[0] - _dblStartDate);

			return true;
		}

		if (1 == iNodeIndex) return _csi.setLeftNode (_dblLeftNodeDF, _dblLeftNodeDFSlope, dblNodeDF);

		if (_adblDate.length - 1 == iNodeIndex) {
			try {
				_dblRightFlatForwardRate = -365.25 * java.lang.Math.log (_csi.response
					(_adblDate[iNodeIndex])) / (_adblDate[iNodeIndex] - _dblStartDate);
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return false;
			}
		}

		return _csi.resetNode (iNodeIndex, dblNodeDF);
	}

	@Override public boolean bumpNodeValue (
		final int iNodeIndex,
		final double dblValue)
	{
		return false;
	}

	@Override public boolean setFlatValue (
		final double dblValue)
	{
		return false;
	}

	@Override public double calcImpliedRate (
		final double dblDt1,
		final double dblDt2)
		throws java.lang.Exception
	{
		if (!org.drip.math.common.NumberUtil.IsValid (dblDt1) || !org.drip.math.common.NumberUtil.IsValid
			(dblDt2))
			throw new java.lang.Exception
				("NonlinearDiscountFactorDiscountCurve::calcImpliedRate => Invalid input dates");

		if (dblDt1 < _dblStartDate || dblDt2 < _dblStartDate) return 0.;

		return 365.25 / (dblDt2 - dblDt1) * java.lang.Math.log (getDF (dblDt1) / getDF (dblDt2));
	}

	@Override public double calcImpliedRate (
		final double dblDate)
		throws java.lang.Exception
	{
		if (!org.drip.math.common.NumberUtil.IsValid (dblDate))
			throw new java.lang.Exception
				("NonlinearDiscountFactorDiscountCurve::calcImpliedRate => Got NaN for date");

		return calcImpliedRate (_dblStartDate, dblDate);
	}

	@Override public double calcImpliedRate (
		final java.lang.String strTenor)
		throws java.lang.Exception
	{
		if (null == strTenor || strTenor.isEmpty())
			throw new java.lang.Exception
				("NonlinearDiscountFactorDiscountCurve::calcImpliedRate => Got empty date");

		return calcImpliedRate (_dblStartDate, new org.drip.analytics.date.JulianDate
			(_dblStartDate).addTenor (strTenor).getJulian());
	}

	@Override public double calcImpliedRate (
		final java.lang.String strTenor1,
		final java.lang.String strTenor2)
		throws java.lang.Exception
	{
		if (null == strTenor1 || strTenor1.isEmpty() || null == strTenor2 || strTenor2.isEmpty())
			throw new java.lang.Exception
				("NonlinearDiscountFactorDiscountCurve::calcImpliedRate => Got empty date");

		org.drip.analytics.date.JulianDate dtStart = new org.drip.analytics.date.JulianDate (_dblStartDate);

		return calcImpliedRate (dtStart.addTenor (strTenor1).getJulian(), dtStart.addTenor
			(strTenor2).getJulian());
	}

	@Override public org.drip.analytics.date.JulianDate getStartDate()
	{
		try {
			return new org.drip.analytics.date.JulianDate (_dblStartDate);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override public byte[] serialize()
	{
		java.lang.StringBuffer sb = new java.lang.StringBuffer();

		return sb.append (getObjectTrailer()).toString().getBytes();
	}

	@Override public org.drip.service.stream.Serializer deserialize (
		final byte[] ab) {
		try {
			return new ForwardRateDiscountCurve (ab);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}
}
