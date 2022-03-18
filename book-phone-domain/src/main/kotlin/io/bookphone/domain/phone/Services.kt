package io.bookphone.domain.phone

import arrow.core.Option
import arrow.core.toOption

interface PhoneSpecService {
  val levenshteinDistance: LevenshteinDistance

  fun List<Pair<PhoneSpec, PhoneName>>.getBestSpec(targetName: PhoneName): Option<PhoneSpec> =
    map { (spec, name) ->
      spec to levenshteinDistance(targetName.value, name.value)
    }
      .reduceRightOrNull { (spec1, distance1), (spec2, distance2) ->
        if (distance1 < distance2) spec1 to distance1
        else spec2 to distance2
      }
      .toOption()
      .map { (spec, _) -> spec }
}
