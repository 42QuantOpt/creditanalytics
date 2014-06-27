
package org.drip.product.definition;

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
 * MarketParamRef interface provides stubs for component name, IR curve, forward curve, credit curve, TSY
 * 	curve, and needed to value the component.
 *
 * @author Lakshmi Krishnamurthy
 */

public interface MarketParamRef {

	/**
	 * Get the component name
	 * 
	 * @return The component name
	 */

	public abstract java.lang.String name();

	/**
	 * Get the Cash Flow Currency Set
	 * 
	 * @return The Cash Flow Currency Set
	 */

	public abstract java.util.Set<java.lang.String> cashflowCurrencySet();

	/**
	 * Get the Coupon Currency
	 * 
	 * @return The Coupon Currency
	 */

	public abstract java.lang.String[] couponCurrency();

	/**
	 * Get the Principal Currency
	 * 
	 * @return The Principal Currency
	 */

	public abstract java.lang.String[] principalCurrency();

	/**
	 * Get the Array of Forward Curve Names
	 * 
	 * @return Array of the Forward Curve Names
	 */

	public abstract java.lang.String[] forwardCurveName();

	/**
	 * Get the credit curve name
	 * 
	 * @return The credit curve name
	 */

	public abstract java.lang.String[] creditCurveName();

	/**
	 * Get the Array of the Currency Pair Codes
	 * 
	 * @return The Array of the Currency Pair Codes
	 */

	public abstract java.lang.String[] currencyPairCode();
}