# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

<!-- Categories: Added, Changed, Deprecated, Removed, Fixed, Security -->

## [Unreleased]

## [3.0.0] - 2021-06-14

### Changed

- add support for markdown and asciidoc markup


## [3.0.0-RC3] - 2021-06-07

### Changed

- change word-wrap for .btn and .allowed-value
- introduce @ApiRequired, give @ApiOptional and @ApiRequired precedence over framework annotations
- process @link to other method for normal description as well

### Fixed

- parse 'name' from springweb parameter annotations, parse 'required' from @RequestHeader
- allow omitted path for springweb mapping annotations
- escape backticks in js-json output
- collect @ApiCommonResponse(s) from all interfaces instead of just the first
- use first content-type of method if unset in @ApiRequestEntity
- fix possible NPE in JavaDocUtils.getReferencedElement


## [3.0.0-RC2] - 2021-04-23

### Changed

- validate 'consumes' contentType is set for methods with body
- add special name handling for array types
- add class javadoc to nested collection & map elements
- include class javadoc as fallback description for @ApiEntity entityClass

### Fixed

- remove excessive dot in logged paths
- replace/escape all non-alphanumeric characters in operation references
- fix excessive newline in descriptions


## [3.0.0-RC1] - 2021-03-05

### Changed

- rewrite parsers with new doclet Api
- rewrite frontend with svelte & openApiV3


## [2.0.0] - 2019-09-25

### Changed

- switch to Java 11


## [1.4.0] - 2019-08-16

### Fixed

- spring-webmvc: add missing handling for @DeleteMapping

### Added

- add @ApiIgnore to exclude a request parameter, DTO field or a whole REST method from documentation

### Changed

- spring-webmvc: parse 'required' attribute also from annotations
- spring-webmvc: skip method parameters without known annotations
- add basic validation of parsed RestMethodData
- replace webfont version of fontawesome with svg + js


## [1.3.1] - 2019-06-13

### Fixed

- fix toString() issues after j2html upgrade

## [1.3.0] - 2019-06-13

### Added

- include constraints from other fields via @see tag
- add optional indicators to response values

### Fixed

- fix allowedValues for custom parameters

## [1.2.1] - 2019-06-03

### Fixed

- parse javax.validation constraints also for top level parameters 

## [1.2.0] - 2019-05-30

### Added

- add @ApiOptional to mark optional parameters
- parse javax.validation constraints as allowedValueHint

### Changed

- allow @ApiAllowedValues on fields, methods and parameters
- replace glyphicons with fontawesome
- use auto placement for template popovers

## [1.1.0] - 2019-04-10

### Added

- support for @FormParam

### Changed

- always show type of parameter and responseValue

### Fixed

- add slash between rootPath and methodPath, if missing
- skip template generation for unsupported media types
- don't append charset to Content-Type in curl if already present
- ignore charset when checking the media type

## [1.0.0] - 2019-04-04

### Added

- First release
