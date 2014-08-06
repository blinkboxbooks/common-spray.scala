# Change Log

## 0.14.0 ([#23](https://git.mobcastdev.com/Platform/common-spray/pull/23) 2014-08-06 13:45:43)

Replaced some code with common-json

### Breaking changes

- `JsonFormats`.`ExplicitTypeHints` and
`JsonFormats.ISODateTimeSerializer` have been removed; use the
corresponding classes in `common-json`.

### Bug fixes

- DateTime query parameters are now parsed correctly if they include a
milliseconds component.
- DateTime query parameters are always converted to UTC even if they
were sent in a different time zone.

## 0.13.1 ([#21](https://git.mobcastdev.com/Platform/common-spray/pull/21) 2014-07-25 17:40:19)

Log more info from HTTP requests

### Improvements

- Now captures interesting HTTP request and response headers in the log
output.
- Logs `401 Unauthorized` calls as INFO rather than WARN because this
is a common BAU code for us.

## 0.13.0 ([#17](https://git.mobcastdev.com/Platform/common-spray/pull/17) 2014-06-30 18:58:17)

Added a health check service trait

### New features

- Added a `HealthCheckHttpService` trait to serve Codahale health check results using Spray.
- Added a `rootPath` directive for easier and more reliable hosting of services at configurable paths.

## 0.12.0 ([#20](https://git.mobcastdev.com/Platform/common-spray/pull/20) 2014-07-03 17:28:41)

Quick fix for asQueryParams

### Breaking change

* Changed `asQueryParams` to return Seq[(String, String)]

## 0.11.0 ([#19](https://git.mobcastdev.com/Platform/common-spray/pull/19) 2014-07-03 16:33:23)

Pagination links improvements

### Breaking changes

* Changed the signature of paginated links generation: accepts a sequence of tuples instead of a map, to support repeated parameters

### Improvements

* Added `asQueryParams` for each `SortOrder` object so that you can combine it with pagination.

## 0.10.0 ([#18](https://git.mobcastdev.com/Platform/common-spray/pull/18) 2014-07-02 16:33:10)

Sorting and paging improvements

### Breaking changes

* Added ordered directive
* Added orderedAndPaged directive
* Paging links now support an optional query param
* Added an implicit conversion from `PageLink` to `Link` so that it can be used with `ListPage`

## 0.9.1 ([#16](https://git.mobcastdev.com/Platform/common-spray/pull/16) 2014-06-27 12:47:47)

monitor directive now handles exceptions

### Improvements

- The `monitor` directive now handles any unhandled exceptions using
the default logic so that all requests are logged irrespective or
whether they finish successfully, with a rejection, or with an
exception.

## 0.9.0 ([#15](https://git.mobcastdev.com/Platform/common-spray/pull/15) 2014-06-26 15:49:50)

Diagnostics

### New features

- Added a new `monitor()` directive which adds MDC context about the request and logs basic details of the request and response.
- Added a `clientIP` function to `HttpRequest`.

## 0.8.1 ([#14](https://git.mobcastdev.com/Platform/common-spray/pull/14) 2014-06-20 11:07:06)

ISODateTimeDeserializer

### Improvements

* Added ISODateTimeDeserializer for Joda DateTime query parameters
* Refactored BigDecimalDeserializer tests

## 0.8.0 ([#13](https://git.mobcastdev.com/Platform/common-spray/pull/13) 2014-06-09 10:12:15)

Convert from Java URI/URL to Spray Uri

### New features

- Defines implicit conversions from the Java `URI` and `URL` classes to
a Spray `Uri` instance, which is nicer to work when you’re using the
Spray framework.

## 0.7.2 ([#11](https://git.mobcastdev.com/Platform/common-spray/pull/11) 2014-06-05 12:13:38)

Tiny change to force rebuild, should be published to Artifactory

Patch to update docs, to force a version number increase and rebuild. This is the first version that will be published to Artifactory.

## 0.7.1 ([#9](https://git.mobcastdev.com/Platform/common-spray/pull/9) 2014-06-04 12:22:21)

Removed whitespace

### Patch 

Merged the previous pull request too early :eyes:

## 0.7.0 ([#8](https://git.mobcastdev.com/Platform/common-spray/pull/8) 2014-06-04 12:14:00)

Added BigDecimal deserialiser

### New features

- Added `BigDecimalDeserializer` to allow `BigDecimal` query parameters

## 0.6.0 ([#7](https://git.mobcastdev.com/Platform/common-spray/pull/7) 2014-05-30 12:30:00)

Moved v1 JSON support into its own package

### Breaking changes

- The v1-specific JSON support is now in a `v1` sub-package.

### Improvements

- Includes resource models for v1 paged lists, links, images and errors.
- You can specify `responseTypeHints` on `Version1JsonSupport` for type
hints that are used only on responses (i.e. if you want to ignore the
type hint on requests, which you typically do).

## 0.5.0 ([#6](https://git.mobcastdev.com/Platform/common-spray/pull/6) 2014-05-28 17:38:46)

Added 'do not cache' directives

### New features

- Added `neverCache` and `uncacheable` directives to allow standard ‘do
not cache’ headers to be set.

## 0.4.0 ([#5](https://git.mobcastdev.com/Platform/common-spray/pull/5) 2014-05-23 16:45:47)

Added directive for setting standard cache headers on cached resources

### New features

* Added "cacheable" directive that allows standard cache headers to be set, with a configurable cache expiry time.


## 0.3.0 ([#4](https://git.mobcastdev.com/Platform/common-spray/pull/4) 2014-05-22 14:01:12)

Switched from native JSON library to Jackson

#### New features

- Parses JSON with Jackson instead of native support, as this is generally seen to be faster, hence should be the default choice for our services.

## 0.2.0 ([#3](https://git.mobcastdev.com/Platform/common-spray/pull/3) 2014-05-20 14:55:05)

JSON support + versionable by proteus

#### Breaking changes

- Removed the `version1ResponseHeaders` directive as this is obsoleted
by the `Version1JsonSupport` trait.

#### New features

- Added an `ISODateTimeSerializer` which serialises JodaTime dates
to/from JSON without losing time zone information, which the Json4s
default serialiser does.
- Added a `Version1JsonSupport` trait which can be mixed in to
configure both input and output for the blinkbox books v1 JSON format.

#### Improvements

- Changed the way the version is loaded so it can now be versioned by proteus.

