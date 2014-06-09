# Change Log

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

