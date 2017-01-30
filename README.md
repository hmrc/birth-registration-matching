# birth-registration-matching

[![Build Status](https://travis-ci.org/hmrc/birth-registration-matching.svg)](https://travis-ci.org/hmrc/birth-registration-matching) [![Download](https://api.bintray.com/packages/hmrc/releases/birth-registration-matching/images/download.svg)](https://bintray.com/hmrc/releases/birth-registration-matching/_latestVersion)

This microservice retrieves a child's birth record from GRO (General Registry Office) - England and Wales.

## Requirements

This service is written in [Scala](http://www.scala-lang.org/) and [Play](http://playframework.com/), so needs a [JRE to](http://www.oracle.com/technetwork/java/javase/overview/index.html) run.

### Quickstart

Base endpoint `/birth-registration-matching`

PATH     | Method | Description
-------- | ------ | --------------------------------------------------------------
`/match` | `POST` | Returns whether there is match against the childs birth record

Headers      | Type     | Example                         | Description
------------ | -------- | -----------------------------   | --------------------------------
Accept       | `String` | application/vnd.hmrc.1.0+json   | API Version
Audit-Source | `String` | dfs                             | Unique identifier of the service
Content-Type | `String` | application/json; charset=utf-8 | Type of payload

Parameters           | Type                                                   | Size      | Description
-------------------- | ------------------------------------------------------ | --------- | -------------------------------------------------------------------------------
birthReferenceNumber | `Optional(String)`                                     | 1+        | Birth reference number
firstName            | `String`                                               | 1-250     | Child's first name
lastName             | `String`                                               | 1-250     | Child's last name
dateOfBirth          | `Date (yyyy-MM-dd)`                                    | 10        | Child's date of birth
whereBirthRegistered | `Enum` `england / wales / scotland / northern ireland` | N/A       | Where the child was registered (England / Wales / Scotland / Northern Ireland)

### Example Request

```bash
curl -X POST -H "Accept: application/vnd.hmrc.1.0+json" -H "Audit-Source: dfs" -H "Content-Type: application/json" -H "Cache-Control: no-cache" -H "Postman-Token: fa8722cf-cf61-163a-e301-2132ce21b344" -d '{
    "birthReferenceNumber" : "400000000",
    "firstName": "Gibby",
    "lastName" : "Haynes",
    "dateOfBirth": "2011-10-01",
    "whereBirthRegistered": "england"
}' "https://localhost:8098/birth-registration-matching/match"
```

### Example response

```json
{
  "matched": false
}
```

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html")

[2fba9783]: https://rawgit.com/hmrc/birth-registration-matching/master/api-documents/api.html "API Documentation"
