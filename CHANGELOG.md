# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

<!-- Categories: Added, Changed, Deprecated, Removed, Fixed, Security -->

## [Unreleased]

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
