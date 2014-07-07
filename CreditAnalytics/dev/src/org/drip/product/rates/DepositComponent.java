
package org.drip.product.rates;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2014 Lakshmi Krishnamurthy
 * Copyright (C) 2013 Lakshmi Krishnamurthy
 * Copyright (C) 2012 Lakshmi Krishnamurthy
 * Copyright (C) 2011 Lakshmi Krishnamurthy
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
 * DepositComponent contains the implementation of the Deposit IR product and its contract/valuation details.
 * 	It exports the following functionality:
 *  - Standard/Custom Constructor for the Deposit Component
 *  - Dates: Effective, Maturity, Coupon dates and Product settlement Parameters
 *  - Coupon/Notional Outstanding as well as schedules
 *  - Market Parameters: Discount, Forward, Credit, Treasury Curves
 *  - Cash Flow Periods: Coupon flows and (Optionally) Loss Flows
 *  - Valuation: Named Measure Generation
 *  - Calibration: The codes and constraints generation
 *  - Jacobians: Quote/DF and PV/DF micro-Jacobian generation
 *  - Serialization into and de-serialization out of byte arrays
 * 
 * @author Lakshmi Krishnamurthy
 */

public class DepositComponent extends org.drip.product.definition.RatesComponent {
	private double _dblNotional = 100.;
	private java.lang.String _strCode = "";
	private java.lang.String _strCalendar = "";
	private java.lang.String _strCurrency = "";
	private java.lang.String _strDayCount = "Act/360";
	private double _dblMaturity = java.lang.Double.NaN;
	private double _dblEffective = java.lang.Double.NaN;
	private org.drip.product.params.FloatingRateIndex _fri = null;
	private org.drip.param.valuation.CashSettleParams _settleParams = null;

	/**
	 * Construct a DepositComponent instance
	 * 
	 * @param dtEffective Effective Date
	 * @param dtMaturity Maturity Date
	 * @param strCurrency Pay Currency
	 * @param strDayCount Day Count
	 * @param strCalendar Calendar
	 * 
	 * @throws java.lang.Exception Thrown if the inputs are invalid
	 */

	public DepositComponent (
		final org.drip.analytics.date.JulianDate dtEffective,
		final org.drip.analytics.date.JulianDate dtMaturity,
		final org.drip.product.params.FloatingRateIndex fri,
		final java.lang.String strCurrency,
		final java.lang.String strDayCount,
		final java.lang.String strCalendar)
		throws java.lang.Exception
	{
		if (null == dtEffective || null == dtMaturity || null == (_strCurrency = strCurrency) ||
			_strCurrency.isEmpty() || (_dblMaturity = dtMaturity.getJulian()) <= (_dblEffective =
				dtEffective.getJulian()))
			throw new java.lang.Exception ("DepositComponent ctr: Invalid Inputs!");

		_fri = fri;
		_strCalendar = strCalendar;
		_strDayCount = strDayCount;
	}

	@Override protected org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> calibMeasures (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams)
	{
		return null;
	}

	/**
	 * DepositComponent de-serialization from input byte array
	 * 
	 * @param ab Byte Array
	 * 
	 * @throws java.lang.Exception Thrown if DepositComponent cannot be properly de-serialized
	 */

	public DepositComponent (
		final byte[] ab)
		throws java.lang.Exception
	{
		if (null == ab || 0 == ab.length)
			throw new java.lang.Exception ("DepositComponent de-serializer: Invalid input Byte array");

		java.lang.String strRawString = new java.lang.String (ab);

		if (null == strRawString || strRawString.isEmpty())
			throw new java.lang.Exception ("DepositComponent de-serializer: Empty state");

		java.lang.String strSerializedDeposit = strRawString.substring (0, strRawString.indexOf
			(objectTrailer()));

		if (null == strSerializedDeposit || strSerializedDeposit.isEmpty())
			throw new java.lang.Exception ("DepositComponent de-serializer: Cannot locate state");

		java.lang.String[] astrField = org.drip.quant.common.StringUtil.Split (strSerializedDeposit,
			fieldDelimiter());

		if (null == astrField || 9 > astrField.length)
			throw new java.lang.Exception ("DepositComponent de-serializer: Invalid reqd field set");

		// double dblVersion = new java.lang.Double (astrField[0]);

		if (null == astrField[1] || astrField[1].isEmpty() ||
			org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[1]))
			throw new java.lang.Exception ("DepositComponent de-serializer: Cannot locate notional");

		_dblNotional = new java.lang.Double (astrField[1]);

		if (null == astrField[2] || astrField[2].isEmpty())
			throw new java.lang.Exception
				("DepositComponent de-serializer: Cannot locate principal currency");

		if (!org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[2]))
			_strCurrency = astrField[2];
		else
			_strCurrency = "";

		if (null == astrField[3] || astrField[3].isEmpty())
			throw new java.lang.Exception ("DepositComponent de-serializer: Cannot locate Deposit code");

		if (!org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[3]))
			_strCode = astrField[3];
		else
			_strCode = "";

		if (null == astrField[4] || astrField[4].isEmpty())
			throw new java.lang.Exception
				("DepositComponent de-serializer: Cannot locate day count convention");

		if (!org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[4]))
			_strDayCount = astrField[4];
		else
			_strDayCount = "";

		if (null == astrField[5] || astrField[5].isEmpty())
			throw new java.lang.Exception ("DepositComponent de-serializer: Cannot locate Calendar");

		if (!org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[4]))
			_strCalendar = astrField[5];
		else
			_strCalendar = "";

		if (null == astrField[6] || astrField[6].isEmpty() ||
			org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[6]))
			throw new java.lang.Exception ("DepositComponent de-serializer: Cannot locate maturity date");

		_dblMaturity = new java.lang.Double (astrField[6]);

		if (null == astrField[7] || astrField[7].isEmpty() ||
			org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[7]))
			throw new java.lang.Exception ("DepositComponent de-serializer: Cannot locate effective date");

		_dblEffective = new java.lang.Double (astrField[7]);

		if (null == astrField[8] || astrField[8].isEmpty())
			throw new java.lang.Exception
				("DepositComponent de-serializer: Cannot locate cash settle params");

		if (org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[8]))
			_settleParams = null;
		else
			_settleParams = new org.drip.param.valuation.CashSettleParams (astrField[8].getBytes());

		if (null == astrField[9] || astrField[9].isEmpty())
			throw new java.lang.Exception
				("DepositComponent de-serializer: Cannot locate the floating Rate Index");

		if (org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[8]))
			_fri = null;
		else
			_fri = org.drip.product.params.FloatingRateIndex.Create (astrField[8]);
	}

	@Override public java.lang.String primaryCode()
	{
		return _strCode;
	}

	@Override public void setPrimaryCode (
		final java.lang.String strCode)
	{
		_strCode = strCode;
	}

	@Override public java.lang.String name()
	{
		return "CD=" + org.drip.analytics.date.JulianDate.fromJulian (_dblMaturity);
	}

	@Override public java.util.Set<java.lang.String> cashflowCurrencySet()
	{
		java.util.Set<java.lang.String> setCcy = new java.util.HashSet<java.lang.String>();

		setCcy.add (_strCurrency);

		return setCcy;
	}

	@Override public java.lang.String[] couponCurrency()
	{
		return new java.lang.String[] {_strCurrency};
	}

	@Override public java.lang.String[] principalCurrency()
	{
		return new java.lang.String[] {_strCurrency};
	}

	@Override public double initialNotional()
	{
		return _dblNotional;
	}

	@Override public double notional (
		final double dblDate)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblDate))
			throw new java.lang.Exception ("DepositComponent::notional => Bad date into getNotional");

		return 1.;
	}

	@Override public double notional (
		final double dblDate1,
		final double dblDate2)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblDate1) || !org.drip.quant.common.NumberUtil.IsValid
			(dblDate2))
			throw new java.lang.Exception ("DepositComponent::notional => Bad date into getNotional");

		return 1.;
	}

	@Override public double coupon (
		final double dblValue,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs)
		throws java.lang.Exception
	{
		return 0.;
	}

	@Override public java.lang.String[] forwardCurveName()
	{
		return null == _fri ? null : new java.lang.String[] {_fri.fullyQualifiedName()};
	}

	@Override public java.lang.String[] creditCurveName()
	{
		return null;
	}

	@Override public java.lang.String[] currencyPairCode()
	{
		return null;
	}

	@Override public org.drip.analytics.date.JulianDate effective()
	{
		try {
			return new org.drip.analytics.date.JulianDate (_dblEffective);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override public org.drip.analytics.date.JulianDate maturity()
	{
		try {
			return new org.drip.analytics.date.JulianDate (_dblMaturity);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override public org.drip.analytics.date.JulianDate firstCouponDate()
	{
		return null;
	}

	@Override public java.util.List<org.drip.analytics.period.CashflowPeriod> cashFlowPeriod()
	{
		return org.drip.analytics.period.CashflowPeriod.GenerateSinglePeriod (_dblEffective, _dblMaturity,
			_strDayCount, _strCalendar, _strCurrency);
	}

	@Override public org.drip.param.valuation.CashSettleParams cashSettleParams()
	{
		return _settleParams;
	}

	@Override public org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> value (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams)
	{
		if (null == valParams || valParams.valueDate() >= _dblMaturity || null == csqs) return null;

		long lStart = System.nanoTime();

		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapResult = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>();

		org.drip.analytics.rates.ForwardCurve fc = csqs.forwardCurve (_fri);

		if (null != fc && null != _fri && fc.name().equalsIgnoreCase (_fri.fullyQualifiedName())) {
			try {
				double dblForwardRate = fc.forward (_dblMaturity);

				mapResult.put ("forward", dblForwardRate);

				mapResult.put ("forwardrate", dblForwardRate);
			} catch (java.lang.Exception e) {
				e.printStackTrace();
			}
		}

		org.drip.analytics.rates.DiscountCurve dc = csqs.fundingCurve (couponCurrency()[0]);

		if (null == dc) {
			mapResult.put ("calctime", (System.nanoTime() - lStart) * 1.e-09);

			return mapResult;
		}

		try {
			double dblCashSettle = null == _settleParams ? valParams.cashPayDate() :
				_settleParams.cashSettleDate (valParams.valueDate());

			double dblUnadjustedAnnuity = dc.df (_dblMaturity) / dc.df (_dblEffective) / dc.df
				(dblCashSettle);

			double dblAdjustedAnnuity = dblUnadjustedAnnuity / dc.df (dblCashSettle);

			mapResult.put ("pv", dblAdjustedAnnuity * _dblNotional * 0.01 * notional (_dblEffective,
				_dblMaturity));

			mapResult.put ("price", 100. * dblAdjustedAnnuity);

			mapResult.put ("rate", ((1. / dblUnadjustedAnnuity) - 1.) /
				org.drip.analytics.daycount.Convention.YearFraction (_dblEffective, _dblMaturity,
					_strDayCount, false, _dblMaturity, null, _strCalendar));
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		mapResult.put ("calctime", (System.nanoTime() - lStart) * 1.e-09);

		return mapResult;
	}

	@Override public java.util.Set<java.lang.String> measureNames()
	{
		java.util.Set<java.lang.String> setstrMeasureNames = new java.util.TreeSet<java.lang.String>();

		setstrMeasureNames.add ("CalcTime");

		setstrMeasureNames.add ("Forward");

		setstrMeasureNames.add ("ForwardRate");

		setstrMeasureNames.add ("Price");

		setstrMeasureNames.add ("PV");

		setstrMeasureNames.add ("Rate");

		return setstrMeasureNames;
	}

	@Override public org.drip.quant.calculus.WengertJacobian jackDDirtyPVDManifestMeasure (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams)
	{
		if (null == valParams || valParams.valueDate() >= _dblMaturity || null == csqs) return null;

		org.drip.analytics.rates.DiscountCurve dcFunding = csqs.fundingCurve (couponCurrency()[0]);

		if (null == dcFunding) return null;

		try {
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapMeasures = value
				(valParams, pricerParams, csqs, quotingParams);

			if (null == mapMeasures) return null;

			org.drip.quant.calculus.WengertJacobian wjDFDF = dcFunding.jackDDFDManifestMeasure (_dblMaturity,
				"Rate");

			if (null == wjDFDF) return null;

			org.drip.quant.calculus.WengertJacobian wjPVDFMicroJack = new
				org.drip.quant.calculus.WengertJacobian (1, wjDFDF.numParameters());

			for (int k = 0; k < wjDFDF.numParameters(); ++k) {
				if (!wjPVDFMicroJack.accumulatePartialFirstDerivative (0, k, wjDFDF.getFirstDerivative (0,
					k)))
					return null;
			}

			return wjPVDFMicroJack;
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override public org.drip.quant.calculus.WengertJacobian manifestMeasureDFMicroJack (
		final java.lang.String strManifestMeasure,
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams)
	{
		if (null == valParams || valParams.valueDate() >= _dblMaturity || null == strManifestMeasure)
			return null;

		org.drip.analytics.rates.DiscountCurve dcFunding = csqs.fundingCurve (couponCurrency()[0]);

		if (null == dcFunding) return null;

		if ("Rate".equalsIgnoreCase (strManifestMeasure)) {
			try {
				org.drip.quant.calculus.WengertJacobian wjDF = dcFunding.jackDDFDManifestMeasure
					(_dblMaturity, "Rate");

				if (null == wjDF) return null;

				org.drip.quant.calculus.WengertJacobian wjDFMicroJack = new
					org.drip.quant.calculus.WengertJacobian (1, wjDF.numParameters());

				for (int k = 0; k < wjDF.numParameters(); ++k) {
					if (!wjDFMicroJack.accumulatePartialFirstDerivative (0, k, -365.25 / (_dblMaturity -
						_dblEffective) / dcFunding.df (_dblMaturity) * wjDF.getFirstDerivative (0, k)))
						return null;
				}

				return wjDFMicroJack;
			} catch (java.lang.Exception e) {
				e.printStackTrace();
			}
		}

		return null;
	}

	@Override public org.drip.state.estimator.PredictorResponseWeightConstraint generateCalibPRLC (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final org.drip.state.representation.LatentStateMetricMeasure lsmm)
	{
		if (null == valParams || null == lsmm) return null;

		double dblValueDate = valParams.valueDate();

		if (dblValueDate >= _dblMaturity) return null;

		java.lang.String strLatentState = lsmm.id();

		java.lang.String[] astrManifestMeasure = lsmm.manifestMeasures();

		java.lang.String strQuantificationMetric = lsmm.quantificationMetric();

		if (org.drip.analytics.rates.DiscountCurve.LATENT_STATE_DISCOUNT.equalsIgnoreCase (strLatentState) &&
			org.drip.analytics.rates.DiscountCurve.QUANTIFICATION_METRIC_DISCOUNT_FACTOR.equalsIgnoreCase
				(strQuantificationMetric)) {
			try {
				org.drip.analytics.rates.RatesLSMM ratesLSMM = (org.drip.analytics.rates.RatesLSMM) lsmm;

				org.drip.analytics.rates.TurnListDiscountFactor tldf = ratesLSMM.turnsDiscount();

				double dblTurnDF = null == tldf ? 1. : tldf.turnAdjust (dblValueDate, _dblMaturity);

				if (org.drip.quant.common.StringUtil.MatchInStringArray (astrManifestMeasure, new
					java.lang.String[] {"Price"}, false)) {
					org.drip.state.estimator.PredictorResponseWeightConstraint prlc = new
						org.drip.state.estimator.PredictorResponseWeightConstraint();

					return prlc.addPredictorResponseWeight (_dblMaturity, dblTurnDF) && prlc.updateValue
						(0.01 * ratesLSMM.measureQuoteValue ("Price")) &&
							prlc.addDResponseWeightDManifestMeasure ("Price", _dblMaturity, 0.) &&
								prlc.updateDValueDManifestMeasure ("Price", 0.01) ? prlc : null;
				}

				if (org.drip.quant.common.StringUtil.MatchInStringArray (astrManifestMeasure, new
					java.lang.String[] {"PV"}, false)) {
					org.drip.state.estimator.PredictorResponseWeightConstraint prlc = new
						org.drip.state.estimator.PredictorResponseWeightConstraint();

					return prlc.addPredictorResponseWeight (_dblMaturity, dblTurnDF) && prlc.updateValue
						(ratesLSMM.measureQuoteValue ("PV")) && prlc.addDResponseWeightDManifestMeasure
							("PV", _dblMaturity, 0.) && prlc.updateDValueDManifestMeasure ("PV", 1.) ? prlc :
								null;
				}

				if (org.drip.quant.common.StringUtil.MatchInStringArray (astrManifestMeasure, new
					java.lang.String[] {"Rate"}, false)) {
					org.drip.state.estimator.PredictorResponseWeightConstraint prlc = new
						org.drip.state.estimator.PredictorResponseWeightConstraint();

					double dblDCF = org.drip.analytics.daycount.Convention.YearFraction (_dblEffective,
						_dblMaturity, _strDayCount, false, _dblMaturity, null, _strCalendar);

					double dblDF = 1. / (1. + ratesLSMM.measureQuoteValue ("Rate") * dblDCF);

					return prlc.addPredictorResponseWeight (_dblMaturity, dblTurnDF) && prlc.updateValue
						(dblDF) && prlc.addDResponseWeightDManifestMeasure ("Rate", _dblMaturity, 0.) &&
							prlc.updateDValueDManifestMeasure ("Rate", -1. * dblDCF * dblDF * dblDF) ? prlc :
								null;
				}
			} catch (java.lang.Exception e) {
				e.printStackTrace();
			}
		}

		if (org.drip.analytics.rates.ForwardCurve.LATENT_STATE_FORWARD.equalsIgnoreCase (strLatentState) &&
			org.drip.analytics.rates.ForwardCurve.QUANTIFICATION_METRIC_FORWARD_RATE.equalsIgnoreCase
				(strQuantificationMetric)) {
			if (org.drip.quant.common.StringUtil.MatchInStringArray (astrManifestMeasure, new
				java.lang.String[] {"Forward", "ForwardRate", "Rate"}, false)) {
				org.drip.state.estimator.PredictorResponseWeightConstraint prlc = new
					org.drip.state.estimator.PredictorResponseWeightConstraint();

				try {
					return prlc.addPredictorResponseWeight (_dblMaturity, 1.) && prlc.updateValue
						(lsmm.measureQuoteValue ("ForwardRate")) && prlc.addDResponseWeightDManifestMeasure
							("ForwardRate", _dblMaturity, 1.) && prlc.updateDValueDManifestMeasure
								("ForwardRate", 1.) ? prlc : null;
				} catch (java.lang.Exception e) {
					e.printStackTrace();
				}
			}
		}

		return null;
	}

	@Override public byte[] serialize()
	{
		java.lang.StringBuffer sb = new java.lang.StringBuffer();

		sb.append (org.drip.service.stream.Serializer.VERSION + fieldDelimiter());

		sb.append (_dblNotional + fieldDelimiter());

		if (null == _strCurrency || _strCurrency.isEmpty())
			sb.append (org.drip.service.stream.Serializer.NULL_SER_STRING + fieldDelimiter());
		else
			sb.append (_strCurrency + fieldDelimiter());

		if (null == _strCode || _strCode.isEmpty())
			sb.append (org.drip.service.stream.Serializer.NULL_SER_STRING + fieldDelimiter());
		else
			sb.append (_strCode + fieldDelimiter());

		if (null == _strDayCount || _strDayCount.isEmpty())
			sb.append (org.drip.service.stream.Serializer.NULL_SER_STRING + fieldDelimiter());
		else
			sb.append (_strDayCount + fieldDelimiter());

		if (null == _strCalendar || _strCalendar.isEmpty())
			sb.append (org.drip.service.stream.Serializer.NULL_SER_STRING + fieldDelimiter());
		else
			sb.append (_strCalendar + fieldDelimiter());

		sb.append (_dblMaturity + fieldDelimiter());

		sb.append (_dblEffective + fieldDelimiter());

		if (null == _settleParams)
			sb.append (org.drip.service.stream.Serializer.NULL_SER_STRING + fieldDelimiter());
		else
			sb.append (new java.lang.String (_settleParams.serialize()) + fieldDelimiter());

		if (null == _fri)
			sb.append (org.drip.service.stream.Serializer.NULL_SER_STRING);
		else
			sb.append (new java.lang.String (_fri.serialize()));

		return sb.append (objectTrailer()).toString().getBytes();
	}

	@Override public org.drip.service.stream.Serializer deserialize (
		final byte[] ab)
	{
		try {
			return new DepositComponent (ab);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public static final void main (
		final java.lang.String[] astrArgs)
		throws java.lang.Exception
	{
		DepositComponent deposit = new DepositComponent (org.drip.analytics.date.JulianDate.Today(),
			org.drip.analytics.date.JulianDate.Today().addTenor ("1Y"),
				org.drip.product.params.FloatingRateIndex.Create ("USD-LIBOR-3M"), "AUD", "Act/360", "BMA");

		byte[] abDeposit = deposit.serialize();

		System.out.println (new java.lang.String (abDeposit));

		DepositComponent depositDeser = new DepositComponent (abDeposit);

		System.out.println (new java.lang.String (depositDeser.serialize()));
	}
}
