
package org.drip.sample.overnight;

import org.drip.analytics.date.*;
import org.drip.analytics.rates.*;
import org.drip.function.deterministic1D.QuadraticRationalShapeControl;
import org.drip.market.otc.*;
import org.drip.param.creator.*;
import org.drip.param.valuation.*;
import org.drip.product.creator.*;
import org.drip.product.definition.CalibratableFixedIncomeComponent;
import org.drip.product.rates.*;
import org.drip.quant.common.FormatUtil;
import org.drip.service.api.CreditAnalytics;
import org.drip.spline.basis.PolynomialFunctionSetParams;
import org.drip.spline.params.*;
import org.drip.spline.stretch.*;
import org.drip.state.curve.DiscountFactorDiscountCurve;
import org.drip.state.estimator.LatentStateStretchBuilder;
import org.drip.state.identifier.*;
import org.drip.state.inference.*;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2015 Lakshmi Krishnamurthy
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
 * CustomOvernightCurveReconciler demonstrates the multi-stretch transition custom Overnight curve
 *  construction, turns application, discount factor extraction, and calibration quote recovery.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class CustomOvernightCurveReconciler {

	private static final FixFloatComponent OTCOISFixFloat (
		final JulianDate dtSpot,
		final String strCurrency,
		final String strMaturityTenor,
		final double dblCoupon)
	{
		FixedFloatSwapConvention ffConv = OvernightFixedFloatContainer.ConventionFromJurisdiction (
			strCurrency
		);

		return ffConv.createFixFloatComponent (
			dtSpot,
			strMaturityTenor,
			dblCoupon,
			0.,
			1.
		);
	}

	/*
	 * Construct the Array of Deposit Instruments from the given set of parameters
	 * 
	 *  	USE WITH CARE: This sample ignores errors and does not handle exceptions.
	 */

	private static final SingleStreamComponent[] DepositInstrumentsFromMaturityDays (
		final JulianDate dtEffective,
		final String strCurrency,
		final int[] aiDay)
		throws Exception
	{
		SingleStreamComponent[] aDeposit = new SingleStreamComponent[aiDay.length];

		for (int i = 0; i < aiDay.length; ++i)
			aDeposit[i] = SingleStreamComponentBuilder.Deposit (
				dtEffective,
				dtEffective.addBusDays (
					aiDay[i],
					strCurrency
				),
				ForwardLabel.Create (strCurrency, "ON")
			);

		return aDeposit;
	}

	/*
	 * Construct the Array of Overnight Index Instruments from the given set of parameters
	 * 
	 *  	USE WITH CARE: This sample ignores errors and does not handle exceptions.
	 */

	private static final FixFloatComponent[] OISFromMaturityTenor (
		final JulianDate dtSpot,
		final String strCurrency,
		final String[] astrMaturityTenor,
		final double[] adblCoupon)
		throws Exception
	{
		FixFloatComponent[] aOIS = new FixFloatComponent[astrMaturityTenor.length];

		for (int i = 0; i < astrMaturityTenor.length; ++i)
			aOIS[i] = OTCOISFixFloat (
				dtSpot,
				strCurrency,
				astrMaturityTenor[i],
				adblCoupon[i]
			);

		return aOIS;
	}

	/*
	 * Construct the Array of Overnight Index Future Instruments from the given set of parameters
	 * 
	 *  	USE WITH CARE: This sample ignores errors and does not handle exceptions.
	 */

	private static final FixFloatComponent[] OISFuturesFromMaturityTenor (
		final JulianDate dtSpot,
		final String strCurrency,
		final String[] astrStartTenor,
		final String[] astrMaturityTenor,
		final double[] adblCoupon)
		throws Exception
	{
		FixFloatComponent[] aOISFutures = new FixFloatComponent[astrMaturityTenor.length];

		for (int i = 0; i < astrMaturityTenor.length; ++i)
			aOISFutures[i] = OTCOISFixFloat (
				dtSpot.addTenor (astrStartTenor[i]),
				strCurrency,
				astrMaturityTenor[i],
				adblCoupon[i]
			);

		return aOISFutures;
	}

	/*
	 * This sample demonstrates the multi-stretch transition custom discount curve construction, turns
	 * 	application, discount factor extraction, and calibration quote recovery. It shows the following
	 * 	steps:
	 * 	- Setup the linear curve calibrator.
	 * 	- Setup the Deposit instruments and their quotes for calibration.
	 * 	- Setup the Deposit instruments stretch latent state representation - this uses the discount factor
	 * 		quantification metric and the "rate" manifest measure.
	 * 	- Setup the OIS instruments and their quotes for calibration.
	 * 	- Setup the OIS instruments stretch latent state representation - this uses the discount factor
	 * 		quantification metric and the "rate" manifest measure.
	 * 	- Calibrate over the instrument set to generate a new overlapping latent state span instance.
	 * 	- Retrieve the "Deposit" stretch from the span.
	 * 	- Retrieve the "OIS" stretch from the span.
	 * 	- Create a discount curve instance by converting the overlapping stretch to an exclusive
	 * 		non-overlapping stretch.
	 * 	- Compare the discount factors and their monotonicity emitted from the discount curve, the
	 * 		non-overlapping span, and the "OIS" stretch across the range of tenor predictor ordinates.
	 * 	- Cross-Recovery of the Deposit Calibration Instrument "Rate" metric across the different curve
	 * 		construction methodologies.
	 * 	- Cross-Recovery of the OIS Calibration Instrument "Rate" metric across the different curve
	 * 		construction methodologies.
	 * 	- Create a turn list instance and add new turn instances.
	 * 	- Update the discount curve with the turn list.
	 * 	- Compare the discount factor implied the discount curve with and without applying the turns
	 * 		adjustment.
	 * 
	 *  	USE WITH CARE: This sample ignores errors and does not handle exceptions.
	 */

	private static final void SplineLinearOISDiscountCurve (
		final JulianDate dtSpot,
		final SegmentCustomBuilderControl prbp,
		final String strHeaderComment,
		final String strCurrency)
		throws Exception
	{
		System.out.println ("\n\t----------------------------------------------------------------");

		System.out.println ("\t" + strHeaderComment);

		/*
		 * Setup the linear curve calibrator
		 */

		LinearLatentStateCalibrator lcc = new LinearLatentStateCalibrator (
			prbp,
			BoundarySettings.NaturalStandard(),
			MultiSegmentSequence.CALIBRATE,
			null,
			null
		);

		/*
		 * Construct the Array of Deposit Instruments and their Quotes from the given set of parameters
		 */

		SingleStreamComponent[] aDepositComp = DepositInstrumentsFromMaturityDays (
			dtSpot,
			strCurrency,
			new int[] {
				1, 2, 3
			}
		);

		double[] adblDepositQuote = new double[] {
			0.0004, 0.0004, 0.0004		 // Deposit
		};

		/*
		 * Construct the Deposit Instrument Set Stretch Builder
		 */

		LatentStateStretchSpec depositStretch = LatentStateStretchBuilder.ForwardFundingStretchSpec (
			"   DEPOSIT   ",
			aDepositComp,
			"ForwardRate",
			adblDepositQuote
		);

		/*
		 * Construct the Array of Short End OIS Instruments and their Quotes from the given set of parameters
		 */

		double[] adblShortEndOISQuote = new double[] {
			0.00070,    //   1W
			0.00069,    //   2W
			0.00078,    //   3W
			0.00074     //   1M
		};

		CalibratableFixedIncomeComponent[] aShortEndOISComp = OISFromMaturityTenor (
			dtSpot,
			strCurrency,
			new java.lang.String[] {
				"1W", "2W", "3W", "1M"
			},
			adblShortEndOISQuote
		);

		/*
		 * Construct the Short End OIS Instrument Set Stretch Builder
		 */

		LatentStateStretchSpec oisShortEndStretch = LatentStateStretchBuilder.ForwardFundingStretchSpec (
			"SHORT END OIS",
			aShortEndOISComp,
			"SwapRate",
			adblShortEndOISQuote
		);

		/*
		 * Construct the Array of OIS Futures Instruments and their Quotes from the given set of parameters
		 */

		double[] adblOISFutureQuote = new double[] {
			 0.00046,    //   1M x 1M
			 0.00016,    //   2M x 1M
			-0.00007,    //   3M x 1M
			-0.00013,    //   4M x 1M
			-0.00014     //   5M x 1M
		};

		CalibratableFixedIncomeComponent[] aOISFutureComp = OISFuturesFromMaturityTenor (
			dtSpot,
			strCurrency,
			new java.lang.String[] {
				"1M", "2M", "3M", "4M", "5M"
			},
			new java.lang.String[] {
				"1M", "1M", "1M", "1M", "1M"
			},
			adblOISFutureQuote
		);

		/*
		 * Construct the OIS Future Instrument Set Stretch Builder
		 */

		LatentStateStretchSpec oisFutureStretch = LatentStateStretchBuilder.ForwardFundingStretchSpec (
			" OIS FUTURE  ",
			aOISFutureComp,
			"SwapRate",
			adblOISFutureQuote
		);

		/*
		 * Construct the Array of Long End OIS Instruments and their Quotes from the given set of parameters
		 */

		double[] adblLongEndOISQuote = new double[] {
			0.00002,    //  15M
			0.00008,    //  18M
			0.00021,    //  21M
			0.00036,    //   2Y
			0.00127,    //   3Y
			0.00274,    //   4Y
			0.00456,    //   5Y
			0.00647,    //   6Y
			0.00827,    //   7Y
			0.00996,    //   8Y
			0.01147,    //   9Y
			0.01280,    //  10Y
			0.01404,    //  11Y
			0.01516,    //  12Y
			0.01764,    //  15Y
			0.01939,    //  20Y
			0.02003,    //  25Y
			0.02038     //  30Y
		};

		CalibratableFixedIncomeComponent[] aLongEndOISComp = OISFromMaturityTenor (
			dtSpot,
			strCurrency,
			new java.lang.String[] {
				"15M", "18M", "21M", "2Y", "3Y", "4Y", "5Y", "6Y", "7Y", "8Y", "9Y", "10Y", "11Y", "12Y", "15Y", "20Y", "25Y", "30Y"
			},
			adblLongEndOISQuote
		);

		/*
		 * Construct the Long End OIS Instrument Set Stretch Builder
		 */

		LatentStateStretchSpec oisLongEndStretch = LatentStateStretchBuilder.ForwardFundingStretchSpec (
			"LONG END OIS ",
			aLongEndOISComp,
			"SwapRate",
			adblLongEndOISQuote
		);

		LatentStateStretchSpec[] aStretchSpec = new LatentStateStretchSpec[] {
			depositStretch,
			oisShortEndStretch,
			oisFutureStretch,
			oisLongEndStretch
		};

		/*
		 * Construct the Shape Preserving Discount Curve by applying the linear curve calibrator to the array
		 *  of Deposit and Swap Stretches.
		 */

		ValuationParams valParams = new ValuationParams (dtSpot, dtSpot, strCurrency);

		/*
		 * Calibrate over the instrument set to generate a new overlapping latent state span instance
		 */

		org.drip.spline.grid.OverlappingStretchSpan ors = lcc.calibrateSpan (
			aStretchSpec,
			1.,
			valParams,
			null,
			null,
			null
		);

		/*
		 * Retrieve the "Deposit" stretch from the span
		 */

		MultiSegmentSequence mssDeposit = ors.getStretch ("   DEPOSIT   ");

		/*
		 * Retrieve the OIS Short End stretch from the span
		 */

		MultiSegmentSequence mssOISShortEnd = ors.getStretch ("SHORT END OIS");

		/*
		 * Retrieve the OIS Future stretch from the span
		 */

		MultiSegmentSequence mssOISFuture = ors.getStretch (" OIS FUTURE  ");

		/*
		 * Retrieve the OIS Long End stretch from the span
		 */

		MultiSegmentSequence mssOISLongEnd = ors.getStretch ("LONG END OIS ");

		/*
		 * Create a discount curve instance by converting the overlapping stretch to an exclusive
		 * 	non-overlapping stretch.
		 */

		DiscountCurve dfdc = new DiscountFactorDiscountCurve (strCurrency, null, ors);

		/*
		 * Compare the discount factors and their monotonicity emitted from the discount curve, the
		 * non-overlapping span, and the "Deposit" stretch across the range of tenor predictor ordinates.
		 */

		System.out.println ("\t----------------------------------------------------------------");

		System.out.println ("\t     DEPOSITS DF           DFDC       STRETCH        LOCAL");

		System.out.println ("\t----------------------------------------------------------------");

		for (double dblX = mssDeposit.getLeftPredictorOrdinateEdge(); dblX <= mssDeposit.getRightPredictorOrdinateEdge();
			dblX += 0.25 * (mssDeposit.getRightPredictorOrdinateEdge() - mssDeposit.getLeftPredictorOrdinateEdge())) {
			try {
				System.out.println ("\tDEPOSIT [" + new JulianDate (dblX) + "] = " +
					FormatUtil.FormatDouble (dfdc.df (dblX), 1, 8, 1.) + " || " +
						ors.getContainingStretch (dblX).name() + " || " +
							FormatUtil.FormatDouble (mssDeposit.responseValue (dblX), 1, 8, 1.) + " | " +
								mssDeposit.monotoneType (dblX));
			} catch (java.lang.Exception e) {
				e.printStackTrace();
			}
		}

		/*
		 * Compare the discount factors and their monotonicity emitted from the discount curve, the
		 * non-overlapping span, and the OIS SHORT END stretch across the range of tenor predictor ordinates.
		 */

		System.out.println ("\n\t----------------------------------------------------------------");

		System.out.println ("\tSHORT END OIS DF        DFDC       STRETCH        LOCAL");

		System.out.println ("\t----------------------------------------------------------------");

		double dblShortOISWidth = 0.2 * (mssOISShortEnd.getRightPredictorOrdinateEdge() - mssOISShortEnd.getLeftPredictorOrdinateEdge());

		for (double dblX = mssOISShortEnd.getLeftPredictorOrdinateEdge(); dblX <= mssOISShortEnd.getRightPredictorOrdinateEdge();
			dblX += dblShortOISWidth) {
				System.out.println ("\tOIS [" + new JulianDate (dblX) + "] = " +
					FormatUtil.FormatDouble (dfdc.df (dblX), 1, 8, 1.) + " || " +
						ors.getContainingStretch (dblX).name() + " || " +
							FormatUtil.FormatDouble (mssOISShortEnd.responseValue (dblX), 1, 8, 1.) + " | " +
								mssOISShortEnd.monotoneType (dblX));
		}

		/*
		 * Compare the discount factors and their monotonicity emitted from the discount curve, the
		 * non-overlapping span, and the OIS FUTURE stretch across the range of tenor predictor ordinates.
		 */

		System.out.println ("\n\t----------------------------------------------------------------");

		System.out.println ("\t OIS FUTURE DF          DFDC       STRETCH        LOCAL");

		System.out.println ("\t----------------------------------------------------------------");

		double dblOISFutureWidth = 0.2 * (mssOISFuture.getRightPredictorOrdinateEdge() - mssOISFuture.getLeftPredictorOrdinateEdge());

		for (double dblX = mssOISFuture.getLeftPredictorOrdinateEdge(); dblX <= mssOISFuture.getRightPredictorOrdinateEdge();
			dblX += dblOISFutureWidth) {
				System.out.println ("\tOIS [" + new JulianDate (dblX) + "] = " +
					FormatUtil.FormatDouble (dfdc.df (dblX), 1, 8, 1.) + " || " +
						ors.getContainingStretch (dblX).name() + " || " +
							FormatUtil.FormatDouble (mssOISFuture.responseValue (dblX), 1, 8, 1.) + " | " +
								mssOISFuture.monotoneType (dblX));
		}

		/*
		 * Compare the discount factors and their monotonicity emitted from the discount curve, the
		 * non-overlapping span, and the OIS LONG END stretch across the range of tenor predictor ordinates.
		 */

		System.out.println ("\n\t----------------------------------------------------------------");

		System.out.println ("\tLONG END OIS DF         DFDC      STRETCH         LOCAL");

		System.out.println ("\t----------------------------------------------------------------");

		for (double dblX = mssOISFuture.getLeftPredictorOrdinateEdge(); dblX <= mssOISFuture.getRightPredictorOrdinateEdge();
			dblX += dblOISFutureWidth) {
				System.out.println ("\tOIS [" + new JulianDate (dblX) + "] = " +
					FormatUtil.FormatDouble (dfdc.df (dblX), 1, 8, 1.) + " || " +
						ors.getContainingStretch (dblX).name() + " || " +
							FormatUtil.FormatDouble (mssOISFuture.responseValue (dblX), 1, 8, 1.) + " | " +
								mssOISFuture.monotoneType (dblX));
		}

		double dblLongOISWidth = 0.1 * (mssOISLongEnd.getRightPredictorOrdinateEdge() - mssOISLongEnd.getLeftPredictorOrdinateEdge());

		for (double dblX = mssOISLongEnd.getLeftPredictorOrdinateEdge() + dblLongOISWidth; dblX <= mssOISLongEnd.getRightPredictorOrdinateEdge();
			dblX += dblLongOISWidth) {
				System.out.println ("\tOIS [" + new JulianDate (dblX) + "] = " +
					FormatUtil.FormatDouble (dfdc.df (dblX), 1, 8, 1.) + " || " +
						ors.getContainingStretch (dblX).name() + " || " +
							FormatUtil.FormatDouble (mssOISLongEnd.responseValue (dblX), 1, 8, 1.) + " | " +
								mssOISLongEnd.monotoneType (dblX));
		}

		System.out.println ("\tOIS [" + dtSpot.addTenor ("60Y") + "] = " +
			FormatUtil.FormatDouble (dfdc.df (dtSpot.addTenor ("60Y")), 1, 8, 1.));

		/*
		 * Cross-Recovery of the Deposit Calibration Instrument "Rate" metric across the different curve
		 * 	construction methodologies.
		 */

		System.out.println ("\n\t----------------------------------------------------------------");

		System.out.println ("\t     DEPOSIT INSTRUMENTS CALIBRATION RECOVERY");

		System.out.println ("\t----------------------------------------------------------------");

		for (int i = 0; i < aDepositComp.length; ++i)
			System.out.println ("\t[" + aDepositComp[i].maturityDate() + "] = " +
				FormatUtil.FormatDouble (aDepositComp[i].measureValue (valParams, null,
					MarketParamsBuilder.Create (dfdc, null, null, null, null, null, null),
						null, "Rate"), 1, 6, 1.) + " | " + FormatUtil.FormatDouble (adblDepositQuote[i], 1, 6, 1.));

		/*
		 * Cross-Recovery of the OIS Short End Calibration Instrument "Rate" metric across the different curve
		 * 	construction methodologies.
		 */

		System.out.println ("\n\t----------------------------------------------------------------");

		System.out.println ("\t      OIS SHORT END INSTRUMENTS CALIBRATION RECOVERY");

		System.out.println ("\t----------------------------------------------------------------");

		for (int i = 0; i < aShortEndOISComp.length; ++i)
			System.out.println ("\t[" + aShortEndOISComp[i].maturityDate() + "] = " +
				FormatUtil.FormatDouble (aShortEndOISComp[i].measureValue (valParams, null,
					MarketParamsBuilder.Create (dfdc, null, null, null, null, null, null),
						null, "SwapRate"), 1, 6, 1.) + " | " + FormatUtil.FormatDouble (adblShortEndOISQuote[i], 1, 6, 1.));

		/*
		 * Cross-Recovery of the OIS Future Calibration Instrument "Rate" metric across the different curve
		 * 	construction methodologies.
		 */

		System.out.println ("\n\t----------------------------------------------------------------");

		System.out.println ("\t      OIS FUTURES INSTRUMENTS CALIBRATION RECOVERY");

		System.out.println ("\t----------------------------------------------------------------");

		for (int i = 0; i < aOISFutureComp.length; ++i)
			System.out.println ("\t[" + aOISFutureComp[i].maturityDate() + "] = " +
				FormatUtil.FormatDouble (aOISFutureComp[i].measureValue (valParams, null,
					MarketParamsBuilder.Create (dfdc, null, null, null, null, null, null),
						null, "SwapRate"), 1, 6, 1.) + " | " + FormatUtil.FormatDouble (adblOISFutureQuote[i], 1, 6, 1.));

		/*
		 * Cross-Recovery of the OIS Long End Calibration Instrument "Rate" metric across the different curve
		 * 	construction methodologies.
		 */

		System.out.println ("\n\t----------------------------------------------------------------");

		System.out.println ("\t      OIS LONG END INSTRUMENTS CALIBRATION RECOVERY");

		System.out.println ("\t----------------------------------------------------------------");

		for (int i = 0; i < aLongEndOISComp.length; ++i)
			System.out.println ("\t[" + aLongEndOISComp[i].maturityDate() + "] = " +
				FormatUtil.FormatDouble (aLongEndOISComp[i].measureValue (valParams, null,
					MarketParamsBuilder.Create (dfdc, null, null, null, null, null, null),
						null, "CalibSwapRate"), 1, 6, 1.) + " | " + FormatUtil.FormatDouble (adblLongEndOISQuote[i], 1, 6, 1.));

		/*
		 * Create a turn list instance and add new turn instances
		 */

		TurnListDiscountFactor tldc = new TurnListDiscountFactor();

		tldc.addTurn (new Turn (
			dtSpot.addTenor ("5Y").julian(),
			dtSpot.addTenor ("40Y").julian(),
			0.001));

		/*
		 * Update the discount curve with the turn list.
		 */

		dfdc.setTurns (tldc);

		/*
		 * Compare the discount factor implied the discount curve with and without applying the turns
		 * 	adjustment.
		 */

		System.out.println ("\n\t-------------------------------");

		System.out.println ("\t  TURNS ADJ DF         DFDC");

		System.out.println ("\t-------------------------------");

		for (double dblX = mssOISShortEnd.getLeftPredictorOrdinateEdge(); dblX <= mssOISLongEnd.getRightPredictorOrdinateEdge();
			dblX += 0.05 * (mssOISLongEnd.getRightPredictorOrdinateEdge() - mssOISShortEnd.getLeftPredictorOrdinateEdge())) {
				System.out.println ("\tOIS [" + new JulianDate (dblX) + "] = " +
					FormatUtil.FormatDouble (dfdc.df (dblX), 1, 8, 1.));
		}
	}

	public static final void main (
		final String[] astrArgs)
		throws Exception
	{
		/*
		 * Initialize the Credit Analytics Library
		 */

		CreditAnalytics.Init ("");

		/*
		 * Construct the segment Custom builder using the following parameters:
		 * 	- Cubic Exponential Mixture Basis Spline Set
		 * 	- Ck = 2, Segment Curvature Penalty = 2
		 * 	- Quadratic Rational Shape Controller
		 */

		SegmentCustomBuilderControl prbpPolynomial = new SegmentCustomBuilderControl (
			MultiSegmentSequenceBuilder.BASIS_SPLINE_POLYNOMIAL,
			new PolynomialFunctionSetParams (4),
			SegmentInelasticDesignControl.Create (2, 2),
			new ResponseScalingShapeControl (true, new QuadraticRationalShapeControl (0.)),
			null
		);

		String strCurrency = "EUR";

		JulianDate dtToday = DateUtil.Today().addTenor ("0D");

		/*
		 * Runs the full spline linear discount curve builder sample using the overnight index discount curve.
		 */

		SplineLinearOISDiscountCurve (dtToday, prbpPolynomial, "---- DISCOUNT CURVE WITH OVERNIGHT INDEX ---", strCurrency);
	}
}
