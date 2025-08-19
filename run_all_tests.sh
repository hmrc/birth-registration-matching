#!/usr/bin/env bash

sbt clean scalafmtAll compile scalafmtAll coverage test dependencyUpdates coverageReport
