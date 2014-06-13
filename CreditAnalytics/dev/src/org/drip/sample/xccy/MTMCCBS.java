
package org.drip.sample.xccy;

import org.drip.analytics.date.JulianDate;
import org.drip.analytics.rates.*;
import org.drip.analytics.support.CaseInsensitiveTreeMap;
import org.drip.param.creator.*;
import org.drip.param.definition.*;
import org.drip.param.valuation.*;
import org.drip.product.fx.*;
import org.drip.product.params.FloatingRateIndex;
import org.drip.product.rates.*;
import org.drip.service.api.CreditAnalytics;
import org.drip.state.creator.DiscountCurveBuilder;

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
 * MTMCCBS demonstrates the construction, the usage, and the eventual valuation of the Mark-to-market Cross
 *  Currency Basis Swap.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class MTMCCBS {
	private static final FloatFloatComponent MakexM6MBasisSwap (
		final JulianDate dtEffective,
		final String strCurrency,
		final String strTenor,
		final int iTenorInMonths)
		throws Exception
	{
		JulianDate dtMaturity = dtEffective.addTenor (strTenor);

		/*
		 * The Reference 6M Leg
		 */

		FloatingStream fsReference = FloatingStream.Create (dtEffective.getJulian(),
			dtMaturity.getJulian(), 0., true, FloatingRateIndex.Create (strCurrency + "-LIBOR-6M"),
				2, "Act/360", false, "Act/360", false, false, null, null, null, null, null, null, null,
					null, null, -1., strCurrency, strCurrency);

		/*
		 * The Derived Leg
		 */

		FloatingStream fsDerived = FloatingStream.Create (dtEffective.getJulian(),
			dtMaturity.getJulian(), 0., false, FloatingRateIndex.Create (strCurrency + "-LIBOR-" + iTenorInMonths + "M"),
				12 / iTenorInMonths, "Act/360", false, "Act/360", false, false, null, null, null, null, null, null, null,
					null, null, 1., strCurrency, strCurrency);

		/*
		 * The float-float swap instance
		 */

		return new FloatFloatComponent (fsReference, fsDerived);
	}

	public static final void main (
		final String[] astrArgs)
		throws Exception
	{
		double dblUSDCollateralRate = 0.02;
		double dblUSD3MForwardRate = 0.02;
		double dblJPYCollateralRate = 0.02;
		double dblJPY3MForwardRate = 0.02;

		/*
		 * Initialize the Credit Analytics Library
		 */

		CreditAnalytics.Init ("");

		JulianDate dtToday = JulianDate.Today();

		ValuationParams valParams = new ValuationParams (dtToday, dtToday, "USD");

		DiscountCurve dcUSDCollatDomestic = DiscountCurveBuilder.CreateFromFlatRate (
			dtToday,
			"USD",
			new CollateralizationParams ("OVERNIGHT_INDEX", "USD"),
			dblUSDCollateralRate);

		ForwardCurve fc3MUSD = ScenarioForwardCurveBuilder.FlatForwardForwardCurve (
			dtToday,
			FloatingRateIndex.Create ("USD", "LIBOR", "3M"),
			dblUSD3MForwardRate,
			new CollateralizationParams ("OVERNIGHT_INDEX", "USD"));

		FloatFloatComponent ffcReferenceUSD = MakexM6MBasisSwap (
			dtToday,
			"USD",
			"2Y",
			3);

		ffcReferenceUSD.setPrimaryCode ("USD_6M::3M::2Y");

		DiscountCurve dcJPYCollatDomestic = DiscountCurveBuilder.CreateFromFlatRate (
			dtToday,
			"JPY",
			new CollateralizationParams ("OVERNIGHT_INDEX", "JPY"),
			dblJPYCollateralRate);

		ForwardCurve fc3MJPY = ScenarioForwardCurveBuilder.FlatForwardForwardCurve (
			dtToday,
			FloatingRateIndex.Create ("JPY", "LIBOR", "3M"),
			dblJPY3MForwardRate,
			new CollateralizationParams ("OVERNIGHT_INDEX", "JPY"));

		FloatFloatComponent ffcDerivedJPY = MakexM6MBasisSwap (
			dtToday,
			"JPY",
			"2Y",
			3);

		ffcDerivedJPY.setPrimaryCode ("JPY_6M::3M::2Y");

		CrossCurrencyComponentPair ccbsUSDJPY = new MTMCrossCurrencyPair (
			"USDJPY_CCBS",
			ffcReferenceUSD,
			ffcDerivedJPY);

		BasketMarketParams bmp = BasketMarketParamsBuilder.CreateBasketMarketParams();

		bmp.addDiscountCurve ("USD", dcUSDCollatDomestic);

		bmp.addDiscountCurve ("JPY", dcJPYCollatDomestic);

		bmp.addForwardCurve (FloatingRateIndex.Create ("USD", "LIBOR", "3M").fullyQualifiedName(), fc3MUSD);

		bmp.addForwardCurve (FloatingRateIndex.Create ("JPY", "LIBOR", "3M").fullyQualifiedName(), fc3MJPY);

		CaseInsensitiveTreeMap<Double> mapMTMOutput = ccbsUSDJPY.value (valParams, null, bmp, null);

		System.out.println ("Base PV : " + mapMTMOutput.get (ccbsUSDJPY.referenceComponent().componentName() + "[PV]"));

		System.out.println ("MTM PV  : " + mapMTMOutput.get ("MTMPV"));
	}
}
