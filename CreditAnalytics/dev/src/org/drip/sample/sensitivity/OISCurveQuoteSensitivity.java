
package org.drip.sample.sensitivity;

import org.drip.analytics.date.JulianDate;
import org.drip.analytics.daycount.*;
import org.drip.analytics.rates.DiscountCurve;
import org.drip.param.creator.*;
import org.drip.param.market.ComponentMarketParamSet;
import org.drip.param.valuation.ValuationParams;
import org.drip.product.creator.*;
import org.drip.product.definition.*;
import org.drip.product.ois.*;
import org.drip.product.rates.*;
import org.drip.quant.calculus.WengertJacobian;
import org.drip.quant.common.FormatUtil;
import org.drip.quant.function1D.QuadraticRationalShapeControl;
import org.drip.service.api.CreditAnalytics;
import org.drip.spline.basis.ExponentialTensionSetParams;
import org.drip.spline.params.*;
import org.drip.spline.stretch.*;
import org.drip.state.estimator.*;

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
 * OISCurveQuoteSensitivity demonstrates the calculation of the OIS discount curve sensitivity to the
 * 	calibration instrument quotes. It does the following:
 * 	- Construct the Array of Cash/OIS Instruments and their Quotes from the given set of parameters.
 * 	- Construct the Cash/OIS Instrument Set Stretch Builder.
 * 	- Set up the Linear Curve Calibrator using the following parameters:
 * 		- Cubic Exponential Mixture Basis Spline Set
 * 		- Ck = 2, Segment Curvature Penalty = 2
 * 		- Quadratic Rational Shape Controller
 * 		- Natural Boundary Setting
 * 	- Construct the Shape Preserving OIS Discount Curve by applying the linear curve calibrator to the array
 * 		of Cash and OIS Stretches.
 * 	- Cross-Comparison of the Cash/OIS Calibration Instrument "Rate" metric across the different curve
 * 		construction methodologies.
 * 	- Display of the Cash Instrument Discount Factor Quote Jacobian Sensitivities.
 * 	- Display of the OIS Instrument Discount Factor Quote Jacobian Sensitivities.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class OISCurveQuoteSensitivity {

	/*
	 * Construct the Array of Deposit Instruments from the given set of parameters
	 * 
	 *  	USE WITH CARE: This sample ignores errors and does not handle exceptions.
	 */

	private static final CalibratableFixedIncomeComponent[] DepositInstrumentsFromMaturityDays (
		final JulianDate dtEffective,
		final int[] aiDay)
		throws Exception
	{
		CalibratableFixedIncomeComponent[] aCalibComp = new CalibratableFixedIncomeComponent[aiDay.length];

		for (int i = 0; i < aiDay.length; ++i)
			aCalibComp[i] = CashBuilder.CreateCash (dtEffective, dtEffective.addBusDays (aiDay[i], "USD"), "USD");

		return aCalibComp;
	}

	/*
	 * Construct the Array of OIS Instruments from the given set of parameters
	 * 
	 *  	USE WITH CARE: This sample ignores errors and does not handle exceptions.
	 */

	private static final CalibratableFixedIncomeComponent[] OISInstrumentsFromMaturityTenor (
		final JulianDate dtEffective,
		final String[] astrTenor,
		final double[] adblCoupon,
		final String strCurrency)
		throws Exception
	{
		CalibratableFixedIncomeComponent[] aCalibComp = new CalibratableFixedIncomeComponent[astrTenor.length];

		DateAdjustParams dap = new DateAdjustParams (Convention.DR_FOLL, strCurrency);

		for (int i = 0; i < astrTenor.length; ++i) {
			JulianDate dtMaturity = dtEffective.addTenorAndAdjust (astrTenor[i], strCurrency);

			OvernightFundFloatingStream floatStream = OvernightFundFloatingStream.Create (dtEffective.getJulian(),
				dtMaturity.getJulian(), 0., OvernightFRIBuilder.JurisdictionFRI (strCurrency),
					"Act/360", dap, dap, null, -1., strCurrency, strCurrency, false);

			FixedStream fixStream = new FixedStream (dtEffective.getJulian(), dtMaturity.getJulian(),
				adblCoupon[i], 2, "Act/360", "Act/360", false, null, dap, dap, dap, dap, dap, null, null, 1.,
					strCurrency, strCurrency);

			IRSComponent ois = new IRSComponent (fixStream, floatStream);

			ois.setPrimaryCode ("OIS." + dtMaturity.toString() + "." + strCurrency);

			aCalibComp[i] = ois;
		}

		return aCalibComp;
	}

	private static final void TenorJack (
		final JulianDate dtStart,
		final String strTenor,
		final DiscountCurve dc)
	{
		RatesComponent irsBespoke = RatesStreamBuilder.CreateIRS (
			dtStart, dtStart.addTenorAndAdjust (strTenor, "USD"),
			0.,
			"USD",
			"USD-LIBOR-6M",
			"USD");

		WengertJacobian wjDFQuoteBespokeMat = dc.jackDDFDManifestMeasure (irsBespoke.getMaturityDate(), "Rate");

		System.out.println (strTenor + " => " + wjDFQuoteBespokeMat.displayString());
	}

	private static final void Forward6MRateJack (
		final JulianDate dtStart,
		final String strStartTenor,
		final DiscountCurve dc)
	{
		JulianDate dtBegin = dtStart.addTenorAndAdjust (strStartTenor, "USD");

		WengertJacobian wjForwardRate = dc.jackDForwardDManifestMeasure (dtBegin, "6M", "Rate", 0.5);

		System.out.println ("[" + dtBegin + " | 6M] => " + wjForwardRate.displayString());
	}

	/*
	 * This sample demonstrates the calculation of the discount curve sensitivity to the calibration
	 * 	instrument quotes. It does the following:
	 * 	- Construct the Array of Cash/OIS Instruments and their Quotes from the given set of parameters.
	 * 	- Construct the Cash/OIS Instrument Set Stretch Builder.
	 * 	- Set up the Linear Curve Calibrator using the following parameters:
	 * 		- Cubic Exponential Mixture Basis Spline Set
	 * 		- Ck = 2, Segment Curvature Penalty = 2
	 * 		- Quadratic Rational Shape Controller
	 * 		- Natural Boundary Setting
	 * 	- Construct the Shape Preserving Discount Curve by applying the linear curve calibrator to the array
	 * 		of Cash and OIS Stretches.
	 * 	- Cross-Comparison of the Cash/OIS Calibration Instrument "Rate" metric across the different curve
	 * 		construction methodologies.
	 * 	- Display of the Cash Instrument Discount Factor Quote Jacobian Sensitivities.
	 * 	- Display of the OIS Instrument Discount Factor Quote Jacobian Sensitivities.
	 * 
	 *  	USE WITH CARE: This sample ignores errors and does not handle exceptions.
	 */

	private static final void OISCurveQuoteSensitivitySample()
		throws Exception
	{
		/*
		 * Initialize the Credit Analytics Library
		 */

		CreditAnalytics.Init ("");

		JulianDate dtToday = JulianDate.Today().addTenorAndAdjust ("0D", "USD");

		/*
		 * Construct the Array of DEPOSIT Instruments and their Quotes from the given set of parameters
		 */

		CalibratableFixedIncomeComponent[] aDepositComp = DepositInstrumentsFromMaturityDays (
			dtToday,
			new int[] {1, 2, 7, 14, 30, 60});

		double[] adblDepositQuote = new double[] {
			0.0013, 0.0017, 0.0017, 0.0018, 0.0020, 0.0023}; // Cash Rate

		/*
		 * Construct the DEPOSIT Instrument Set Stretch Builder
		 */

		StretchRepresentationSpec rbsDeposit = StretchRepresentationSpec.CreateStretchBuilderSet (
			"DEPOSIT",
			DiscountCurve.LATENT_STATE_DISCOUNT,
			DiscountCurve.QUANTIFICATION_METRIC_DISCOUNT_FACTOR,
			aDepositComp,
			"Rate",
			adblDepositQuote,
			null);

		/*
		 * Construct the Array of FUTURE Instruments and their Quotes from the given set of parameters
		 */

		CalibratableFixedIncomeComponent[] aFutureComp = EDFutureBuilder.GenerateEDPack (dtToday, 8, "USD");

		double[] adblFutureQuote = new double[] {
			0.0027, 0.0032, 0.0041, 0.0054, 0.0077, 0.0104, 0.0134, 0.0160}; // EDF Rate;

		/*
		 * Construct the FUTURE Instrument Set Stretch Builder
		 */

		StretchRepresentationSpec rbsFuture = StretchRepresentationSpec.CreateStretchBuilderSet (
			"FUTURE",
			DiscountCurve.LATENT_STATE_DISCOUNT,
			DiscountCurve.QUANTIFICATION_METRIC_DISCOUNT_FACTOR,
			aFutureComp,
			"Rate",
			adblFutureQuote,
			null);

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

		CalibratableFixedIncomeComponent[] aOISComp = OISInstrumentsFromMaturityTenor (
			dtToday,
			new java.lang.String[]
				{"4Y", "5Y", "6Y", "7Y", "8Y", "9Y", "10Y"},
			adblOISQuote,
			"USD");

		/*
		 * Construct the OIS Instrument Set Stretch Builder
		 */

		StretchRepresentationSpec rbsOIS = StretchRepresentationSpec.CreateStretchBuilderSet (
			"OIS",
			DiscountCurve.LATENT_STATE_DISCOUNT,
			DiscountCurve.QUANTIFICATION_METRIC_DISCOUNT_FACTOR,
			aOISComp,
			"Rate",
			adblOISQuote,
			null);

		StretchRepresentationSpec[] aSRS = new StretchRepresentationSpec[] {rbsDeposit, rbsFuture, rbsOIS};

		/*
		 * Set up the Linear Curve Calibrator using the following Default Segment Control parameters:
		 * 	- Cubic Exponential Mixture Basis Spline Set
		 * 	- Ck = 2, Segment Curvature Penalty = 2
		 * 	- Quadratic Rational Shape Controller
		 * 	- Prior Quote Sensitivity Control with first derivative tail fade, with FADE ON
		 * 	- Natural Boundary Setting
		 */

		LinearCurveCalibrator lcc = new LinearCurveCalibrator (
			new SegmentCustomBuilderControl (
				MultiSegmentSequenceBuilder.BASIS_SPLINE_KLK_HYPERBOLIC_TENSION,
				new ExponentialTensionSetParams (2.),
				SegmentInelasticDesignControl.Create (2, 2),
				new ResponseScalingShapeControl (true, new QuadraticRationalShapeControl (0.)),
				new org.drip.spline.params.PreceedingManifestSensitivityControl (true, 1, null)),
			BoundarySettings.NaturalStandard(),
			MultiSegmentSequence.CALIBRATE,
			null,
			null);

		/*
		 * Set up the DEPOSIT Segment Control parameters with the following details:
		 * 	- Cubic Exponential Mixture Basis Spline Set
		 * 	- Ck = 2, Segment Curvature Penalty = 2
		 * 	- Quadratic Rational Shape Controller
		 * 	- Prior Quote Sensitivity Control with first derivative tail fade, with FADE ON
		 * 	- Natural Boundary Setting
		 */

		lcc.setStretchSegmentBuilderControl (
			rbsDeposit.getName(),
			new SegmentCustomBuilderControl (
				MultiSegmentSequenceBuilder.BASIS_SPLINE_KLK_HYPERBOLIC_TENSION,
				new ExponentialTensionSetParams (2.),
				SegmentInelasticDesignControl.Create (2, 2),
				new ResponseScalingShapeControl (true, new QuadraticRationalShapeControl (0.)),
				new org.drip.spline.params.PreceedingManifestSensitivityControl (true, 1, null)));

		/*
		 * Set up the FUTURE Segment Control parameters with the following details:
		 * 	- Cubic Exponential Mixture Basis Spline Set
		 * 	- Ck = 2, Segment Curvature Penalty = 2
		 * 	- Quadratic Rational Shape Controller
		 * 	- Prior Quote Sensitivity Control with first derivative tail fade, with FADE OFF, RETAIN ON
		 * 	- Natural Boundary Setting
		 */

		lcc.setStretchSegmentBuilderControl (
			rbsFuture.getName(),
			new SegmentCustomBuilderControl (
				MultiSegmentSequenceBuilder.BASIS_SPLINE_KLK_HYPERBOLIC_TENSION,
				new ExponentialTensionSetParams (2.),
				SegmentInelasticDesignControl.Create (2, 2),
				new ResponseScalingShapeControl (true, new QuadraticRationalShapeControl (0.)),
				new org.drip.spline.params.PreceedingManifestSensitivityControl (false, 1, null)));

		/*
		 * Set up the OIS Segment Control parameters with the following details:
		 * 	- Cubic Exponential Mixture Basis Spline Set
		 * 	- Ck = 2, Segment Curvature Penalty = 2
		 * 	- Quadratic Rational Shape Controller
		 * 	- Prior Quote Sensitivity Control with first derivative tail fade, with FADE ON
		 * 	- Natural Boundary Setting
		 */

		lcc.setStretchSegmentBuilderControl (
			rbsOIS.getName(),
			new SegmentCustomBuilderControl (
				MultiSegmentSequenceBuilder.BASIS_SPLINE_KLK_HYPERBOLIC_TENSION,
				new ExponentialTensionSetParams (2.),
				SegmentInelasticDesignControl.Create (2, 2),
				new ResponseScalingShapeControl (true, new QuadraticRationalShapeControl (0.)),
				new org.drip.spline.params.PreceedingManifestSensitivityControl (true, 1, null)));

		/*
		 * Construct the Shape Preserving Discount Curve by applying the linear curve calibrator to the array
		 *  of DEPOSIT, FUTURE, and OIS Stretches.
		 */

		DiscountCurve dc = ScenarioDiscountCurveBuilder.ShapePreservingDFBuild (
			lcc,
			aSRS,
			new ValuationParams (dtToday, dtToday, "USD"),
			null,
			null,
			null,
			1.);

		/*
		 * Cross-Comparison of the DEPOSIT Calibration Instrument "Rate" metric across the different curve
		 * 	construction methodologies.
		 */

		System.out.println ("\n\t----------------------------------------------------------------");

		System.out.println ("\t     DEPOSIT INSTRUMENTS CALIBRATION RECOVERY");

		System.out.println ("\t----------------------------------------------------------------");

		for (int i = 0; i < aDepositComp.length; ++i)
			System.out.println ("\t[" + aDepositComp[i].getMaturityDate() + "] = " +
				FormatUtil.FormatDouble (aDepositComp[i].calcMeasureValue (new ValuationParams (dtToday, dtToday, "USD"), null,
					ComponentMarketParamsBuilder.CreateComponentMarketParams (dc, null, null, null, null, null, null),
						null, "Rate"), 1, 6, 1.) + " | " + FormatUtil.FormatDouble (adblDepositQuote[i], 1, 6, 1.));

		/*
		 * Cross-Comparison of the FUTURE Calibration Instrument "Rate" metric across the different curve
		 * 	construction methodologies.
		 */

		System.out.println ("\n\t----------------------------------------------------------------");

		System.out.println ("\t     FUTURE INSTRUMENTS CALIBRATION RECOVERY");

		System.out.println ("\t----------------------------------------------------------------");

		for (int i = 0; i < aFutureComp.length; ++i)
			System.out.println ("\t[" + aFutureComp[i].getMaturityDate() + "] = " +
				FormatUtil.FormatDouble (aFutureComp[i].calcMeasureValue (new ValuationParams (dtToday, dtToday, "USD"), null,
					ComponentMarketParamsBuilder.CreateComponentMarketParams (dc, null, null, null, null, null, null),
						null, "Rate"), 1, 6, 1.) + " | " + FormatUtil.FormatDouble (adblFutureQuote[i], 1, 6, 1.));

		/*
		 * Cross-Comparison of the OIS Calibration Instrument "Rate" metric across the different curve
		 * 	construction methodologies.
		 */

		System.out.println ("\n\t----------------------------------------------------------------");

		System.out.println ("\t     OIS INSTRUMENTS CALIBRATION RECOVERY");

		System.out.println ("\t----------------------------------------------------------------");

		for (int i = 0; i < aOISComp.length; ++i)
			System.out.println ("\t[" + aOISComp[i].getMaturityDate() + "] = " +
				FormatUtil.FormatDouble (aOISComp[i].calcMeasureValue (new ValuationParams (dtToday, dtToday, "USD"), null,
					ComponentMarketParamsBuilder.CreateComponentMarketParams (dc, null, null, null, null, null, null),
						null, "CalibSwapRate"), 1, 6, 1.) + " | " + FormatUtil.FormatDouble (adblOISQuote[i], 1, 6, 1.));

		/*
		 * Display of the DEPOSIT Instrument Discount Factor Quote Jacobian Sensitivities.
		 */

		System.out.println ("\n\t----------------------------------------------------------------");

		System.out.println ("\t     DEPOSIT MATURITY DISCOUNT FACTOR JACOBIAN");

		System.out.println ("\t----------------------------------------------------------------");

		for (int i = 0; i < aDepositComp.length; ++i) {
			org.drip.quant.calculus.WengertJacobian wj = dc.jackDDFDManifestMeasure (aDepositComp[i].getMaturityDate(), "Rate");

			System.out.println (aDepositComp[i].getMaturityDate() + " => " + wj.displayString());
		}

		/*
		 * Display of the FUTURE Instrument Discount Factor Quote Jacobian Sensitivities.
		 */

		System.out.println ("\n\t----------------------------------------------------------------");

		System.out.println ("\t     FUTURE MATURITY DISCOUNT FACTOR JACOBIAN");

		System.out.println ("\t----------------------------------------------------------------");

		for (int i = 0; i < aFutureComp.length; ++i) {
			org.drip.quant.calculus.WengertJacobian wj = dc.jackDDFDManifestMeasure (aFutureComp[i].getMaturityDate(), "Rate");

			System.out.println (aFutureComp[i].getMaturityDate() + " => " + wj.displayString());
		}

		/*
		 * Display of the OIS Instrument Discount Factor Quote Jacobian Sensitivities.
		 */

		System.out.println ("\n\t----------------------------------------------------------------");

		System.out.println ("\t     OIS MATURITY DISCOUNT FACTOR JACOBIAN");

		System.out.println ("\t----------------------------------------------------------------");

		for (int i = 0; i < aOISComp.length; ++i) {
			org.drip.quant.calculus.WengertJacobian wjDFQuote = dc.jackDDFDManifestMeasure (aOISComp[i].getMaturityDate(), "Rate");

			System.out.println (aOISComp[i].getMaturityDate() + " => " + wjDFQuote.displayString());
		}

		System.out.println ("\n\t----------------------------------------------------------------");

		System.out.println ("\t     COMPONENT-BY-COMPONENT QUOTE JACOBIAN");

		System.out.println ("\t----------------------------------------------------------------");

		WengertJacobian wj = dc.compJackDPVDManifestMeasure (dtToday);

		System.out.println (wj.displayString());

		System.out.println ("\n\t----------------------------------------------------------------");

		System.out.println ("\t     BESPOKE 35Y OIS QUOTE JACOBIAN");

		System.out.println ("\t----------------------------------------------------------------");

		RatesComponent irs35Y = RatesStreamBuilder.CreateIRS (
			dtToday, dtToday.addTenorAndAdjust ("35Y", "USD"),
			0.,
			"USD",
			"USD-LIBOR-6M",
			"USD");

		WengertJacobian wjIRSBespokeQuoteJack = irs35Y.jackDDirtyPVDManifestMeasure (
			new ValuationParams (dtToday, dtToday, "USD"),
			null,
			new ComponentMarketParamSet (dc, null, null, null, null, null, null, null, null),
			null);

		System.out.println (wjIRSBespokeQuoteJack.displayString());

		System.out.println ("\n\t----------------------------------------------------------------");

		System.out.println ("\t     BESPOKE OIS MATURITY QUOTE JACOBIAN");

		System.out.println ("\t----------------------------------------------------------------");

		TenorJack (dtToday, "30Y", dc);

		TenorJack (dtToday, "32Y", dc);

		TenorJack (dtToday, "34Y", dc);

		TenorJack (dtToday, "36Y", dc);

		TenorJack (dtToday, "38Y", dc);

		TenorJack (dtToday, "40Y", dc);

		System.out.println ("\n\t----------------------------------------------------------------");

		System.out.println ("\t     OIS CURVE IMPLIED 6M FORWARD RATE QUOTE JACOBIAN");

		System.out.println ("\t----------------------------------------------------------------");

		Forward6MRateJack (dtToday, "1D", dc);

		Forward6MRateJack (dtToday, "3M", dc);

		Forward6MRateJack (dtToday, "6M", dc);

		Forward6MRateJack (dtToday, "1Y", dc);

		Forward6MRateJack (dtToday, "2Y", dc);

		Forward6MRateJack (dtToday, "5Y", dc);
	}

	public static final void main (
		final String[] astrArgs)
		throws Exception
	{
		OISCurveQuoteSensitivitySample();
	}
}
