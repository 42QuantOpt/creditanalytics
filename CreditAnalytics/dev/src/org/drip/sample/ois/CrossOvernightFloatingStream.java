
package org.drip.sample.ois;

import java.util.*;

import org.drip.analytics.date.JulianDate;
import org.drip.analytics.daycount.Convention;
import org.drip.analytics.definition.LatentStateStatic;
import org.drip.analytics.rates.*;
import org.drip.analytics.support.*;
import org.drip.param.creator.*;
import org.drip.param.market.*;
import org.drip.param.valuation.*;
import org.drip.product.calib.*;
import org.drip.product.creator.*;
import org.drip.product.definition.CalibratableFixedIncomeComponent;
import org.drip.product.rates.*;
import org.drip.quant.common.*;
import org.drip.quant.function1D.*;
import org.drip.service.api.CreditAnalytics;
import org.drip.spline.basis.PolynomialFunctionSetParams;
import org.drip.spline.params.*;
import org.drip.spline.stretch.*;
import org.drip.state.identifier.*;
import org.drip.state.inference.*;
import org.drip.state.representation.LatentStateSpecification;

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
 * CrossOvernightStream demonstrates the construction, customization, and valuation of Cross-Currency
 * 	Overnight Floating Streams.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class CrossOvernightFloatingStream {

	/*
	 * Construct the Array of Deposit Instruments from the given set of parameters
	 * 
	 *  	USE WITH CARE: This sample ignores errors and does not handle exceptions.
	 */

	private static final GenericDepositComponent[] DepositInstrumentsFromMaturityDays (
		final JulianDate dtEffective,
		final String strCurrency,
		final int[] aiDay)
		throws Exception
	{
		GenericDepositComponent[] aDeposit = new GenericDepositComponent[aiDay.length];

		for (int i = 0; i < aiDay.length; ++i)
			aDeposit[i] = DepositBuilder.CreateDeposit (
				dtEffective,
				dtEffective.addBusDays (
					aiDay[i],
					strCurrency
				),
				null,
				strCurrency
			);

		return aDeposit;
	}

	private static final LatentStateStretchSpec DepositStretch (
		final GenericDepositComponent[] aDeposit,
		final double[] adblQuote)
		throws Exception
	{
		LatentStateSegmentSpec[] aSegmentSpec = new LatentStateSegmentSpec[aDeposit.length];

		String strCurrency = aDeposit[0].payCurrency()[0];

		for (int i = 0; i < aDeposit.length; ++i) {
			DepositComponentQuoteSet depositQuote = new DepositComponentQuoteSet (
				new LatentStateSpecification[] {
					new LatentStateSpecification (
						LatentStateStatic.LATENT_STATE_FUNDING,
						LatentStateStatic.DISCOUNT_QM_DISCOUNT_FACTOR,
						FundingLabel.Standard (strCurrency)
					)
				}
			);

			depositQuote.setRate (adblQuote[i]);

			aSegmentSpec[i] = new LatentStateSegmentSpec (
				aDeposit[i],
				depositQuote
			);
		}

		return new LatentStateStretchSpec (
			"DEPOSIT",
			aSegmentSpec
		);
	}

	private static final LatentStateStretchSpec EDFStretch (
		final EDFComponent[] aEDF,
		final double[] adblQuote)
		throws Exception
	{
		LatentStateSegmentSpec[] aSegmentSpec = new LatentStateSegmentSpec[aEDF.length];

		String strCurrency = aEDF[0].payCurrency()[0];

		for (int i = 0; i < aEDF.length; ++i) {
			EDFComponentQuoteSet edfQuote = new EDFComponentQuoteSet (
				new LatentStateSpecification[] {
					new LatentStateSpecification (
						LatentStateStatic.LATENT_STATE_FUNDING,
						LatentStateStatic.DISCOUNT_QM_DISCOUNT_FACTOR,
						FundingLabel.Standard (strCurrency)
					),
					new LatentStateSpecification (
						LatentStateStatic.LATENT_STATE_FORWARD,
						LatentStateStatic.FORWARD_QM_FORWARD_RATE,
						aEDF[i].forwardLabel()[0]
					)
				}
			);

			edfQuote.setRate (adblQuote[i]);

			aSegmentSpec[i] = new LatentStateSegmentSpec (
				aEDF[i],
				edfQuote
			);
		}

		return new LatentStateStretchSpec (
			"EDF",
			aSegmentSpec
		);
	}

	/*
	 * Construct the Array of OIS Instruments from the given set of parameters
	 * 
	 *  	USE WITH CARE: This sample ignores errors and does not handle exceptions.
	 */

	private static final GenericFixFloatComponent[] OISInstrumentsFromMaturityTenor (
		final JulianDate dtEffective,
		final String[] astrMaturityTenor,
		final double[] adblCoupon,
		final String strCurrency)
		throws Exception
	{
		GenericFixFloatComponent[] aCalibComp = new GenericFixFloatComponent[astrMaturityTenor.length];

		for (int i = 0; i < astrMaturityTenor.length; ++i) {
			GenericStream floatStream = new GenericStream (
				PeriodBuilder.DailyPeriodDailyReset (
					dtEffective.julian(),
					dtEffective.addTenor (astrMaturityTenor[i]).julian(),
					Double.NaN,
					null,
					null,
					"Act/360",
					strCurrency,
					-1.,
					null,
					0.,
					strCurrency,
					strCurrency,
					CompositePeriodUtil.ACCRUAL_COMPOUNDING_RULE_ARITHMETIC,
					OvernightFRIBuilder.JurisdictionFRI (strCurrency),
					null
				)
			);

			GenericStream fixStream = new GenericStream (
				PeriodBuilder.RegularPeriodSingleReset (
					dtEffective.julian(),
					astrMaturityTenor[i],
					Double.NaN,
					null,
					null,
					null,
					null,
					null,
					null,
					null,
					null,
					2,
					"Act/360",
					false,
					"Act/360",
					false,
					false,
					strCurrency,
					1.,
					null,
					adblCoupon[i],
					strCurrency,
					strCurrency,
					null,
					null
				)
			);

			GenericFixFloatComponent ois = new GenericFixFloatComponent (
				fixStream,
				floatStream,
				new CashSettleParams (0, strCurrency, 0)
			);

			ois.setPrimaryCode ("OIS." + astrMaturityTenor + "." + strCurrency);

			aCalibComp[i] = ois;
		}

		return aCalibComp;
	}

	private static final LatentStateStretchSpec OISStretch (
		final CalibratableFixedIncomeComponent[] aCFIC,
		final double[] adblQuote)
		throws Exception
	{
		LatentStateSegmentSpec[] aSegmentSpec = new LatentStateSegmentSpec[aCFIC.length];

		String strCurrency = aCFIC[0].payCurrency()[0];

		for (int i = 0; i < aCFIC.length; ++i) {
			FixFloatQuoteSet oisQuote = new FixFloatQuoteSet (
				new LatentStateSpecification[] {
					new LatentStateSpecification (
						LatentStateStatic.LATENT_STATE_FUNDING,
						LatentStateStatic.DISCOUNT_QM_DISCOUNT_FACTOR,
						FundingLabel.Standard (strCurrency)
					),
					new LatentStateSpecification (
						LatentStateStatic.LATENT_STATE_FORWARD,
						LatentStateStatic.FORWARD_QM_FORWARD_RATE,
						aCFIC[i].forwardLabel()[0]
					)
				}
			);

			oisQuote.setSwapRate (adblQuote[i]);

			aSegmentSpec[i] = new LatentStateSegmentSpec (
				aCFIC[i],
				oisQuote
			);
		}

		return new LatentStateStretchSpec (
			"OIS",
			aSegmentSpec
		);
	}

	private static final DiscountCurve CustomOISCurveBuilderSample (
		final JulianDate dtSpot,
		final String strCurrency)
		throws Exception
	{
		/*
		 * Construct the Array of Deposit Instruments and their Quotes from the given set of parameters
		 */

		GenericDepositComponent[] aDepositComp = DepositInstrumentsFromMaturityDays (
			dtSpot,
			strCurrency,
			new int[] {
				1, 2, 3, 7, 14, 21, 30, 60
			}
		);

		double[] adblDepositQuote = new double[] {
			0.01200, 0.01200, 0.01200, 0.01450, 0.01550, 0.01600, 0.01660, 0.01850, // Cash
		};

		/*
		 * Construct the Deposit Instrument Set Stretch Builder
		 */

		LatentStateStretchSpec depositStretch = DepositStretch (
			aDepositComp,
			adblDepositQuote
		);

		/*
		 * Construct the Array of EDF Instruments and their Quotes from the given set of parameters
		 */

		EDFComponent[] aEDFComp = EDFutureBuilder.GenerateEDPack (
			dtSpot,
			4,
			strCurrency
		);

		double[] adblEDFQuote = new double[] {
			0.01612, 0.01580, 0.01589, 0.01598
		};

		/*
		 * Construct the Cash Instrument Set Stretch Builder
		 */

		LatentStateStretchSpec edfStretch = EDFStretch (
			aEDFComp,
			adblEDFQuote
		);

		/*
		 * Construct the Array of OIS Instruments and their Quotes from the given set of parameters
		 */

		double[] adblOISQuote = new double[] {
			0.02604,    //  4Y
			0.02808,    //  5Y
			0.02983,    //  6Y
			0.03136,    //  7Y
			0.03268,    //  8Y
			0.03383,    //  9Y
			0.03488     // 10Y
		};

		GenericFixFloatComponent[] aOISComp = OISInstrumentsFromMaturityTenor (
			dtSpot,
			new java.lang.String[] {
				"4Y", "5Y", "6Y", "7Y", "8Y", "9Y", "10Y"
			},
			adblOISQuote,
			strCurrency
		);

		/*
		 * Construct the OIS Instrument Set Stretch Builder
		 */

		LatentStateStretchSpec oisStretch = OISStretch (
			aOISComp,
			adblOISQuote
		);

		LatentStateStretchSpec[] aStretchSpec = new LatentStateStretchSpec[] {
			depositStretch,
			edfStretch,
			oisStretch
		};

		/*
		 * Set up the Linear Curve Calibrator using the following parameters:
		 * 	- Cubic Exponential Mixture Basis Spline Set
		 * 	- Ck = 2, Segment Curvature Penalty = 2
		 * 	- Quadratic Rational Shape Controller
		 * 	- Natural Boundary Setting
		 */

		LinearLatentStateCalibrator lcc = new LinearLatentStateCalibrator (
			new SegmentCustomBuilderControl (
				MultiSegmentSequenceBuilder.BASIS_SPLINE_POLYNOMIAL,
				new PolynomialFunctionSetParams (4),
				SegmentInelasticDesignControl.Create (2, 2),
				new ResponseScalingShapeControl (true, new QuadraticRationalShapeControl (0.)),
				null),
			BoundarySettings.NaturalStandard(),
			MultiSegmentSequence.CALIBRATE,
			null,
			null
		);

		/*
		 * Construct the Shape Preserving Discount Curve by applying the linear curve calibrator to the array
		 *  of Cash and Swap Stretches.
		 */

		return ScenarioDiscountCurveBuilder.ShapePreservingDFBuild (
			lcc,
			aStretchSpec,
			new ValuationParams (dtSpot, dtSpot, strCurrency),
			null,
			null,
			null,
			1.
		);
	}

	private static final LatentStateFixingsContainer SetFlatOvernightFixings (
		final JulianDate dtStart,
		final JulianDate dtEnd,
		final JulianDate dtValue,
		final ForwardLabel fri,
		final double dblFlatFixing,
		final double dblNotional)
		throws Exception
	{
		LatentStateFixingsContainer lsfc = new LatentStateFixingsContainer();

		double dblAccount = 1.;

		double dblPrevDate = dtStart.julian();

		JulianDate dt = dtStart.addDays (1);

		while (dt.julian() <= dtEnd.julian()) {
			lsfc.add (dt, fri, dblFlatFixing);

			if (dt.julian() <= dtValue.julian()) {
				double dblAccrualFraction = Convention.YearFraction (
					dblPrevDate,
					dt.julian(),
					"Act/360",
					false,
					null,
					"USD"
				);

				dblAccount *= (1. + dblFlatFixing * dblAccrualFraction);
			}

			dblPrevDate = dt.julian();

			dt = dt.addBusDays (1, "USD");
		}

		System.out.println ("\tManual Calc Float Accrued (Geometric Compounding): " + (dblAccount - 1.) * dblNotional);

		System.out.println ("\tManual Calc Float Accrued (Arithmetic Compounding): " +
			((dtValue.julian() - dtStart.julian()) * dblNotional * dblFlatFixing / 360.));

		return lsfc;
	}

	public static final void main (
		final String[] astrArgs)
		throws Exception
	{

		double dblOISVol = 0.3;
		double dblUSDFundingVol = 0.3;
		double dblUSDFundingUSDOISCorrelation = 0.3;

		/*
		 * Initialize the Credit Analytics Library
		 */

		CreditAnalytics.Init ("");

		String strCurrency = "USD";

		JulianDate dtToday = JulianDate.Today().addTenorAndAdjust (
			"0D",
			strCurrency
		);

		DiscountCurve dc = CustomOISCurveBuilderSample (
			dtToday,
			strCurrency
		);

		JulianDate dtCustomOISStart = dtToday.subtractTenor ("2M");

		JulianDate dtCustomOISMaturity = dtToday.addTenor ("4M");

		ForwardLabel fri = OvernightFRIBuilder.JurisdictionFRI (strCurrency);

		GenericStream floatStreamGeometric = new GenericStream (
			PeriodBuilder.RegularPeriodDailyReset (
				dtCustomOISStart.julian(),
				"6M",
				Double.NaN,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				4,
				"Act/360",
				false,
				"Act/360",
				false,
				false,
				strCurrency,
				-1.,
				null,
				0.,
				strCurrency,
				strCurrency,
				CompositePeriodUtil.ACCRUAL_COMPOUNDING_RULE_GEOMETRIC,
				OvernightFRIBuilder.JurisdictionFRI (strCurrency),
				null
			)
		);

		GenericStream floatStreamArithmetic = new GenericStream (
			PeriodBuilder.RegularPeriodDailyReset (
				dtCustomOISStart.julian(),
				"6M",
				Double.NaN,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				4,
				"Act/360",
				false,
				"Act/360",
				false,
				false,
				strCurrency,
				-1.,
				null,
				0.,
				strCurrency,
				strCurrency,
				CompositePeriodUtil.ACCRUAL_COMPOUNDING_RULE_ARITHMETIC,
				OvernightFRIBuilder.JurisdictionFRI (strCurrency),
				null
			)
		);

		CurveSurfaceQuoteSet mktParams = MarketParamsBuilder.Create (
			dc,
			null,
			null,
			null,
			null,
			null,
			SetFlatOvernightFixings (
				dtCustomOISStart,
				dtCustomOISMaturity,
				dtToday,
				fri,
				0.003,
				-1.)
			);

		ValuationParams valParams = new ValuationParams (dtToday, dtToday, strCurrency);

		FundingLabel fundingLabelUSD = FundingLabel.Standard ("USD");

		mktParams.setFundingCurveVolSurface (fundingLabelUSD, new FlatUnivariate (dblUSDFundingVol));

		mktParams.setForwardCurveVolSurface (fri, new FlatUnivariate (dblOISVol));

		mktParams.setForwardFundingCorrSurface (fri, fundingLabelUSD, new FlatUnivariate (dblUSDFundingUSDOISCorrelation));

		Map<String, Double> mapGeometricOutput = floatStreamGeometric.value (
			valParams,
			null,
			mktParams,
			null);

		Map<String, Double> mapArithmeticOutput = floatStreamArithmetic.value (
			valParams,
			null,
			mktParams,
			null);

		System.out.println ("\n\t-----------------------------------");

		System.out.println ("\t  GEOMETRIC |  ARITHMETIC | CHECK");

		System.out.println ("\t-----------------------------------\n");

		for (Map.Entry<String, Double> meGeometric : mapGeometricOutput.entrySet()) {
			String strKey = meGeometric.getKey();

			double dblGeometricMeasure = meGeometric.getValue();

			double dblArithmeticMeasure = mapArithmeticOutput.get (strKey);

			String strMatch = NumberUtil.WithinTolerance (dblGeometricMeasure, dblArithmeticMeasure, 1.e-08, 1.e-04) ?
				"MATCH " :
				"DIFFER";

			System.out.println ("\t" +
				FormatUtil.FormatDouble (dblGeometricMeasure, 1, 8, 1.) + " | " +
				FormatUtil.FormatDouble (dblArithmeticMeasure, 1, 8, 1.) + " | " +
				strMatch + " <= " + strKey
			);
		}
	}
}
