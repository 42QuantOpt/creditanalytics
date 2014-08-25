
package org.drip.product.fra;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2014 Lakshmi Krishnamurthy
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
 * FRAStandardComponent contains the implementation of the Standard Multi-Curve FRA product.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class FRAStandardComponent extends org.drip.product.definition.CalibratableFixedIncomeComponent {
	private double _dblNotional = 1.;
	private java.lang.String _strCode = "";
	private java.lang.String _strDayCount = "";
	private java.lang.String _strCalendar = "";
	private java.lang.String _strCurrency = "";
	private double _dblStrike = java.lang.Double.NaN;
	private double _dblEffectiveDate = java.lang.Double.NaN;
	private org.drip.state.identifier.ForwardLabel _fri = null;
	private org.drip.analytics.date.JulianDate _dtMaturity = null;
	private org.drip.param.valuation.CashSettleParams _settleParams = null;

	private org.drip.state.estimator.PredictorResponseWeightConstraint discountFactorPRWC (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final org.drip.product.calib.ProductQuoteSet pqs)
	{
		double dblValueDate = valParams.valueDate();

		if (dblValueDate > _dblEffectiveDate) return null;

		org.drip.product.calib.FRAComponentQuoteSet fcqs = (org.drip.product.calib.FRAComponentQuoteSet) pqs;

		if (!fcqs.containsFRARate()) return null;

		double dblForwardDF = java.lang.Double.NaN;

		double dblMaturity = _dtMaturity.julian();

		try {
			dblForwardDF = 1. / (1. + (fcqs.fraRate() * org.drip.analytics.daycount.Convention.YearFraction
				(_dblEffectiveDate, dblMaturity, _strDayCount, false, dblMaturity, null, _strCalendar)));
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		org.drip.state.estimator.PredictorResponseWeightConstraint prwc = new
			org.drip.state.estimator.PredictorResponseWeightConstraint();

		if (!prwc.addPredictorResponseWeight (_dblEffectiveDate, _dblNotional * dblForwardDF)) return null;

		if (!prwc.addDResponseWeightDManifestMeasure ("PV", _dblEffectiveDate, _dblNotional * dblForwardDF))
			return null;

		if (!prwc.addPredictorResponseWeight (dblMaturity, -1. * _dblNotional)) return null;

		if (!prwc.addDResponseWeightDManifestMeasure ("PV", dblMaturity, -1. * _dblNotional)) return null;

		if (!prwc.updateValue (0.)) return null;

		if (!prwc.updateDValueDManifestMeasure ("PV", 1.)) return null;

		return prwc;
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
	 * FRAStandardComponent constructor
	 * 
	 * @param dblNotional Component Notional
	 * @param strCurrency Pay Currency
	 * @param strCode FRA Product Code
	 * @param strCalendar FRA Calendar
	 * @param dblEffectiveDate FRA Effective Date
	 * @param fri FRA Floating Rate Index
	 * @param dblStrike FRA Strike
	 * @param strDayCount Day Count Convention
	 * 
	 * @throws java.lang.Exception Thrown if Inputs are Invalid
	 */

	public FRAStandardComponent (
		final double dblNotional,
		final java.lang.String strCurrency,
		final java.lang.String strCode,
		final java.lang.String strCalendar,
		final double dblEffectiveDate,
		final org.drip.state.identifier.ForwardLabel fri,
		final double dblStrike,
		java.lang.String strDayCount)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (_dblNotional = dblNotional) || 0. == _dblNotional ||
			null == (_strCurrency = strCurrency) || _strCurrency.isEmpty() || null == (_strCode = strCode) ||
				_strCode.isEmpty() || null == (_strCalendar = strCalendar) || _strCalendar.isEmpty() ||
					!org.drip.quant.common.NumberUtil.IsValid (_dblEffectiveDate = dblEffectiveDate) || null
						== (_fri = fri) || !org.drip.quant.common.NumberUtil.IsValid (_dblStrike =
							dblStrike) || null == (_strDayCount = strDayCount) || _strDayCount.isEmpty())
			throw new java.lang.Exception ("FRAStandardComponent ctr => Invalid Inputs!");

		_dtMaturity = new org.drip.analytics.date.JulianDate (_dblEffectiveDate).addTenor (_fri.tenor());
	}

	/**
	 * FRAStandardComponent de-serialization from input byte array
	 * 
	 * @param ab Byte Array
	 * 
	 * @throws java.lang.Exception Thrown if FRAStandardComponent cannot be properly de-serialized
	 */

	public FRAStandardComponent (
		final byte[] ab)
		throws java.lang.Exception
	{
		if (null == ab || 0 == ab.length)
			throw new java.lang.Exception ("FRAStandardComponent de-serializer: Invalid input Byte array");

		java.lang.String strFRAComponent = new java.lang.String (ab);

		if (null == strFRAComponent || strFRAComponent.isEmpty())
			throw new java.lang.Exception ("FRAStandardComponent de-serializer: Empty state");

		java.lang.String strSerializedFRAComponent = strFRAComponent.substring (0, strFRAComponent.indexOf
			(objectTrailer()));

		if (null == strSerializedFRAComponent || strSerializedFRAComponent.isEmpty())
			throw new java.lang.Exception ("FRAStandardComponent de-serializer: Cannot locate state");

		java.lang.String[] astrField = org.drip.quant.common.StringUtil.Split (strSerializedFRAComponent,
			fieldDelimiter());

		if (null == astrField || 9 > astrField.length)
			throw new java.lang.Exception ("FRAStandardComponent de-serializer: Invalid reqd field set");

		// double dblVersion = new java.lang.Double (astrField[0]);

		if (null == astrField[1] || astrField[1].isEmpty() ||
			org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[1]))
			throw new java.lang.Exception ("FRAStandardComponent de-serializer: Cannot locate notional");

		_dblNotional = new java.lang.Double (astrField[1]);

		if (null == astrField[2] || astrField[2].isEmpty())
			throw new java.lang.Exception
				("FRAStandardComponent de-serializer: Cannot locate IR curve name");

		if (!org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[2]))
			_strCurrency = astrField[2];
		else
			_strCurrency = "";

		if (null == astrField[3] || astrField[3].isEmpty())
			throw new java.lang.Exception ("FRAStandardComponent de-serializer: Cannot locate code");

		if (!org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[3]))
			_strCode = astrField[3];
		else
			_strCode = "";

		if (null == astrField[4] || astrField[4].isEmpty())
			throw new java.lang.Exception ("FRAStandardComponent de-serializer: Cannot locate calendar");

		if (!org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[4]))
			_strCalendar = astrField[4];
		else
			_strCalendar = "";

		if (null == astrField[5] || astrField[5].isEmpty() ||
			org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[5]))
			throw new java.lang.Exception
				("FRAStandardComponent de-serializer: Cannot locate Effective Date");

		_dblEffectiveDate = new java.lang.Double (astrField[5]);

		if (null == astrField[6] || astrField[6].isEmpty())
			throw new java.lang.Exception ("FRAStandardComponent de-serializer: Cannot locate rate index");

		if (org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[6]))
			_fri = null;
		else
			_fri = new org.drip.state.identifier.ForwardLabel (astrField[6].getBytes());

		if (null == astrField[7] || astrField[7].isEmpty())
			throw new java.lang.Exception
				("FRAStandardComponent de-serializer: Cannot locate cash settle params");

		if (org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[7]))
			_settleParams = null;
		else
			_settleParams = new org.drip.param.valuation.CashSettleParams (astrField[7].getBytes());

		if (null == astrField[8] || astrField[8].isEmpty() ||
			org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[8]))
			throw new java.lang.Exception ("FRAStandardComponent de-serializer: Cannot locate Strike");

		_dblStrike = new java.lang.Double (astrField[8]);
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
		return "FRA=" + _fri.fullyQualifiedName();
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
		if (!org.drip.quant.common.NumberUtil.IsValid (dblDate) || dblDate < _dblEffectiveDate || dblDate >
			_dtMaturity.julian())
			throw new java.lang.Exception ("FRAStandardComponent::notional => Bad date into getNotional");

		return 1.;
	}

	@Override public double notional (
		final double dblDate1,
		final double dblDate2)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblDate1) || !org.drip.quant.common.NumberUtil.IsValid
			(dblDate2) || dblDate1 < _dblEffectiveDate || dblDate2 < _dblEffectiveDate)
			throw new java.lang.Exception ("FRAStandardComponent::notional => Bad date into getNotional");

		double dblMaturity = _dtMaturity.julian();

		if (dblDate1 > dblMaturity || dblDate2 > dblMaturity)
			throw new java.lang.Exception ("FRAStandardComponent::notional => Bad date into getNotional");

		return 1.;
	}

	@Override public org.drip.analytics.output.CouponPeriodMetrics coupon (
		final double dblAccrualEndDate,
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs)
	{
		return null;
	}

	@Override public int freq()
	{
		return cashFlowPeriod().get (0).freq();
	}

	@Override public org.drip.state.identifier.ForwardLabel[] forwardLabel()
	{
		return new org.drip.state.identifier.ForwardLabel[] {_fri};
	}

	@Override public org.drip.state.identifier.CreditLabel[] creditLabel()
	{
		return null;
	}

	@Override public org.drip.state.identifier.FXLabel[] fxLabel()
	{
		return null;
	}

	@Override public org.drip.analytics.date.JulianDate effective()
	{
		try {
			return new org.drip.analytics.date.JulianDate (_dblEffectiveDate);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override public org.drip.analytics.date.JulianDate maturity()
	{
		return _dtMaturity;
	}

	@Override public org.drip.analytics.date.JulianDate firstCouponDate()
	{
		return maturity();
	}

	@Override public java.util.List<org.drip.analytics.period.CouponPeriod> cashFlowPeriod()
	{
		try {
			return org.drip.analytics.support.PeriodHelper.SinglePeriodSingleReset (_dblEffectiveDate, new
				org.drip.analytics.date.JulianDate (_dblEffectiveDate).addTenor (_fri.tenor()).julian(),
					_strDayCount, _strCalendar, _strCurrency, _strCurrency, _fri, null);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
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
		if (null == valParams || null == csqs) return null;

		org.drip.analytics.rates.DiscountCurve dcFunding = csqs.fundingCurve
			(org.drip.state.identifier.FundingLabel.Standard (couponCurrency()[0]));

		if (null == dcFunding) return null;

		long lStart = System.nanoTime();

		double dblParForward = java.lang.Double.NaN;

		double dblValueDate = valParams.valueDate();

		if (dblValueDate > _dblEffectiveDate) return null;

		double dblMaturity = _dtMaturity.julian();

		org.drip.analytics.rates.ForwardRateEstimator fc = csqs.forwardCurve (_fri);

		if (null == fc || !_fri.match (fc.index())) return null;

		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapResult = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>();

		try {
			double dblCashSettle = null == _settleParams ? valParams.cashPayDate() :
				_settleParams.cashSettleDate (dblValueDate);

			dblParForward = csqs.available (_dtMaturity, _fri) ? csqs.getFixing (_dtMaturity, _fri) :
				fc.forward (_dtMaturity);

			org.drip.analytics.date.JulianDate dtEffective = effective();

			double dblMultiplicativeQuantoAdjustment = java.lang.Math.exp (-1. *
				org.drip.analytics.support.OptionHelper.IntegratedCrossVolQuanto (csqs.forwardCurveVolSurface
					(_fri), csqs.customMetricVolSurface (org.drip.state.identifier.CustomMetricLabel.Standard
						("ForwardToDomesticExchangeVolatility"), dtEffective), csqs.customMetricVolSurface
							(org.drip.state.identifier.CustomMetricLabel.Standard
								("FRIForwardToDomesticExchangeCorrelation"), dtEffective), dblValueDate,
									_dblEffectiveDate));

			double dblDCF = org.drip.analytics.daycount.Convention.YearFraction (_dblEffectiveDate,
				dblMaturity, _strDayCount, false, dblMaturity, null, _strCalendar);

			double dblQuantoAdjustedParForward = dblParForward * dblMultiplicativeQuantoAdjustment;

			double dblPV = dcFunding.df (dblMaturity) / dcFunding.df (dblCashSettle) * _dblNotional *
				(dblQuantoAdjustedParForward - _dblStrike);

			double dblDCParForward = dcFunding.libor (_dblEffectiveDate, dblMaturity);

			mapResult.put ("additivequantoadjustment", dblQuantoAdjustedParForward - dblParForward);

			mapResult.put ("discountcurveadditivebasis", dblQuantoAdjustedParForward - dblDCParForward);

			mapResult.put ("discountcurvemultiplicativebasis", dblQuantoAdjustedParForward /
				dblDCParForward);

			mapResult.put ("discountcurveparforward", dblDCParForward);

			mapResult.put ("forward", dblParForward);

			mapResult.put ("forwardrate", dblParForward);

			mapResult.put ("mercuriorfactor", (dblDCF * dblDCParForward + 1.) / (dblDCF *
				dblQuantoAdjustedParForward + 1.));

			mapResult.put ("multiplicativequantoadjustment", dblMultiplicativeQuantoAdjustment);

			mapResult.put ("parforward", dblParForward);

			mapResult.put ("parforwardrate", dblParForward);

			mapResult.put ("price", dblPV);

			mapResult.put ("pv", dblPV);

			mapResult.put ("quantoadjustedparforward", dblQuantoAdjustedParForward);

			mapResult.put ("upfront", dblPV);
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		mapResult.put ("calctime", (System.nanoTime() - lStart) * 1.e-09);

		return mapResult;
	}

	@Override public java.util.Set<java.lang.String> measureNames()
	{
		java.util.Set<java.lang.String> setstrMeasureNames = new java.util.TreeSet<java.lang.String>();

		setstrMeasureNames.add ("AdditiveQuantoAdjustment");

		setstrMeasureNames.add ("CalcTime");

		setstrMeasureNames.add ("DiscountCurveAdditiveBasis");

		setstrMeasureNames.add ("DiscountCurveMultiplicativeBasis");

		setstrMeasureNames.add ("DiscountCurveParForward");

		setstrMeasureNames.add ("Forward");

		setstrMeasureNames.add ("ForwardRate");

		setstrMeasureNames.add ("MercurioRFactor");

		setstrMeasureNames.add ("MultiplicativeQuantoAdjustment");

		setstrMeasureNames.add ("ParForward");

		setstrMeasureNames.add ("ParForwardRate");

		setstrMeasureNames.add ("Price");

		setstrMeasureNames.add ("PV");

		setstrMeasureNames.add ("QuantoAdjustedParForward");

		setstrMeasureNames.add ("Upfront");

		return setstrMeasureNames;
	}

	@Override public org.drip.quant.calculus.WengertJacobian jackDDirtyPVDManifestMeasure (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams)
	{
		return null;
	}

	@Override public org.drip.quant.calculus.WengertJacobian manifestMeasureDFMicroJack (
		final java.lang.String strManifestMeasure,
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams)
	{
		return null;
	}

	@Override public org.drip.product.calib.ProductQuoteSet calibQuoteSet (
		final org.drip.state.representation.LatentStateSpecification[] aLSS)
	{
		try {
			return new org.drip.product.calib.FRAComponentQuoteSet (aLSS);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override public org.drip.state.estimator.PredictorResponseWeightConstraint fundingPRWC (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final org.drip.product.calib.ProductQuoteSet pqs)
	{
		return null;

		/*
		if (null == valParams || null == pqs || !(pqs instanceof org.drip.product.calib.FRAComponentQuoteSet)
			|| !pqs.contains (org.drip.analytics.rates.DiscountCurve.LATENT_STATE_DISCOUNT,
				org.drip.analytics.rates.DiscountCurve.QUANTIFICATION_METRIC_DISCOUNT_FACTOR,
					org.drip.state.identifier.FundingLabel.Standard (_strCurrency)))
			return null;

		return discountFactorPRWC (valParams, pricerParams, csqs, quotingParams, pqs); */
	}

	@Override public org.drip.state.estimator.PredictorResponseWeightConstraint forwardPRWC (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final org.drip.product.calib.ProductQuoteSet pqs)
	{
		if (null == valParams || null == pqs || !(pqs instanceof
			org.drip.product.calib.FRAComponentQuoteSet))
			return null;

		if (valParams.valueDate() > _dblEffectiveDate) return null;

		org.drip.product.calib.FRAComponentQuoteSet fcqs = (org.drip.product.calib.FRAComponentQuoteSet) pqs;

		if (!fcqs.containsFRARate() && !fcqs.containsParForwardRate()) return null;

		double dblForwardRate = java.lang.Double.NaN;

		try {
			if (fcqs.containsParForwardRate())
				dblForwardRate = fcqs.parForwardRate();
			else if (fcqs.containsFRARate())
				dblForwardRate = fcqs.fraRate();
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		org.drip.state.estimator.PredictorResponseWeightConstraint prwc = new
			org.drip.state.estimator.PredictorResponseWeightConstraint();

		double dblMaturity = _dtMaturity.julian();

		if (!prwc.addPredictorResponseWeight (dblMaturity, 1.)) return null;

		if (!prwc.addDResponseWeightDManifestMeasure ("Rate", dblMaturity, 1.)) return null;

		if (!prwc.updateValue (dblForwardRate)) return null;

		if (!prwc.updateDValueDManifestMeasure ("Rate", 1.)) return null;

		return prwc;
	}

	@Override public org.drip.state.estimator.PredictorResponseWeightConstraint fundingForwardPRWC (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final org.drip.product.calib.ProductQuoteSet pqs)
	{
		if (null == valParams || null == pqs || !(pqs instanceof org.drip.product.calib.FRAComponentQuoteSet)
			|| !pqs.contains (org.drip.analytics.rates.DiscountCurve.LATENT_STATE_DISCOUNT,
				org.drip.analytics.rates.DiscountCurve.QUANTIFICATION_METRIC_DISCOUNT_FACTOR,
					org.drip.state.identifier.FundingLabel.Standard (_strCurrency)) || !pqs.contains
						(org.drip.analytics.rates.ForwardCurve.LATENT_STATE_FORWARD,
							org.drip.analytics.rates.ForwardCurve.QUANTIFICATION_METRIC_FORWARD_RATE, _fri))
			return null;

		org.drip.state.estimator.PredictorResponseWeightConstraint prwc = discountFactorPRWC (valParams,
			pricerParams, csqs, quotingParams, pqs);

		return null != prwc && prwc.addMergeLabel (_fri) ? prwc : null;
	}

	@Override public byte[] serialize()
	{
		java.lang.StringBuffer sb = new java.lang.StringBuffer();

		sb.append (org.drip.service.stream.Serializer.VERSION + fieldDelimiter());

		sb.append (_dblNotional + fieldDelimiter());

		sb.append (_strCurrency + fieldDelimiter());

		sb.append (_strCode + fieldDelimiter());

		sb.append (_strCalendar + fieldDelimiter());

		sb.append (_dblEffectiveDate + fieldDelimiter());

		if (null == _fri)
			sb.append (org.drip.service.stream.Serializer.NULL_SER_STRING + fieldDelimiter());
		else
			sb.append (new java.lang.String (_fri.serialize()) + fieldDelimiter());

		if (null == _settleParams)
			sb.append (org.drip.service.stream.Serializer.NULL_SER_STRING + fieldDelimiter());
		else
			sb.append (new java.lang.String (_settleParams.serialize()) + fieldDelimiter());

		return sb.append (_dblStrike + objectTrailer()).toString().getBytes();
	}

	@Override public org.drip.service.stream.Serializer deserialize (
		final byte[] ab)
	{
		try {
			return new FRAStandardComponent (ab);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Retrieve the Floating Rate Index
	 * 
	 * @return The Floating Rate Index
	 */

	public org.drip.state.identifier.ForwardLabel fri()
	{
		return _fri;
	}

	/**
	 * Retrieve the FRA Strike
	 * 
	 * @return The FRA Strike
	 */

	public double strike()
	{
		return _dblStrike;
	}

	/**
	 * Retrieve the Day Count
	 * 
	 * @return The Day Count
	 */

	public java.lang.String dayCount()
	{
		return _strDayCount;
	}

	/**
	 * Retrieve the Calendar
	 * 
	 * @return The Calendar
	 */

	public java.lang.String calendar()
	{
		return _strCalendar;
	}

	public static final void main (
		final java.lang.String[] astrArgs)
		throws java.lang.Exception
	{
		FRAStandardComponent fra = new FRAStandardComponent (1., "JPY", "JPY-FRA-3M", "JPY",
			org.drip.analytics.date.JulianDate.Today().julian(),
				org.drip.state.identifier.ForwardLabel.Standard ("JPY-LIBOR-6M"), 0.01, "Act/360");

		byte[] abFRA = fra.serialize();

		System.out.println (new java.lang.String (abFRA));

		FRAStandardComponent fraDeser = new FRAStandardComponent (abFRA);

		System.out.println (new java.lang.String (fraDeser.serialize()));
	}
}
