
package org.drip.product.calib;

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
 * FixFloatQuoteSet extends the ProductQuoteSet by implementing the Calibration Parameters for the Fix-Float
 *  Swap Component. Currently it exposes the PV, the Reference Basis, and the Derived Basis Quote Fields.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class FixFloatQuoteSet extends org.drip.product.calib.ProductQuoteSet {

	/**
	 * Set the PV
	 * 
	 * @param dblPV The PV
	 * 
	 * @return TRUE => PV successfully set
	 */

	public boolean setPV (
		final double dblPV)
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblPV)) return false;

		_mapQuote.put ("PV", dblPV);

		return true;
	}

	/**
	 * Indicate if the PV Field exists
	 * 
	 * @return TRUE => PV Field Exists
	 */

	public boolean containsPV()
	{
		return _mapQuote.containsKey ("PV");
	}

	/**
	 * Retrieve the PV
	 * 
	 * @return The PV
	 * 
	 * @throws java.lang.Exception Thrown if the PV Field does not exist
	 */

	public double pv()
		throws java.lang.Exception
	{
		if (!containsPV()) throw new java.lang.Exception ("FixFloatQuoteSet::pv => Does not contain PV");

		return _mapQuote.get ("PV");
	}

	/**
	 * Set the Derived Basis
	 * 
	 * @param dblDerivedBasis The Derived Basis
	 * 
	 * @return TRUE => The Derived Basis successfully set
	 */

	public boolean setDerivedBasis (
		final double dblDerivedBasis)
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblDerivedBasis)) return false;

		_mapQuote.put ("DerivedBasis", dblDerivedBasis);

		return true;
	}

	/**
	 * Indicate if the Derived Basis Field exists
	 * 
	 * @return TRUE => The Derived Basis Field Exists
	 */

	public boolean containsDerivedBasis()
	{
		return _mapQuote.containsKey ("DerivedBasis");
	}

	/**
	 * Retrieve the Derived Basis
	 * 
	 * @return The Derived Basis
	 * 
	 * @throws java.lang.Exception Thrown if the Derived Basis Field does not exist
	 */

	public double derivedBasis()
		throws java.lang.Exception
	{
		if (!containsDerivedBasis())
			throw new java.lang.Exception
				("FixFloatQuoteSet::derivedBasis => Does not contain the Derived Basis");

		return _mapQuote.get ("DerivedBasis");
	}

	/**
	 * Set the Swap Rate
	 * 
	 * @param dblSwapRate The Swap Rate
	 * 
	 * @return TRUE => The Swap Rate successfully set
	 */

	public boolean setSwapRate (
		final double dblSwapRate)
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblSwapRate)) return false;

		_mapQuote.put ("SwapRate", dblSwapRate);

		return true;
	}

	/**
	 * Indicate if the Swap Rate Field exists
	 * 
	 * @return TRUE => The Swap Rate Field Exists
	 */

	public boolean containsSwapRate()
	{
		return _mapQuote.containsKey ("SwapRate");
	}

	/**
	 * Retrieve the Swap Rate
	 * 
	 * @return The Swap Rate
	 * 
	 * @throws java.lang.Exception Thrown if the Swap Rate Field does not exist
	 */

	public double swapRate()
		throws java.lang.Exception
	{
		if (!containsSwapRate())
			throw new java.lang.Exception ("FixFloatQuoteSet::swapRate => Does not contain the Swap Rate");

		return _mapQuote.get ("SwapRate");
	}
}
