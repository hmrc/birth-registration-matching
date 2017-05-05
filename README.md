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

Parameters           | Type                                                   | Size                         | Description
---------------------|--------------------------------------------------------|------------------------------|-------------------------------------------------------------------------------
birthReferenceNumber | `Optional(String)`                                     | 9 (england/wales) OR 10 (scotland) (10 = 4,3,3. In that first 4 digits are child's birth year i.e. 2017 and then after 3 digits are child's district number i.e. 417 and last 3 digits are entry number. So Scotish BRN will be '2017417001')  | Birth reference number for England or Wales / Scotland
firstName            | `String`                                               | 1-250                        | Child's first name
additionalNames      | `Optional(String)`                                     | 1-250                        | Child's additional names (It can contain space seprated names)
lastName             | `String`                                               | 1-250                        | Child's last name
dateOfBirth          | `Date (yyyy-MM-dd)`                                    | 10                           | Child's date of birth
whereBirthRegistered | `Enum` `england / wales / scotland / northern ireland` | N/A                          | Where the child was registered (England / Wales / Scotland / Northern Ireland)

### POST /birth-registration-matching/match

#### Example Request

##### cURL (Without additionalNames)
```bash
curl --request POST \
  --url http://localhost:8098/birth-registration-matching/match \
  --header 'accept: application/vnd.hmrc.1.0+json' \
  --header 'audit-source: test' \
  --header 'cache-control: no-cache' \
  --header 'content-type: application/json' \
  --data '{
    "birthReferenceNumber": "123456789",
    "firstName": "Adam Test",
    "lastName": "Smith",
    "dateOfBirth": "2010-01-01",
    "whereBirthRegistered": "england"
  }'
```
##### cURL (With additionalNames)
```bash
curl --request POST \
  --url http://localhost:8098/birth-registration-matching/match \
  --header 'accept: application/vnd.hmrc.1.0+json' \
  --header 'audit-source: test' \
  --header 'cache-control: no-cache' \
  --header 'content-type: application/json' \
  --data '{
    "birthReferenceNumber": "123456789",
    "firstName": "Adam Test",
    "additionalNames": "David",
    "lastName": "Smith",
    "dateOfBirth": "2010-01-01",
    "whereBirthRegistered": "england"
  }'
```

##### HTTP

```http
POST /birth-registration-matching/match HTTP/1.1
Host: localhost:8098
Content-Type: application/json
Audit-Source: test
Accept: application/vnd.hmrc.1.0+json
Cache-Control: no-cache

{
	"birthReferenceNumber": "123456789",
	"firstName": "Adam Test",
	"lastName": "Smith",
	"dateOfBirth": "2010-01-01",
	"whereBirthRegistered": "england"
}
```

#### 2xx responses:

##### Payload did not match record

```http
HTTP/1.1 200 OK
Accept: application/vnd.hmrc.1.0+json
Cache-Control: no-cache,no-store,max-age=0
Content-Type: application/json; charset=utf-8

{"matched":false}
```

##### Payload matched record

```http
HTTP/1.1 200 OK
Accept: application/vnd.hmrc.1.0+json
Cache-Control: no-cache,no-store,max-age=0
Content-Type: application/json; charset=utf-8

{"matched":true}
```

#### 4xx responses:

##### BadRequest (coming soon)

```http
HTTP/1.1 400 Bad Request
Accept: application/vnd.hmrc.1.0+json
Cache-Control: no-cache,no-store,max-age=0
Content-Type: application/json; charset=utf-8

```

##### BadRequest - BirthReferenceNumber is incorrect for supplied whereBirthRegistered value

```http
HTTP/1.1 400 Bad Request
Accept: application/vnd.hmrc.1.0+json
Cache-Control: no-cache,no-store,max-age=0
Content-Type: application/json; charset=utf-8

{
  "code":"INVALID_BIRTH_REFERENCE_NUMBER",
  "message":"The birth reference number does not meet the required length"
}
```

##### BadRequest - firstName does not meet required length or contains invalid characters

```http
HTTP/1.1 400 Bad Request
Accept: application/vnd.hmrc.1.0+json
Cache-Control: no-cache,no-store,max-age=0
Content-Type: application/json; charset=utf-8

{
  "code": "INVALID_FIRSTNAME",
  "message": "Provided firstName is invalid."
}
```
##### BadRequest - additionalNames do not meet required length or contain invalid characters

```http
HTTP/1.1 400 Bad Request
Accept: application/vnd.hmrc.1.0+json
Cache-Control: no-cache,no-store,max-age=0
Content-Type: application/json; charset=utf-8

{
  "code": "INVALID_ADDITIONALNAMES",
  "message": "Provided additionalNames are invalid."
}
```

##### BadRequest - lastName does not meet required length or contains invalid characters

```http
HTTP/1.1 400 Bad Request
Accept: application/vnd.hmrc.1.0+json
Cache-Control: no-cache,no-store,max-age=0
Content-Type: application/json; charset=utf-8

{
  "code": "INVALID_LASTNAME",
  "message": "Provided lastName is invalid."
}
```

##### BadRequest - dateOfBirth does not meet required format

```http
HTTP/1.1 400 Bad Request
Accept: application/vnd.hmrc.1.0+json
Cache-Control: no-cache,no-store,max-age=0
Content-Type: application/json; charset=utf-8

{
  "code": "INVALID_DATE_OF_BIRTH",
  "message": "Provided dateOfBirth is invalid."
}
```
##### Forbidden - whereBirthRegistered is invalid

```http
HTTP/1.1 403 Forbidden
Accept: application/vnd.hmrc.1.0+json
Cache-Control: no-cache,no-store,max-age=0
Content-Type: application/json; charset=utf-8

{
  "code": "INVALID_WHERE_BIRTH_REGISTERED",
  "message": " Provided Country is invalid. "
}
```

##### Unauthorized - Audit-Source is invalid

```http
HTTP/1.1 401 unauthorized
Accept: application/vnd.hmrc.1.0+json
Cache-Control: no-cache,no-store,max-age=0
Content-Type: application/json; charset=utf-8

{
  "code": "INVALID_AUDITSOURCE",
  "message": "Provided Audit-Source is invalid."
}
```

##### BadRequest 

```http
HTTP/1.1 400 Bad Request
Accept: application/vnd.hmrc.1.0+json
Cache-Control: no-cache,no-store,max-age=0
Content-Type: application/json; charset=utf-8

{
  "code": "BAD_REQUEST",
  "message": "Provided request is invalid."
}
```

##### NotAcceptable - Accept header contains invalid content type

```http
HTTP/1.1 406 Not Acceptable
Accept: application/vnd.hmrc.1.0+xml
Cache-Control: no-cache,no-store,max-age=0
Content-Type: application/json; charset=utf-8

{
  "code": "INVALID_CONTENT_TYPE",
  "message": "Accept header is invalid."
}
```

##### NotAcceptable - Accept header contains invalid version

```http
HTTP/1.1 406 Not Acceptable
Accept: application/vnd.hmrc.12.0+xml
Cache-Control: no-cache,no-store,max-age=0
Content-Type: application/json; charset=utf-8

{
  "code": "INVALID_CONTENT_TYPE",
  "message": "Accept header is invalid."
}
```

#### 5xx responses:

##### InternalServerError
```http
HTTP/1.1 500 Internal Server Error
Accept: application/vnd.hmrc.1.0+json
Cache-Control: no-cache,no-store,max-age=0
Content-Type: application/json; charset=utf-8


```

##### ServiceUnavailable

```http
HTTP/1.1 503 Service Unavailable
Accept: application/vnd.hmrc.1.0+json
Cache-Control: no-cache,no-store,max-age=0
Content-Type: application/json; charset=utf-8

{
  "code":"GRO_CONNECTION_DOWN",
  "message":"General Registry Office: England and Wales is unavailable"
}

{
  "code":"DES_CONNECTION_DOWN",
  "message":"DES is unavailable"
}

{
  "code":"NRS_CONNECTION_DOWN",
  "message":"National Records Scotland: Scotland is unavailable"
}


```

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html")
