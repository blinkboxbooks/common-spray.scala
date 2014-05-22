# Change Log

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

