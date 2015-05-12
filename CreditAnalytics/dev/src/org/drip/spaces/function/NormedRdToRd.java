
package org.drip.spaces.function;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2015 Lakshmi Krishnamurthy
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
 * NormedRdToRd is the abstract class underlying the f : Post-Validated R^d -> Post-Validated R^d Normed
 *  Function Spaces.
 * 
 * The Reference we've used is:
 * 
 * 	- Carl, B., and I. Stephani (1990): Entropy, Compactness, and Approximation of Operators, Cambridge
 * 		University Press, Cambridge UK.
 *
 * @author Lakshmi Krishnamurthy
 */

public abstract class NormedRdToRd extends org.drip.spaces.function.NormedRdInput {
	private org.drip.function.deterministic.RdToRd _funcRdToRd = null;
	private org.drip.spaces.metric.RealMultidimensionalNormedSpace _rmnsOutput = null;

	protected NormedRdToRd (
		final org.drip.spaces.metric.RealMultidimensionalNormedSpace rmnsInput,
		final org.drip.spaces.metric.RealMultidimensionalNormedSpace rmnsOutput,
		final org.drip.function.deterministic.RdToRd funcRdToRd)
		throws java.lang.Exception
	{
		super (rmnsInput);

		if (null == (_rmnsOutput = rmnsOutput) || null == (_funcRdToRd = funcRdToRd))
			throw new java.lang.Exception ("NormedRdToRd ctr: Invalid Inputs");
	}

	/**
	 * Retrieve the Underlying RdToRd Function
	 * 
	 * @return The Underlying RdToRd Function
	 */

	public org.drip.function.deterministic.RdToRd function()
	{
		return _funcRdToRd;
	}

	/**
	 * Retrieve the Sample Supremum R^d Norm Array
	 * 
	 * @param vrmInstance The Validated Real Valued Multidimensional Instance
	 * 
	 * @return The Sample Supremum R^d Norm Array
	 */

	public double[] sampleRdSupremumNorm (
		final org.drip.spaces.instance.ValidatedRealMultidimensional vrmInstance)
		throws java.lang.Exception
	{
		if (null == vrmInstance || !vrmInstance.tensorSpaceType().match (input())) return null;

		double[][] aadblInstance = vrmInstance.instance();

		int iNumSample = aadblInstance.length;

		int iOutputDimension = _rmnsOutput.dimension();

		double[] adblSupremumNorm = _funcRdToRd.evaluate (aadblInstance[0]);

		if (null == adblSupremumNorm || iOutputDimension != adblSupremumNorm.length ||
			!org.drip.quant.common.NumberUtil.IsValid (adblSupremumNorm))
			return null;

		for (int i = 0; i < iOutputDimension; ++i)
			adblSupremumNorm[i] = java.lang.Math.abs (adblSupremumNorm[i]);

		for (int i = 1; i < iNumSample; ++i) {
			double[] adblSampleNorm = _funcRdToRd.evaluate (aadblInstance[i]);

			if (null == adblSampleNorm || iOutputDimension != adblSampleNorm.length) return null;

			for (int j = 0; j < iOutputDimension; ++j) {
				if (!org.drip.quant.common.NumberUtil.IsValid (adblSampleNorm[j])) return null;

				if (adblSampleNorm[j] > adblSupremumNorm[j]) adblSupremumNorm[j] = adblSampleNorm[j];
			}
		}

		return adblSupremumNorm;
	}

	/**
	 * Retrieve the Sample R^d Metric Norm Array
	 * 
	 * @param vrmInstance The Validated Real Valued Multidimensional Instance
	 * 
	 * @return The Sample R^d Metric Norm Array
	 */

	public double[] sampleRdMetricNorm (
		final org.drip.spaces.instance.ValidatedRealMultidimensional vrmInstance)
	{
		if (null == vrmInstance || !vrmInstance.tensorSpaceType().match (input())) return null;

		double[][] aadblInstance = vrmInstance.instance();

		int iOutputDimension = _rmnsOutput.dimension();

		double[] adblMetricNorm = new double[iOutputDimension];
		int iNumSample = aadblInstance.length;

		int iPNorm = output().pNorm();

		for (int i = 0; i < iNumSample; ++i)
			adblMetricNorm[i] = 0.;

		for (int i = 0; i < iNumSample; ++i) {
			double[] adblPointValue = _funcRdToRd.evaluate (aadblInstance[i]);

			if (null == adblPointValue || iOutputDimension != adblPointValue.length) return null;

			for (int j = 0; j < iOutputDimension; ++j) {
				if (!org.drip.quant.common.NumberUtil.IsValid (adblPointValue[j])) return null;

				adblMetricNorm[j] += java.lang.Math.pow (java.lang.Math.abs (adblPointValue[j]), iPNorm);
			}
		}

		for (int i = 0; i < iNumSample; ++i)
			adblMetricNorm[i] = java.lang.Math.pow (adblMetricNorm[i], 1. / iPNorm);

		return adblMetricNorm;
	}

	/**
	 * Retrieve the Population R^d ESS (Essential Spectrum) Array
	 * 
	 * @return The Population R^d ESS (Essential Spectrum) Array
	 */

	public double[] populationRdESS()
	{
		return _funcRdToRd.evaluate (((org.drip.spaces.metric.ContinuousRealMultidimensionalBanach)
			input()).populationMode());
	}

	/**
	 * Retrieve the Population R^d Metric Norm Array
	 * 
	 * @return The Population R^d Metric Norm Array
	 */

	public abstract double[] populationRdMetricNorm();

	@Override public org.drip.spaces.metric.RealMultidimensionalNormedSpace output()
	{
		return _rmnsOutput;
	}

	@Override public double sampleSupremumNorm (
		final org.drip.spaces.instance.GeneralizedValidatedVectorInstance gvvi)
		throws java.lang.Exception
	{
		if (null == gvvi)
			throw new java.lang.Exception ("NormedRdToRd::sampleSupremumNorm => Invalid Inputs");

		double[] adblSampleSupremumNorm = sampleRdSupremumNorm
			((org.drip.spaces.instance.ValidatedRealMultidimensional) gvvi);

		if (null == adblSampleSupremumNorm)
			throw new java.lang.Exception
				("NormedRdToRd::sampleSupremumNorm => Cannot compute Sample Supremum Array");

		double dblSampleSupremumNorm = java.lang.Double.NaN;
		int iOutputDimension = adblSampleSupremumNorm.length;

		if (0 == iOutputDimension)
			throw new java.lang.Exception
				("NormedRdToRd::sampleSupremumNorm => Cannot compute Sample Supremum Array");

		for (int i = 0; i < iOutputDimension; ++i) {
			if (!org.drip.quant.common.NumberUtil.IsValid (dblSampleSupremumNorm))
				dblSampleSupremumNorm = adblSampleSupremumNorm[i];
			else {
				if (dblSampleSupremumNorm < adblSampleSupremumNorm[i])
					dblSampleSupremumNorm = adblSampleSupremumNorm[i];
			}
		}

		return dblSampleSupremumNorm;
	}

	@Override public double sampleMetricNorm (
		final org.drip.spaces.instance.GeneralizedValidatedVectorInstance gvvi)
		throws java.lang.Exception
	{
		if (null == gvvi) throw new java.lang.Exception ("NormedRdToRd::sampleMetricNorm => Invalid Inputs");

		double[] adblSampleMetricNorm = sampleRdMetricNorm
			((org.drip.spaces.instance.ValidatedRealMultidimensional) gvvi);

		if (null == adblSampleMetricNorm)
			throw new java.lang.Exception
				("NormedRdToRd::sampleMetricNorm => Cannot compute Sample Metric Array");

		int iOutputDimension = adblSampleMetricNorm.length;
		double dblSampleMetricNorm = 0.;

		int iPNorm = output().pNorm();

		if (0 == iOutputDimension)
			throw new java.lang.Exception
				("NormedRdToRd::sampleMetricNorm => Cannot compute Sample Metric Array");

		for (int i = 0; i < iOutputDimension; ++i)
			dblSampleMetricNorm += java.lang.Math.pow (java.lang.Math.abs (adblSampleMetricNorm[i]), iPNorm);

		return java.lang.Math.pow (dblSampleMetricNorm, 1. / iPNorm);
	}

	@Override public double populationESS()
		throws java.lang.Exception
	{
		double[] adblPopulationRdESS = populationRdESS();

		if (null == adblPopulationRdESS)
			throw new java.lang.Exception
				("NormedRdToRd::populationRdESS => Cannot compute Population Rd ESS Array");

		double dblPopulationESS = java.lang.Double.NaN;
		int iOutputDimension = adblPopulationRdESS.length;

		if (0 == iOutputDimension)
			throw new java.lang.Exception
				("NormedRdToRd::populationRdESS => Cannot compute Population Rd ESS Array");

		for (int i = 0; i < iOutputDimension; ++i) {
			if (!org.drip.quant.common.NumberUtil.IsValid (dblPopulationESS ))
				dblPopulationESS = adblPopulationRdESS[i];
			else {
				if (dblPopulationESS < adblPopulationRdESS[i]) dblPopulationESS = adblPopulationRdESS[i];
			}
		}

		return dblPopulationESS;
	}

	@Override public double populationMetricNorm()
		throws java.lang.Exception
	{
		double[] adblPopulationMetricNorm = populationRdMetricNorm();

		if (null == adblPopulationMetricNorm)
			throw new java.lang.Exception
				("NormedRdToRd::populationMetricNorm => Cannot compute Population Metric Array");

		int iOutputDimension = adblPopulationMetricNorm.length;
		double dblPopulationMetricNorm = 0.;

		int iPNorm = output().pNorm();

		if (0 == iOutputDimension)
			throw new java.lang.Exception
				("NormedRdToRd::populationMetricNorm => Cannot compute Population Metric Array");

		for (int i = 0; i < iOutputDimension; ++i)
			dblPopulationMetricNorm += java.lang.Math.pow (java.lang.Math.abs (adblPopulationMetricNorm[i]),
				iPNorm);

		return java.lang.Math.pow (dblPopulationMetricNorm, 1. / iPNorm);
	}
}
