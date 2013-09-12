
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
 * This concrete class extends segment, and implements the segment's Ck based spline functionality. It
 * 	exports the following:
 * 	- Calibration: Head Calibration, Regular Calibration
 *  - Estimated Segment Elastics: The Basis Functions and their coefficients, Ck, the shape controller
 *  - Local Point Evaluation: Value, Ordered Derivative
 *  - Local Monotonicity
 *  - Local coefficient/derivative micro-Jack, and value/coefficient micro-Jack
 *  - Local Jacobians: Value Micro Jacobian, Value Elastic Jacobian, Composite Value Jacobian
 * 
 * @author Lakshmi Krishnamurthy
 */

public class PredictorResponseBasisSpline extends org.drip.math.segment.PredictorResponse {
	private static final int DISPLAY_SEGMENT_PREDICTOR_PARTITION = 5;

	private double[] _adblResponseBasisCoeff = null;
	private org.drip.math.segment.DesignInelasticParams _ep = null;
	private double[][] _aadblDResponseBasisCoeffDConstraint = null;
	private org.drip.math.function.AbstractUnivariate _auShapeControl = null;
	private org.drip.math.function.AbstractUnivariate[] _aAUResponseBasis = null;
	private org.drip.math.calculus.WengertJacobian _wjDBasisCoeffDEdgeParams = null;

	class CrossBasisDerivativeProduct extends org.drip.math.function.AbstractUnivariate {
		int _iOrder = -1;
		org.drip.math.function.AbstractUnivariate _aAU1 = null;
		org.drip.math.function.AbstractUnivariate _aAU2 = null;

		CrossBasisDerivativeProduct (
			final int iOrder,
			final org.drip.math.function.AbstractUnivariate aAU1,
			final org.drip.math.function.AbstractUnivariate aAU2)
		{
			super (null);

			_aAU1 = aAU1;
			_aAU2 = aAU2;
			_iOrder = iOrder;
		}

		public double evaluate (
			final double dblVariate)
			throws java.lang.Exception
		{
			return _aAU1.calcDerivative (dblVariate, _iOrder) * _aAU2.calcDerivative (dblVariate, _iOrder);
		}
	}

	/**
	 * Build the PredictorResponseBasisSpline instance from the Basis Set
	 * 
	 * @param dblLeftPredictorOrdinate Left Predictor Ordinate
	 * @param dblRightPredictorOrdinate Right Predictor Ordinate
	 * @param aAUResponseBasis Response Basis Set Functions
	 * @param auShapeControl Shape Control Basis Function
	 * @param ep Elastic Parameters
	 * 
	 * @return Instance of PredictorResponseBasisSpline
	 */

	public static final org.drip.math.segment.PredictorResponseBasisSpline Create (
		final double dblLeftPredictorOrdinate,
		final double dblRightPredictorOrdinate,
		final org.drip.math.function.AbstractUnivariate[] aAUResponseBasis,
		final org.drip.math.function.AbstractUnivariate auShapeControl,
		final org.drip.math.segment.DesignInelasticParams ep)
	{
		try {
			return new org.drip.math.segment.PredictorResponseBasisSpline (dblLeftPredictorOrdinate,
				dblRightPredictorOrdinate, aAUResponseBasis, auShapeControl, ep);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	private double basisFunctionResponseDerivative (
		final double dblPredictorOrdinate,
		final int iOrder)
		throws java.lang.Exception
	{
		double dblDerivative = 0.;
		int iNumBasis = _aAUResponseBasis.length;

		for (int i = 0; i < iNumBasis; ++i)
			dblDerivative += _adblResponseBasisCoeff[i] * _aAUResponseBasis[i].calcDerivative
				(dblPredictorOrdinate, iOrder);

		return dblDerivative;
	}

	private double basisFunctionResponse (
		final double dblPredictorOrdinate)
		throws java.lang.Exception
	{
		double dblResponse = 0.;
		int iNumBasis = _aAUResponseBasis.length;

		for (int i = 0; i < iNumBasis; ++i)
			dblResponse += _adblResponseBasisCoeff[i] * _aAUResponseBasis[i].evaluate (dblPredictorOrdinate);

		return dblResponse;
	}

	private boolean calibFromRVC (
		final org.drip.math.segment.ResponseValueConstraint rvc)
	{
		try {
			int iCk = _ep.getCk();

			double[] adblLeftDeriv = null;

			if (0 != iCk) {
				adblLeftDeriv = new double[iCk];

				for (int i = 0; i < iCk; ++i)
					adblLeftDeriv[i] = responseDerivative (0., i);
			}

			return calibrate (new org.drip.math.segment.CalibrationParams (new double[] {0.}, new double[]
				{response (0.)}, adblLeftDeriv, null, new org.drip.math.segment.ResponseValueConstraint[]
					{rvc}));
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	private boolean setJackDCoeffDEdge()
	{
		if (null == _aadblDResponseBasisCoeffDConstraint) return false;

		int iSize = _aadblDResponseBasisCoeffDConstraint.length;

		for (int i = 0; i < iSize; ++i) {
			for (int j = 0; j < iSize; ++j) {
				if (!_wjDBasisCoeffDEdgeParams.accumulatePartialFirstDerivative (i, j,
					_aadblDResponseBasisCoeffDConstraint[i][j]))
					return false;
			}
		}

		return true;
	}

	private double[] basisDResponseDBasisCoeff (
		final double dblPredictorOrdinate)
	{
		int iNumBasis = _aAUResponseBasis.length;
		double[] adblDResponseDBasisCoeff = new double[iNumBasis];

		for (int i = 0; i < iNumBasis; ++i) {
			try {
				adblDResponseDBasisCoeff[i] = _aAUResponseBasis[i].evaluate (dblPredictorOrdinate);
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}
		}

		return adblDResponseDBasisCoeff;
	}

	protected PredictorResponseBasisSpline (
		final double dblLeftPredictorOrdinate,
		final double dblRightPredictorOrdinate,
		final org.drip.math.function.AbstractUnivariate[] aAUPredictorBasis,
		final org.drip.math.function.AbstractUnivariate auShapeControl,
		final org.drip.math.segment.DesignInelasticParams ep)
		throws java.lang.Exception
	{
		super (dblLeftPredictorOrdinate, dblRightPredictorOrdinate);

		if (null == (_aAUResponseBasis = aAUPredictorBasis) || null == (_ep = ep))
			throw new java.lang.Exception ("PredictorResponseBasisSpline ctr: Invalid Basis Functions!");

		_auShapeControl = auShapeControl;
		int iNumBasis = _aAUResponseBasis.length;
		_adblResponseBasisCoeff = new double[iNumBasis];

		if (0 >= iNumBasis || _ep.getCk() > iNumBasis - 2)
			throw new java.lang.Exception ("PredictorResponseBasisSpline ctr: Invalid inputs!");
	}

	@Override protected boolean isMonotone()
	{
		return 1 >= _ep.getCk();
	}

	@Override public double response (
		final double dblPredictorOrdinate)
		throws java.lang.Exception
	{
		return null == _auShapeControl ? basisFunctionResponse (dblPredictorOrdinate) : basisFunctionResponse
			(dblPredictorOrdinate) * _auShapeControl.evaluate (dblPredictorOrdinate);
	}

	@Override public double responseDerivative (
		final double dblPredictorOrdinate,
		final int iOrder)
		throws java.lang.Exception
	{
		if (null == _auShapeControl) return basisFunctionResponseDerivative (dblPredictorOrdinate, iOrder);

		double dblResponseDerivative = 0.;

		for (int i = 0; i <= iOrder; ++i) {
			double dblBasisFunctionDeriv = 0 == i ? basisFunctionResponse (dblPredictorOrdinate):
				basisFunctionResponseDerivative (dblPredictorOrdinate, i);

			if (!org.drip.math.common.NumberUtil.IsValid (dblBasisFunctionDeriv))
				throw new java.lang.Exception
					("PredictorResponseBasisSpline::responseDerivative => Cannot compute Basis Function Derivative");

			double dblShapeControlDeriv = iOrder == i ? _auShapeControl.evaluate (dblPredictorOrdinate) :
				_auShapeControl.calcDerivative (dblPredictorOrdinate, iOrder - i);

			if (!org.drip.math.common.NumberUtil.IsValid (dblShapeControlDeriv))
				throw new java.lang.Exception
					("PredictorResponseBasisSpline::responseDerivative => Cannot compute Shape Control Derivative");

			dblResponseDerivative += (org.drip.math.common.NumberUtil.NCK (iOrder,  i) *
				dblBasisFunctionDeriv * dblShapeControlDeriv);
		}

		return dblResponseDerivative;
	}

	/**
	 * Get the Ck constraint number
	 * 
	 * @return The "k" in Ck
	 */

	public int getCk()
	{
		return _ep.getCk();
	}

	/**
	 * Retrieve the Shape Control
	 * 
	 * @return The Shape Control
	 */

	public org.drip.math.function.AbstractUnivariate getShapeControl()
	{
		return _auShapeControl;
	}

	@Override public int numBasis()
	{
		return _aAUResponseBasis.length;
	}

	@Override public int numParameters()
	{
		return _ep.getCk() + 2;
	}

	@Override public double calcOrderedResponseDerivative (
		final double dblPredictorOrdinate,
		final int iOrder,
		final boolean bLocal)
		throws java.lang.Exception
	{
		double dblLocalPredictorOrdinate = localize (dblPredictorOrdinate);

		if (0 == iOrder) return response (dblLocalPredictorOrdinate);

		if (_ep.getCk() < iOrder && (0. == dblPredictorOrdinate || 1. == dblPredictorOrdinate))
			throw new java.lang.Exception
				("PredictorResponseBasisSpline::calcOrderedResponseDerivative => Segment Discontinuous: C" +
					_ep.getCk() + " less than deriv order " + iOrder + " at segment edges!");

		double dblOrderedResponseDerivative = responseDerivative (dblLocalPredictorOrdinate, iOrder);

		if (bLocal) return dblOrderedResponseDerivative;

		double dblSegmentWidth = width();

		for (int i = 0; i < iOrder; ++i)
			dblOrderedResponseDerivative /= dblSegmentWidth;

		return dblOrderedResponseDerivative;
	}

	@Override public boolean calibrate (
		final org.drip.math.segment.CalibrationParams cp)
	{
		if (null == cp) return false;

		double[] adblCalibResponse = cp.reponses();

		double[] adblCalibLeftEdgeDeriv = cp.leftDeriv();

		double[] adblCalibRightEdgeDeriv = cp.rightDeriv();

		double[] adblCalibPredictorOrdinate = cp.predictorOrdinates();

		org.drip.math.segment.ResponseBasisConstraint[] aCalibRBC = cp.getBasisFunctionConstraint
			(_aAUResponseBasis, this);

		int iNumResponseBasisCoeff = _adblResponseBasisCoeff.length;
		int iNumSegmentCalibConstraint = null == aCalibRBC ? 0 : aCalibRBC.length;
		double[] adblPredictorResponseConstraintValue = new double[iNumResponseBasisCoeff];
		int iNumCalibLeftDeriv = null == adblCalibLeftEdgeDeriv ? 0 : adblCalibLeftEdgeDeriv.length;
		int iNumCalibRightDeriv = null == adblCalibRightEdgeDeriv ? 0 : adblCalibRightEdgeDeriv.length;
		double[][] aadblResponseBasisCoeffConstraint = new
			double[iNumResponseBasisCoeff][iNumResponseBasisCoeff];
		int iNumCalibPredictorOrdinate = null == adblCalibPredictorOrdinate ? 0 :
			adblCalibPredictorOrdinate.length;

		if (iNumResponseBasisCoeff < iNumCalibPredictorOrdinate + iNumCalibLeftDeriv + iNumCalibRightDeriv +
			iNumSegmentCalibConstraint)
			return false;

		for (int j = 0; j < iNumResponseBasisCoeff; ++j) {
			if (j < iNumCalibPredictorOrdinate)
				adblPredictorResponseConstraintValue[j] = adblCalibResponse[j];
			else if (j < iNumCalibPredictorOrdinate + iNumSegmentCalibConstraint)
				adblPredictorResponseConstraintValue[j] =
					aCalibRBC[j - iNumCalibPredictorOrdinate].contraintValue();
			else if (j < iNumCalibPredictorOrdinate + iNumSegmentCalibConstraint + iNumCalibLeftDeriv)
				adblPredictorResponseConstraintValue[j] =
					adblCalibLeftEdgeDeriv[j - iNumCalibPredictorOrdinate - iNumSegmentCalibConstraint];
			else if (j < iNumCalibPredictorOrdinate + iNumSegmentCalibConstraint + iNumCalibLeftDeriv +
				iNumCalibRightDeriv)
				adblPredictorResponseConstraintValue[j] = adblCalibRightEdgeDeriv[j -
				    iNumCalibPredictorOrdinate - iNumSegmentCalibConstraint - iNumCalibLeftDeriv];
			else
				adblPredictorResponseConstraintValue[j] = 0.;
		}

		for (int i = 0; i < iNumResponseBasisCoeff; ++i) {
			try {
				for (int l = 0; l < iNumResponseBasisCoeff; ++l) {
					double[] adblCalibBasisConstraintWeight = null;

					if (0 != iNumSegmentCalibConstraint && (l >= iNumCalibPredictorOrdinate && l <
						iNumCalibPredictorOrdinate + iNumSegmentCalibConstraint))
						adblCalibBasisConstraintWeight =
							aCalibRBC[l - iNumCalibPredictorOrdinate].responseBasisCoeffWeights();

					if (l < iNumCalibPredictorOrdinate)
						aadblResponseBasisCoeffConstraint[l][i] = _aAUResponseBasis[i].evaluate
							(adblCalibPredictorOrdinate[l]);
					else if (l < iNumCalibPredictorOrdinate + iNumSegmentCalibConstraint)
						aadblResponseBasisCoeffConstraint[l][i] = adblCalibBasisConstraintWeight[i];
					else if (l < iNumCalibPredictorOrdinate + iNumSegmentCalibConstraint +
						iNumCalibLeftDeriv)
						aadblResponseBasisCoeffConstraint[l][i] = _aAUResponseBasis[i].calcDerivative (0., l
							- iNumCalibPredictorOrdinate - iNumSegmentCalibConstraint + 1);
					else if (l < iNumCalibPredictorOrdinate + iNumSegmentCalibConstraint + iNumCalibLeftDeriv
						+ iNumCalibRightDeriv)
						aadblResponseBasisCoeffConstraint[l][i] = _aAUResponseBasis[i].calcDerivative (1., l
							- iNumCalibPredictorOrdinate - iNumSegmentCalibConstraint - iNumCalibLeftDeriv +
								1);
					else
						aadblResponseBasisCoeffConstraint[l][i] = org.drip.math.calculus.Integrator.Boole
							(new CrossBasisDerivativeProduct (_ep.getRoughnessPenaltyDerivativeOrder(),
								_aAUResponseBasis[i], _aAUResponseBasis[l]), 0., 1.);
				}
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return false;
			}
		}

		org.drip.math.linearalgebra.LinearizationOutput lss =
			org.drip.math.linearalgebra.LinearSystemSolver.SolveUsingMatrixInversion
				(aadblResponseBasisCoeffConstraint, adblPredictorResponseConstraintValue);

		if (null == lss && null == (lss =
			org.drip.math.linearalgebra.LinearSystemSolver.SolveUsingGaussianElimination
				(aadblResponseBasisCoeffConstraint, adblPredictorResponseConstraintValue)))
			return false;

		double[] adblCalibResponseBasisCoeff = lss.getTransformedRHS();

		if (null == adblCalibResponseBasisCoeff || adblCalibResponseBasisCoeff.length !=
			iNumResponseBasisCoeff || null == (_aadblDResponseBasisCoeffDConstraint =
				lss.getTransformedMatrix()) || _aadblDResponseBasisCoeffDConstraint.length !=
					iNumResponseBasisCoeff || _aadblDResponseBasisCoeffDConstraint[0].length !=
						iNumResponseBasisCoeff)
			return false;

		for (int i = 0; i < iNumResponseBasisCoeff; ++i) {
			if (!org.drip.math.common.NumberUtil.IsValid (_adblResponseBasisCoeff[i] =
				adblCalibResponseBasisCoeff[i]))
				return false;
		}

		return true;
	}

	@Override public boolean calibrate (
		final org.drip.math.segment.PredictorResponse prPrev,
		final org.drip.math.segment.ResponseValueConstraint rvcCalib)
	{
		if (null == rvcCalib) return false;

		if (null == prPrev) return calibFromRVC (rvcCalib);

		double dblSegmentLeftPredictorOrdinate = left();

		double dblGlobalSegmentPredictorWidth = width();

		try {
			double dblOrderedResponseDerivScale = 1.;
			double[] adblResponseDerivAtLeftOrdinate = null;

			int iCK = _ep.getCk();

			if (0 != iCK) {
				adblResponseDerivAtLeftOrdinate = new double[iCK];

				for (int i = 0; i < iCK; ++i) {
					dblOrderedResponseDerivScale *= dblGlobalSegmentPredictorWidth;

					adblResponseDerivAtLeftOrdinate[i] = prPrev.calcOrderedResponseDerivative
						(dblSegmentLeftPredictorOrdinate, i + 1, false) * dblOrderedResponseDerivScale;
				}
			}

			return calibrate (new org.drip.math.segment.CalibrationParams (new double[] {0.}, new double[]
				{prPrev.calcValue (dblSegmentLeftPredictorOrdinate)}, adblResponseDerivAtLeftOrdinate, null,
					new org.drip.math.segment.ResponseValueConstraint[] {rvcCalib}));
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	@Override public org.drip.math.calculus.WengertJacobian jackDCoeffDEdgeParams()
	{
		if (null != _wjDBasisCoeffDEdgeParams) return _wjDBasisCoeffDEdgeParams;

		int iNumResponseBasisCoeff = numBasis();

		try {
			_wjDBasisCoeffDEdgeParams = new org.drip.math.calculus.WengertJacobian (iNumResponseBasisCoeff,
				iNumResponseBasisCoeff);
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return _wjDBasisCoeffDEdgeParams = null;
		}

		for (int i = 0; i < iNumResponseBasisCoeff; ++i) {
			if (!_wjDBasisCoeffDEdgeParams.setWengert (i, _adblResponseBasisCoeff[i]))
				return _wjDBasisCoeffDEdgeParams = null;
		}

		return setJackDCoeffDEdge() ? _wjDBasisCoeffDEdgeParams : (_wjDBasisCoeffDEdgeParams = null);
	}

	@Override public org.drip.math.calculus.WengertJacobian jackDResponseDEdgeParams (
		final double dblPredictorOrdinate)
	{
		int iNumResponseBasisCoeff = numBasis();

		double dblLocalPredictorOrdinate = java.lang.Double.NaN;
		org.drip.math.calculus.WengertJacobian wjDResponseDEdgeParams = null;
		double[][] aadblDBasisCoeffDEdgeParams = new double[iNumResponseBasisCoeff][iNumResponseBasisCoeff];

		try {
			dblLocalPredictorOrdinate = localize (dblPredictorOrdinate);
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		double[] adblDResponseDBasisCoeff = basisDResponseDBasisCoeff (dblLocalPredictorOrdinate);

		if (null == adblDResponseDBasisCoeff || iNumResponseBasisCoeff != adblDResponseDBasisCoeff.length)
			return null;

		org.drip.math.calculus.WengertJacobian wjDBasisCoeffDEdgeParams = (null == _wjDBasisCoeffDEdgeParams)
			? jackDCoeffDEdgeParams() : _wjDBasisCoeffDEdgeParams;

		for (int i = 0; i < iNumResponseBasisCoeff; ++i) {
			for (int j = 0; j < iNumResponseBasisCoeff; ++j)
				aadblDBasisCoeffDEdgeParams[j][i] = wjDBasisCoeffDEdgeParams.getFirstDerivative (j, i);
		}

		try {
			if (!(wjDResponseDEdgeParams = new org.drip.math.calculus.WengertJacobian (1,
				iNumResponseBasisCoeff)).setWengert (0, response (dblLocalPredictorOrdinate)))
				return null;
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		for (int i = 0; i < iNumResponseBasisCoeff; ++i) {
			for (int j = 0; j < iNumResponseBasisCoeff; ++j) {
				if (!wjDResponseDEdgeParams.accumulatePartialFirstDerivative (0, i,
					adblDResponseDBasisCoeff[j] * aadblDBasisCoeffDEdgeParams[j][i]))
					return null;
			}
		}

		return wjDResponseDEdgeParams;
	}

	@Override public org.drip.math.calculus.WengertJacobian jackDResponseDBasisCoeff (
		final double dblPredictorOrdinate) {
		int iNumResponseBasisCoeff = numBasis();

		double dblLocalPredictorOrdinate = java.lang.Double.NaN;
		org.drip.math.calculus.WengertJacobian wjDResponseDBasisCoeff = null;

		try {
			dblLocalPredictorOrdinate = localize (dblPredictorOrdinate);
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		double[] adblBasisDResponseDBasisCoeff = basisDResponseDBasisCoeff (dblLocalPredictorOrdinate);

		if (null == adblBasisDResponseDBasisCoeff || iNumResponseBasisCoeff !=
			adblBasisDResponseDBasisCoeff.length)
			return null;

		try {
			wjDResponseDBasisCoeff = new org.drip.math.calculus.WengertJacobian (1, iNumResponseBasisCoeff);
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		for (int i = 0; i < iNumResponseBasisCoeff; ++i) {
			if (!wjDResponseDBasisCoeff.accumulatePartialFirstDerivative (0, i,
				adblBasisDResponseDBasisCoeff[i]))
				return null;
		}

		return wjDResponseDBasisCoeff;
	}

	@Override public java.lang.String displayString()
	{
		double dblSegmentPartitionResponse = java.lang.Double.NaN;

		double dblGlobalPredictorOrdinateLeft = left();

		double dblGlobalSegmentPredictorWidth = width() / DISPLAY_SEGMENT_PREDICTOR_PARTITION;

		java.lang.StringBuffer sb = new java.lang.StringBuffer();

		for (int i = 0; i <= DISPLAY_SEGMENT_PREDICTOR_PARTITION; ++i) {
			double dblGlobalPredictorOrdinate = dblGlobalPredictorOrdinateLeft + i *
				dblGlobalSegmentPredictorWidth;

			try {
				dblSegmentPartitionResponse = calcValue (dblGlobalPredictorOrdinate);
			} catch (java.lang.Exception e) {
				e.printStackTrace();
			}

			sb.append ("\t\t\t" + dblGlobalPredictorOrdinate + " = " + dblSegmentPartitionResponse + "\n");
		}

		return sb.toString();
	}
}
