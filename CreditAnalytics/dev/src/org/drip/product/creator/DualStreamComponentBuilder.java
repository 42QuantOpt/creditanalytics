
package org.drip.product.creator;

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
 * DualStreamComponentBuilder contains the suite of helper functions for creating the Stream-based Dual
 *  Streams from different kinds of inputs. In particular, it exposes the following functionality:
 *  - Construction of the fix-float swap component.
 *  - Construction of the float-float swap component.
 *  - Construction of the generic dual stream component.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class DualStreamComponentBuilder {

	/**
	 * Make the FixFloatComponent Instance from the Reference Fixed and the Derived Floating Streams
	 * 
	 * @param fixReference The Reference Fixed Stream
	 * @param floatDerived The Derived Floating Stream
	 * 
	 * @return The FixFloatComponent Instance
	 */

	public static final org.drip.product.rates.FixFloatComponent MakeFixFloat (
		final org.drip.product.cashflow.FixedStream fixReference,
		final org.drip.product.cashflow.FloatingStream floatDerived)
	{
		try {
			return new org.drip.product.rates.FixFloatComponent (fixReference, floatDerived);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Make the FloatFloatComponent Instance from the Reference and the Derived Floating Streams
	 * 
	 * @param floatReference The Reference Floating Stream
	 * @param floatDerived The Derived Floating Stream
	 * 
	 * @return The FloatFloatComponent Instance
	 */

	public static final org.drip.product.rates.FloatFloatComponent MakeFloatFloat (
		final org.drip.product.cashflow.FloatingStream floatReference,
		final org.drip.product.cashflow.FloatingStream floatDerived)
	{
		try {
			return new org.drip.product.rates.FloatFloatComponent (floatReference, floatDerived);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Make the DualStreamComponent Instance from the Reference and the Derived Component Streams
	 * 
	 * @param rcReference The Reference CalibratableFixedIncomeComponent Stream
	 * @param rcDerived The Derived CalibratableFixedIncomeComponent Stream
	 * 
	 * @return The DualStreamComponent Instance
	 */

	public static final org.drip.product.cashflow.DualStreamComponent MakeDualStream (
		final org.drip.product.definition.CalibratableFixedIncomeComponent rcReference,
		final org.drip.product.definition.CalibratableFixedIncomeComponent rcDerived)
	{
		if (null == rcReference || null == rcDerived) return null;

		if (rcReference instanceof org.drip.product.cashflow.FloatingStream && rcDerived instanceof
			org.drip.product.cashflow.FloatingStream)
			return MakeFloatFloat ((org.drip.product.cashflow.FloatingStream) rcReference,
				(org.drip.product.cashflow.FloatingStream) rcDerived);

		if (rcReference instanceof org.drip.product.cashflow.FixedStream && rcDerived instanceof
			org.drip.product.cashflow.FloatingStream)
			return MakeFixFloat ((org.drip.product.cashflow.FixedStream) rcReference,
				(org.drip.product.cashflow.FloatingStream) rcDerived);

		return null;
	}
}
