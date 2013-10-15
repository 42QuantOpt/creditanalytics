	
package org.drip.sample.regime;

import org.drip.math.common.FormatUtil;
import org.drip.math.function.*;
import org.drip.math.regime.*;
import org.drip.math.segment.*;
import org.drip.math.spline.*;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2013 Lakshmi Krishnamurthy
 * 
 * This file is part of CreditAnalytics, a free-software/open-source library for fixed income analysts and
 * 		developers - http://www.credit-trader.org
 * 
 * CreditAnalytics is a free, full featured, fixed income credit analytics library, developed with a special
 * 		focus towards the needs of the bonds and credit products community.
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
 * RegimeEstimation demonstrates the Regime builder and usage API. It shows the following:
 * 	- Construction of segment control parameters - polynomial (regular/Bernstein) segment control,
 * 		exponential/hyperbolic tension segment control, Kaklis-Pandelis tension segment control.
 * 	- Control the segment using the rational shape controller, and the appropriate Ck
 * 	- Construct a calibrated regime Estimator.
 * 	- Insert a knot into the regime
 * 	- Estimate the node value and the node value Jacobian
 * 	- Calculate the segment/regime monotonicity
 *
 * @author Lakshmi Krishnamurthy
 */

public class RegimeEstimation {

	/**
	 * Build Polynomial Segment Control Parameters
	 * 
	 * @param iNumBasis Number of Polynomial Basis Functions
	 * @param segParams Inelastic Segment Parameters
	 * @param rssc Shape Controller
	 * 
	 * 	WARNING: Insufficient Error Checking, so use caution
	 * 
	 * @return Polynomial Segment Control Parameters
	 */

	public static final PredictorResponseBuilderParams PolynomialSegmentControlParams (
		final int iNumBasis,
		final DesignInelasticParams segParams,
		final ResponseScalingShapeController rssc)
		throws Exception
	{
		return new PredictorResponseBuilderParams (RegimeBuilder.BASIS_SPLINE_POLYNOMIAL, new PolynomialBasisSetParams (iNumBasis), segParams, rssc);
	}

	/**
	 * Build Bernstein Polynomial Segment Control Parameters
	 * 
	 * @param iNumBasis Number of Polynomial Basis Functions
	 * @param segParams Inelastic Segment Parameters
	 * @param rssc Shape Controller
	 * 
	 * 	WARNING: Insufficient Error Checking, so use caution
	 * 
	 * @return Bernstein Polynomial Segment Control Parameters
	 */

	public static final PredictorResponseBuilderParams BernsteinPolynomialSegmentControlParams (
		final int iNumBasis,
		final DesignInelasticParams segParams,
		final ResponseScalingShapeController rssc)
		throws Exception
	{
		return new PredictorResponseBuilderParams (RegimeBuilder.BASIS_SPLINE_BERNSTEIN_POLYNOMIAL, new PolynomialBasisSetParams (iNumBasis), segParams, rssc);
	}

	/**
	 * Build Exponential Tension Segment Control Parameters
	 * 
	 * @param dblTension Segment Tension
	 * @param segParams Inelastic Segment Parameters
	 * @param rssc Shape Controller
	 * 
	 * 	WARNING: Insufficient Error Checking, so use caution
	 * 
	 * @return Exponential Tension Segment Control Parameters
	 */

	public static final PredictorResponseBuilderParams ExponentialTensionSegmentControlParams (
		final double dblTension,
		final DesignInelasticParams segParams,
		final ResponseScalingShapeController rssc)
		throws Exception
	{
		return new PredictorResponseBuilderParams (RegimeBuilder.BASIS_SPLINE_EXPONENTIAL_TENSION, new ExponentialTensionBasisSetParams (dblTension), segParams, rssc);
	}

	/**
	 * Build Hyperbolic Tension Segment Control Parameters
	 * 
	 * @param dblTension Segment Tension
	 * @param segParams Inelastic Segment Parameters
	 * @param rssc Shape Controller
	 * 
	 * 	WARNING: Insufficient Error Checking, so use caution
	 * 
	 * @return Hyperbolic Tension Segment Control Parameters
	 */

	public static final PredictorResponseBuilderParams HyperbolicTensionSegmentControlParams (
		final double dblTension,
		final DesignInelasticParams segParams,
		final ResponseScalingShapeController rssc)
		throws Exception
	{
		return new PredictorResponseBuilderParams (RegimeBuilder.BASIS_SPLINE_HYPERBOLIC_TENSION, new ExponentialTensionBasisSetParams (dblTension), segParams, rssc);
	}

	/**
	 * Build Kaklis-Pandelis Segment Control Parameters
	 * 
	 * @param iKPTensionDegree KP Polynomial Tension Degree
	 * @param segParams Inelastic Segment Parameters
	 * @param rssc Shape Controller
	 * 
	 * 	WARNING: Insufficient Error Checking, so use caution
	 * 
	 * @return Kaklis-Pandelis Segment Control Parameters
	 */

	public static final PredictorResponseBuilderParams KaklisPandelisSegmentControlParams (
		final int iKPTensionDegree,
		final DesignInelasticParams segParams,
		final ResponseScalingShapeController rssc)
		throws Exception
	{
		return new PredictorResponseBuilderParams (RegimeBuilder.BASIS_SPLINE_KAKLIS_PANDELIS, new KaklisPandelisBasisSetParams (iKPTensionDegree), segParams, rssc);
	}

	/**
	 * Perform the following sequence of tests for a given segment control for a predictor/response range
	 * 	- Estimate
	 *  - Compute the segment-by-segment monotonicity
	 *  - Regime Jacobian
	 *  - Regime knot insertion
	 * 
	 * @param adblX The Predictor Array
	 * @param adblY The Response Array
	 * @param sbp The Segment Builder Parameters
	 * 
	 * 	WARNING: Insufficient Error Checking, so use caution
	 */

	public static final void BasisSplineRegimeTest (
		final double[] adblX,
		final double[] adblY,
		final PredictorResponseBuilderParams sbp)
		throws Exception
	{
		double dblX = 1.;
		double dblXMax = 10.;

		/*
		 * Array of Segment Builder Parameters - one per segment
		 */

		PredictorResponseBuilderParams[] aSBP = new PredictorResponseBuilderParams[adblX.length - 1]; 

		for (int i = 0; i < adblX.length - 1; ++i)
			aSBP[i] = sbp;

		/*
		 * Construct a Regime instance 
		 */

		MultiSegmentRegime regime = RegimeBuilder.CreateCalibratedRegimeEstimator (
			"SPLINE_REGIME",
			adblX, // predictors
			adblY, // responses
			aSBP, // Basis Segment Builder parameters
			MultiSegmentRegime.BOUNDARY_CONDITION_NATURAL, // Boundary Condition - Natural
			MultiSegmentRegime.CALIBRATE); // Calibrate the Regime predictors to the responses

		/*
		 * Estimate, compute the segment-by-segment monotonicity and the Regime Jacobian
		 */

		while (dblX <= dblXMax) {
			System.out.println ("Y[" + dblX + "] " + FormatUtil.FormatDouble (regime.responseValue (dblX), 1, 2, 1.) + " | " + regime.monotoneType (dblX));

			System.out.println ("Jacobian Y[" + dblX + "]=" + regime.jackDResponseDResponseInput (dblX).displayString());

			dblX += 1.;
		}

		/*
		 * Construct a new Regime instance by inserting a pair of of predictor/response knots
		 */

		MultiSegmentRegime regimeInsert = RegimeModifier.InsertKnot (regime,
			9.,
			10.,
			MultiSegmentRegime.BOUNDARY_CONDITION_NATURAL, // Boundary Condition - Natural
			MultiSegmentRegime.CALIBRATE); // Calibrate the Regime predictors to the responses

		dblX = 1.;

		/*
		 * Estimate, compute the sgement-by-segment monotonicty and the Regime Jacobian
		 */

		while (dblX <= dblXMax) {
			System.out.println ("Inserted Y[" + dblX + "] " + FormatUtil.FormatDouble (regimeInsert.responseValue (dblX), 1, 2, 1.)
				+ " | " + regimeInsert.monotoneType (dblX));

			dblX += 1.;
		}
	}

	/**
	 * This function demonstrates the construction, the calibration, and the usage of Local Control Segment Spline.
	 * 	It does the following:
	 * 	- Set up the predictor/variates, the shape controller, and the basis spline (in this case polynomial)
	 *  - Create the left and the right segment edge parameters for each segment
	 *  - Construct the Regime Estimator
	 *  - Verify the Estimated Value/Jacobian
	 *  - Insert a Hermite local knot, a Cardinal knot, and a Catmull-Rom knot and examine the Estimated output/Jacobian
	 * 
	 * @throws java.lang.Exception Thrown if the test does not succeed
	 */

	private static final void TestHermiteCatmullRomCardinal()
		throws java.lang.Exception
	{
		/*
		 * X predictors
		 */

		double[] adblX = new double[] {0.00, 1.00,  2.00,  3.00,  4.00};

		/*
		 * Y responses
		 */

		double[] adblY = new double[] {1.00, 4.00, 15.00, 40.00, 85.00};

		/*
		 * DY/DX explicit local shape control for the responses
		 */

		double[] adblDYDX = new double[] {1.00, 6.00, 17.00, 34.00, 57.00};

		/*
		 * Construct a rational shape controller with the shape controller tension of 1.
		 */

		double dblShapeControllerTension = 1.;

		ResponseScalingShapeController rssc = new ResponseScalingShapeController (true, new RationalShapeControl (dblShapeControllerTension));

		/*
		 * Construct the segment inelastic parameter that is C2 (iK = 2 sets it to C2), with 2nd order
		 * 	roughness penalty derivative, and without constraint
		 */

		int iK = 1;
		int iRoughnessPenaltyDerivativeOrder = 2;

		DesignInelasticParams segParams = DesignInelasticParams.Create (iK, iRoughnessPenaltyDerivativeOrder);

		/* 
		 * Construct the C1 Hermite Polynomial Spline based Regime Estimator by using the following steps:
		 * 
		 * - 1) Set up the Regime Builder Parameter
		 */

		int iNumBasis = 4;

		PredictorResponseBuilderParams sbp = new PredictorResponseBuilderParams (
			RegimeBuilder.BASIS_SPLINE_POLYNOMIAL,
			new PolynomialBasisSetParams (iNumBasis),
			segParams,
			rssc);

		/*
		 *	- 2a) Set the array of Segment Builder Parameters - one per segment
		 */

		PredictorResponseBuilderParams[] aSBP = new PredictorResponseBuilderParams[adblX.length - 1]; 

		for (int i = 0; i < adblX.length - 1; ++i)
			aSBP[i] = sbp;

		/* 
		 * - 2b) Construct the Regime
		 */

		MultiSegmentRegime regime = RegimeBuilder.CreateUncalibratedRegimeEstimator ("SPLINE_REGIME", adblX, aSBP);

		PredictorOrdinateResponseDerivative[] aSEPLeft = new PredictorOrdinateResponseDerivative[adblY.length - 1];
		PredictorOrdinateResponseDerivative[] aSEPRight = new PredictorOrdinateResponseDerivative[adblY.length - 1];

		 /* 
		  * - 3) Set up the left and the local control Parameters
		  */

		for (int i = 0; i < adblY.length - 1; ++i) {
			aSEPLeft[i] = new PredictorOrdinateResponseDerivative (adblY[i], new double[] {adblDYDX[i]});

			aSEPRight[i] = new PredictorOrdinateResponseDerivative (adblY[i + 1], new double[] {adblDYDX[i + 1]});
		}

		/* 
		 * - 4) Calibrate the Regime and compute the Jacobian
		 */

		System.out.println ("Regime Setup Succeeded: " + regime.setupHermite (aSEPLeft, aSEPRight, null, MultiSegmentRegime.CALIBRATE));

		double dblX = 0.;
		double dblXMax = 4.;

		/* 
		 * - 5) Display the Estimated Y and the Regime Jacobian across the variates
		 */

		while (dblX <= dblXMax) {
			System.out.println ("Y[" + dblX + "] " + FormatUtil.FormatDouble (regime.responseValue (dblX), 1, 2, 1.) + " | " +
				regime.monotoneType (dblX));

			System.out.println ("Jacobian Y[" + dblX + "]=" + regime.jackDResponseDResponseInput (dblX).displayString());

			dblX += 0.5;
		}

		/* 
		 * We now insert a Hermite local control knot. The following are the steps:
		 * 
		 * - 1) Set up the left and the right segment edge parameters
		 * - 2) Insert the pair of SEP's at the chosen variate node.
		 * - 3) Compute the Estimated segment value and the motonicity across a suitable variate range.
		 */

		PredictorOrdinateResponseDerivative sepLeftSegmentRightNode = new PredictorOrdinateResponseDerivative (27.5, new double[] {25.5});

		PredictorOrdinateResponseDerivative sepRightSegmentLeftNode = new PredictorOrdinateResponseDerivative (27.5, new double[] {25.5});

		MultiSegmentRegime regimeInsert = RegimeModifier.InsertKnot (regime, 2.5, sepLeftSegmentRightNode, sepRightSegmentLeftNode);

		dblX = 1.;

		while (dblX <= dblXMax) {
			System.out.println ("Inserted Y[" + dblX + "] " + FormatUtil.FormatDouble (regimeInsert.responseValue (dblX), 1, 2, 1.)
				+ " | " + regimeInsert.monotoneType (dblX));

			dblX += 0.5;
		}

		/* 
		 * We now insert a Cardinal local control knot. The following are the steps:
		 * 
		 * - 1) Set up the left and the right segment edge parameters
		 * - 2) Insert the pair of SEP's at the chosen variate node.
		 * - 3) Compute the Estimated segment value and the motonicity across a suitable variate range.
		 */

		MultiSegmentRegime regimeCardinalInsert = RegimeModifier.InsertCardinalKnot (regime, 2.5, 0.);

		dblX = 1.;

		while (dblX <= dblXMax) {
			System.out.println ("Cardinal Inserted Y[" + dblX + "] " + FormatUtil.FormatDouble
				(regimeCardinalInsert.responseValue (dblX), 1, 2, 1.) + " | " + regimeInsert.monotoneType (dblX));

			dblX += 0.5;
		}

		/* 
		 * We now insert a Catmull-Rom local control knot. The following are the steps:
		 * 
		 * - 1) Set up the left and the right segment edge parameters
		 * - 2) Insert the pair of SEP's at the chosen variate node.
		 * - 3) Compute the Estimated segment value and the motonicity across a suitable variate range.
		 */

		MultiSegmentRegime regimeCatmullRomInsert = RegimeModifier.InsertCatmullRomKnot (regime, 2.5);

		dblX = 1.;

		while (dblX <= dblXMax) {
			System.out.println ("Catmull-Rom Inserted Y[" + dblX + "] " + FormatUtil.FormatDouble
				(regimeCatmullRomInsert.responseValue (dblX), 1, 2, 1.) + " | " + regimeInsert.monotoneType (dblX));

			dblX += 0.5;
		}
	}

	/**
	 * This function demonstrates the construction, the calibration, and the usage of Lagrange Polynomial Regime.
	 * 	It does the following:
	 * 	- Set up the predictors and the Lagrange Polynomial Regime.
	 *  - Calibrate to a target Y array.
	 *  - Calibrate the value to a target X.
	 *  - Calibrate the value Jacobian to a target X.
	 *  - Verify the local monotonicity and convexity (both the co- and the local versions).
	 * 
	 * @throws java.lang.Exception Thrown if the test does not succeed
	 */

	private static final void TestLagrangePolynomialRegime()
		throws java.lang.Exception
	{
		SingleSegmentRegime lps = new LagrangePolynomialRegime (new double[] {-2., -1., 2., 5.});

		System.out.println ("Setup: " + lps.setup (0.25, new double[] {0.25, 0.25, 12.25, 42.25},
			MultiSegmentRegime.BOUNDARY_CONDITION_NATURAL, // Boundary Condition - Natural
			MultiSegmentRegime.CALIBRATE)); // Calibrate the Regime predictors to the responses

		System.out.println ("Value = " + lps.responseValue (2.16));

		System.out.println ("Value Jacobian = " + lps.jackDResponseDResponseInput (2.16).displayString());

		System.out.println ("Value Monotone Type: " + lps.monotoneType (2.16));

		System.out.println ("Is Locally Monotone: " + lps.isLocallyMonotone());
	}

	/**
	 * Perform the following sequence of tests for a given segment control for a predictor/response range
	 * 	- Estimate
	 *  - Compute the segment-by-segment monotonicity
	 *  - Regime Jacobian
	 *  - Regime knot insertion
	 * 
	 * @param adblX The Predictor Array
	 * @param adblY The Response Array
	 * @param sbp The Segment Builder Parameters
	 * 
	 * 	WARNING: Insufficient Error Checking, so use caution
	 */

	public static final void BesselHermiteSplineRegimeTest (
		final double[] adblX,
		final double[] adblY,
		final PredictorResponseBuilderParams sbp)
		throws Exception
	{
		double dblX = 1.;
		double dblXMax = 10.;

		/*
		 * Array of Segment Builder Parameters - one per segment
		 */

		PredictorResponseBuilderParams[] aSBP = new PredictorResponseBuilderParams[adblX.length - 1]; 

		for (int i = 0; i < adblX.length - 1; ++i)
			aSBP[i] = sbp;

		/*
		 * Construct a Regime instance 
		 */

		MultiSegmentRegime regime = LocalControlRegimeBuilder.CreateBesselCubicSplineRegime (
			"BESSEL_REGIME",
			adblX, // predictors
			adblY, // responses
			aSBP, // Basis Segment Builder parameters
			MultiSegmentRegime.CALIBRATE);

		/*
		 * Estimate, compute the segment-by-segment monotonicity and the Regime Jacobian
		 */

		while (dblX <= dblXMax) {
			System.out.println ("Y[" + dblX + "] " + FormatUtil.FormatDouble (regime.responseValue (dblX), 1, 2, 1.) + " | " + regime.monotoneType (dblX));

			System.out.println ("Jacobian Y[" + dblX + "]=" + regime.jackDResponseDResponseInput (dblX).displayString());

			dblX += 1.;
		}

		/*
		 * Construct a new Regime instance by inserting a pair of of predictor/response knots
		 */

		MultiSegmentRegime regimeInsert = RegimeModifier.InsertKnot (regime,
			9.,
			10.,
			MultiSegmentRegime.BOUNDARY_CONDITION_NATURAL, // Boundary Condition - Natural
			MultiSegmentRegime.CALIBRATE); // Calibrate the Regime predictors to the responses

		dblX = 1.;

		/*
		 * Estimate, compute the sgement-by-segment monotonicty and the Regime Jacobian
		 */

		while (dblX <= dblXMax) {
			System.out.println ("Inserted Y[" + dblX + "] " + FormatUtil.FormatDouble (regimeInsert.responseValue (dblX), 1, 2, 1.)
				+ " | " + regimeInsert.monotoneType (dblX));

			dblX += 1.;
		}
	}

	/**
	 * Perform the following sequence of tests for a given segment control for a predictor/response range
	 * 	- Estimate
	 *  - Compute the segment-by-segment monotonicity
	 *  - Regime Jacobian
	 *  - Regime knot insertion
	 * 
	 * @param adblX The Predictor Array
	 * @param adblY The Response Array
	 * @param sbp The Segment Builder Parameters
	 * 
	 * 	WARNING: Insufficient Error Checking, so use caution
	 */

	public static final void Hyman83HermiteMonotoneRegimeTest (
		final double[] adblX,
		final double[] adblY,
		final PredictorResponseBuilderParams sbp)
		throws Exception
	{
		double dblX = 1.;
		double dblXMax = 10.;

		/*
		 * Array of Segment Builder Parameters - one per segment
		 */

		PredictorResponseBuilderParams[] aSBP = new PredictorResponseBuilderParams[adblX.length - 1]; 

		for (int i = 0; i < adblX.length - 1; ++i)
			aSBP[i] = sbp;

		/*
		 * Construct a Regime instance 
		 */

		MultiSegmentRegime regime = LocalControlRegimeBuilder.CreateHyman83MonotoneRegime (
			"HYMAN83_MONOTONE_REGIME",
			adblX, // predictors
			adblY, // responses
			aSBP, // Basis Segment Builder parameters
			MultiSegmentRegime.CALIBRATE,
			true); // TRUE => Eliminate Spurious Segment Extrema

		/*
		 * Estimate, compute the segment-by-segment monotonicity and the Regime Jacobian
		 */

		while (dblX <= dblXMax) {
			System.out.println ("Y[" + dblX + "] " + FormatUtil.FormatDouble (regime.responseValue (dblX), 1, 2, 1.) + " | " + regime.monotoneType (dblX));

			System.out.println ("Jacobian Y[" + dblX + "]=" + regime.jackDResponseDResponseInput (dblX).displayString());

			dblX += 1.;
		}

		/*
		 * Construct a new Regime instance by inserting a pair of of predictor/response knots
		 */

		MultiSegmentRegime regimeInsert = RegimeModifier.InsertKnot (regime,
			9.,
			10.,
			MultiSegmentRegime.BOUNDARY_CONDITION_NATURAL, // Boundary Condition - Natural
			MultiSegmentRegime.CALIBRATE); // Calibrate the Regime predictors to the responses

		dblX = 1.;

		/*
		 * Estimate, compute the sgement-by-segment monotonicty and the Regime Jacobian
		 */

		while (dblX <= dblXMax) {
			System.out.println ("Inserted Y[" + dblX + "] " + FormatUtil.FormatDouble (regimeInsert.responseValue (dblX), 1, 2, 1.)
				+ " | " + regimeInsert.monotoneType (dblX));

			dblX += 1.;
		}
	}

	/**
	 * Perform the following sequence of tests for a given segment control for a predictor/response range
	 * 	- Estimate
	 *  - Compute the segment-by-segment monotonicity
	 *  - Regime Jacobian
	 *  - Regime knot insertion
	 * 
	 * @param adblX The Predictor Array
	 * @param adblY The Response Array
	 * @param sbp The Segment Builder Parameters
	 * 
	 * 	WARNING: Insufficient Error Checking, so use caution
	 */

	public static final void Hyman89HermiteMonotoneRegimeTest (
		final double[] adblX,
		final double[] adblY,
		final PredictorResponseBuilderParams sbp)
		throws Exception
	{
		double dblX = 1.;
		double dblXMax = 10.;

		/*
		 * Array of Segment Builder Parameters - one per segment
		 */

		PredictorResponseBuilderParams[] aSBP = new PredictorResponseBuilderParams[adblX.length - 1]; 

		for (int i = 0; i < adblX.length - 1; ++i)
			aSBP[i] = sbp;

		/*
		 * Construct a Regime instance 
		 */

		MultiSegmentRegime regime = LocalControlRegimeBuilder.CreateHyman89MonotoneRegime (
			"HYMAN89_MONOTONE_REGIME",
			adblX, // predictors
			adblY, // responses
			aSBP, // Basis Segment Builder parameters
			MultiSegmentRegime.CALIBRATE);

		/*
		 * Estimate, compute the segment-by-segment monotonicity and the Regime Jacobian
		 */

		while (dblX <= dblXMax) {
			System.out.println ("Y[" + dblX + "] " + FormatUtil.FormatDouble (regime.responseValue (dblX), 1, 2, 1.) + " | " + regime.monotoneType (dblX));

			System.out.println ("Jacobian Y[" + dblX + "]=" + regime.jackDResponseDResponseInput (dblX).displayString());

			dblX += 1.;
		}

		/*
		 * Construct a new Regime instance by inserting a pair of of predictor/response knots
		 */

		MultiSegmentRegime regimeInsert = RegimeModifier.InsertKnot (regime,
			9.,
			10.,
			MultiSegmentRegime.BOUNDARY_CONDITION_NATURAL, // Boundary Condition - Natural
			MultiSegmentRegime.CALIBRATE); // Calibrate the Regime predictors to the responses

		dblX = 1.;

		/*
		 * Estimate, compute the sgement-by-segment monotonicty and the Regime Jacobian
		 */

		while (dblX <= dblXMax) {
			System.out.println ("Inserted Y[" + dblX + "] " + FormatUtil.FormatDouble (regimeInsert.responseValue (dblX), 1, 2, 1.)
				+ " | " + regimeInsert.monotoneType (dblX));

			dblX += 1.;
		}
	}

	/**
	 * Perform the following sequence of tests for a given segment control for a predictor/response range
	 * 	- Estimate
	 *  - Compute the segment-by-segment monotonicity
	 *  - Regime Jacobian
	 *  - Regime knot insertion
	 * 
	 * @param adblX The Predictor Array
	 * @param adblY The Response Array
	 * @param sbp The Segment Builder Parameters
	 * @param bApplyMonotoneFilter TRUE => Apply the Monotone Filter
	 * 
	 * 	WARNING: Insufficient Error Checking, so use caution
	 */

	public static final void HarmonicHermiteMonotoneRegimeTest (
		final double[] adblX,
		final double[] adblY,
		final PredictorResponseBuilderParams sbp,
		final boolean bApplyMonotoneFilter)
		throws Exception
	{
		double dblX = 1.;
		double dblXMax = 10.;

		/*
		 * Array of Segment Builder Parameters - one per segment
		 */

		PredictorResponseBuilderParams[] aSBP = new PredictorResponseBuilderParams[adblX.length - 1]; 

		for (int i = 0; i < adblX.length - 1; ++i)
			aSBP[i] = sbp;

		/*
		 * Construct a Regime instance 
		 */

		MultiSegmentRegime regime = LocalControlRegimeBuilder.CreateHarmonicMonotoneRegime (
			"HARMONIC_MONOTONE_REGIME",
			adblX, // predictors
			adblY, // responses
			aSBP, // Basis Segment Builder parameters
			MultiSegmentRegime.CALIBRATE,
			bApplyMonotoneFilter);

		/*
		 * Estimate, compute the segment-by-segment monotonicity and the Regime Jacobian
		 */

		while (dblX <= dblXMax) {
			System.out.println ("Y[" + dblX + "] " + FormatUtil.FormatDouble (regime.responseValue (dblX), 1, 2, 1.) + " | " + regime.monotoneType (dblX));

			System.out.println ("Jacobian Y[" + dblX + "]=" + regime.jackDResponseDResponseInput (dblX).displayString());

			dblX += 1.;
		}

		/*
		 * Construct a new Regime instance by inserting a pair of of predictor/response knots
		 */

		MultiSegmentRegime regimeInsert = RegimeModifier.InsertKnot (regime,
			9.,
			10.,
			MultiSegmentRegime.BOUNDARY_CONDITION_NATURAL, // Boundary Condition - Natural
			MultiSegmentRegime.CALIBRATE); // Calibrate the Regime predictors to the responses

		dblX = 1.;

		/*
		 * Estimate, compute the sgement-by-segment monotonicty and the Regime Jacobian
		 */

		while (dblX <= dblXMax) {
			System.out.println ("Inserted Y[" + dblX + "] " + FormatUtil.FormatDouble (regimeInsert.responseValue (dblX), 1, 2, 1.)
				+ " | " + regimeInsert.monotoneType (dblX));

			dblX += 1.;
		}
	}

	/**
	 * Perform the following sequence of tests for a given segment control for a predictor/response range
	 * 	- Estimate
	 *  - Compute the segment-by-segment monotonicity
	 *  - Regime Jacobian
	 *  - Regime knot insertion
	 * 
	 * @param adblX The Predictor Array
	 * @param adblY The Response Array
	 * @param sbp The Segment Builder Parameters
	 * @param bApplyMonotoneFilter TRUE => Apply the Monotone Filter
	 * 
	 * 	WARNING: Insufficient Error Checking, so use caution
	 */

	public static final void VanLeerLimiterRegimeTest (
		final double[] adblX,
		final double[] adblY,
		final PredictorResponseBuilderParams sbp,
		final boolean bApplyMonotoneFilter)
		throws Exception
	{
		double dblX = 1.;
		double dblXMax = 10.;

		/*
		 * Array of Segment Builder Parameters - one per segment
		 */

		PredictorResponseBuilderParams[] aSBP = new PredictorResponseBuilderParams[adblX.length - 1]; 

		for (int i = 0; i < adblX.length - 1; ++i)
			aSBP[i] = sbp;

		/*
		 * Construct a Regime instance 
		 */

		MultiSegmentRegime regime = LocalControlRegimeBuilder.CreateVanLeerLimiterRegime (
			"VAN_LEER_MONOTONE_REGIME",
			adblX, // predictors
			adblY, // responses
			aSBP, // Basis Segment Builder parameters
			MultiSegmentRegime.CALIBRATE,
			bApplyMonotoneFilter);

		/*
		 * Estimate, compute the segment-by-segment monotonicity and the Regime Jacobian
		 */

		while (dblX <= dblXMax) {
			System.out.println ("Y[" + dblX + "] " + FormatUtil.FormatDouble (regime.responseValue (dblX), 1, 2, 1.) + " | " + regime.monotoneType (dblX));

			System.out.println ("Jacobian Y[" + dblX + "]=" + regime.jackDResponseDResponseInput (dblX).displayString());

			dblX += 1.;
		}

		/*
		 * Construct a new Regime instance by inserting a pair of of predictor/response knots
		 */

		MultiSegmentRegime regimeInsert = RegimeModifier.InsertKnot (regime,
			9.,
			10.,
			MultiSegmentRegime.BOUNDARY_CONDITION_NATURAL, // Boundary Condition - Natural
			MultiSegmentRegime.CALIBRATE); // Calibrate the Regime predictors to the responses

		dblX = 1.;

		/*
		 * Estimate, compute the sgement-by-segment monotonicty and the Regime Jacobian
		 */

		while (dblX <= dblXMax) {
			System.out.println ("Inserted Y[" + dblX + "] " + FormatUtil.FormatDouble (regimeInsert.responseValue (dblX), 1, 2, 1.)
				+ " | " + regimeInsert.monotoneType (dblX));

			dblX += 1.;
		}
	}

	/**
	 * Perform the following sequence of tests for a given segment control for a predictor/response range
	 * 	- Estimate
	 *  - Compute the segment-by-segment monotonicity
	 *  - Regime Jacobian
	 *  - Regime knot insertion
	 * 
	 * @param adblX The Predictor Array
	 * @param adblY The Response Array
	 * @param sbp The Segment Builder Parameters
	 * @param bApplyMonotoneFilter TRUE => Apply the Monotone Filter
	 * 
	 * 	WARNING: Insufficient Error Checking, so use caution
	 */

	public static final void HuynhLeFlochLimiterRegimeTest (
		final double[] adblX,
		final double[] adblY,
		final PredictorResponseBuilderParams sbp,
		final boolean bApplyMonotoneFilter)
		throws Exception
	{
		double dblX = 1.;
		double dblXMax = 10.;

		/*
		 * Array of Segment Builder Parameters - one per segment
		 */

		PredictorResponseBuilderParams[] aSBP = new PredictorResponseBuilderParams[adblX.length - 1]; 

		for (int i = 0; i < adblX.length - 1; ++i)
			aSBP[i] = sbp;

		/*
		 * Construct a Regime instance 
		 */

		MultiSegmentRegime regime = LocalControlRegimeBuilder.CreateHuynhLeFlochLimiterRegime (
			"VAN_LEER_MONOTONE_REGIME",
			adblX, // predictors
			adblY, // responses
			aSBP, // Basis Segment Builder parameters
			MultiSegmentRegime.CALIBRATE,
			bApplyMonotoneFilter);

		/*
		 * Estimate, compute the segment-by-segment monotonicity and the Regime Jacobian
		 */

		while (dblX <= dblXMax) {
			System.out.println ("Y[" + dblX + "] " + FormatUtil.FormatDouble (regime.responseValue (dblX), 1, 2, 1.) + " | " + regime.monotoneType (dblX));

			System.out.println ("Jacobian Y[" + dblX + "]=" + regime.jackDResponseDResponseInput (dblX).displayString());

			dblX += 1.;
		}

		/*
		 * Construct a new Regime instance by inserting a pair of of predictor/response knots
		 */

		MultiSegmentRegime regimeInsert = RegimeModifier.InsertKnot (regime,
			9.,
			10.,
			MultiSegmentRegime.BOUNDARY_CONDITION_NATURAL, // Boundary Condition - Natural
			MultiSegmentRegime.CALIBRATE); // Calibrate the Regime predictors to the responses

		dblX = 1.;

		/*
		 * Estimate, compute the sgement-by-segment monotonicty and the Regime Jacobian
		 */

		while (dblX <= dblXMax) {
			System.out.println ("Inserted Y[" + dblX + "] " + FormatUtil.FormatDouble (regimeInsert.responseValue (dblX), 1, 2, 1.)
				+ " | " + regimeInsert.monotoneType (dblX));

			dblX += 1.;
		}
	}

	/**
	 * Perform the following sequence of tests for a given segment control for a predictor/response range
	 * 	- Estimate
	 *  - Compute the segment-by-segment monotonicity
	 *  - Regime Jacobian
	 *  - Regime knot insertion
	 * 
	 * @param adblX The Predictor Array
	 * @param adblY The Response Array
	 * @param sbp The Segment Builder Parameters
	 * 
	 * 	WARNING: Insufficient Error Checking, so use caution
	 */

	public static final void AkimaRegimeTest (
		final double[] adblX,
		final double[] adblY,
		final PredictorResponseBuilderParams sbp)
		throws Exception
	{
		double dblX = 1.;
		double dblXMax = 10.;

		/*
		 * Array of Segment Builder Parameters - one per segment
		 */

		PredictorResponseBuilderParams[] aSBP = new PredictorResponseBuilderParams[adblX.length - 1]; 

		for (int i = 0; i < adblX.length - 1; ++i)
			aSBP[i] = sbp;

		/*
		 * Construct a Regime instance 
		 */

		MultiSegmentRegime regime = LocalControlRegimeBuilder.CreateAkimaRegime(
			"AKIMA_LOCAL_REGIME",
			adblX, // predictors
			adblY, // responses
			aSBP, // Basis Segment Builder parameters
			MultiSegmentRegime.CALIBRATE);

		/*
		 * Estimate, compute the segment-by-segment monotonicity and the Regime Jacobian
		 */

		while (dblX <= dblXMax) {
			System.out.println ("Y[" + dblX + "] " + FormatUtil.FormatDouble (regime.responseValue (dblX), 1, 2, 1.) + " | " + regime.monotoneType (dblX));

			System.out.println ("Jacobian Y[" + dblX + "]=" + regime.jackDResponseDResponseInput (dblX).displayString());

			dblX += 1.;
		}

		/*
		 * Construct a new Regime instance by inserting a pair of of predictor/response knots
		 */

		MultiSegmentRegime regimeInsert = RegimeModifier.InsertKnot (regime,
			9.,
			10.,
			MultiSegmentRegime.BOUNDARY_CONDITION_NATURAL, // Boundary Condition - Natural
			MultiSegmentRegime.CALIBRATE); // Calibrate the Regime predictors to the responses

		dblX = 1.;

		/*
		 * Estimate, compute the sgement-by-segment monotonicty and the Regime Jacobian
		 */

		while (dblX <= dblXMax) {
			System.out.println ("Inserted Y[" + dblX + "] " + FormatUtil.FormatDouble (regimeInsert.responseValue (dblX), 1, 2, 1.)
				+ " | " + regimeInsert.monotoneType (dblX));

			dblX += 1.;
		}
	}

	/**
	 * Perform the following sequence of tests for a given segment control for a predictor/response range
	 * 	- Estimate
	 *  - Compute the segment-by-segment monotonicity
	 *  - Regime Jacobian
	 *  - Regime knot insertion
	 * 
	 * @param adblX The Predictor Array
	 * @param adblY The Response Array
	 * @param sbp The Segment Builder Parameters
	 * 
	 * 	WARNING: Insufficient Error Checking, so use caution
	 */

	public static final void KrugerRegimeTest (
		final double[] adblX,
		final double[] adblY,
		final PredictorResponseBuilderParams sbp)
		throws Exception
	{
		double dblX = 1.;
		double dblXMax = 10.;

		/*
		 * Array of Segment Builder Parameters - one per segment
		 */

		PredictorResponseBuilderParams[] aSBP = new PredictorResponseBuilderParams[adblX.length - 1]; 

		for (int i = 0; i < adblX.length - 1; ++i)
			aSBP[i] = sbp;

		/*
		 * Construct a Regime instance 
		 */

		MultiSegmentRegime regime = LocalControlRegimeBuilder.CreateKrugerRegime(
			"KRUGER_LOCAL_REGIME",
			adblX, // predictors
			adblY, // responses
			aSBP, // Basis Segment Builder parameters
			MultiSegmentRegime.CALIBRATE);

		/*
		 * Estimate, compute the segment-by-segment monotonicity and the Regime Jacobian
		 */

		while (dblX <= dblXMax) {
			System.out.println ("Y[" + dblX + "] " + FormatUtil.FormatDouble (regime.responseValue (dblX), 1, 2, 1.) + " | " + regime.monotoneType (dblX));

			System.out.println ("Jacobian Y[" + dblX + "]=" + regime.jackDResponseDResponseInput (dblX).displayString());

			dblX += 1.;
		}

		/*
		 * Construct a new Regime instance by inserting a pair of of predictor/response knots
		 */

		MultiSegmentRegime regimeInsert = RegimeModifier.InsertKnot (regime,
			9.,
			10.,
			MultiSegmentRegime.BOUNDARY_CONDITION_NATURAL, // Boundary Condition - Natural
			MultiSegmentRegime.CALIBRATE); // Calibrate the Regime predictors to the responses

		dblX = 1.;

		/*
		 * Estimate, compute the sgement-by-segment monotonicty and the Regime Jacobian
		 */

		while (dblX <= dblXMax) {
			System.out.println ("Inserted Y[" + dblX + "] " + FormatUtil.FormatDouble (regimeInsert.responseValue (dblX), 1, 2, 1.)
				+ " | " + regimeInsert.monotoneType (dblX));

			dblX += 1.;
		}
	}

	public static final void main (
		final String[] astrArgs)
		throws Exception
	{
		/*
		 * X predictors
		 */

		double[] adblX = new double[] { 1.00,  1.50,  2.00, 3.00, 4.00, 5.00, 6.50, 8.00, 10.00};

		/*
		 * Y responses
		 */

		double[] adblY = new double[] {25.00, 20.25, 16.00, 9.00, 4.00, 1.00, 0.25, 4.00, 16.00};

		/*
		 * Construct a rational shape controller with the shape controller tension of 1.
		 */

		double dblShapeControllerTension = 1.;

		ResponseScalingShapeController rssc = new ResponseScalingShapeController (true, new RationalShapeControl (dblShapeControllerTension));

		/*
		 * Construct the segment inelastic parameter that is C2 (iK = 2 sets it to C2), with 2nd order
		 * 	roughness penalty derivative, and without constraint
		 */

		int iK = 2;
		int iRoughnessPenaltyDerivativeOrder= 2;

		DesignInelasticParams segParams = DesignInelasticParams.Create (iK, iRoughnessPenaltyDerivativeOrder);

		System.out.println (" \n---------- \n BERNSTEIN POLYNOMIAL \n ---------- \n");

		int iBernPolyNumBasis = 4;

		BasisSplineRegimeTest (adblX, adblY, BernsteinPolynomialSegmentControlParams (iBernPolyNumBasis, segParams, rssc));

		System.out.println (" \n---------- \n POLYNOMIAL \n ---------- \n");

		int iPolyNumBasis = 4;

		BasisSplineRegimeTest (adblX, adblY, PolynomialSegmentControlParams (iPolyNumBasis, segParams, rssc));

		System.out.println (" \n---------- \n EXPONENTIAL TENSION \n ---------- \n");

		double dblTension = 1.;

		BasisSplineRegimeTest (adblX, adblY, ExponentialTensionSegmentControlParams (dblTension, segParams, rssc));

		System.out.println (" \n---------- \n HYPERBOLIC TENSION \n ---------- \n");

		BasisSplineRegimeTest (adblX, adblY, HyperbolicTensionSegmentControlParams (dblTension, segParams, rssc));

		System.out.println (" \n---------- \n KAKLIS PANDELIS \n ---------- \n");

		int iKPTensionDegree = 2;

		BasisSplineRegimeTest (adblX, adblY, KaklisPandelisSegmentControlParams (iKPTensionDegree, segParams, rssc));

		System.out.println (" \n---------- \n HERMITE - CATMULL ROM - CARDINAL \n ---------- \n");

		TestHermiteCatmullRomCardinal();

		System.out.println (" \n---------- \n LAGRANGE POLYNOMIAL REGIME \n ---------- \n");

		TestLagrangePolynomialRegime();

		System.out.println (" \n---------- \n C1 BESSEL/HERMITE \n ---------- \n");

		BesselHermiteSplineRegimeTest (adblX, adblY, PolynomialSegmentControlParams (iPolyNumBasis, segParams, rssc));

		System.out.println (" \n---------- \n C1 HYMAN 1983 MONOTONE \n ---------- \n");

		Hyman83HermiteMonotoneRegimeTest (adblX, adblY, PolynomialSegmentControlParams (iPolyNumBasis, segParams, rssc));

		System.out.println (" \n---------- \n C1 HYMAN 1989 MONOTONE \n ---------- \n");

		Hyman89HermiteMonotoneRegimeTest (adblX, adblY, PolynomialSegmentControlParams (iPolyNumBasis, segParams, rssc));

		System.out.println (" \n---------- \n C1 HARMONIC MONOTONE WITH FILTER \n ---------- \n");

		HarmonicHermiteMonotoneRegimeTest (adblX, adblY, PolynomialSegmentControlParams (iPolyNumBasis, segParams, rssc), true);

		System.out.println (" \n---------- \n C1 HARMONIC MONOTONE WITHOUT FILTER \n ---------- \n");

		HarmonicHermiteMonotoneRegimeTest (adblX, adblY, PolynomialSegmentControlParams (iPolyNumBasis, segParams, rssc), false);

		System.out.println (" \n---------- \n C1 VAN LEER LIMITER REGIME WITHOUT FILTER \n ---------- \n");

		VanLeerLimiterRegimeTest (adblX, adblY, PolynomialSegmentControlParams (iPolyNumBasis, segParams, rssc), false);

		System.out.println (" \n---------- \n C1 HUYNH LE-FLOCH LIMITER REGIME WITHOUT FILTER \n ---------- \n");

		HuynhLeFlochLimiterRegimeTest (adblX, adblY, PolynomialSegmentControlParams (iPolyNumBasis, segParams, rssc), false);

		System.out.println (" \n---------- \n C1 AKIMA REGIME \n ---------- \n");

		AkimaRegimeTest (adblX, adblY, PolynomialSegmentControlParams (iPolyNumBasis, segParams, rssc));

		System.out.println (" \n---------- \n C1 KRUGER REGIME \n ---------- \n");

		KrugerRegimeTest (adblX, adblY, PolynomialSegmentControlParams (iPolyNumBasis, segParams, rssc));
	}
}
