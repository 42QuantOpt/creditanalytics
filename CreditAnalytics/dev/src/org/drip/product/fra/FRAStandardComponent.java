
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

public class FRAStandardComponent extends org.drip.product.definition.RatesComponent {
	private double _dblNotional = 1.;
	private java.lang.String _strIR = "";
	private java.lang.String _strCode = "";
	private java.lang.String _strDayCount = "";
	private java.lang.String _strCalendar = "USD";
	private double _dblStrike = java.lang.Double.NaN;
	private double _dblEffectiveDate = java.lang.Double.NaN;
	private org.drip.analytics.date.JulianDate _dtMaturity = null;
	private org.drip.product.params.FloatingRateIndex _fri = null;
	private org.drip.param.valuation.CashSettleParams _settleParams = null;

	@Override protected org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> calibMeasures (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.definition.ComponentMarketParams mktParams,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams)
	{
		return null;
	}

	/**
	 * FRAStandardComponent constructor
	 * 
	 * @param dblNotional Component Notional
	 * @param strIR IR Curve
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
		final java.lang.String strIR,
		final java.lang.String strCode,
		final java.lang.String strCalendar,
		final double dblEffectiveDate,
		final org.drip.product.params.FloatingRateIndex fri,
		final double dblStrike,
		java.lang.String strDayCount)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (_dblNotional = dblNotional) || 0. == _dblNotional ||
			null == (_strIR = strIR) || _strIR.isEmpty() || null == (_strCode = strCode) ||
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
			(getObjectTrailer()));

		if (null == strSerializedFRAComponent || strSerializedFRAComponent.isEmpty())
			throw new java.lang.Exception ("FRAStandardComponent de-serializer: Cannot locate state");

		java.lang.String[] astrField = org.drip.quant.common.StringUtil.Split (strSerializedFRAComponent,
			getFieldDelimiter());

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
			_strIR = astrField[2];
		else
			_strIR = "";

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
			_fri = new org.drip.product.params.FloatingRateIndex (astrField[6].getBytes());

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

	@Override public java.lang.String getPrimaryCode()
	{
		return _strCode;
	}

	@Override public void setPrimaryCode (
		final java.lang.String strCode)
	{
		_strCode = strCode;
	}

	@Override public java.lang.String componentName()
	{
		return "FRA=" + _fri.fullyQualifiedName();
	}

	@Override public java.lang.String getTreasuryCurveName()
	{
		return "";
	}

	@Override public java.lang.String getEDSFCurveName()
	{
		return "";
	}

	@Override public double getInitialNotional()
	{
		return _dblNotional;
	}

	@Override public double getNotional (
		final double dblDate)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblDate) || dblDate < _dblEffectiveDate || dblDate >
			_dtMaturity.getJulian())
			throw new java.lang.Exception ("FRAStandardComponent::getNotional => Bad date into getNotional");

		return 1.;
	}

	@Override public double getNotional (
		final double dblDate1,
		final double dblDate2)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblDate1) || !org.drip.quant.common.NumberUtil.IsValid
			(dblDate2) || dblDate1 < _dblEffectiveDate || dblDate2 < _dblEffectiveDate)
			throw new java.lang.Exception ("FRAStandardComponent::getNotional => Bad date into getNotional");

		double dblMaturity = _dtMaturity.getJulian();

		if (dblDate1 > dblMaturity || dblDate2 > dblMaturity)
			throw new java.lang.Exception ("FRAStandardComponent::getNotional => Bad date into getNotional");

		return 1.;
	}

	@Override public double getCoupon (
		final double dblValue,
		final org.drip.param.definition.ComponentMarketParams mktParams)
		throws java.lang.Exception
	{
		return 0.;
	}

	@Override public boolean setCurves (
		final java.lang.String strIR,
		final java.lang.String strIRTSY,
		final java.lang.String strCC)
	{
		if (null == strIR || strIR.isEmpty()) return false;

		_strIR = strIR;
		return true;
	}

	@Override public java.lang.String getIRCurveName()
	{
		return _strIR;
	}

	@Override public java.lang.String[] getForwardCurveName()
	{
		return new java.lang.String[] {_fri.fullyQualifiedName()};
	}

	@Override public java.lang.String creditCurveName()
	{
		return "";
	}

	@Override public org.drip.analytics.date.JulianDate getEffectiveDate()
	{
		try {
			return new org.drip.analytics.date.JulianDate (_dblEffectiveDate);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override public org.drip.analytics.date.JulianDate getMaturityDate()
	{
		return _dtMaturity;
	}

	@Override public org.drip.analytics.date.JulianDate getFirstCouponDate()
	{
		return getMaturityDate();
	}

	@Override public java.util.List<org.drip.analytics.period.CashflowPeriod> getCashFlowPeriod()
	{
		try {
			return org.drip.analytics.period.CashflowPeriod.GetSinglePeriod (_dblEffectiveDate, new
				org.drip.analytics.date.JulianDate (_dblEffectiveDate).addTenor (_fri.tenor()).getJulian(),
					_strCalendar);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override public org.drip.param.valuation.CashSettleParams getCashSettleParams()
	{
		return _settleParams;
	}

	@Override public org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> value (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.definition.ComponentMarketParams mktParams,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams)
	{
		if (null == valParams || null == mktParams) return null;

		long lStart = System.nanoTime();

		double dblParForward = java.lang.Double.NaN;

		double dblValueDate = valParams.valueDate();

		if (dblValueDate > _dblEffectiveDate) return null;

		double dblMaturity = _dtMaturity.getJulian();

		org.drip.analytics.rates.DiscountCurve dc = mktParams.fundingCurve();

		if (null == dc) return null;

		org.drip.analytics.rates.ForwardRateEstimator fc = mktParams.forwardCurve (_fri);

		if (null == fc || !_fri.match (fc.index())) return null;

		java.lang.String strFRI = _fri.fullyQualifiedName();

		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapResult = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>();

		try {
			double dblCashSettle = null == _settleParams ? valParams.cashPayDate() :
				_settleParams.cashSettleDate (dblValueDate);

			java.util.Map<org.drip.analytics.date.JulianDate,
				org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>> mapFixings =
					mktParams.getFixings();

			if (null != mapFixings && mapFixings.containsKey (_dtMaturity)) {
				org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapFixing =
					mapFixings.get (_dtMaturity);

				dblParForward = null != mapFixing && mapFixing.containsKey (strFRI) ? mapFixing.get (strFRI)
					: fc.forward (dblMaturity);
			} else
				dblParForward = fc.forward (dblMaturity);

			double dblMultiplicativeQuantoAdjustment =
				org.drip.analytics.support.OptionHelper.MultiplicativeCrossVolQuanto (mktParams, strFRI,
					"ForwardToDomesticExchangeVolatility", "FRIForwardToDomesticExchangeCorrelation",
						dblValueDate, _dblEffectiveDate);

			double dblDCF = org.drip.analytics.daycount.Convention.YearFraction (_dblEffectiveDate,
				dblMaturity, _strDayCount, false, dblMaturity, null, _strCalendar);

			double dblQuantoAdjustedParForward = dblParForward * dblMultiplicativeQuantoAdjustment;

			double dblPV = dc.df (dblMaturity) / dc.df (dblCashSettle) * _dblNotional *
				(dblQuantoAdjustedParForward - _dblStrike);

			double dblDCParForward = dc.libor (_dblEffectiveDate, dblMaturity);

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

	@Override public java.util.Set<java.lang.String> getMeasureNames()
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
		final org.drip.param.definition.ComponentMarketParams mktParams,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams)
	{
		if (null == valParams || null == mktParams || null == mktParams.fundingCurve())
			return null;

		return null;
	}

	@Override public org.drip.quant.calculus.WengertJacobian calcQuoteDFMicroJack (
		final java.lang.String strQuote,
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.definition.ComponentMarketParams mktParams,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams)
	{
		if (null == valParams || null == strQuote || null == mktParams || null ==
			mktParams.fundingCurve())
			return null;

		return null;
	}

	@Override public org.drip.state.estimator.PredictorResponseWeightConstraint generateCalibPRLC (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.definition.ComponentMarketParams mktParams,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final org.drip.state.representation.LatentStateMetricMeasure lsmm)
	{
		if (null == valParams || null == lsmm) return null;

		if (org.drip.analytics.rates.ForwardCurve.LATENT_STATE_FORWARD.equalsIgnoreCase (lsmm.getID()) &&
			org.drip.analytics.rates.ForwardCurve.QUANTIFICATION_METRIC_FORWARD_RATE.equalsIgnoreCase
				(lsmm.getQuantificationMetric())) {
			if (org.drip.quant.common.StringUtil.MatchInStringArray (lsmm.getManifestMeasures(), new
				java.lang.String[] {"Forward", "ForwardRate", "ParForward", "ParForwardRate", "Rate"},
					false)) {
				org.drip.state.estimator.PredictorResponseWeightConstraint prlc = new
					org.drip.state.estimator.PredictorResponseWeightConstraint();

				double dblMaturity = _dtMaturity.getJulian();

				return prlc.addPredictorResponseWeight (dblMaturity, 1.) && prlc.updateValue
					(lsmm.getMeasureQuoteValue()) && prlc.addDResponseWeightDManifestMeasure
						("ParForwardRate", dblMaturity, 1.) && prlc.updateDValueDManifestMeasure
							("ParForwardRate", 1.) ? prlc : null;
			}
		}

		return null;
	}

	@Override public byte[] serialize()
	{
		java.lang.StringBuffer sb = new java.lang.StringBuffer();

		sb.append (org.drip.service.stream.Serializer.VERSION + getFieldDelimiter());

		sb.append (_dblNotional + getFieldDelimiter());

		sb.append (_strIR + getFieldDelimiter());

		sb.append (_strCode + getFieldDelimiter());

		sb.append (_strCalendar + getFieldDelimiter());

		sb.append (_dblEffectiveDate + getFieldDelimiter());

		if (null == _fri)
			sb.append (org.drip.service.stream.Serializer.NULL_SER_STRING + getFieldDelimiter());
		else
			sb.append (new java.lang.String (_fri.serialize()) + getFieldDelimiter());

		if (null == _settleParams)
			sb.append (org.drip.service.stream.Serializer.NULL_SER_STRING + getFieldDelimiter());
		else
			sb.append (new java.lang.String (_settleParams.serialize()) + getFieldDelimiter());

		return sb.append (_dblStrike + getObjectTrailer()).toString().getBytes();
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

	public org.drip.product.params.FloatingRateIndex fri()
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
			org.drip.analytics.date.JulianDate.Today().getJulian(),
				org.drip.product.params.FloatingRateIndex.Create ("JPY-LIBOR-6M"), 0.01, "Act/360");

		byte[] abFRA = fra.serialize();

		System.out.println (new java.lang.String (abFRA));

		FRAStandardComponent fraDeser = new FRAStandardComponent (abFRA);

		System.out.println (new java.lang.String (fraDeser.serialize()));
	}
}
