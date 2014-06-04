# Change Log

## 0.5.1 ([#9](https://git.mobcastdev.com/Platform/common-spray/pull/9) 2014-06-04 12:22:21)

Removed whitespace

### Patch 

Merged the previous pull request too early :eyes: 

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

