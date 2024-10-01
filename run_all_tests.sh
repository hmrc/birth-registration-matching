#!/usr/bin/env bash

sbt clean scalafmtAll scalastyleAll compile scalafmtAll coverage test it/test dependencyUpdates coverageReport
