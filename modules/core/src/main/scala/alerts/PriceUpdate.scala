package org.lamedh.voltrad.core
package alerts

import io.circe.Codec
import cats.kernel.Eq
import cats.Show
import cats.derived.*

final case class PriceUpdate(
    symbol: Symbol,
    prices: Prices
) derives Eq, Show
