
package org.drip.param.creator;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2014 Lakshmi Krishnamurthy
 * Copyright (C) 2013 Lakshmi Krishnamurthy
 * Copyright (C) 2012 Lakshmi Krishnamurthy
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
 * BasketMarketParamsBuilder implements the various ways of constructing, de-serializing, and building the
 *  Basket Market Parameters.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class BasketMarketParamsBuilder {

	/**
	 * Construct a BasketMarketParams instance from the map of discount curve, the map of Forward curve, the
	 *  map of credit curve, and a double map of date/rate index and fixings.
	 * 
	 * @param mapDC Map of discount curve
	 * @param mapFC Map of Forward Curve
	 * @param mapCC Map of Credit curve
	 * @param mapCQComp Map of component quotes
	 * @param mmFixings Double map of date/rate index and fixings
	 */

	public static final org.drip.param.definition.BasketMarketParams CreateBasketMarketParams (
		final org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.rates.DiscountCurve>
			mapDC,
		final org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.rates.ForwardCurve>
			mapFC,
		final org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.definition.CreditCurve>
			mapCC,
		final org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.param.definition.ComponentQuote>
			mapCQComp,
		final java.util.Map<org.drip.analytics.date.JulianDate,
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>> mmFixings)
	{
		try {
			return new org.drip.param.market.BasketMarketParamSet (mapDC, mapFC, mapCC, mapCQComp, mmFixings);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Construct an empty instance of the BasketMarketParams object.
	 * 
	 * @return Instance of the Basket Market Params interface
	 */

	public static final org.drip.param.definition.BasketMarketParams CreateBasketMarketParams()
	{
		try {
			return new org.drip.param.market.BasketMarketParamSet (null, null, null, null, null);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Create a Basket Market Parameter Instance from the byte array
	 * 
	 * @param ab Byte Array
	 * 
	 * @return Basket Market Parameter Instance
	 */

	public static final org.drip.param.definition.BasketMarketParams FromByteArray (
		final byte[] ab)
	{
		if (null == ab || 0 == ab.length) return null;

		try {
			return new org.drip.param.market.BasketMarketParamSet (ab);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}
}
