
package org.drip.product.params;

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
 * CurrencySetting contains the component's coupon, and the principal currency arrays
 *
 * @author Lakshmi Krishnamurthy
 */

public class CurrencySetting implements org.drip.product.params.Validatable {
	private java.lang.String _strCouponCurrency = null;
	private java.lang.String _strPrincipalCurrency = null;

	/**
	 * Create a Single Currency CurrencySet Instance
	 * 
	 * @param strCurrency The Currency
	 * 
	 * @return The CurrencySet Instance
	 */

	public static final CurrencySetting Create (
		final java.lang.String strCurrency)
	{
		CurrencySetting cs = new CurrencySetting (strCurrency, strCurrency);

		return cs.validate() ? cs : null;
	}

	/**
	 * Construct the CurrencySetting object from the coupon and the principal currencies.
	 * 
	 * @param strCouponCurrency Coupon Currency
	 * @param strPrincipalCurrency Principal Currency
	 */

	public CurrencySetting (
		final java.lang.String strCouponCurrency,
		final java.lang.String strPrincipalCurrency)
	{
		_strCouponCurrency = strCouponCurrency;
		_strPrincipalCurrency = strPrincipalCurrency;
	}

	@Override public boolean validate()
	{
		return null != _strCouponCurrency && !_strCouponCurrency.isEmpty() && null != _strPrincipalCurrency
			&& !_strPrincipalCurrency.isEmpty();
	}

	/**
	 * Retrieve the Coupon Currency
	 * 
	 * @return The Coupon Currency
	 */

	public java.lang.String couponCurrency()
	{
		return _strCouponCurrency;
	}

	/**
	 * Retrieve the Principal Currency
	 * 
	 * @return The Principal Currency
	 */

	public java.lang.String principalCurrency()
	{
		return _strPrincipalCurrency;
	}
}
