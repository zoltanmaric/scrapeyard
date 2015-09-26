# scrapeyard
### A simple REST API for scheduling batches of flight searches.

1. Set your search for **more than one** departure and destination airport
2. Give it **a range of departure and return dates**
3. Let it tumble through all the combinations for you, on multiple search engines
4. Get notified of the results via email

## Usage

The API accepts JSON POST requests on `/search` containing the following information:

* an email address
* a set of suitable departure airports
* a set of suitable destination airports
* a range of suitable departure dates
* a range of suitable return dates

Here's an example request:

```json
{
 "email": "zoltanmaric@github.com",
 "criteria": {
  "origs": ["ZAG", "BUD"],
  "dests": ["DPS"],
  "depFrom": "2015-07-20",
  "depUntil": "2015-07-20",
  "retFrom": "2015-07-29",
  "retUntil": "2015-07-30",
  "minStayDays": 9,
  "maxStayDays": 10
 }
}
```

which can be interpreted as

* Depart from Zagreb **or** Budapest
* Arrive in Denpasar (Bali)
* Depart on Jul 20, 2015
* Return between Jul 29, 2015 and Jul 30, 2015
* Stay **at least** 9 days
* Stay **at most** 10 days
* When done, send search results to `zoltanmaric@github.com`

The server schedules one search for each combination of departure and destination airports, and departure and return dates; and sends the user an e-mail containing all the search results.
The following is (part of) the body of an example result email:

```json
[{
  "params": {
    "origin": "BUD",
    "destination": "DPS",
    "departure": "2015-07-20",
    "returning": "2015-07-29"
  },
  "yld": {
    "value": 674.0,
    "currency": "EUR",
    "url": "http://www.momondo.com/flightsearch/?Search=true&TripType=2&SegNo=2&SO0=BUD&SD0=DPS&SDP0=20-07-2015&SO1=DPS&SD1=BUD&SDP1=29-07-2015&AD=1&TK=ECO&DO=false&NA=false#Search=true&TripType=2&SegNo=2&SO0=BUD&SD0=DPS&SDP0=20-07-2015&SO1=DPS&SD1=BUD&SDP1=29-07-2015&AD=1&TK=ECO&DO=false&NA=false"
  }
}, {
  "params": {
    "origin": "BUD",
    "destination": "DPS",
    "departure": "2015-07-20",
    "returning": "2015-07-30"
  },
  "yld": {
    "value": 674.0,
    "currency": "EUR",
    "url": "http://www.momondo.com/flightsearch/?Search=true&TripType=2&SegNo=2&SO0=BUD&SD0=DPS&SDP0=20-07-2015&SO1=DPS&SD1=BUD&SDP1=30-07-2015&AD=1&TK=ECO&DO=false&NA=false#Search=true&TripType=2&SegNo=2&SO0=BUD&SD0=DPS&SDP0=20-07-2015&SO1=DPS&SD1=BUD&SDP1=30-07-2015&AD=1&TK=ECO&DO=false&NA=false"
  }
}, {
  "params": {
    "origin": "ZAG",
    "destination": "DPS",
    "departure": "2015-07-20",
    "returning": "2015-07-30"
  },
  "yld": {
    "value": 894.0,
    "currency": "EUR",
    "url": "http://www.momondo.com/flightsearch/?Search=true&TripType=2&SegNo=2&SO0=ZAG&SD0=DPS&SDP0=20-07-2015&SO1=DPS&SD1=ZAG&SDP1=30-07-2015&AD=1&TK=ECO&DO=false&NA=false#Search=true&TripType=2&SegNo=2&SO0=ZAG&SD0=DPS&SDP0=20-07-2015&SO1=DPS&SD1=ZAG&SDP1=30-07-2015&AD=1&TK=ECO&DO=false&NA=false"
  }
}, {
  "params": {
    "origin": "ZAG",
    "destination": "DPS",
    "departure": "2015-07-20",
    "returning": "2015-07-29"
  },
  "yld": {
    "value": 950.0,
    "currency": "EUR",
    "url": "http://www.momondo.com/flightsearch/?Search=true&TripType=2&SegNo=2&SO0=ZAG&SD0=DPS&SDP0=20-07-2015&SO1=DPS&SD1=ZAG&SDP1=29-07-2015&AD=1&TK=ECO&DO=false&NA=false#Search=true&TripType=2&SegNo=2&SO0=ZAG&SD0=DPS&SDP0=20-07-2015&SO1=DPS&SD1=ZAG&SDP1=29-07-2015&AD=1&TK=ECO&DO=false&NA=false"
  }
}]
```

The search is performed by scraping various flight search engine sites. Currently it scrapes the following sites:

* [Momondo](http://momondo.com)
* [Qatar Airways](http://qatarairways.com)
* [AIR.HR](http://air.hr)

## Prerequisites

### Gmail

To send email notifications, the server currently requires a working Gmail account. To set this up, create a file called `secret.properties` in `server/src/main/resources` with the following contents:

```properties
username=<gmail-username>
password=<gmail-password>
```

### Browsers

The server internally runs browsers to scrape search engines. It currently works with [Firefox 41.0](https://ftp.mozilla.org/pub/mozilla.org/firefox/releases/41.0/) and [PhantomJS 2.0.1](https://github.com/ariya/phantomjs/releases/tag/2.0.0), both of which have to be installed on the machine running the server.

### Troubleshooting

If you encounter `java.lang.NoClassDefFoundError` errors, that usually means that the version of Selenium is outdated for the version of Firefox you're using. Open [server/build.sbt](server/build.sbt) and update the version of `selenium-java` to the latest version found [here](http://search.maven.org/#search|ga|1|selenium-java).
