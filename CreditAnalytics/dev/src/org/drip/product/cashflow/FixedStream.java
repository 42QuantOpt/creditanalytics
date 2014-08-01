
package org.drip.product.cashflow;

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
 * FixedStream contains an implementation of the Fixed leg cash flow stream. It exports the following
 * functionality:
 *  - Standard/Custom Constructor for the FixedStream Component
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

public class FixedStream extends org.drip.product.definition.RatesComponent {
	private double _dblNotional = 1.;
	private double _dblCoupon = 0.0001;
	private java.lang.String _strCode = "";
	private java.lang.String _strCurrency = "";
	private double _dblMaturity = java.lang.Double.NaN;
	private double _dblEffective = java.lang.Double.NaN;
	private org.drip.product.params.FXMTMSetting _fxmtm = null;
	private org.drip.product.params.FactorSchedule _notlSchedule = null;
	private org.drip.param.valuation.CashSettleParams _settleParams = null;
	private java.util.List<org.drip.analytics.period.CashflowPeriod> _lsCouponPeriod = null;

	private org.drip.state.estimator.PredictorResponseWeightConstraint discountFactorPRWC (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final org.drip.product.calib.ProductQuoteSet pqs)
	{
		double dblValueDate = valParams.valueDate();

		if (dblValueDate >= _dblMaturity) return null;

		double dblPV = 0.;
		double dblCoupon = _dblCoupon;
		org.drip.product.calib.FixedStreamQuoteSet fsqs = (org.drip.product.calib.FixedStreamQuoteSet) pqs;

		try {
			if (fsqs.containsPV()) dblPV = fsqs.pv();

			if (fsqs.containsCoupon()) dblCoupon = fsqs.coupon();
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		org.drip.state.estimator.PredictorResponseWeightConstraint prwc = new
			org.drip.state.estimator.PredictorResponseWeightConstraint();

		for (org.drip.analytics.period.CashflowPeriod period : _lsCouponPeriod) {
			double dblPeriodEndDate = period.end();

			if (dblPeriodEndDate < dblValueDate) continue;

			try {
				double dblPeriodCV100 = _dblNotional * notional (dblPeriodEndDate) * (period.contains
					(dblValueDate) ? period.accrualDCF (dblValueDate) : period.couponDCF()) * dblCoupon;

				double dblPeriodPayDate = period.pay();

				if (!prwc.addPredictorResponseWeight (dblPeriodPayDate, dblPeriodCV100)) return null;

				if (!prwc.addDResponseWeightDManifestMeasure ("PV", dblPeriodPayDate, dblPeriodCV100))
					return null;
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}
		}

		if (prwc.updateValue (dblPV)) return null;

		if (prwc.updateDValueDManifestMeasure ("PV", 1.)) return null;

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
	 * Full-featured instantiation of the Fixed Stream instance
	 * 
	 * @param strCurrency => Pay Currency
	 * @param fxmtm FX MTM setting
	 * @param dblCoupon Fixed Coupon
	 * @param dblNotional => Notional Amount
	 * @param notlSchedule => Notional Schedule
	 * @param lsCouponPeriod => List of the Coupon Periods
	 * 
	 * @throws java.lang.Exception Thrown if the inputs are invalid
	 */

	public FixedStream (
		final java.lang.String strCurrency,
		final org.drip.product.params.FXMTMSetting fxmtm,
		final double dblCoupon,
		final double dblNotional,
		final org.drip.product.params.FactorSchedule notlSchedule,
		final java.util.List<org.drip.analytics.period.CashflowPeriod> lsCouponPeriod)
		throws java.lang.Exception
	{
		if (null == (_strCurrency = strCurrency) || _strCurrency.isEmpty() ||
			!org.drip.quant.common.NumberUtil.IsValid (_dblCoupon = dblCoupon) ||
				!org.drip.quant.common.NumberUtil.IsValid (_dblNotional = dblNotional) || 0. == _dblNotional
					|| null == (_lsCouponPeriod = lsCouponPeriod))
			throw new java.lang.Exception ("FixedStream ctr: Invalid Params!");

		int iNumCouponPeriod = _lsCouponPeriod.size();

		if (0 == iNumCouponPeriod) throw new java.lang.Exception ("FixedStream ctr: Invalid Params!");

		if (null == (_notlSchedule = notlSchedule))
			_notlSchedule = org.drip.product.params.FactorSchedule.CreateBulletSchedule();

		_fxmtm = fxmtm;

		_dblEffective = _lsCouponPeriod.get (0).start();

		_dblMaturity = _lsCouponPeriod.get (iNumCouponPeriod - 1).end();

		_strCode = _strCurrency + "::" + (12 / _lsCouponPeriod.get (0).freq()) + "M::" + new
			org.drip.analytics.date.JulianDate (_dblMaturity);
	}

	/**
	 * FixedStream de-serialization from input byte array
	 * 
	 * @param ab Byte Array
	 * 
	 * @throws java.lang.Exception Thrown if FixedStream cannot be properly de-serialized
	 */

	public FixedStream (
		final byte[] ab)
		throws java.lang.Exception
	{
		if (null == ab || 0 == ab.length)
			throw new java.lang.Exception ("FixedStream de-serializer: Invalid input Byte array");

		java.lang.String strRawString = new java.lang.String (ab);

		if (null == strRawString || strRawString.isEmpty())
			throw new java.lang.Exception ("FixedStream de-serializer: Empty state");

		java.lang.String strSerializedFixedStream = strRawString.substring (0, strRawString.indexOf
			(objectTrailer()));

		if (null == strSerializedFixedStream || strSerializedFixedStream.isEmpty())
			throw new java.lang.Exception ("FixedStream de-serializer: Cannot locate state");

		java.lang.String[] astrField = org.drip.quant.common.StringUtil.Split (strSerializedFixedStream,
			fieldDelimiter());

		if (null == astrField || 10 > astrField.length)
			throw new java.lang.Exception ("FixedStream de-serializer: Invalid reqd field set");

		// double dblVersion = new java.lang.Double (astrField[0]);

		if (null == astrField[1] || astrField[1].isEmpty() ||
			org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[1]))
			throw new java.lang.Exception ("FixedStream de-serializer: Cannot locate notional");

		_dblNotional = new java.lang.Double (astrField[1]);

		if (null == astrField[2] || astrField[2].isEmpty() ||
			org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[2]))
			throw new java.lang.Exception ("FixedStream de-serializer: Cannot locate coupon");

		_dblCoupon = new java.lang.Double (astrField[2]);

		if (null == astrField[3] || astrField[3].isEmpty())
			throw new java.lang.Exception ("FixedStream de-serializer: Cannot locate IR curve name");

		if (!org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[3]))
			_strCurrency = astrField[3];
		else
			_strCurrency = "";

		if (null == astrField[4] || astrField[4].isEmpty())
			throw new java.lang.Exception ("FixedStream de-serializer: Cannot locate code");

		if (!org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[4]))
			_strCode = astrField[4];
		else
			_strCode = "";

		if (null == astrField[5] || astrField[5].isEmpty() ||
			org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[5]))
			throw new java.lang.Exception ("FixedStream de-serializer: Cannot locate maturity date");

		_dblMaturity = new java.lang.Double (astrField[5]);

		if (null == astrField[6] || astrField[6].isEmpty() ||
			org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[6]))
			throw new java.lang.Exception ("FixedStream de-serializer: Cannot locate effective date");

		_dblEffective = new java.lang.Double (astrField[6]);

		if (null == astrField[7] || astrField[7].isEmpty())
			throw new java.lang.Exception ("FixedStream de-serializer: Cannot locate notional schedule");

		if (org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[7]))
			_notlSchedule = null;
		else
			_notlSchedule = new org.drip.product.params.FactorSchedule (astrField[7].getBytes());

		if (null == astrField[8] || astrField[8].isEmpty())
			throw new java.lang.Exception ("FixedStream de-serializer: Cannot locate cash settle params");

		if (org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[8]))
			_settleParams = null;
		else
			_settleParams = new org.drip.param.valuation.CashSettleParams (astrField[8].getBytes());

		if (null == astrField[9] || astrField[9].isEmpty())
			throw new java.lang.Exception ("FixedStream de-serializer: Cannot locate the periods");

		if (org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrField[9]))
			_lsCouponPeriod = null;
		else {
			java.lang.String[] astrRecord = org.drip.quant.common.StringUtil.Split (astrField[9],
				collectionRecordDelimiter());

			if (null != astrRecord && 0 != astrRecord.length) {
				for (int i = 0; i < astrRecord.length; ++i) {
					if (null == astrRecord[i] || astrRecord[i].isEmpty() ||
						org.drip.service.stream.Serializer.NULL_SER_STRING.equalsIgnoreCase (astrRecord[i]))
						continue;

					if (null == _lsCouponPeriod)
						_lsCouponPeriod = new java.util.ArrayList<org.drip.analytics.period.CashflowPeriod>();

					_lsCouponPeriod.add (new org.drip.analytics.period.CashflowPeriod
						(astrRecord[i].getBytes()));
				}
			}
		}
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
		return _strCode;
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
			throw new java.lang.Exception ("FixedStream::notional => Bad date into getNotional");

		return null == _notlSchedule ? 1. : _notlSchedule.getFactor (dblDate);
	}

	@Override public double notional (
		final double dblDate1,
		final double dblDate2)
		throws java.lang.Exception
	{
		if (null == _notlSchedule || !org.drip.quant.common.NumberUtil.IsValid (dblDate1) ||
			!org.drip.quant.common.NumberUtil.IsValid (dblDate2))
			throw new java.lang.Exception ("FixedStream::notional => Bad date into getNotional");

		return _notlSchedule.getFactor (dblDate1, dblDate2);
	}

	@Override public org.drip.analytics.output.PeriodCouponMeasures coupon (
		final double dblAccrualEndDate,
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs)
	{
		return org.drip.analytics.output.PeriodCouponMeasures.Nominal (_dblCoupon);
	}

	@Override public org.drip.state.identifier.ForwardLabel[] forwardLabel()
	{
		return null;
	}

	@Override public org.drip.state.identifier.CreditLabel[] creditLabel()
	{
		return null;
	}

	@Override public org.drip.state.identifier.FXLabel[] fxLabel()
	{
		if (null == _fxmtm) return null;

		org.drip.product.params.CurrencyPair cp = _fxmtm.currencyPair();

		return null == cp ? null : new org.drip.state.identifier.FXLabel[]
			{org.drip.state.identifier.FXLabel.Standard (cp.code())};
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
		try {
			return new org.drip.analytics.date.JulianDate (_lsCouponPeriod.get (0).end());
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override public java.util.List<org.drip.analytics.period.CashflowPeriod> cashFlowPeriod()
	{
		return _lsCouponPeriod;
	}

	@Override public int freq()
	{
		return cashFlowPeriod().get (0).freq();
	}

	@Override public org.drip.param.valuation.CashSettleParams cashSettleParams()
	{
		return _settleParams;
	}

	@Override public org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> value (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams vcp)
	{
		if (null == valParams || null == csqs) return null;

		org.drip.state.identifier.FundingLabel fundingLabel = org.drip.state.identifier.FundingLabel.Standard
			(couponCurrency()[0]);

		org.drip.analytics.rates.DiscountCurve dcFunding = csqs.fundingCurve (fundingLabel);

		if (null == dcFunding) return null;

		double dblValueDate = valParams.valueDate();

		long lStart = System.nanoTime();

		double dblAccrued01 = 0.;
		boolean bFirstPeriod = true;
		double dblUnadjustedDirtyDV01 = 0.;
		double dblQuantoAdjustedDirtyDV01 = 0.;
		double dblAdjustedNotional = _dblNotional;
		double dblCashPayDF = java.lang.Double.NaN;
		double dblValueNotional = java.lang.Double.NaN;
		org.drip.state.identifier.FXLabel fxLabel = null;
		org.drip.quant.function1D.AbstractUnivariate auFX = null;

		org.drip.state.identifier.FXLabel[] aFXLabel = fxLabel();

		if (null != aFXLabel && 0 != aFXLabel.length) auFX = csqs.fxCurve (fxLabel = aFXLabel[0]);

		try {
			dblAdjustedNotional *= (null != auFX && null != _fxmtm && !_fxmtm.mtmMode() ? auFX.evaluate
				(dblValueDate) : 1.);
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		for (org.drip.analytics.period.CashflowPeriod period : _lsCouponPeriod) {
			double dblPeriodQuantoAdjustment = 1.;
			double dblPeriodUnadjustedDirtyDV01 = java.lang.Double.NaN;

			double dblPeriodAccrualStartDate = period.accrualStart();

			double dblPeriodPayDate = period.pay();

			if (dblPeriodPayDate < dblValueDate) continue;

			try {
				if (bFirstPeriod) {
					bFirstPeriod = false;

					if (period.start() < dblValueDate)
						dblAccrued01 = period.accrualDCF (dblValueDate) * 0.0001 * notional
							(dblPeriodAccrualStartDate, dblValueDate);
				}

				if (null != _fxmtm && _fxmtm.mtmMode())
					dblPeriodQuantoAdjustment *= java.lang.Math.exp
						(org.drip.analytics.support.OptionHelper.IntegratedCrossVolQuanto
							(csqs.fundingCurveVolSurface (fundingLabel), csqs.fxCurveVolSurface (fxLabel),
								csqs.fundingFXCorrSurface (fundingLabel, fxLabel), dblValueDate,
									dblPeriodPayDate));

				dblPeriodUnadjustedDirtyDV01 = 0.0001 * period.couponDCF() * dcFunding.df (dblPeriodPayDate)
					* notional (dblPeriodAccrualStartDate, period.end());
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}

			dblUnadjustedDirtyDV01 += dblPeriodUnadjustedDirtyDV01;
			dblQuantoAdjustedDirtyDV01 += dblPeriodUnadjustedDirtyDV01 * dblPeriodQuantoAdjustment;
		}

		try {
			double dblCashSettle = dblValueDate;

			if (null != _settleParams) dblCashSettle = _settleParams.cashSettleDate (dblValueDate);

			dblCashPayDF = dcFunding.df (dblCashSettle);
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		dblAccrued01 *= dblAdjustedNotional;
		double dblAccrued = dblAccrued01 * 10000. * _dblCoupon;
		dblUnadjustedDirtyDV01 *= (dblAdjustedNotional / dblCashPayDF);
		dblQuantoAdjustedDirtyDV01 *= (dblAdjustedNotional / dblCashPayDF);
		double dblUnadjustedCleanDV01 = dblUnadjustedDirtyDV01 - dblAccrued01;
		double dblUnadjustedCleanPV = dblUnadjustedCleanDV01 * 10000. * _dblCoupon;
		double dblUnadjustedDirtyPV = dblUnadjustedDirtyDV01 * 10000. * _dblCoupon;
		double dblQuantoAdjustedCleanDV01 = dblQuantoAdjustedDirtyDV01 - dblAccrued01;
		double dblQuantoAdjustedCleanPV = dblQuantoAdjustedCleanDV01 * 10000. * _dblCoupon;
		double dblQuantoAdjustedDirtyPV = dblQuantoAdjustedDirtyDV01 * 10000. * _dblCoupon;

		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapResult = new
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>();

		mapResult.put ("Accrued", dblAccrued);

		mapResult.put ("Accrued01", dblAccrued01);

		mapResult.put ("CleanDV01", dblQuantoAdjustedCleanDV01);

		mapResult.put ("CleanPV", dblQuantoAdjustedCleanPV);

		mapResult.put ("CV01", dblQuantoAdjustedCleanDV01);

		mapResult.put ("DirtyDV01", dblQuantoAdjustedDirtyDV01);

		mapResult.put ("DirtyPV", dblQuantoAdjustedDirtyPV);

		mapResult.put ("DV01", dblQuantoAdjustedCleanDV01);

		mapResult.put ("PV", dblQuantoAdjustedCleanPV);

		mapResult.put ("QuantoAdjustedCleanDV01", dblQuantoAdjustedCleanDV01);

		mapResult.put ("QuantoAdjustedCleanPV", dblQuantoAdjustedCleanPV);

		mapResult.put ("QuantoAdjustedDirtyDV01", dblQuantoAdjustedDirtyDV01);

		mapResult.put ("QuantoAdjustedDirtyPV", dblQuantoAdjustedDirtyPV);

		mapResult.put ("QuantoAdjustedDV01", dblQuantoAdjustedCleanDV01);

		mapResult.put ("QuantoAdjustedPV", dblQuantoAdjustedCleanPV);

		mapResult.put ("QuantoAdjustedUpfront", dblQuantoAdjustedCleanPV);

		mapResult.put ("QuantoAdjustmentFactor", dblQuantoAdjustedDirtyDV01 / dblUnadjustedDirtyDV01);

		mapResult.put ("QuantoAdjustmentPremium", (dblQuantoAdjustedCleanPV - dblUnadjustedCleanPV) /
			dblAdjustedNotional);

		mapResult.put ("UnadjustedCleanDV01", dblUnadjustedCleanDV01);

		mapResult.put ("UnadjustedCleanPV", dblUnadjustedCleanPV);

		mapResult.put ("UnadjustedDirtyDV01", dblUnadjustedDirtyDV01);

		mapResult.put ("UnadjustedDirtyPV", dblUnadjustedDirtyPV);

		mapResult.put ("UnadjustedDV01", dblUnadjustedCleanDV01);

		mapResult.put ("UnadjustedPV", dblUnadjustedCleanPV);

		mapResult.put ("UnadjustedUpfront", dblUnadjustedCleanPV);

		mapResult.put ("Upfront", dblQuantoAdjustedCleanPV);

		try {
			dblValueNotional = notional (dblValueDate) * (null != auFX && null != _fxmtm && !_fxmtm.mtmMode()
				? auFX.evaluate (dblValueDate) : 1.);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		if (org.drip.quant.common.NumberUtil.IsValid (dblValueNotional)) {
			double dblUnadjustedCleanPrice = 100. * (1. + (dblUnadjustedCleanPV / dblValueNotional));

			double dblUnadjustedDirtyPrice = 100. * (1. + (dblUnadjustedDirtyPV / dblValueNotional));

			double dblQuantoAdjustedCleanPrice = 100. * (1. + (dblQuantoAdjustedCleanPV / dblValueNotional));

			double dblQuantoAdjustedDirtyPrice = 100. * (1. + (dblQuantoAdjustedDirtyPV / dblValueNotional));

			mapResult.put ("CleanPrice", dblQuantoAdjustedCleanPrice);

			mapResult.put ("DirtyPrice", dblQuantoAdjustedDirtyPrice);

			mapResult.put ("Price", dblQuantoAdjustedCleanPrice);

			mapResult.put ("QuantoAdjustedCleanPrice", dblQuantoAdjustedCleanPrice);

			mapResult.put ("QuantoAdjustedDirtyPrice", dblQuantoAdjustedDirtyPrice);

			mapResult.put ("QuantoAdjustedPrice", dblQuantoAdjustedCleanPrice);

			mapResult.put ("UnadjustedCleanPrice", dblUnadjustedCleanPrice);

			mapResult.put ("UnadjustedDirtyPrice", dblUnadjustedDirtyPrice);

			mapResult.put ("UnadjustedPrice", dblUnadjustedCleanPrice);
		}

		mapResult.put ("CalcTime", (System.nanoTime() - lStart) * 1.e-09);

		return mapResult;
	}

	@Override public java.util.Set<java.lang.String> measureNames()
	{
		java.util.Set<java.lang.String> setstrMeasureNames = new java.util.TreeSet<java.lang.String>();

		setstrMeasureNames.add ("Accrued");

		setstrMeasureNames.add ("Accrued01");

		setstrMeasureNames.add ("CalcTime");

		setstrMeasureNames.add ("CleanDV01");

		setstrMeasureNames.add ("CleanPrice");

		setstrMeasureNames.add ("CleanPV");

		setstrMeasureNames.add ("CV01");

		setstrMeasureNames.add ("DirtyDV01");

		setstrMeasureNames.add ("DirtyPrice");

		setstrMeasureNames.add ("DirtyPV");

		setstrMeasureNames.add ("DV01");

		setstrMeasureNames.add ("Price");

		setstrMeasureNames.add ("PV");

		setstrMeasureNames.add ("QuantoAdjustedCleanDV01");

		setstrMeasureNames.add ("QuantoAdjustedCleanPrice");

		setstrMeasureNames.add ("QuantoAdjustedCleanPV");

		setstrMeasureNames.add ("QuantoAdjustedDirtyDV01");

		setstrMeasureNames.add ("QuantoAdjustedDirtyPrice");

		setstrMeasureNames.add ("QuantoAdjustedDirtyPV");

		setstrMeasureNames.add ("QuantoAdjustedDV01");

		setstrMeasureNames.add ("QuantoAdjustedPrice");

		setstrMeasureNames.add ("QuantoAdjustedPV");

		setstrMeasureNames.add ("QuantoAdjustedUpfront");

		setstrMeasureNames.add ("QuantoAdjustmentFactor");

		setstrMeasureNames.add ("QuantoAdjustmentPremium");

		setstrMeasureNames.add ("UnadjustedCleanDV01");

		setstrMeasureNames.add ("UnadjustedCleanPrice");

		setstrMeasureNames.add ("UnadjustedCleanPV");

		setstrMeasureNames.add ("UnadjustedDirtyDV01");

		setstrMeasureNames.add ("UnadjustedDirtyPrice");

		setstrMeasureNames.add ("UnadjustedDirtyPV");

		setstrMeasureNames.add ("UnadjustedDV01");

		setstrMeasureNames.add ("UnadjustedPrice");

		setstrMeasureNames.add ("UnadjustedPV");

		setstrMeasureNames.add ("UnadjustedUpfront");

		setstrMeasureNames.add ("Upfront");

		return setstrMeasureNames;
	}

	@Override public org.drip.quant.calculus.WengertJacobian jackDDirtyPVDManifestMeasure (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams)
	{
		if (null == valParams || valParams.valueDate() >= _dblMaturity || null == csqs) return null;

		org.drip.analytics.rates.DiscountCurve dcFunding = csqs.fundingCurve
			(org.drip.state.identifier.FundingLabel.Standard (couponCurrency()[0]));

		if (null == dcFunding) return null;

		try {
			org.drip.quant.calculus.WengertJacobian jackDDirtyPVDManifestMeasure = null;

			for (org.drip.analytics.period.CashflowPeriod p : _lsCouponPeriod) {
				double dblPeriodPayDate = p.pay();

				if (p.start() < valParams.valueDate()) continue;

				org.drip.quant.calculus.WengertJacobian jackDDFDManifestMeasure =
					dcFunding.jackDDFDManifestMeasure (dblPeriodPayDate, "Rate");

				if (null == jackDDFDManifestMeasure) continue;

				int iNumQuote = jackDDFDManifestMeasure.numParameters();

				if (0 == iNumQuote) continue;

				if (null == jackDDirtyPVDManifestMeasure)
					jackDDirtyPVDManifestMeasure = new org.drip.quant.calculus.WengertJacobian (1,
						iNumQuote);

				double dblPeriodNotional = _dblNotional * notional (p.start(), p.end());

				double dblPeriodDCF = p.couponDCF();

				for (int k = 0; k < iNumQuote; ++k) {
					if (!jackDDirtyPVDManifestMeasure.accumulatePartialFirstDerivative (0, k,
						dblPeriodNotional * dblPeriodDCF * jackDDFDManifestMeasure.getFirstDerivative (0,
							k)))
						return null;
				}
			}

			return jackDDirtyPVDManifestMeasure;
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
		if (null == valParams || valParams.valueDate() >= _dblMaturity || null == strManifestMeasure || null
			== csqs)
			return null;

		org.drip.analytics.rates.DiscountCurve dcFunding = csqs.fundingCurve
			(org.drip.state.identifier.FundingLabel.Standard (couponCurrency()[0]));

		if (null == dcFunding) return null;

		if ("Rate".equalsIgnoreCase (strManifestMeasure) || "SwapRate".equalsIgnoreCase (strManifestMeasure))
		{
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapMeasures = value
				(valParams, pricerParams, csqs, quotingParams);

			if (null == mapMeasures) return null;

			double dblDirtyDV01 = mapMeasures.get ("DirtyDV01");

			double dblParSwapRate = mapMeasures.get ("SwapRate");

			try {
				org.drip.quant.calculus.WengertJacobian wjSwapRateDFMicroJack = null;

				for (org.drip.analytics.period.CashflowPeriod p : _lsCouponPeriod) {
					double dblPeriodPayDate = p.pay();

					if (dblPeriodPayDate < valParams.valueDate()) continue;

					org.drip.quant.calculus.WengertJacobian wjPeriodFwdRateDF =
						dcFunding.jackDForwardDManifestMeasure (p.start(), p.end(), "Rate", p.couponDCF());

					org.drip.quant.calculus.WengertJacobian wjPeriodPayDFDF =
						dcFunding.jackDDFDManifestMeasure (dblPeriodPayDate, "Rate");

					if (null == wjPeriodFwdRateDF || null == wjPeriodPayDFDF) continue;

					double dblForwardRate = dcFunding.libor (p.start(), p.end());

					double dblPeriodPayDF = dcFunding.df (dblPeriodPayDate);

					if (null == wjSwapRateDFMicroJack)
						wjSwapRateDFMicroJack = new org.drip.quant.calculus.WengertJacobian (1,
							wjPeriodFwdRateDF.numParameters());

					double dblPeriodNotional = notional (p.start(), p.end());

					double dblPeriodDCF = p.couponDCF();

					for (int k = 0; k < wjPeriodFwdRateDF.numParameters(); ++k) {
						double dblPeriodMicroJack = (dblForwardRate - dblParSwapRate) *
							wjPeriodPayDFDF.getFirstDerivative (0, k) + dblPeriodPayDF *
								wjPeriodFwdRateDF.getFirstDerivative (0, k);

						if (!wjSwapRateDFMicroJack.accumulatePartialFirstDerivative (0, k, dblPeriodNotional
							* dblPeriodDCF * dblPeriodMicroJack / dblDirtyDV01))
							return null;
					}
				}

				return wjSwapRateDFMicroJack;
			} catch (java.lang.Exception e) {
				e.printStackTrace();
			}
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
		return null == valParams || null == pqs || !(pqs instanceof
			org.drip.product.calib.FixedStreamQuoteSet) || !pqs.contains
				(org.drip.analytics.rates.DiscountCurve.LATENT_STATE_DISCOUNT,
					org.drip.analytics.rates.DiscountCurve.QUANTIFICATION_METRIC_DISCOUNT_FACTOR,
						org.drip.state.identifier.FundingLabel.Standard (couponCurrency()[0])) ? null :
							discountFactorPRWC (valParams, pricerParams, csqs, quotingParams, pqs);
	}

	@Override public org.drip.state.estimator.PredictorResponseWeightConstraint forwardPRWC (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final org.drip.product.calib.ProductQuoteSet pqs)
	{
		double dblValueDate = valParams.valueDate();

		org.drip.analytics.rates.DiscountCurve dcFunding = csqs.fundingCurve
			(org.drip.state.identifier.FundingLabel.Standard (couponCurrency()[0]));

		if (dblValueDate >= _dblMaturity || null == dcFunding) return null;

		double dblPV = 0.;
		double dblCoupon = _dblCoupon;
		org.drip.product.calib.FixedStreamQuoteSet fsqs = (org.drip.product.calib.FixedStreamQuoteSet) pqs;

		try {
			if (fsqs.containsPV()) dblPV = fsqs.pv();

			if (fsqs.containsCoupon()) dblCoupon = fsqs.coupon();
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		org.drip.state.estimator.PredictorResponseWeightConstraint prwc = new
			org.drip.state.estimator.PredictorResponseWeightConstraint();

		for (org.drip.analytics.period.CashflowPeriod period : _lsCouponPeriod) {
			double dblPeriodEndDate = period.end();

			if (dblPeriodEndDate < dblValueDate) continue;

			try {
				dblPV -= _dblNotional * notional (dblPeriodEndDate) * (period.contains (dblValueDate) ?
					period.accrualDCF (dblValueDate) : period.couponDCF()) * dblCoupon * dcFunding.df
						(period.pay());
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}
		}

		if (!prwc.updateValue (dblPV)) return null;

		if (!prwc.updateDValueDManifestMeasure ("PV", 1.)) return null;

		return prwc;
	}

	@Override public org.drip.state.estimator.PredictorResponseWeightConstraint fundingForwardPRWC (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final org.drip.product.calib.ProductQuoteSet pqs)
	{
		return null == valParams || null == pqs || !(pqs instanceof
			org.drip.product.calib.FixedStreamQuoteSet) ? null : discountFactorPRWC (valParams, pricerParams,
				csqs, quotingParams, pqs);
	}

	@Override public org.drip.state.estimator.PredictorResponseWeightConstraint generateCalibPRWC (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final org.drip.state.representation.LatentStateMetricMeasure lsmm)
	{
		return null;
	}

	@Override public java.lang.String fieldDelimiter()
	{
		return "!";
	}

	@Override public java.lang.String objectTrailer()
	{
		return "&";
	}

	@Override public byte[] serialize()
	{
		java.lang.StringBuffer sb = new java.lang.StringBuffer();

		sb.append (org.drip.service.stream.Serializer.VERSION + fieldDelimiter());

		sb.append (_dblNotional + fieldDelimiter());

		sb.append (_dblCoupon + fieldDelimiter());

		if (null == _strCurrency || _strCurrency.isEmpty())
			sb.append (org.drip.service.stream.Serializer.NULL_SER_STRING + fieldDelimiter());
		else
			sb.append (_strCurrency + fieldDelimiter());

		if (null == _strCode || _strCode.isEmpty())
			sb.append (org.drip.service.stream.Serializer.NULL_SER_STRING + fieldDelimiter());
		else
			sb.append (_strCode + fieldDelimiter());

		sb.append (_dblMaturity + fieldDelimiter());

		sb.append (_dblEffective + fieldDelimiter());

		if (null == _notlSchedule)
			sb.append (org.drip.service.stream.Serializer.NULL_SER_STRING + fieldDelimiter());
		else
			sb.append (new java.lang.String (_notlSchedule.serialize()) + fieldDelimiter());

		if (null == _settleParams)
			sb.append (org.drip.service.stream.Serializer.NULL_SER_STRING + fieldDelimiter());
		else
			sb.append (new java.lang.String (_settleParams.serialize()) + fieldDelimiter());

		if (null == _lsCouponPeriod)
			sb.append (org.drip.service.stream.Serializer.NULL_SER_STRING);
		else {
			boolean bFirstEntry = true;

			java.lang.StringBuffer sbPeriods = new java.lang.StringBuffer();

			for (org.drip.analytics.period.CashflowPeriod p : _lsCouponPeriod) {
				if (null == p) continue;

				if (bFirstEntry)
					bFirstEntry = false;
				else
					sbPeriods.append (collectionRecordDelimiter());

				sbPeriods.append (new java.lang.String (p.serialize()));
			}

			if (sbPeriods.toString().isEmpty())
				sb.append (org.drip.service.stream.Serializer.NULL_SER_STRING);
			else
				sb.append (sbPeriods.toString());
		}

		return sb.append (objectTrailer()).toString().getBytes();
	}

	@Override public org.drip.service.stream.Serializer deserialize (
		final byte[] ab)
	{
		try {
			return new FixedStream (ab);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public static final void main (
		final java.lang.String[] astrArgs)
		throws java.lang.Exception
	{
		org.drip.service.api.CreditAnalytics.Init ("");

		org.drip.analytics.date.JulianDate dtToday = org.drip.analytics.date.JulianDate.Today();

		java.util.List<org.drip.analytics.period.CashflowPeriod> lsCouponPeriod =
			org.drip.analytics.period.CashflowPeriod.GeneratePeriodsRegular (dtToday.julian(), "4Y", null,
				2, "30/360", false, true, "JPY", "JPY");

		FixedStream fs = new org.drip.product.cashflow.FixedStream ("JPY", null, 0.03, 100., null,
			lsCouponPeriod);

		byte[] abFS = fs.serialize();

		System.out.println (new java.lang.String (abFS));

		FixedStream fsDeser = new FixedStream (abFS);

		System.out.println (new java.lang.String (fsDeser.serialize()));
	}
}
