# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

<!-- Categories: Added, Changed, Deprecated, Removed, Fixed, Security -->

## [Unreleased]

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
