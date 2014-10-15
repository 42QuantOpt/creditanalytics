
package org.drip.product.rates;

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
 * SingleStreamComponent implements fixed income component that is based off of a single stream.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class SingleStreamComponent extends org.drip.product.definition.FixedIncomeComponent {
	private java.lang.String _strName = "";
	private org.drip.product.rates.GenericStream _stream = null;
	private org.drip.param.valuation.CashSettleParams _csp = null;

	/**
	 * SingleStreamComponent constructor
	 * 
	 * @param strName The Component Name
	 * @param stream The Single Stream Instance
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are invalid
	 */

	public SingleStreamComponent (
		final java.lang.String strName,
		final org.drip.product.rates.GenericStream stream)
		throws java.lang.Exception
	{
		if (null == (_strName = strName) || _strName.isEmpty() || null == (_stream = stream))
			throw new java.lang.Exception ("SingleStreamComponent ctr: Invalid Inputs");
	}

	/**
	 * Retrieve the Stream Instance
	 * 
	 * @return The Stream Instance
	 */

	public org.drip.product.rates.GenericStream stream()
	{
		return _stream;
	}

	@Override public java.lang.String name()
	{
		return _strName;
	}

	@Override public java.util.Set<java.lang.String> cashflowCurrencySet()
	{
		return _stream.cashflowCurrencySet();
	}

	@Override public java.lang.String[] payCurrency()
	{
		return new java.lang.String[] {_stream.payCurrency()};
	}

	@Override public java.lang.String[] principalCurrency()
	{
		return _stream.principalCurrency();
	}

	@Override public org.drip.state.identifier.ForwardLabel[] forwardLabel()
	{
		org.drip.state.identifier.ForwardLabel forwardLabel = _stream.forwardLabel();

		return null == forwardLabel ? null : new org.drip.state.identifier.ForwardLabel[] {forwardLabel};
	}

	@Override public org.drip.state.identifier.FundingLabel[] fundingLabel()
	{
		return new org.drip.state.identifier.FundingLabel[] {_stream.fundingLabel()};
	}

	@Override public org.drip.state.identifier.CreditLabel[] creditLabel()
	{
		org.drip.state.identifier.CreditLabel creditLabel = _stream.creditLabel();

		return null == creditLabel ? null : new org.drip.state.identifier.CreditLabel[] {creditLabel};
	}

	@Override public org.drip.state.identifier.FXLabel[] fxLabel()
	{
		org.drip.state.identifier.FXLabel fxLabel = _stream.fxLabel();

		return null == fxLabel ? null : new org.drip.state.identifier.FXLabel[] {fxLabel};
	}

	@Override public double initialNotional()
		throws java.lang.Exception
	{
		return _stream.initialNotional();
	}

	@Override public double notional (
		final double dblDate)
		throws java.lang.Exception
	{
		return _stream.notional (dblDate);
	}

	@Override public double notional (
		final double dblDate1,
		final double dblDate2)
		throws java.lang.Exception
	{
		return _stream.notional (dblDate1, dblDate2);
	}

	@Override public org.drip.analytics.output.GenericCouponPeriodMetrics coupon (
		final double dblAccrualEndDate,
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs)
	{
		return _stream.coupon (dblAccrualEndDate, valParams, csqs);
	}

	@Override public int freq()
	{
		return _stream.freq();
	}

	@Override public org.drip.analytics.date.JulianDate effective()
	{
		return _stream.effective();
	}

	@Override public org.drip.analytics.date.JulianDate maturity()
	{
		return _stream.maturity();
	}

	@Override public org.drip.analytics.date.JulianDate firstCouponDate()
	{
		return _stream.firstCouponDate();
	}

	@Override public java.util.List<org.drip.analytics.cashflow.GenericCouponPeriod> cashFlowPeriod()
	{
		return _stream.cashFlowPeriod();
	}

	@Override public org.drip.param.valuation.CashSettleParams cashSettleParams()
	{
		return _csp;
	}

	@Override public org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> value (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams)
	{
		return _stream.value (valParams, pricerParams, csqs, quotingParams);
	}

	@Override public java.util.Set<java.lang.String> measureNames()
	{
		return null;
	}
}
