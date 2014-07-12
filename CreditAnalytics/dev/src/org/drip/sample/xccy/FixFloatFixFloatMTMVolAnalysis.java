
package org.drip.sample.xccy;

import java.util.List;

import org.drip.analytics.date.JulianDate;
import org.drip.analytics.daycount.Convention;
import org.drip.analytics.daycount.DateAdjustParams;
import org.drip.analytics.period.CashflowPeriod;
import org.drip.analytics.rates.*;
import org.drip.analytics.support.CaseInsensitiveTreeMap;
import org.drip.param.creator.*;
import org.drip.param.market.CurveSurfaceQuoteSet;
import org.drip.param.valuation.*;
import org.drip.product.fx.*;
import org.drip.product.params.*;
import org.drip.product.rates.*;
import org.drip.quant.function1D.FlatUnivariate;
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
 * FixFloatFixFloatMTMVolAnalysis demonstrates the impact of Funding Volatility, FX Volatility, and Funding/FX
 * 	Correlation on the Valuation of an MTM Cross Currency Swap built out of a pair of fix-float swaps.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class FixFloatFixFloatMTMVolAnalysis {

	private static final FixFloatComponent MakeFixFloatSwap (
		final JulianDate dtEffective,
		final String strCurrency,
		final String strTenor,
		final int iTenorInMonths)
		throws Exception
	{
		DateAdjustParams dap = new DateAdjustParams (Convention.DR_FOLL, strCurrency);

			/*
			 * The Fixed Leg
			 */

		List<CashflowPeriod> lsFixPeriods = CashflowPeriod.GeneratePeriodsRegular (
			dtEffective.getJulian(),
			strTenor,
			dap,
			2,
			"Act/360",
			false,
			false,
			strCurrency,
			strCurrency
		);

		FixedStream fixStream = new FixedStream (
			strCurrency,
			0.,
			-1.,
			null,
			lsFixPeriods
		);

		/*
		 * The Derived Leg
		 */

		List<CashflowPeriod> lsDerivedFloatPeriods = CashflowPeriod.GeneratePeriodsRegular (
			dtEffective.getJulian(),
			strTenor,
			dap,
			12 / iTenorInMonths,
			"Act/360",
			false,
			false,
			strCurrency,
			strCurrency
		);

		FloatingStream floatStream = new FloatingStream (
			strCurrency,
			0.,
			1.,
			null,
			lsDerivedFloatPeriods,
			FloatingRateIndex.Create (strCurrency + "-LIBOR-" + iTenorInMonths + "M"),
			false
		);

		/*
		 * The fix-float swap instance
		 */

		return new FixFloatComponent (fixStream, floatStream);
	}

	private static final void SetMarketParams (
		final CurveSurfaceQuoteSet mktParams,
		final CurrencyPair cp,
		final String strCurrency,
		final double dblFundingVol,
		final double dblFXVol,
		final double dblFundingFXCorr)
		throws Exception
	{
		mktParams.setFundingCurveVolSurface (strCurrency, new FlatUnivariate (dblFundingVol));

		mktParams.setFXCurveVolSurface (cp, new FlatUnivariate (dblFXVol));

		mktParams.setFundingFXCorrSurface (strCurrency, cp, new FlatUnivariate (dblFundingFXCorr));
	}

	private static final void VolCorrScenario (
		final BasketMTM mtmccbs,
		final CurrencyPair cp,
		final String strCurrency,
		final ValuationParams valParams,
		final CurveSurfaceQuoteSet mktParams,
		final double dblFundingVol,
		final double dblFXVol,
		final double dblFundingFXCorr)
		throws Exception
	{
		SetMarketParams (mktParams, cp, strCurrency, dblFundingVol, dblFXVol, dblFundingFXCorr);

		CaseInsensitiveTreeMap<Double> mapMTMOutput = mtmccbs.value (valParams, null, mktParams, null);

		System.out.println ("\t[" +
			org.drip.quant.common.FormatUtil.FormatDouble (dblFundingVol, 2, 0, 100.) + "%," +
			org.drip.quant.common.FormatUtil.FormatDouble (dblFXVol, 2, 0, 100.) + "%," +
			org.drip.quant.common.FormatUtil.FormatDouble (dblFundingFXCorr, 2, 0, 100.) + "%] = " +
			org.drip.quant.common.FormatUtil.FormatDouble (mapMTMOutput.get ("MTMAdditiveAdjustment"), 1, 2, 100.) + "%");
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

		FixFloatComponent fixFloatUSD = MakeFixFloatSwap (
			dtToday,
			"USD",
			"2Y",
			3);

		fixFloatUSD.setPrimaryCode ("USD_IRS::3M::2Y");

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

		FixFloatComponent fixFloatJPY = MakeFixFloatSwap (
			dtToday,
			"JPY",
			"2Y",
			3);

		fixFloatJPY.setPrimaryCode ("JPY_IRS::3M::2Y");

		BasketMTM ccbsUSDJPY = new BasketMTM (
			new CrossCurrencyComponentPair (
			"USDJPY_CCBS",
			fixFloatUSD,
			fixFloatJPY)
		);

		CurveSurfaceQuoteSet mktParams = new CurveSurfaceQuoteSet();

		mktParams.setFundingCurve (dcUSDCollatDomestic);

		mktParams.setFundingCurve (dcJPYCollatDomestic);

		mktParams.setForwardCurve (fc3MUSD);

		mktParams.setForwardCurve (fc3MJPY);

		CurrencyPair cp = CurrencyPair.FromCode (ccbsUSDJPY.fxCode());

		double[] adblFundingVol = new double[] {0.1, 0.2, 0.3, 0.4};

		double[] adblFXVol = new double[] {0.1, 0.2, 0.3, 0.4};

		double[] adblFundingFXCorr = new double[] {-0.4, -0.1, 0.1, 0.4};

		for (double dblFundingVol : adblFundingVol) {
			for (double dblFXVol : adblFXVol) {
				for (double dblFundingFXCorr : adblFundingFXCorr)
					VolCorrScenario (
						ccbsUSDJPY,
						cp,
						"USD",
						valParams,
						mktParams,
						dblFundingVol,
						dblFXVol,
						dblFundingFXCorr
					);
			}
		}
	}
}
