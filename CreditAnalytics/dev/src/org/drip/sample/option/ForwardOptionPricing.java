
package org.drip.sample.option;

import java.util.List;

import org.drip.analytics.date.JulianDate;
import org.drip.analytics.period.CashflowPeriod;
import org.drip.analytics.rates.DiscountCurve;
import org.drip.param.creator.ScenarioDiscountCurveBuilder;
import org.drip.param.pricer.HestonOptionPricerParams;
import org.drip.param.valuation.ValuationParams;
import org.drip.pricer.option.*;
import org.drip.product.cashflow.*;
import org.drip.product.creator.*;
import org.drip.product.definition.CalibratableFixedIncomeComponent;
import org.drip.product.option.EuropeanCallPut;
import org.drip.quant.fourier.PhaseAdjuster;
import org.drip.quant.function1D.FlatUnivariate;
import org.drip.service.api.CreditAnalytics;
import org.drip.state.identifier.ForwardLabel;

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
 * ForwardOptionPricing illustrates pricing a forward using the Black '76 variant and the Heston's stochastic
 * 	Volatility Models.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class ForwardOptionPricing {

	/*
	 * Construct the Array of Cash Instruments from the given set of parameters
	 * 
	 *  	USE WITH CARE: This sample ignores errors and does not handle exceptions.
	 */

	private static final CalibratableFixedIncomeComponent[] CashInstrumentsFromMaturityDays (
		final JulianDate dtEffective,
		final int[] aiDay,
		final int iNumFutures,
		final String strCurrency)
		throws Exception
	{
		CalibratableFixedIncomeComponent[] aCalibComp = new CalibratableFixedIncomeComponent[aiDay.length + iNumFutures];

		for (int i = 0; i < aiDay.length; ++i)
			aCalibComp[i] = DepositBuilder.CreateDeposit (
				dtEffective,
				dtEffective.addBusDays (aiDay[i], strCurrency),
				null,
				strCurrency);

		CalibratableFixedIncomeComponent[] aEDF = EDFutureBuilder.GenerateEDPack (dtEffective, iNumFutures, strCurrency);

		for (int i = aiDay.length; i < aiDay.length + iNumFutures; ++i)
			aCalibComp[i] = aEDF[i - aiDay.length];

		return aCalibComp;
	}

	/*
	 * Construct the Array of Swap Instruments from the given set of parameters
	 * 
	 *  	USE WITH CARE: This sample ignores errors and does not handle exceptions.
	 */

	private static final CalibratableFixedIncomeComponent[] SwapInstrumentsFromMaturityTenor (
		final JulianDate dtEffective,
		final String[] astrTenor,
		final double[] adblCoupon,
		final String strCurrency)
		throws Exception
	{
		CalibratableFixedIncomeComponent[] aCalibComp = new CalibratableFixedIncomeComponent[astrTenor.length];

		for (int i = 0; i < astrTenor.length; ++i) {
			JulianDate dtMaturity = dtEffective.addTenorAndAdjust (astrTenor[i], strCurrency);

			List<CashflowPeriod> lsFloatPeriods = CashflowPeriod.GeneratePeriodsRegular (
				dtEffective.julian(),
				astrTenor[i],
				null,
				4,
				"Act/360",
				false,
				false,
				strCurrency,
				strCurrency
			);

			FloatingStream floatStream = new FloatingStream (
				strCurrency,
				null,
				0.,
				-1.,
				null,
				lsFloatPeriods,
				ForwardLabel.Create (strCurrency + "-LIBOR-6M"),
				false
			);

			List<CashflowPeriod> lsFixedPeriods = CashflowPeriod.GeneratePeriodsRegular (
				dtEffective.julian(),
				astrTenor[i],
				null,
				2,
				"Act/360",
				false,
				false,
				strCurrency,
				strCurrency
			);

			FixedStream fixStream = new FixedStream (
				strCurrency,
				null,
				adblCoupon[i],
				1.,
				null,
				lsFixedPeriods
			);

			org.drip.product.rates.IRSComponent irs = new org.drip.product.rates.IRSComponent (fixStream,
				floatStream);

			irs.setPrimaryCode ("IRS." + dtMaturity.toString() + "." + strCurrency);

			aCalibComp[i] = irs;
		}

		return aCalibComp;
	}

	/*
	 * Construct the discount curve using the following steps:
	 * 	- Construct the array of cash instruments and their quotes.
	 * 	- Construct the array of swap instruments and their quotes.
	 * 	- Construct a shape preserving and smoothing KLK Hyperbolic Spline from the cash/swap instruments.
	 * 
	 *  	USE WITH CARE: This sample ignores errors and does not handle exceptions.
	 */

	private static final DiscountCurve MakeDC (
		final JulianDate dtSpot,
		final String strCurrency)
		throws Exception
	{
		/*
		 * Construct the array of cash instruments and their quotes.
		 */

		CalibratableFixedIncomeComponent[] aCashComp = CashInstrumentsFromMaturityDays (
			dtSpot,
			new int[] {1, 2, 3, 7, 14, 21, 30, 60},
			4,
			strCurrency);

		double[] adblCashQuote = new double[] {
			0.01200, 0.01200, 0.01200, 0.01450, 0.01550, 0.01600, 0.01660, 0.01850, // Cash
			0.01612, 0.01580, 0.01589, 0.01598}; // Futures

		/*
		 * Construct the array of Swap instruments and their quotes.
		 */

		double[] adblSwapQuote = new double[] {
			0.02604,    //  4Y
			0.02808,    //  5Y
			0.02983,    //  6Y
			0.03136,    //  7Y
			0.03268,    //  8Y
			0.03383,    //  9Y
			0.03488,    // 10Y
			0.03583,    // 11Y
			0.03668,    // 12Y
			0.03833,    // 15Y
			0.03854,    // 20Y
			0.03672,    // 25Y
			0.03510,    // 30Y
			0.03266,    // 40Y
			0.03145     // 50Y
		};

		CalibratableFixedIncomeComponent[] aSwapComp = SwapInstrumentsFromMaturityTenor (
			dtSpot,
			new java.lang.String[] {"4Y", "5Y", "6Y", "7Y", "8Y", "9Y", "10Y", "11Y", "12Y", "15Y", "20Y", "25Y", "30Y", "40Y", "50Y"},
			adblSwapQuote,
			strCurrency);

		/*
		 * Construct a shape preserving and smoothing KLK Hyperbolic Spline from the cash/swap instruments.
		 */

		return ScenarioDiscountCurveBuilder.CubicKLKHyperbolicDFRateShapePreserver (
			"KLK_HYPERBOLIC_SHAPE_TEMPLATE",
			new ValuationParams (dtSpot, dtSpot, "USD"),
			aCashComp,
			adblCashQuote,
			aSwapComp,
			adblSwapQuote,
			true);
	}

	public static final void main (
		final String[] astrArgs)
		throws Exception
	{
		/*
		 * Initialize the Credit Analytics Library
		 */

		CreditAnalytics.Init ("");

		JulianDate dtToday = org.drip.analytics.date.JulianDate.Today();

		ValuationParams valParams = new ValuationParams (dtToday, dtToday, "USD");

		/*
		 * Construct the Discount Curve using its instruments and quotes
		 */

		DiscountCurve dc = MakeDC (dtToday, "USD");

		JulianDate dtMaturity = dtToday.addTenor ("6M");

		double dblStrike = 1.;

		EuropeanCallPut option = new EuropeanCallPut (
			dtMaturity,
			dblStrike);

		double dblSpot = 1.;

		double dblRho = 0.3;
		double dblKappa = 1.;
		double dblSigma = 0.5;
		double dblTheta = 0.2;
		double dblLambda = 0.;
		double dblSpotVolatility = 0.2;

		HestonOptionPricerParams fphp = new HestonOptionPricerParams (
			HestonStochasticVolatilityAlgorithm.PAYOFF_TRANSFORM_SCHEME_HESTON_1993,
			dblRho, 			// Rho
			dblKappa,			// Kappa
			dblSigma,			// Sigma
			dblTheta,			// Theta
			dblLambda,			// Lambda
			PhaseAdjuster.MULTI_VALUE_BRANCH_POWER_PHASE_TRACKER_KAHL_JACKEL); // Indicates Apply Phase Tracking Adjustment for Log + Power

		FokkerPlanckGenerator fpg = new HestonStochasticVolatilityAlgorithm (
			fphp);				// FP Heston Parameters

		System.out.println ("---------------------------------------------------------");

		System.out.println ("\t\t HESTON 1993 SPOT OPTION PRICING");

		System.out.println ("---------------------------------------------------------");

		System.out.println (
			option.value (
				valParams,
				dblSpot,
				false,
				dc,
				new FlatUnivariate (dblSpotVolatility),
				fpg
				)
			);

		System.out.println ("---------------------------------------------------------");

		System.out.println ("\t\t HESTON 1993 FORWARD OPTION PRICING");

		System.out.println ("---------------------------------------------------------");

		System.out.println (
			option.value (
				valParams,
				dblSpot,
				true,
				dc,
				new FlatUnivariate (dblSpotVolatility),
				fpg)
			);

		System.out.println ("---------------------------------------------------------");

		System.out.println ("\t\t BLACK SCHOLES SPOT OPTION PRICING");

		System.out.println ("---------------------------------------------------------");

		System.out.println (
			option.value (
				valParams,
				dblSpot,
				false,
				dc,
				new FlatUnivariate (dblSpotVolatility),
				new BlackScholesAlgorithm())
			);

		System.out.println ("---------------------------------------------------------");

		System.out.println ("\t\t BLACK '76 FORWARD OPTION PRICING");

		System.out.println ("---------------------------------------------------------");

		System.out.println (
			option.value (
				valParams,
				dblSpot,
				true,
				dc,
				new FlatUnivariate (dblSpotVolatility),
				new BlackScholesAlgorithm())
			);
	}
}
