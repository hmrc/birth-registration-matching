# birth-registration-matching

[![Build Status](https://travis-ci.org/hmrc/birth-registration-matching.svg)](https://travis-ci.org/hmrc/birth-registration-matching) [![Download](https://api.bintray.com/packages/hmrc/releases/birth-registration-matching/images/download.svg)](https://bintray.com/hmrc/releases/birth-registration-matching/_latestVersion)

This microservice retrieves a child's birth record from one of the following services:

* GRO (General Registry Office) - England and Wales
* NRS (National Records Scotland) - Scotland

## Requirements

This service is written in [Scala](http://www.scala-lang.org/) and [Play](http://playframework.com/), so needs a [JRE to](http://www.oracle.com/technetwork/java/javase/overview/index.html) run.

### Quickstart

The API takes a post request of child details (see parameters below), and returns a json response. If the submissions passes validation and contains no client errors, the response returned will be 200 OK with a JSON body of _"matched": true_ or _"matched": false_

Path     | Supported Methods | Description
-------- | ------ | --------------------------------------------------------------
`/birth-registration-matching/match` | `POST` | Returns whether there is match against the childs birth record

Headers      | Type     | Example                         | Size | Description
------------ | -------- | -----------------------------   | ---- | --------------------------
Accept       | `String` | application/vnd.hmrc.1.0+json   | N/A  | API Version
Audit-Source | `String` | dfs                             | 20   | Unique identifier of the service
Content-Type | `String` | application/json; charset=utf-8 | N/A  | Type of payload

Parameters           | Type                                                   | Size      | Description
-------------------- | ------------------------------------------------------ | --------- | -------------------------------------------------------------------------------
birthReferenceNumber | `Optional(String)`                                     | 9         | Birth reference number for England or Wales
    <br/>            | <br/>                                                  | 10        | Birth reference number for Scotland
firstName            | `String`                                               | 1-250     | Child's first name
lastName             | `String`                                               | 1-250     | Child's last name
dateOfBirth          | `Date (yyyy-MM-dd)`                                    | 10        | Child's date of birth
whereBirthRegistered | `Enum` `england / wales / scotland / northern ireland` | N/A       | Where the child was registered (England / Wales / Scotland / Northern Ireland)

### POST /birth-registration-matching/match

Example Request

```bash
curl -X POST -H "Accept: application/vnd.hmrc.1.0+json" -H "Audit-Source: dfs" -H "Content-Type: application/json" -H "Cache-Control: no-cache" -H "Postman-Token: fa8722cf-cf61-163a-e301-2132ce21b344" -d '{
    "birthReferenceNumber" : "400000000",
    "firstName": "Gibby",
    "lastName" : "Haynes",
    "dateOfBirth": "2011-10-01",
    "whereBirthRegistered": "england"
}' "https://localhost:8098/birth-registration-matching/match"
```

Example responses

Record not found

```json
{
  "matched": false
}
```

Record found

```json
{
  "matched": true
}
```

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html")
