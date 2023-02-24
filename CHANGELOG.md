# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

<!-- Categories: Added, Changed, Deprecated, Removed, Fixed, Security -->

## [Unreleased]

## [4.0.1] - 2023-02-24

### Fixed

- fix handling of special characters in json property names on expandable fields
- fix handling of recursive request/response classes
- parse getters and ctor for request and response entity classes in different order
- fix usage of multiple @ApiAllowedValue on parameter in interface


## [4.0.0] - 2023-02-17

### Changed

- compile with jdk 17
- add support for jakarta.ws.rs
- add support for jakarta.validation annotations


## [3.0.14] - 2023-01-20

### Fixed

- fix schema for @FormParam parameters without container


## [3.0.13] - 2022-10-26

### Changed

- add handling for jakarta.xml.bind annotations

### Fixed

- ignore getters in records
- append apiBasePath to server url
- don't handle byte[] as collection type


## [3.0.12] - 2022-08-31

### Fixed

- fix openApi types


## [3.0.11] - 2022-06-13

### Changed

- better support for java records


## [3.0.10] - 2022-04-11

### Changed

- update jackson-databind to 2.13.2.2


## [3.0.9] - 2022-04-06

### Fixed

- fix handling of nested getter & ctor DTOs


## [3.0.8] - 2022-03-30

### Fixed

- fix allowedValues in @ApiParameter


## [3.0.7] - 2022-03-18

### Changed

- allow usage of primitive types for parameters
- enable @ApiAllowedValue & javax constraints on type arguments

### Fixed

- fix handling of empty return body


## [3.0.6] - 2022-01-21

### Changed

- add @ApiFormParam to allow usage of @RequestParam for form parameters

### Fixed

- fix @ApiRequestEntity when using MultiValueMap for form parameters
- fix wrong popover contents after switching to a different resource


## [3.0.5] - 2021-12-05

### Fixed

- allow same path to be used by different resources with different httpMethods
- fix @ApiType for form params


## [3.0.4] - 2021-12-02

### Fixed

- make Accept header in curl optional for methods that don't need it


## [3.0.3] - 2021-11-29

### Changed

- upgrade to bootstrap 5
- pull most regex escaping to json

### Fixed

- revert to js-based fontawesome


## [3.0.2] - 2021-11-10

### Changed

- add option to use @ApiLabel to rename classes
- add scrollbar to OperationsTOC on overflow

### Fixed

- fix display of multiple methods for same path
- refactor filterbox


## [3.0.1] - 2021-11-05

### Changed

- use fontawesome via webfonts
- add another way to use list entities via custom interface

### Fixed

- add enclosing class name to references to avoid conflicts


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
