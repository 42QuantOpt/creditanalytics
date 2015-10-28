# **[DRIP CreditAnalytics](http://www.credit-trader.net/CreditAnalytics.html)** #



Lakshmi Krishnamurthy
**v2.3**  _23 January 2014_


[DRIP CreditAnalytics](http://www.credit-trader.net/CreditAnalytics.html) and [DRIP CreditProduct](http://www.credit-trader.net/CreditProduct.html) are part of [DRIP CreditSuite](http://www.credit-trader.net/Begin.html) – open suite analytics and risk/trading/valuation system for fixed income products. Detailed documentation and downloads may be found [here](http://www.credit-trader.net/Begin.html).

[DRIP CreditProduct](http://www.credit-trader.net/CreditProduct.html) provides the functional and behavioral interfaces behind curves, products, and different parameter types (market, valuation, pricing, and product parameters). To facilitate this, it implements various day count conventions, holiday sets, period generators, and calculation outputs.

[DRIP CreditProduct](http://www.credit-trader.net/CreditProduct.html) library achieves its design goal by implementing its functionality over several packages:
  * _Dates and holidays coverage_: Covers a variety of day count conventions, 120+ holiday locations, as well as custom user-defined holidays
  * _Curve and analytics definitions_: Defines the base functional interfaces for the variants of discount curves, credit curves, and FX curves
  * _Market Parameter definitions_: Defines quotes, component/basket market parameters, and custom scenario parameters
  * _Valuation and Pricing Parameters_: Defines valuation, settlement/work-out, and pricing parameters of different variants
  * _Product and product parameter definitions_: Defines the product creation and behavior interfaces for Cash/EDF/IRS (all rates), bonds/CDS (credit), and basket bond/CDS, and their feature parameters.
  * _Output measures container_: Defines generalized component and basket outputs, as well customized outputs for specific products

[DRIP CreditAnalytics](http://www.credit-trader.net/CreditAnalytics.html) provides the functionality behind creation, calibration, and implementation of the curve, the parameter, and the product interfaces defined in [DRIP CreditProduct](http://www.credit-trader.net/CreditProduct.html). It also implements a curve/parameter/product/analytics management environment, and has packaged samples and testers.

[DRIP CreditAnalytics](http://www.credit-trader.net/CreditAnalytics.html) library achieves its design goal by implementing its functionality over several packages:
  * _Curve calibration and creation_: Functional implementation and creation factories for rates curves, credit curves, and FX curves of al types
  * _Market Parameter implementation and creation_: Implementation and creation of quotes, component/basket market parameters, as well as scenario parameters.
  * _Product implementation and creation_: Implementation and creation factories for rates products (cash/EDF/IRS), credit products (bonds/CDS), as well as basket products.
  * _Reference data/marks loaders_: Loaders for bond/CDX, as well a sub-universe of closing marks
  * _Calculation Environment Manager_: Implementation of the market parameter container, manager for live/closing curves, stub/client functionality for serverization/distribution, input/output serialization.
  * _Samples_: Samples for curve, parameters, product, and analytics creation and usage
  * _Unit functional testers_: Detailed unit scenario test of various analytics, curve, parameter, and product functionality.

Download [DRIP CreditAnalytics](http://www.credit-trader.net/CreditAnalytics.html) binary along with the complete [DRIP CreditSuite](http://www.credit-trader.net/CreditSuite.html) source from the link [here](http://www.credit-trader.net/Downloads.html). Samples are available [here](http://www.credit-trader.net/Samples.html).

To install [DRIP CreditAnalytics](http://www.credit-trader.net/CreditAnalytics.html), put the [CreditAnalytics.jar](http://www.credit-trader.net/CreditAnalytics.html) onto the class-path. Use Config.xml to configure custom holidays. The Oracle ODBC driver is optional – it is used for the ref data connection.

## Licence ##

[DRIP CreditAnalytics](http://www.credit-trader.net/CreditAnalytics.html) is distributed under the Apache 2.0 licence - please see the attached Licence for details.
