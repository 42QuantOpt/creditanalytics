
package org.drip.pricer.option;

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
 * FokkerPlanckGenerator exposes the interface that the performs the PDF evolution oriented Option Pricing.
 * 
 * @author Lakshmi Krishnamurthy
 */

public interface FokkerPlanckGenerator {

	/**
	 * Carry out a Pricing Run and generate the Pricing related measure set
	 * 
	 * @param dblStrike Option Strike
	 * @param dbTimeToExpiry Option Time To Expiry
	 * @param dblRiskFreeRate Option Risk Free Rate
	 * @param dblSpot Option Spot Value
	 * @param dblSpotVolatility Option Spot Volatility Value
	 * 
	 * @return TRUE => Computation Successful
	 */

	public abstract boolean compute (
		final double dblStrike,
		final double dbTimeToExpiry,
		final double dblRiskFreeRate,
		final double dblSpot,
		final double dblSpotVolatility);

	/**
	 * The Option Terminal Discount Factor
	 * 
	 * @return The Option Terminal Discount Factor
	 */

	public abstract double df();

	/**
	 * The Call Option Price
	 * 
	 * @return The Call Option Price
	 */

	public abstract double callPrice();

	/**
	 * The Call Option Delta
	 * 
	 * @return The Call Option Delta
	 */

	public abstract double callDelta();

	/**
	 * The Call Prob 1 Term
	 * 
	 * @return The Call Prob 1 Term
	 */

	public abstract double callProb1();

	/**
	 * The Call Prob 2 Term
	 * 
	 * @return The Call Prob 2 Term
	 */

	public abstract double callProb2();

	/**
	 * The Put Option Price
	 * 
	 * @return The Put Option Price
	 */

	public abstract double putPrice();

	/**
	 * The Put Option Price Computed from the Put-Call Parity Relation
	 * 
	 * @return The Put Option Price Computed from the Put-Call Parity Relation
	 */

	public abstract double putPriceFromParity();

	/**
	 * The Put Option Delta
	 * 
	 * @return The Put Option Delta
	 */

	public abstract double putDelta();

	/**
	 * The Put Prob 1 Term
	 * 
	 * @return The Put Prob 1 Term
	 */

	public abstract double putProb1();

	/**
	 * The Put Prob 2 Term
	 * 
	 * @return The Put Prob 2 Term
	 */

	public abstract double putProb2();
}
