#!/usr/bin/env bash

sbt clean scalafmtAll scalastyleAll compile scalafmtAll coverage test dependencyUpdates coverageReport
