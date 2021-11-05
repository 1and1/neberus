#!/bin/bash
npm install;

# copy jquery
rm -rf ./public/plugin/jquery
mkdir -p ./public/plugin/jquery
cp -r ./node_modules/jquery/dist/* ./public/plugin/jquery

# copy bootstrap
rm -rf ./public/plugin/bootstrap
mkdir -p ./public/plugin/bootstrap
cp -r ./node_modules/bootstrap/dist/* ./public/plugin/bootstrap

# copy fontawesome
rm -rf ./public/plugin/fontawesome
mkdir -p ./public/plugin/fontawesome
cp -r ./node_modules/@fortawesome/fontawesome-free/* ./public/plugin/fontawesome

# export
rm -rf ./public/build
npm run build;

# cleanup possibly existing files
rm -rf ../neberus-core/src/main/resources/generated/
mkdir -p ../neberus-core/src/main/resources/generated/

# copy exported files to core module
cp -r ./public/* ../neberus-core/src/main/resources/generated/