
package org.drip.math.segment;

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
 * This abstract class contains the basis spline segment in-elastic ordinates. Interpolating segment spline
 *  functions and their coefficients are implemented/calibrated in the overriding spline classes. It provides
 *  functionality for assessing the various segment attributes:
 *  - Segment Monotonicity
 *  - Interpolated Function Value, the ordered derivative, and the corresponding Jacobian
 *  - Segment Local/Global Derivative
 *  - Evaluation of the Segment Micro-Jack
 *  - Head / Regular Segment calibration - both of the basis function coefficients and the Jacobian
 *
 * @author Lakshmi Krishnamurthy
 */

public abstract class PredictorResponse extends org.drip.math.segment.Inelastics {

	/**
	 * LEFT NODE VALUE PARAMETER INDEX
	 */

	public static final int LEFT_NODE_VALUE_PARAMETER_INDEX = 0;

	/**
	 * RIGHT NODE VALUE PARAMETER INDEX
	 */

	public static final int RIGHT_NODE_VALUE_PARAMETER_INDEX = 1;

	protected PredictorResponse (
		final double dblPredictorOrdinateLeft,
		final double dblPredictorOrdinateRight)
		throws java.lang.Exception
	{
		super (dblPredictorOrdinateLeft, dblPredictorOrdinateRight);
	}

	protected abstract boolean isMonotone();

	/**
	 * Response given the Predictor Ordinate
	 * 
	 * @param dblPredictorOrdinate Predictor Ordinate
	 * 
	 * @return Response
	 * 
	 * @throws java.lang.Exception Thrown if Response Cannot be computed.
	 */

	public abstract double response (
		final double dblPredictorOrdinate)
		throws java.lang.Exception;

	/**
	 * nth order Response Derivative at the Predictor Ordinate
	 * 
	 * @param dblPredictorOrdinate Predictor Ordinate
	 * @param iOrder Order of the Derivative
	 * 
	 * @return nth order Response Derivative at the Predictor Ordinate
	 * 
	 * @throws java.lang.Exception Thrown if the nth order Response Derivative at the Predictor Ordinate
	 *  cannot be computed.
	 */

	public abstract double responseDerivative (
		final double dblPredictorOrdinate,
		final int iOrder)
		throws java.lang.Exception;

	/**
	 * Retrieve the number of Basis Functions
	 * 
	 * @return The Number of Basis Functions
	 */

	public abstract int numBasis();

	/**
	 * Retrieve the Number of Parameters
	 * 
	 * @return The Number of Parameters
	 */

	public abstract int numParameters();

	/**
	 * Calibrate the segment from the Calibration Parameters
	 * 
	 * @param scp Segment Calibration Parameters
	 * 
	 * @return TRUE => Segment Successfully Calibrated
	 */

	public abstract boolean calibrate (
		final org.drip.math.segment.CalibrationParams scp);

	/**
	 * Calibrate the coefficients from the prior Predictor/Response Segment and the Constraint
	 * 
	 * @param prPrev Prior Predictor/Response Segment
	 * @param rvc The Segment Response Value Constraint
	 * 
	 * @return TRUE => If the calibration succeeds
	 */

	public abstract boolean calibrate (
		final PredictorResponse prPrev,
		final org.drip.math.segment.ResponseValueConstraint rvc);

	/**
	 * Calibrate the coefficients from the prior Segment and the Response Vakue at the Right Predictor
	 *  Ordinate
	 * 
	 * @param prPrev Prior Predictor/Response Segment
	 * @param dblResponseAtRightOrdinate Response Value at the Right Predictor Ordinate
	 * 
	 * @return TRUE => If the calibration succeeds
	 */

	public boolean calibrate (
		final PredictorResponse segPrev,
		final double dblResponseAtRightOrdinate)
	{
		try {
			return calibrate (segPrev, org.drip.math.segment.ResponseValueConstraint.FromPredictorResponse
				(delocalize (1.), dblResponseAtRightOrdinate));
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	/**
	 * Calculate the Ordered Response Derivative at the Predictor Ordinate
	 * 
	 * @param dblPredictorOrdinate Predictor Ordinate at which the ordered Response Derivative is to be
	 * 	calculated
	 * @param iOrder Derivative Order
	 * @param bLocal TRUE => Get the localized transform of the Derivative; FALSE => Get the untransformed
	 * 
	 * @throws Thrown if the Ordered Response Derivative cannot be calculated
	 * 
	 * @return Retrieve the Ordered Response Derivative
	 */

	public abstract double calcOrderedResponseDerivative (
		final double dblPredictorOrdinate,
		final int iOrder,
		final boolean bLocal)
		throws java.lang.Exception;

	/**
	 * Calculate the Response Value at the given Predictor Ordinate
	 * 
	 * @param dblPredictorOrdinate Predictor Ordinate
	 * 
	 * @return The Response Value
	 * 
	 * @throws java.lang.Exception Thrown if the calculation did not succeed
	 */

	public double calcValue (
		final double dblPredictorOrdinate)
		throws java.lang.Exception
	{
		return response (localize (dblPredictorOrdinate));
	}

	/**
	 * Calculate the Jacobian of the Segment's Response Basis Function Coefficients to the Edge Parameters
	 * 
	 * @return The Jacobian of the Segment's Response Basis Function Coefficients to the Edge Parameters
	 */

	public abstract org.drip.math.calculus.WengertJacobian jackDCoeffDEdgeParams();

	/**
	 * Calculate the Jacobian of the Response to the Edge Parameters at the given Predictor Ordinate
	 * 
	 * @param dblPredictorOrdinate The Predictor Ordinate
	 * 
	 * @return The Jacobian of the Response to the Edge Parameters at the given Predictor Ordinate
	 */

	public abstract org.drip.math.calculus.WengertJacobian jackDResponseDEdgeParams (
		final double dblPredictorOrdinate);

	/**
	 * Calculate the Jacobian of the Response to the Basis Coefficients at the given Predictor Ordinate
	 * 
	 * @param dblPredictorOrdinate The Predictor Ordinate
	 * 
	 * @return The Jacobian of the Response to the Basis Coefficients at the given Predictor Ordinate
	 */

	public abstract org.drip.math.calculus.WengertJacobian jackDResponseDBasisCoeff (
		final double dblPredictorOrdinate);

	/**
	 * Indicate whether the given segment is monotone. If monotone, may optionally indicate the nature of
	 * 	the extrema contained inside (maxima/minima/infection).
	 *  
	 * @return The monotone Type
	 */

	public org.drip.math.segment.Monotonocity monotoneType()
	{
		if (isMonotone()) {
			try {
				return new org.drip.math.segment.Monotonocity
					(org.drip.math.segment.Monotonocity.MONOTONIC);
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}
		}

		org.drip.math.function.AbstractUnivariate ofDeriv = new org.drip.math.function.AbstractUnivariate
			(null) {
			@Override public double evaluate (
				final double dblX)
				throws java.lang.Exception
			{
				return responseDerivative (dblX, 1);
			}

			@Override public org.drip.math.calculus.Differential calcDifferential (
				final double dblX,
				final double dblOFBase,
				final int iOrder)
			{
				try {
					double dblVariateInfinitesimal = _dc.getVariateInfinitesimal (dblX);

					return new org.drip.math.calculus.Differential (dblVariateInfinitesimal, responseDerivative
						(dblX, iOrder) * dblVariateInfinitesimal);
				} catch (java.lang.Exception e) {
					e.printStackTrace();
				}

				return null;
			}
		};

		try {
			org.drip.math.solver1D.FixedPointFinderOutput fpop = new
				org.drip.math.solver1D.FixedPointFinderBrent (0., ofDeriv).findRoot
					(org.drip.math.solver1D.InitializationHeuristics.FromHardSearchEdges (0., 1.));

			if (null == fpop || !fpop.containsRoot())
				return new org.drip.math.segment.Monotonocity
					(org.drip.math.segment.Monotonocity.MONOTONIC);

			double dblExtremum = fpop.getRoot();

			if (!org.drip.math.common.NumberUtil.IsValid (dblExtremum) || dblExtremum <= 0. || dblExtremum >=
				1.)
				return new org.drip.math.segment.Monotonocity
					(org.drip.math.segment.Monotonocity.MONOTONIC);

			double dbl2ndDeriv = responseDerivative (dblExtremum, 2);

			if (0. > dbl2ndDeriv)
				return new org.drip.math.segment.Monotonocity
					(org.drip.math.segment.Monotonocity.MAXIMA);

			if (0. < dbl2ndDeriv)
				return new org.drip.math.segment.Monotonocity
					(org.drip.math.segment.Monotonocity.MINIMA);

			if (0. == dbl2ndDeriv)
				return new org.drip.math.segment.Monotonocity
					(org.drip.math.segment.Monotonocity.INFLECTION);

			return new org.drip.math.segment.Monotonocity
				(org.drip.math.segment.Monotonocity.NON_MONOTONIC);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		try {
			return new org.drip.math.segment.Monotonocity
				(org.drip.math.segment.Monotonocity.MONOTONIC);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Calibrate the segment and calculate the Jacobian of the Segment's Response Basis Function Coefficients
	 *  to the Edge Parameters
	 * 
	 * @param cp Segment Calibration Parameters
	 * 
	 * @return The Jacobian of the Segment's Response Basis Function Coefficients to the Edge Parameters
	 */

	public org.drip.math.calculus.WengertJacobian jackDCoeffDEdgeParams (
		final org.drip.math.segment.CalibrationParams cp)
	{
		return calibrate (cp) ? jackDCoeffDEdgeParams() : null;
	}

	/**
	 * Calibrate the Coefficients from the Edge Response Values and the Left Edge Response Slope
	 * 
	 * @param dblLeftEdgeResponseValue Left Edge Response Value
	 * @param dblLeftEdgeResponseSlope Left Edge Response Slope
	 * @param dblRightEdgeResponseValue Right Edge Response Value
	 * 
	 * @return TRUE => If the calibration succeeds
	 */

	public boolean calibrate (
		final double dblLeftEdgeResponseValue,
		final double dblLeftEdgeResponseSlope,
		final double dblRightEdgeResponseValue)
	{
		if (!org.drip.math.common.NumberUtil.IsValid (dblLeftEdgeResponseValue) ||
			!org.drip.math.common.NumberUtil.IsValid (dblLeftEdgeResponseSlope) ||
				!org.drip.math.common.NumberUtil.IsValid (dblRightEdgeResponseValue))
			return false;

		try {
			return calibrate (new org.drip.math.segment.CalibrationParams (new double[] {0., 1.}, new
				double[] {dblLeftEdgeResponseValue, dblRightEdgeResponseValue},
					org.drip.math.common.CollectionUtil.DerivArrayFromSlope (numParameters() - 2,
						dblLeftEdgeResponseSlope), null, null));
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	/**
	 * Calibrate the coefficients from the Left Edge Response Value, the Left Edge Response Slope, and the
	 * 	Right Edge Response Value Constraint
	 * 
	 * @param dblLeftEdgeResponseValue Left Edge Response Value
	 * @param dblLeftEdgeResponseSlope Left Edge Response Slope
	 * @param rvcRight Right Edge Response Value Constraint
	 * 
	 * @return TRUE => If the calibration succeeds
	 */

	public boolean calibrate (
		final double dblLeftEdgeResponseValue,
		final double dblLeftEdgeResponseSlope,
		final org.drip.math.segment.ResponseValueConstraint rvcRight)
	{
		if (!org.drip.math.common.NumberUtil.IsValid (dblLeftEdgeResponseValue) ||
			!org.drip.math.common.NumberUtil.IsValid (dblLeftEdgeResponseSlope) || null == rvcRight)
			return false;

		try {
			return calibrate (new org.drip.math.segment.CalibrationParams (null, null,
				org.drip.math.common.CollectionUtil.DerivArrayFromSlope (numParameters() - 2,
					dblLeftEdgeResponseSlope), null, new org.drip.math.segment.ResponseValueConstraint[]
						{org.drip.math.segment.ResponseValueConstraint.FromPredictorResponse (delocalize
							(0.), dblLeftEdgeResponseValue), rvcRight}));
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	/**
	 * Calibrate the coefficients from the Left Edge Response Value Constraint, the Left Edge Response Slope,
	 *  and the Right Edge Response Value Constraint
	 * 
	 * @param rvcLeft Left Edge Response Value Constraint
	 * @param dblLeftEdgeResponseSlope Left Edge Response Slope
	 * @param rvcRight Right Edge Response Value Constraint
	 * 
	 * @return TRUE => If the calibration succeeds
	 */

	public boolean calibrate (
		final org.drip.math.segment.ResponseValueConstraint rvcLeft,
		final double dblLeftEdgeResponseSlope,
		final org.drip.math.segment.ResponseValueConstraint rvcRight)
	{
		if (null == rvcLeft || !org.drip.math.common.NumberUtil.IsValid (dblLeftEdgeResponseSlope) || null ==
			rvcRight)
			return false;

		try {
			return calibrate (new org.drip.math.segment.CalibrationParams (null, null,
				org.drip.math.common.CollectionUtil.DerivArrayFromSlope (numParameters() - 2,
					dblLeftEdgeResponseSlope), null, new org.drip.math.segment.ResponseValueConstraint[]
						{rvcLeft, rvcRight}));
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	/**
	 * Calibrate the Coefficients from the Edge Response Values and the Left Edge Response Slope and
	 *  calculate the Jacobian of the Segment's Response Basis Function Coefficients to the Edge Parameters
	 * 
	 * @param dblLeftEdgeResponseValue Left Edge Response Value
	 * @param dblLeftEdgeResponseSlope Left Edge Response Slope
	 * @param dblRightEdgeResponseValue Right Edge Response Value
	 * 
	 * @return The Jacobian of the Segment's Response Basis Function Coefficients to the Edge Parameters
	 */

	public org.drip.math.calculus.WengertJacobian jackDCoeffDEdgeParams (
		final double dblLeftEdgeResponseValue,
		final double dblLeftEdgeResponseSlope,
		final double dblRightEdgeResponseValue)
	{
		if (!calibrate (dblLeftEdgeResponseValue, dblLeftEdgeResponseSlope, dblRightEdgeResponseValue))
			return null;

		return jackDCoeffDEdgeParams();
	}

	/**
	 * Calibrate the coefficients from the prior Segment and the Response Value at the Right Predictor
	 *  Ordinate and calculate the Jacobian of the Segment's Response Basis Function Coefficients to the Edge
	 *  Parameters
	 * 
	 * @param prPrev Previous Predictor/Response Segment
	 * @param dblRightEdgeResponseValue Right Edge Response Value
	 * 
	 * @return The Jacobian
	 */

	public org.drip.math.calculus.WengertJacobian jackDCoeffDEdgeParams (
		final PredictorResponse prPrev,
		final double dblRightEdgeResponseValue)
	{
		if (!calibrate (prPrev, dblRightEdgeResponseValue)) return null;

		return jackDCoeffDEdgeParams();
	}

	/**
	 * Display the string representation for diagnostic purposes
	 * 
	 * @return The string representation
	 */

	public abstract java.lang.String displayString();
}
