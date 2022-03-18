rootProject.name = "book-phone"
include(
    "book-phone-app:web",
    "book-phone-domain",
    "book-phone-infra:persistence",
    "book-phone-infra:adapter"
)
